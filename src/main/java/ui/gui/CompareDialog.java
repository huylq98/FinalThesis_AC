package ui.gui;

import core.Submission;
import ptrie.Location;
import ptrie.Node;
import ptrie.PTrie;
import stringmap.Mapper;
import ui.gui.CommonHighlighter.Interval;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JTabbedPane;
import javax.swing.text.BadLocationException;

import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.*;

public class CompareDialog {

	private Submission subjectA = null;
	private Submission subjectB = null;
	private String sourceA;
	private String sourceB;
	private ArrayList<Interval> intervalA;
	private ArrayList<Interval> intervalB;

	public CompareDialog(Submission a, Submission b) {
		jTabbedPaneA = new JTabbedPane();
		jTabbedPaneB = new JTabbedPane();
		addSubmission(a, jTabbedPaneA);
		subjectA = a;
		addSubmission(b, jTabbedPaneB);
		subjectB = b;
	}
	
	public void startHighlight(int numSegs) {
		//numSegs = 20
		if (subjectA == null || subjectB == null) {
			return;
		}

		// find index of currently-represented programs
		int selA = jTabbedPaneA.getSelectedIndex();
		int selB = jTabbedPaneB.getSelectedIndex();

		// build PTrie (min run length = 10)
		PTrie pt = new PTrie();
		JEditTextArea aa = getJEditArea(0, selA);
		JEditTextArea ab = getJEditArea(1, selB);
		Mapper ma = new Mapper(aa.getText(), "\\p{javaWhitespace}+", "");
		Mapper mb = new Mapper(ab.getText(), "\\p{javaWhitespace}+", "");
		pt.add(ma.getDest(), subjectA);
		pt.add(mb.getDest(), subjectB);
		ArrayList<Node> l = new ArrayList<Node>();
		for (Node n : pt.findRare(2, 2)) {
			if (n.getData().length() > 10) {
				l.add(n);
			}
		}
		// sort by size (largest first)
		Collections.sort(l, new Comparator<Node>() {
			public int compare(Node a, Node b) {
				return (b.getEnd() - b.getStart())
						- (a.getEnd() - a.getStart());
			}
		});
		// choose first numSeg non-overlapping ones (requires O(N*N) checks...)
		ArrayList<Node> nodes = new ArrayList<Node>();
		while (nodes.size() < numSegs && !l.isEmpty()) {
			Node c = l.remove(0);
			boolean ok = true;
			for (Node n : nodes) {
				if (n.overlaps(c)) {
					ok = false;
					break;
				}
			}
			if (ok) {
				nodes.add(c);
			}
		}

		// build the highlight
		CommonHighlighter ha = new CommonHighlighter(nodes, ma, subjectA);
		CommonHighlighter hb = new CommonHighlighter(nodes, mb, subjectB);
		ha.setPeer(hb);
		hb.setPeer(ha);
		aa.getPainter().addCustomHighlight(ha);
		aa.setRightClickPopup(ha);
		aa.setCaretVisible(false);
		ab.getPainter().addCustomHighlight(hb);
		ab.setRightClickPopup(hb);
		ab.setCaretVisible(false);
		
		
		float ci = 1f / nodes.size();
		this.intervalA = new ArrayList<>(nodes.size());

		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			int j = (i % 2 == 0) ? i / 2 : nodes.size() / 2 + i;
			Color color = Color.getHSBColor(j * ci, 0.10f, 1f);
			for (Location loc : n.getLocations()) {
				if (loc.getBase() == subjectA) {
					Interval in = new Interval(ma.rmap(loc.getOffset(), true), ma
							.rmap(loc.getOffset() + n.getStringLength(), false),
							color, n, loc);
					this.intervalA.add(in);
				}
			}
		}
		
		// color increment
		this.intervalB = new ArrayList<>(nodes.size());

		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			int j = (i % 2 == 0) ? i / 2 : nodes.size() / 2 + i;
			Color color = Color.getHSBColor(j * ci, 0.10f, 1f);
			for (Location loc : n.getLocations()) {
				if (loc.getBase() == subjectB) {
					Interval in = new Interval(mb.rmap(loc.getOffset(), true), mb
							.rmap(loc.getOffset() + n.getStringLength(), false),
							color, n, loc);
					this.intervalB.add(in);
				}
			}
		}
		
		try {
			this.sourceA = aa.getDocument().getText(0, aa.getDocumentLength());
			this.sourceB = ab.getDocument().getText(0, ab.getDocumentLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
	}

	public JEditTextArea getJEditArea(int pos, int source) {
		return (JEditTextArea) ((pos == 0) ? jTabbedPaneA
				.getComponentAt(source) : jTabbedPaneB.getComponentAt(source));
	}

	public static JEditTextArea getSourcePanel(String source, String extension) {
		JEditTextArea jeta = new JEditTextArea();
		jeta.setHorizontalOffset(6);
		jeta.setDocument(new SyntaxDocument());
		if (extension.equalsIgnoreCase("java")) {
			jeta.setTokenMarker(new JavaTokenMarker());
		} else if (extension.equalsIgnoreCase("h")
				|| extension.equalsIgnoreCase("c")
				|| extension.equalsIgnoreCase("cpp")
				|| extension.equalsIgnoreCase("cc")
				|| extension.equalsIgnoreCase("c++")) {
			jeta.setTokenMarker(new CCTokenMarker());
		} else if (extension.equalsIgnoreCase("php")) {
			jeta.setTokenMarker(new PHPTokenMarker());
		} else if (extension.equalsIgnoreCase("js")) {
			jeta.setTokenMarker(new JavaScriptTokenMarker());
		} else if (extension.equalsIgnoreCase("xml")
				|| extension.equalsIgnoreCase("html")
				|| extension.equalsIgnoreCase("htm")) {
			jeta.setTokenMarker(new HTMLTokenMarker());
		} else if (extension.equalsIgnoreCase("py")) {
			jeta.setTokenMarker(new PythonTokenMarker());
		}
		jeta.setText(source);
		return jeta;
	}

	public static void addSubmission(Submission s, JTabbedPane jtp) {
		for (int i = 0; i < s.getSources().size(); i++) {
			String source = s.getSourceCode(i);
			String sourceName = s.getSourceName(i);
			String extension = sourceName
					.substring(sourceName.lastIndexOf('.') + 1);
			jtp.add(s.getId() + ":" + sourceName, getSourcePanel(source,
					extension));
			jtp.setToolTipTextAt(i, s.getOriginalPath());
		}
	}

	private String wrapText(String text, int maxCols) {
		maxCols = 120;
		StringBuilder sb = new StringBuilder();
		int n = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '\n') {
				n = 0;
			}
			sb.append(text.charAt(i));
			n++;
			if (n >= maxCols) {
				sb.append('\n');
				n = 0;
			}
		}
		return sb.toString();
	}

	public void wrapAndHighlight(int maxCols) {
		if (subjectA == null || subjectB == null) {
			return;
		}

		// find index of currently-represented programs
		int selA = 0;
		int selB = 0;

		JEditTextArea aa = getJEditArea(0, selA);
		JEditTextArea ab = getJEditArea(1, selB);
		aa.setText(wrapText(subjectA.getSourceCode(selA), maxCols));
		ab.setText(wrapText(subjectB.getSourceCode(selB), maxCols));
		startHighlight(20);
	}

	private javax.swing.JTabbedPane jTabbedPaneA;
	private javax.swing.JTabbedPane jTabbedPaneB;

	public String getSourceA() {
		return sourceA;
	}

	public void setSourceA(String sourceA) {
		this.sourceA = sourceA;
	}

	public String getSourceB() {
		return sourceB;
	}

	public void setSourceB(String sourceB) {
		this.sourceB = sourceB;
	}

	public ArrayList<Interval> getIntervalA() {
		return intervalA;
	}

	public void setIntervalA(ArrayList<Interval> intervalA) {
		this.intervalA = intervalA;
	}

	public ArrayList<Interval> getIntervalB() {
		return intervalB;
	}

	public void setIntervalB(ArrayList<Interval> intervalB) {
		this.intervalB = intervalB;
	}
}
