package com.ivailozn.trackme;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author ivaylo
 * 
 */
public class Session implements Serializable {

	private String name;
	ArrayList<Lap> laps = new ArrayList<Lap>();

	public Session() {
	}

	public Session(String name) {
		super();
		this.name = name;
	}

	public Session(String name, ArrayList<Lap> laps) {
		super();
		this.name = name;
		this.laps = laps;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Lap> getLaps() {
		return laps;
	}

	public void setLaps(ArrayList<Lap> laps) {
		this.laps = laps;
	}

}
