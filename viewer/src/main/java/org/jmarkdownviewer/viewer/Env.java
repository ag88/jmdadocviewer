package org.jmarkdownviewer.viewer;

import java.util.HashMap;
import java.util.Map;

public class Env {
	
	Map<String, Object> keyValues = new HashMap<String, Object>();

	String lastdir;
	
	private static Env instance;

	private Env() {
		init();
	}

	public void init() {
		lastdir = System.getProperty("user.dir");
		put("title", "Markdown (and AsciiDoc) viewer");
	}
	
	public static Env getInstance() {
		if (instance == null)
			instance = new Env();
		return instance;
	}
	
	public Object get(String key) {
		return keyValues.get(key);
	}
	
	public void put(String key, Object object) {
		keyValues.put(key, object);
	}
	
	public String getLastdir() {
		return lastdir;
	}

	public void setLastdir(String lastdir) {
		this.lastdir = lastdir;
	}

	
}
