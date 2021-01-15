package ui.gui;

import ui.ptrie.Location;
import ui.ptrie.Node;

public class Interval {
	public int so, eo; // start, end
	public Node node;
	public Location loc;

	public Interval(int so, int eo, Node node, Location loc) {
		this.so = so;
		this.eo = eo;
		this.node = node;
		this.loc = loc;
	}

	public int getSo() {
		return so;
	}

	public void setSo(int so) {
		this.so = so;
	}

	public int getEo() {
		return eo;
	}

	public void setEo(int eo) {
		this.eo = eo;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}
}
