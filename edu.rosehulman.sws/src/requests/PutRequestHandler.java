/*
 * PostRequestHandler.java
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
import java.io.FileOutputStream;
import java.io.IOException;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.ProtocolException;
import protocol.Servlet;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class PutRequestHandler implements RequestHandler {
	/* (non-Javadoc)
	 * @see requests.RequestHandler#handle(server.Server, protocol.HttpRequest)
	 */
	@Override
	public HttpResponse handle(Servlet server, HttpRequest request, HttpResponse toFill) throws ProtocolException {
		String rootDirectory = server.getRootDirectory();
		String uri = request.getUri();
		File file = new File(rootDirectory + uri);
		char[] buffer = new char[Protocol.BUFFER_SIZE];
		boolean doesExist=false;
		if(file.exists()){
			doesExist=true;
		}
		try {
			if(!doesExist){
				file.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(file.getAbsoluteFile(),doesExist);
			while (request.hasBodyData()) {
				int read = request.readBody(buffer);
				out.write((new String(buffer)).getBytes(), 0, read);
			}
			out.close();
			toFill.setStatus(doesExist ? Protocol.ACCEPTED_CODE : Protocol.CREATED_CODE);
			toFill.setFile(new File(uri));
		} catch (IOException e) {
			e.printStackTrace();
			toFill.setStatus(Protocol.INTERNAL_SERVER_ERROR_CODE);
		}
		
		return toFill;
	}

}
