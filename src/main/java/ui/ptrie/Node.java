package ui.ptrie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * A simple radix tree; recursive implementation
 *
 * @author mfreire
 */
public class Node {
	private TreeMap<Character, Node> children;
	private HashMap<Object, ArrayList<Integer>> locations;

	private String source;
	private int start;
	private int end;

	// statistics: total and unique locations found under here
	private Node parent;
	private int total;
	private int uniqueCount;
	private HashSet<Object> unique;

	public Node(String source, int start, int end, Object base, int offset) {
		this(source, start, end);
		this.addLocation(base, offset);
	}

	public Node(String source, int start, int end) {
		this(source, start, end, new TreeMap<Character, Node>(),
				new HashMap<Object, ArrayList<Integer>>());
	}

	private Node(String source, int start, int end,
			TreeMap<Character, Node> children,
			HashMap<Object, ArrayList<Integer>> locations) {
		this.children = children;
		this.locations = locations;

		this.source = source;
		this.start = start;
		this.end = end;

		this.total = 0;
		this.unique = new HashSet<Object>();
	}

	public String getSource() {
		return source;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getStringLength() {
		int l = end - start;
		for (Node p = parent; p != null; p = p.parent) {
			l += p.end - p.start;
		}
		return l;
	}

	/**
	 * This is *not* the length of the full string, only the length of the 
	 * string fragment that hangs under this node; all ancestors should be
	 * considered to build the full length
	 * @return
	 */
	public int getDataLength() {
		return end - start;
	}

	public boolean overlaps(Node n) {
		if (n.getSource() == source) {
			return (start <= n.start && n.start <= end)
					|| (start <= n.end && n.end <= end);
		}
		return false;
	}

	public Set<Character> getChildrenChars() {
		return children.keySet();
	}

	public Node getChild(char c) {
		return children.get(c);
	}

	public void putChild(char c, Node n) {
		children.put(c, n);
	}

	public String getData() {
		return source.substring(start, end);
	}

	/**
	 * Should be called only with suffixes of current data
	 * (Otherwise, the source would have to be altered)
	 */
	public void setData(String data) {
		String oldData = getData();
		if (!oldData.endsWith(data)) {
			throw new IllegalArgumentException("" + "Not a suffix:\n  '"
					+ oldData + "'\n  '" + data + "'");
		}
		start += oldData.lastIndexOf(data);
	}

	/**
	 * Count all locations where the string in 'data'
	 * (supposing prefix nodes were prefixed) can be found;
	 * keeps separate counts for 'totals' and 'unique'.
	 */
	public void updateStats() {
		total = 0;
		unique.clear();

		// propagate children stats upwards
		for (char c : children.keySet()) {
			Node child = children.get(c);
			child.parent = this;
			if (child.children.isEmpty()) {
				child.unique.clear();
				child.total = 0;
				child.propagateStats();
			} else {
				child.updateStats();
			}
		}

		// all children's stats have propagated upwards; propagate own
		propagateStats();
	}

	/**
	 * Child updating parent's stats
	 */
	private void propagateStats() {
		unique.addAll(locations.keySet());
		for (Object o : locations.keySet()) {
			total += locations.get(o).size();
		}

		for (Node p = parent; p != null; p = p.parent) {
			p.total += total;
			p.unique.addAll(locations.keySet());
		}

		uniqueCount = unique.size();
	}

	public void findRare(ArrayList<Node> found, int minFreq, int maxFreq) {
		if ((uniqueCount >= minFreq) && (uniqueCount <= maxFreq)) {
			found.add(this);
		}

		for (char c : children.keySet()) {
			children.get(c).findRare(found, minFreq, maxFreq);
		}
	}

	/**
	 * removes all locations from this node
	 */
	public void killLocalLocations() {
		locations.clear();
	}

	/**
	 * Retrieves the full string referred to by this node
	 */
	public String getString() {
		StringBuffer sb = new StringBuffer(getData());
		for (Node p = parent; p != null; p = p.parent) {
			sb.insert(0, p.getData());
		}
		return sb.toString();
	}

	/**
	 *Returns the total ocurrences of the string represented by this node
	 */
	public int getUniqueCount() {

		return uniqueCount;
	}

	/**
	 * Return all locations where the string in 'data'
	 * (supposing prefix nodes were prefixed) can be found
	 */
	public ArrayList<Location> getLocations() {
		return getLocations(null);
	}

	private ArrayList<Location> getLocations(ArrayList<Location> al) {
		if (al == null) {
			al = new ArrayList<Location>();
		}
		for (Object o : locations.keySet()) {
			for (int i : locations.get(o)) {
				al.add(new Location(o, i));
			}
		}
		for (char c : children.keySet()) {
			children.get(c).getLocations(al);
		}
		return al;
	}

	/**
	 * Return the count of different-base locations where the string
	 * in 'data' (with all prefixes) can be found.
	 * WARNING: You should run 'updateStats' on the root prior to calling this
	 */
	public HashSet getDistinctLocations() {
		return unique;
	}

	/**
	 * Add another location to this
	 */
	public void addLocation(Object obj, int pos) {
		ArrayList<Integer> l = locations.get(obj);
		if (l == null) {
			l = new ArrayList<Integer>();
			locations.put(obj, l);
		}
		// FIXME: Avoid memory hogs
		l.add(pos);
	}
}
