/*
 * GetRequestHandler.java
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

import java.io.File;

//import org.json.JSONException;
//import org.json.JSONObject;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.ProtocolException;
import protocol.Servlet;
import server.Server;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class GetRequestHandler implements RequestHandler {
	/* (non-Javadoc)
	 * @see requests.RequestHandler#handle(protocol.HttpRequest)
	 */
	@Override
	public HttpResponse handle(Servlet server, HttpRequest request, HttpResponse toFill) throws ProtocolException {
		String uri = request.getUri();
		Server serv = server.getServer();
		char[] body = serv.inCache(uri);
//		if(uri.equals("/test.jpg")){
//			JSONObject jo = new JSONObject();
//			try {
//				jo.put("message", "hello world");
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			System.out.println(jo.toString());
//			
//			StringBuilder sb = new StringBuilder();
//			sb.append(jo.toString());
//			sb.append(Protocol.CRLF);
//			
//			body = sb.toString().toCharArray();
//		}
		
		if (body != null) {
			toFill.setStatus(Protocol.OK_CODE);
			toFill.setConnection(Protocol.CLOSE);
			toFill.setFile(body);
		}
		else {
			// Get root directory path from server
			String rootDirectory = server.getRootDirectory();
			// Combine them together to form absolute file path
			File file = new File(rootDirectory + uri);
			// Check if the file exists
			if(file.exists()) {
				if(file.isDirectory()) {
					// Look for default index.html file in a directory
					String location = rootDirectory + uri + System.getProperty("file.separator") + Protocol.DEFAULT_FILE;
					file = new File(location);
				}
			}
			
			toFill.setStatus(file.exists() ? Protocol.OK_CODE : Protocol.NOT_FOUND_CODE);
			toFill.setConnection(Protocol.CLOSE);
			toFill.setFile(file);
			serv.putInCache(uri, file);
		}
		
		
		return toFill;
	}

}
