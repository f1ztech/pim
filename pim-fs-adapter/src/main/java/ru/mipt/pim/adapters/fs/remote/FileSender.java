package ru.mipt.pim.adapters.fs.remote;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ru.mipt.pim.adapters.fs.Utils;

/**
 * Sends files in the queue
 */
@Component
public class FileSender implements Runnable {

	private static final int FAILURES_COUNT_THRESHOLD = 10;

	public static enum EventType {
		SEND_FILE, REMOVE_FILE, ADD_FOLDER, REMOVE_FOLDER;
	}

	@Resource
	private RepositoryEndpoint repositoryEndpoint;
	
	@Resource
	private Utils utils;


	private BlockingQueue<Event> queue = new LinkedBlockingQueue<>();
	private List<Event> errors = new ArrayList<>();

	private Thread thread;

	@PostConstruct
	private void init() {
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		// handle queue in separate thread
		Event nextEvent;
		try {
			while ((nextEvent = queue.take()) != null) {
				if (!handleEvent(nextEvent)) {
					if (nextEvent.getFailuresCount() > FAILURES_COUNT_THRESHOLD) {
						errors.add(nextEvent);
					} else { // try once more
						queue.put(nextEvent);
						nextEvent.increaseFailures();
					}
				}
			}
		} catch (InterruptedException e) {
			utils.logToFile("FileSender interrupted", e);
		}
	}
	
	public void sleep(long milliseconds) throws InterruptedException {
		Thread.sleep(milliseconds);
	}

	private boolean handleEvent(Event event) {
		try {
			switch (event.getEventType()) {
			case SEND_FILE:
				return repositoryEndpoint.addFile(event.getFile(), event.getFolder());
			case REMOVE_FILE:
				return repositoryEndpoint.removeFile(event.getFile(), event.getFolder());
			case ADD_FOLDER:
				return repositoryEndpoint.addFolder(event.getFolder());				
			case REMOVE_FOLDER:
				return repositoryEndpoint.removeFolder(event.getFolder());
			default:
				break;
			}
			return true;
		} catch (RepositoryException e) {
			return false;
		}
	}

	public void sendFile(File file, String path) {
		Utils.ignoreExceptions(() -> queue.put(new Event(file, path, EventType.SEND_FILE)));
	}
	
	public void removeFile(File file, String path) {
		Utils.ignoreExceptions(() -> queue.put(new Event(file, path, EventType.REMOVE_FILE)));
	}

	public void addFolder(String path) {
		Utils.ignoreExceptions(() -> queue.put(new Event(path, EventType.ADD_FOLDER)));
	}
	
	public void removeFolder(String path) {
		Utils.ignoreExceptions(() -> queue.put(new Event(path, EventType.REMOVE_FOLDER)));
	}


}
