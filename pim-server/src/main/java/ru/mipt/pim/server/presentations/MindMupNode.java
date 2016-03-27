package ru.mipt.pim.server.presentations;

import java.util.HashMap;

public class MindMupNode {

	public static class MindMapNodeAttributes {
		private HashMap<String, String> style = new HashMap<>();
		private MindMapNodeIcon icon;

		public HashMap<String, String> getStyle() {
			return style;
		}

		public void setStyle(HashMap<String, String> style) {
			this.style = style;
		}

		public MindMapNodeIcon getIcon() {
			return icon;
		}

		public void setIcon(MindMapNodeIcon icon) {
			this.icon = icon;
		}
	}

	public static class MindMapNodeIcon {
		private String url;
		private String iconClass = "glyphicon glyphicon-folder-close";
		private int width;
		private int height;
		private String position;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public String getPosition() {
			return position;
		}

		public void setPosition(String position) {
			this.position = position;
		}

		public String getIconClass() {
			return iconClass;
		}

		public void setIconClass(String iconClass) {
			this.iconClass = iconClass;
		}
	}

	private String title;
	private long id;
	private MindMapNodeAttributes attr;
	private HashMap<String, MindMupNode> ideas = new HashMap<String, MindMupNode>();
	private int leftIndex = -1;
	private int rightIndex = 1;

	public void addIdea(MindMupNode idea) {
		if (Math.abs(leftIndex) < rightIndex) {
			addIdea(--leftIndex, idea);
		} else {
			addIdea(++rightIndex, idea);
		}
	}

	public void addIdea(int index, MindMupNode idea) {
		ideas.put(String.valueOf(index), idea);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public MindMapNodeAttributes getAttr() {
		return attr;
	}

	public void setAttr(MindMapNodeAttributes attr) {
		this.attr = attr;
	}

	public HashMap<String, MindMupNode> getIdeas() {
		return ideas;
	}

	public void setIdeas(HashMap<String, MindMupNode> ideas) {
		this.ideas = ideas;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
