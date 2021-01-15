package ui.gui;

import core.Submission;
import ui.gui.Interval;
import ui.ptrie.Location;
import ui.ptrie.Node;
import ui.ptrie.PTrie;
import ui.stringmap.Mapper;
import utils.Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CompareDialog {

	private Submission submissionA;
	private Submission submissionB;
	private String sourceA;
	private String sourceB;
	private ArrayList<Interval> intervalA;
	private ArrayList<Interval> intervalB;

	public CompareDialog(Submission submissionA, Submission submissionB) {
		this.submissionA = submissionA;
		this.submissionB = submissionB;
		this.sourceA = this.wrapText(submissionA.getSourceCode(0), Constant.MAX_COLUMN);
		this.sourceB = this.wrapText(submissionB.getSourceCode(0), Constant.MAX_COLUMN);
	}

	public void startHighlight(int numSegs) {
		// numSegs = 20
		if (submissionA == null || submissionB == null) {
			return;
		}

		// build PTrie (min run length = 10)
		PTrie pt = new PTrie();
		Mapper ma = new Mapper(this.sourceA, "\\p{javaWhitespace}+", "");
		Mapper mb = new Mapper(this.sourceB, "\\p{javaWhitespace}+", "");
		pt.add(ma.getDest(), this.submissionA);
		pt.add(mb.getDest(), this.submissionB);
		ArrayList<Node> l = new ArrayList<Node>();
		for (Node n : pt.findRare(2, 2)) {
			if (n.getData().length() > 10) {
				l.add(n);
			}
		}
		// sort by size (largest first)
		Collections.sort(l, new Comparator<Node>() {
			public int compare(Node a, Node b) {
				return (b.getEnd() - b.getStart()) - (a.getEnd() - a.getStart());
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

		this.intervalA = new ArrayList<>(nodes.size());

		for (Node n : nodes) {
			for (Location loc : n.getLocations()) {
				if (loc.getBase() == this.submissionA) {
					Interval in = new Interval(ma.rmap(loc.getOffset(), true),
							ma.rmap(loc.getOffset() + n.getStringLength(), false), n, loc);
					this.intervalA.add(in);
				}
			}
		}

		// color increment
		this.intervalB = new ArrayList<>(nodes.size());

		for (Node n : nodes) {
			for (Location loc : n.getLocations()) {
				if (loc.getBase() == this.submissionB) {
					Interval in = new Interval(mb.rmap(loc.getOffset(), true),
							mb.rmap(loc.getOffset() + n.getStringLength(), false), n, loc);
					this.intervalB.add(in);
				}
			}
		}
	}

	private String wrapText(String text, int maxCols) {
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
