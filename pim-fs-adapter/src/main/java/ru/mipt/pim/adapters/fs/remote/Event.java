package ru.mipt.pim.adapters.fs.remote;

import java.io.File;

import ru.mipt.pim.adapters.fs.remote.FileSender.EventType;

public class Event {
	private File file;
	private String folder;
	private EventType eventType;
	private int failuresCount = 0;

	public Event(File file, String folder, EventType eventType) {
		this.file = file;
		this.folder = folder;
		this.eventType = eventType;
	}

	public Event(String folder, EventType eventType) {
		this.folder = folder;
		this.eventType = eventType;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public synchronized void increaseFailures() {
		failuresCount++;
	}
	
	public int getFailuresCount() {
		return failuresCount;
	}
}