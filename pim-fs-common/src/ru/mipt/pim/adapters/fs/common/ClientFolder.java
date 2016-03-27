package ru.mipt.pim.adapters.fs.common;

import java.util.HashSet;
import java.util.Set;

public class ClientFolder extends ClientFile {

	private Set<ClientFolder> subFolders = new HashSet<ClientFolder>();
	private Set<ClientFile> files = new HashSet<ClientFile>();

	public Set<ClientFolder> getSubFolders() {
		return subFolders;
	}

	public Set<ClientFile> getFiles() {
		return files;
	}
	
	public String formatTree() {
		return formatTree("");
	}
	
	public String formatTree(String tabs) {
		StringBuffer ret = new StringBuffer();
		ret.append(tabs).append("[").append(getName()).append("]");
		for (ClientFile file : files) {
			ret.append("\n").append(tabs).append("\t - ").append(file.getName());
		}
		for (ClientFolder subFolder : subFolders) {
			ret.append("\n").append(subFolder.formatTree(tabs + "\t"));
		}
		return ret.toString();
	}
}
