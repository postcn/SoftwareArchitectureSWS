/*
 * Servlet.java
 * Apr 30, 2015
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
 
package protocol;

import java.util.LinkedList;
import java.util.List;

import requests.RequestHandler;
import requests.RequestHandlerFactory;
import server.Server;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public abstract class Servlet {
	
	Server running;
	Plugin container;
	
	/**
	 * Gets the routing path for the server. ie. <hostname>/path
	 * this is the location that the servlet is hosted at.
	 * @return servlet path location
	 */
	public abstract String getPath();
	
	/**
	 * Root directory for serving files from the servlet.
	 * @return the root directory
	 */
	public String getRootDirectory() {
		String serverRoot = running.getRootDirectory();
		return serverRoot;
	}
	
	public void setServer(Server running) {
		this.running = running;
	}
	
	public Server getServer() {
		return running;
	}
	
	public void setPlugin(Plugin container) {
		this.container = container;
	}
	
	public void handle(HttpRequest request, HttpResponse response) throws ProtocolException {
		RequestHandler handler = RequestHandlerFactory.getRequestHandler(request);
		
		handler.handle(this, request, response);
	}
	
	/**
	 * Method which returns the accepted methods for this servlet. The default servlet implementation accepts
	 * {@link Protocol#GET}, {@link Protocol#PUT}, {@link Protocol#POST}, and {@link Protocol#DELETE}
	 * 
	 * To change this behavior override this method and return other values.
	 * @return the HTTP Methods accepted by this servlet.
	 */
	public List<String> getAcceptedMethods() {
		LinkedList<String> methods = new LinkedList<>();
		
		methods.add(Protocol.GET);
		methods.add(Protocol.PUT);
		methods.add(Protocol.DELETE);
		methods.add(Protocol.POST);
		
		return methods;
	}

}
