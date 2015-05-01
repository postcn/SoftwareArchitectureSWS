/*
 * RequestHandlerFactory.java
 * Apr 23, 2015
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
 
package requests;

import java.util.HashMap;
import java.util.Map;

import export.HttpRequest;
import export.Protocol;
import export.ProtocolException;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class RequestHandlerFactory {
	private static Map<String, RequestHandler> handlers; 
	
	static {
		handlers = new HashMap<String, RequestHandler>();
		handlers.put(Protocol.GET, new GetRequestHandler());
		handlers.put(Protocol.POST, new PostRequestHandler());
		handlers.put(Protocol.PUT, new PutRequestHandler());
		handlers.put(Protocol.DELETE, new DeleteRequestHandler());
	}
	
	public static RequestHandler getRequestHandler(HttpRequest request) throws ProtocolException {
		if (!handlers.containsKey(request.getMethod())) {
			throw new ProtocolException(Protocol.METHOD_NOT_ALLOWED_CODE, Protocol.METHOD_NOT_ALLOWED_TEXT);
		}
		else {
			RequestHandler handler = handlers.get(request.getMethod());
			try {
				//Try to give them a new instance so we don't run into concurrency issues.
				return handler.getClass().newInstance();
			} catch (InstantiationException e) {
				//These errors should not occur. If they do, mask the problem and return the instance we already have.
				e.printStackTrace();
				return handler;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return handler;
			}
		}
	}

}
