/*
 * PluginManager.java
 * May 2, 2015
 *
 * Simple Web Server (SWS) for EE407/507 and CS455/555
 * 
 * Copyright (C) 2011 Chandan Raj Rupakheti, Clarkson University
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 * Contact Us:
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Department of Electrical and Computer Engineering
 * Clarkson University
 * Potsdam
 * NY 13699-5722
 * http://clarkson.edu/~rupakhcr
 */
 
package server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import protocol.Plugin;
import protocol.Servlet;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class PluginManager implements Runnable{
	//TODO: Implement the rest of the PluginManager
	
	@SuppressWarnings("unused")
	private Map<String, Servlet> locationMapping;
	private Server parent;
	final File PLUGIN_FILE = new File("./Plugins"); //This is the directory Location that we are using.
	private WatchService watcher;
	private Path dir;
	
	public PluginManager(Server parent) {
		this.locationMapping = new HashMap<String, Servlet>();
		this.parent = parent;
		if (!PLUGIN_FILE.exists()) {
			PLUGIN_FILE.mkdir();
		}
		
		loadPlugins();
		try {
			registerWatcher();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Servlet getServletAtLocation(String location) {
		if(locationMapping.containsKey(location)){
			return locationMapping.get(location);
		}
		return new DefaultServlet(parent);
	}

	public void load(Servlet servlet){
		String servletPath = servlet.getPath();
		if(locationMapping.containsKey(servletPath)){
			locationMapping.remove(servletPath);
		}
		locationMapping.put(servletPath, servlet);	
	}
	
	public void unload(Servlet servlet){
		return;//TODO: implement this method
	}
//	TODO: implement a watcher for deleted files
	
	private void loadPlugin(Plugin plugin){
		plugin.load();
		List<Servlet> servlets = plugin.getServlets();
		for(int i=0;i<servlets.size();i++){
			load(servlets.get(i));
		}
	}

	private void registerWatcher() throws IOException{
		this.dir = PLUGIN_FILE.toPath();
		this.watcher = FileSystems.getDefault().newWatchService();
		this.dir.register(watcher,StandardWatchEventKinds.ENTRY_CREATE);
	}
	
	private void loadPlugins() {
		File[] files = PLUGIN_FILE.listFiles();
		if (files == null) {
			// "Critical Error: Plugins directory is missing from current folder"
			return;
		}
		for (int i = 0; i < files.length; i++) {
			try {
				loadAndScanJar(files[i]);
			} catch (ClassNotFoundException | IOException e) {
				// "Error occurred while processing file: " + files[i].getName()));
				// "An Error Occurred while loading a possible jar file: " + e.getLocalizedMessage()
			}
		}

	}

	private void loadAndScanJar(File file) throws ClassNotFoundException, ZipException, IOException {
		LinkedList<Class<?>> pluginClasses = new LinkedList<>();
		JarFile jarFile = new JarFile(file);
		Enumeration<?> jarEntries = jarFile.entries();
		URL[] urls = { new URL("jar:file:" + file.getAbsolutePath() + "!/") };
		URLClassLoader classLoader = URLClassLoader.newInstance(urls);
		String suffix=".class";
		while (jarEntries.hasMoreElements()) {
			JarEntry jarEntry = (JarEntry) jarEntries.nextElement();
			if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(suffix)) {
				continue;
			}
			String className = jarEntry.getName().substring(0, jarEntry.getName().length() - suffix.length());
			className = className.replace('/', '.');
			Class<?> clazz = classLoader.loadClass(className);
			for (Class<?> inter : clazz.getInterfaces()) {
				if (inter.equals(Plugin.class)) {//the class to load
					pluginClasses.add(clazz);
					break;
				}
			}
		}
		for (Class<?> c : pluginClasses) {
			try {
//				handler.register((Plugin) c.newInstance());//handler is the pluginhandler. It is a function registration call
				loadPlugin((Plugin) c.newInstance());
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			}
		}
		jarFile.close();
	}

	@Override
	public void run() {
		for(;;){
			WatchKey key;
			try {
		        key = this.watcher.take();
		    } catch (InterruptedException x) {
		        return;
		    }
	
		    for (WatchEvent<?> event: key.pollEvents()) {
		        WatchEvent.Kind<?> kind = event.kind();
	
		        // This key is registered only for ENTRY_CREATE events, but an OVERFLOW event can occur regardless if events are lost or discarded.
		        if (kind == StandardWatchEventKinds.OVERFLOW) {
		            continue;
		        }
	
		        // The filename is the context of the event.
		        @SuppressWarnings("unchecked")
				WatchEvent<Path> ev = (WatchEvent<Path>)event;
		        Path filename = ev.context();
	
		        Path child = dir.resolve(filename);
				if (child.getFileName().toString().endsWith(".jar")) {
					// "Dynamically adding jar file added after initial startup"
					try {						
						loadAndScanJar(child.toFile());
						
					} catch (ClassNotFoundException | IOException e) {
						e.printStackTrace();
					}
				}else{
					//"New File: ("+filename+") is not a jar file"
				    continue;
				}
		    }
	
		    // Reset the key -- this step is critical if you want to receive further watch events.  If the key is no longer valid,
		    // the directory is inaccessible so exit the loop.
		    boolean valid = key.reset();
		    if (!valid) {
		        break;
		    }
        }
	}
}
