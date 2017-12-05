package net.bleujin.rcraken;

import net.ion.framework.util.StringUtil;

public class Fqn {

	private String[] paths;
	public Fqn(String path) {
		this.paths = StringUtil.split(path, "/") ;
	}

	public static Fqn from(String path) {
		return new Fqn(path);
	}

	public String absPath() {
		return "/" + StringUtil.join(paths, "/") ;
	}

	public boolean isRoot() {
		return paths.length == 0;
	}

}
