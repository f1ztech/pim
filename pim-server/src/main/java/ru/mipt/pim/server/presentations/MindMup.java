package ru.mipt.pim.server.presentations;

import java.util.HashMap;

public class MindMup extends MindMupNode {

	private String formatVersion = "2";
	private int currentId = 0;
	private HashMap<Integer, String> idToUriMap = new HashMap<Integer, String>();

	public String getFormatVersion() {
		return formatVersion;
	}

	public void setFormatVersion(String formatVersion) {
		this.formatVersion = formatVersion;
	}

	public HashMap<Integer, String> getIdToUriMap() {
		return idToUriMap;
	}

	public void setIdToUriMap(HashMap<Integer, String> idToUriMap) {
		this.idToUriMap = idToUriMap;
	}
	
	public synchronized int nextId() {
		return ++currentId;
	}
	
}
