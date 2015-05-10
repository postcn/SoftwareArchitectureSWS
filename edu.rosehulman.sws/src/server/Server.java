/*
 * Server.java
 * Oct 7, 2012
 *
 * Simple Web Server (SWS) for CSSE 477
 * 
 * Copyright (C) 2012 Chandan Raj Rupakheti
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
 */
 
package server;

import gui.WebServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This represents a welcoming server for the incoming
 * TCP request from a HTTP client such as a web browser. 
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class Server implements Runnable {
	public static final int MAX_CACHE_FILE_SIZE = 4096;
	public static final int BUFFER_SIZE = 1048576;
	private static final int MAX_THREADS_PER_USER = 15;
	private static final int BLACKLIST_PENALTY_UNIT = Calendar.MINUTE;
	private static final int BLACKLIST_PENALTY_TIME = 30;
	
	private String rootDirectory;
	private int port;
	private boolean stop;
	private ServerSocket welcomeSocket;
	private PluginManager pluginManager;
	private LogManager logManager;
	private static final String LOG_FILE = "log.txt";
	
	private long connections;
	private long serviceTime;
	
	private double time = 0;
	private long count = 0;
	
	private WebServer window;
	private HashMap<String, Integer> connectionMap; 
	private HashMap<String, Calendar> blacklistMap;
	private HashMap<String, char[]> cache;
	/**
	 * @param rootDirectory
	 * @param port
	 */
	public Server(String rootDirectory, int port, WebServer window) {
		this.rootDirectory = rootDirectory;
		this.port = port;
		this.stop = false;
		this.connections = 0;
		this.serviceTime = 0;
		this.window = window;
		this.pluginManager = new PluginManager(this);
		connectionMap = new HashMap<String,Integer>();
		blacklistMap = new HashMap<String,Calendar>();
	    Thread t = new Thread(this.pluginManager);
	    t.start();
	    this.logManager = new LogManager(new File(LOG_FILE));
	    Thread thread = new Thread(logManager);
	    thread.start();
	    
	    cache = new HashMap<String, char[]>();
	    Timer timer = new Timer();
	    timer.schedule(new ClearCache(), 0, 15000);
	}

	/**
	 * Gets the root directory for this web server.
	 * 
	 * @return the rootDirectory
	 */
	public String getRootDirectory() {
		return rootDirectory;
	}


	/**
	 * Gets the port number for this web server.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Returns connections serviced per second. 
	 * Synchronized to be used in threaded environment.
	 * 
	 * @return
	 */
	public synchronized double getServiceRate() {
		if(this.serviceTime == 0)
			return Long.MIN_VALUE;
		double rate = this.connections/(double)this.serviceTime;
		rate = rate * 1000;
		return rate;
	}
	
	/**
	 * Increments number of connection by the supplied value.
	 * Synchronized to be used in threaded environment.
	 * 
	 * @param value
	 */
	public synchronized void incrementConnections(long value) {
		this.connections += value;
	}
	
	/**
	 * Increments the service time by the supplied value.
	 * Synchronized to be used in threaded environment.
	 * 
	 * @param value
	 */
	public synchronized void incrementServiceTime(long value) {
		this.serviceTime += value;
	}
	
	public synchronized void removeThread(String ip){
		System.out.println(ip);
		if(connectionMap.get(ip)==1){
			connectionMap.remove(ip);
		}else{
			connectionMap.put(ip, connectionMap.get(ip)-1);
		}
	}

	/**
	 * The entry method for the main server thread that accepts incoming
	 * TCP connection request and creates a {@link ConnectionHandler} for
	 * the request.
	 */
	public void run() {
		try {
			this.welcomeSocket = new ServerSocket(port);
			
			// Now keep welcoming new connections until stop flag is set to true
			while(true) {
				// Listen for incoming socket connection
				// This method block until somebody makes a request
				Socket connectionSocket = this.welcomeSocket.accept();
				// Come out of the loop if the stop flag is set
				if(this.stop)
					break;
				
				String ip = connectionSocket.getInetAddress().getHostAddress();
				Calendar cal = Calendar.getInstance();
				
				if(blacklistMap.containsKey(ip)){
					if(blacklistMap.get(ip).after(cal)){
						cal.add(BLACKLIST_PENALTY_UNIT, BLACKLIST_PENALTY_TIME);
						blacklistMap.put(ip, cal);
						continue;
					}else{
						blacklistMap.remove(ip);
					}
				}
					
				if(connectionMap.containsKey(ip)){
					int value = connectionMap.get(ip);
					if(value>=MAX_THREADS_PER_USER){
						cal.add(BLACKLIST_PENALTY_UNIT, BLACKLIST_PENALTY_TIME);
						blacklistMap.put(ip, cal);
						continue;
					}else{
						connectionMap.put(ip, value + 1);
					}
				}else{
					System.out.println("put "+ip);
				}				
				
				// Create a handler for this incoming connection and start the handler in a new thread
				ConnectionHandler handler = new ConnectionHandler(this, connectionSocket, pluginManager);
				new Thread(handler).start();
			}
			this.welcomeSocket.close();
		}
		catch(Exception e) {
			window.showSocketException(e);
		}
	}
	
	/**
	 * Stops the server from listening further.
	 */
	public synchronized void stop() {
		if(this.stop)
			return;
		
		// Set the stop flag to be true
		this.stop = true;
		try {
			// This will force welcomeSocket to come out of the blocked accept() method 
			// in the main loop of the start() method
			Socket socket = new Socket(InetAddress.getLocalHost(), port);
			
			// We do not have any other job for this socket so just close it
			socket.close();
		}
		catch(Exception e){}
	}
	
	/**
	 * Checks if the server is stopeed or not.
	 * @return
	 */
	public boolean isStoped() {
		if(this.welcomeSocket != null)
			return this.welcomeSocket.isClosed();
		return true;
	}
	
	public synchronized void addLatency(long time) {
		this.time += time;
		this.count++;
		
		System.out.println("SERVER: Average connection = " + this.time / count);
	}
	
	public LogManager getLogManager() {
		return logManager;
	}
	
	public char[] inCache(String request) {
		return cache.get(request);
	}
	
	public synchronized void putInCache(String request, char[] body) {
		if (body.length > MAX_CACHE_FILE_SIZE) {
			return;
		}
		else {
			cache.put(request, body);
		}
	}
	
	public synchronized void putInCache(String request, File file) {
		if (file.length() > MAX_CACHE_FILE_SIZE) {
			return;
		}
		else {
			try {
				char[] contents = new char[(int) file.length()];
				BufferedReader reader = new BufferedReader(new FileReader(file));
				reader.read(contents);
				reader.close();
				cache.put(request, contents);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class ClearCache extends TimerTask {

		/* (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
			cache = new HashMap<>();
		}
	}
}
