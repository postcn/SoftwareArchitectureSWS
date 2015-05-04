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
	
	private Map<String, Servlet> locationMapping;
	private Server parent;
	final File PLUGIN_FOLDER = new File("Plugins");
	private WatchService watcher;
	private Path dir;
	private enum Action{CREATE}
	
	public PluginManager(Server parent) {
		this.locationMapping = new HashMap<>();
		this.parent = parent;
		if (!PLUGIN_FOLDER.exists()) {
			PLUGIN_FOLDER.mkdir();
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

	public void load(Servlet servlet, String pluginPath){
		String servletPath = servlet.getPath();
		String location = pluginPath+"/"+servletPath;
		if(locationMapping.containsKey(servletPath)){
			locationMapping.remove(servletPath);
		}
		locationMapping.put(location, servlet);
		servlet.setServer(parent);
	}
	
	private void loadPlugin(Plugin plugin, String fileName){
		plugin.load();
		List<Servlet> servlets = plugin.getServlets();
		for(int i=0;i<servlets.size();i++){
			load(servlets.get(i), plugin.getLocation());
		}
	}
	

	private void registerWatcher() throws IOException{
		this.dir = PLUGIN_FOLDER.toPath();
		this.watcher = FileSystems.getDefault().newWatchService();
		this.dir.register(watcher,StandardWatchEventKinds.ENTRY_CREATE);
	}
	
	private void loadPlugins() {
		File[] files = PLUGIN_FOLDER.listFiles();
		if (files == null) {
			return;
		}
		for (int i = 0; i < files.length; i++) {
			try {
				loadAndScanJar(files[i],Action.CREATE);
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void loadAndScanJar(File file,Action action) throws ClassNotFoundException, ZipException, IOException {
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
				Plugin plugin = (Plugin) c.newInstance();
				System.out.println(action);
				switch(action){
					case CREATE:
						loadPlugin(plugin,file.getAbsolutePath());
						break;
				}
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
	
		        if (kind == StandardWatchEventKinds.OVERFLOW) {
		            System.out.println("got an overflow event");
		        	continue;
		        }else{
		        	// The filename is the context of the event.
			        @SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>)event;
			        Path filename = ev.context();
		
			        Path child = dir.resolve(filename);
			        if (child.getFileName().toString().endsWith(".jar")) {
			        	Action action = null; 
			        	if(kind==StandardWatchEventKinds.ENTRY_CREATE){		        		
				        	System.out.println("got a create event: "+child);
				        	action=Action.CREATE;
				        }
			        	try {						
							loadAndScanJar(child.toFile(),action);
						} catch (ClassNotFoundException | IOException e) {
							e.printStackTrace();
						}
			        }
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
