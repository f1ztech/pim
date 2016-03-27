package ru.mipt.pim.adapters.fs.common;

import java.io.File;

public class ClientFile {

	private String id;
	private String name;
	private String path;
	private ClientFolder folder;
	private File fsFile;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ClientFolder getFolder() {
		return folder;
	}

	public void setFolder(ClientFolder folder) {
		this.folder = folder;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientFile other = (ClientFile) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public File getFsFile() {
		return fsFile;
	}

	public void setFsFile(File fsFile) {
		this.fsFile = fsFile;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	
	
}
