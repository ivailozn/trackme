package com.ivailozn.trackme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

public class Lap implements Serializable {

	private String name;

	ArrayList<MyLocation> points = new ArrayList<MyLocation>();

	public Lap() {
	}

	public Lap(String name) {
		this.name = name;
	}

	public ArrayList<MyLocation> getPoints() {
		return points;
	}

	public void setPoints(ArrayList<MyLocation> points) {
		this.points = points;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
