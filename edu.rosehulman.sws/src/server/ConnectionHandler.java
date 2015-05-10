/*
 * ConnectionHandler.java
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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.Protocol;
import protocol.ProtocolException;
import protocol.Servlet;

/**
 * This class is responsible for handling a incoming request by creating a
 * {@link HttpRequest} object and sending the appropriate response be creating a
 * {@link HttpResponse} object. It implements {@link Runnable} to be used in
 * multi-threaded environment.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class ConnectionHandler implements Runnable {
	private Server server;
	private Socket socket;
	private PluginManager manager;
	private long start;
	private long end;

	public ConnectionHandler(Server server, Socket socket, PluginManager manager) {
		this.server = server;
		this.socket = socket;
		this.manager = manager;
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * The entry point for connection handler. It first parses incoming request
	 * and creates a {@link HttpRequest} object, then it creates an appropriate
	 * {@link HttpResponse} object and sends the response back to the client
	 * (web browser).
	 */
	public void run() {
		// Get the start time
		long start = System.currentTimeMillis();

		InputStream inStream = null;
		OutputStream outStream = null;

		try {
			inStream = this.socket.getInputStream();
			outStream = this.socket.getOutputStream();
		} catch (Exception e) {
			// Cannot do anything if we have exception reading input or output
			// stream
			// May be have text to log this for further analysis?
			e.printStackTrace();

			// Increment number of connections by 1
			server.incrementConnections(1);
			// Get the end time
			long end = System.currentTimeMillis();
			this.server.incrementServiceTime(end - start);
			return;
		}

		// At this point we have the input and output stream of the socket
		// Now lets create a HttpRequest object
		HttpRequest request = null;
		HttpResponse response = HttpResponseFactory.createBlankResponse(
				Protocol.CLOSE, outStream);
		try {
			request = HttpRequest.read(inStream);
//			System.out.println(request);
			start = Calendar.getInstance().getTimeInMillis();
		} catch (ProtocolException pe) {
			// We have some sort of protocol exception. Get its status code and
			// create response
			// We know only two kind of exception is possible inside
			// fromInputStream
			// Protocol.BAD_REQUEST_CODE and Protocol.NOT_SUPPORTED_CODE
			response = HttpResponseFactory.createResponse(pe.getStatus(),
					Protocol.CLOSE, null, outStream);
		} catch (Exception e) {
			e.printStackTrace();
			// For any other error, we will create bad request response as well
			response = HttpResponseFactory.createResponse(
					Protocol.BAD_REQUEST_CODE, Protocol.CLOSE, null, outStream);
		}

		// We reached here means no error so far, so lets process further
		if (response.getStatus() == Protocol.MISSING_METHOD_CODE) {
			// location can either be the first element or the first two
			// elements when split
			Servlet handler;
			String method = request.getMethod();
			String[] split = request.getUri().split("/");
			String location = split.length >= 2 ? split[1] : "";
			String location2 = split.length >= 3 ? location + "/"
					+ request.getUri().split("/")[2] : null;

			handler = manager.getServletAtLocation(location);
			if (location2 != null && handler.getClass() == DefaultServlet.class) {
				handler = manager.getServletAtLocation(location2);
			}

			// now we have the servlet that will be handling the data.
			if (!handler.getAcceptedMethods().contains(method)) {
				response = HttpResponseFactory.createResponse(
						Protocol.METHOD_NOT_ALLOWED_CODE, Protocol.CLOSE, null,
						outStream);
			}

			else {
				try {
					handler.handle(request, response);
					
				} catch (ProtocolException pe) {
					response = HttpResponseFactory.createResponse(
							pe.getStatus(), Protocol.CLOSE, null, outStream);
				}
			}

		}

		try {
			// Write response and we are all done so close the socket
			end = Calendar.getInstance().getTimeInMillis();
			System.out.println("Serviced request in " + (end-start) + " milliseconds.");
			server.addLatency(end-start);
			response.write();
			socket.close();
		} catch (Exception e) {
			// We will ignore this exception
			e.printStackTrace();
		}
		
		LogRequest l = new LogRequest(socket.getInetAddress().toString(), request.toString(), response.toString());
		server.getLogManager().logRequest(l);

		// Increment number of connections by 1
		server.incrementConnections(1);
		// Get the end time
		long end = System.currentTimeMillis();
		this.server.incrementServiceTime(end - start);
		
		this.server.removeThread(this.socket.getInetAddress().getHostAddress());
	}
}
