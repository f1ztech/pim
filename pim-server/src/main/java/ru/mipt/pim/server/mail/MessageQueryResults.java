package ru.mipt.pim.server.mail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageQueryResults {

	private List<Message> messages;
	private boolean hasMore;
	private Map<String, Object> attributes = new HashMap<>();

	public boolean isHasMore() {
		return hasMore;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}
	
	public <T> T getAttribute(String key) {
		return (T) attributes.get(key);
	}
	
}
