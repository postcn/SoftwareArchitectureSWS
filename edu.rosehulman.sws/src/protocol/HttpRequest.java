/*
 * HttpRequest.java
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

 
package protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import server.Server;

/**
 * Represents a request object for HTTP.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class HttpRequest {
	private String method;
	private String uri;
	private String version;
	private Map<String, String> header;
	private BufferedReader input;
	private boolean EOS = false;
	private int remainingData = 0;
	
	private HttpRequest() {
		this.header = new HashMap<String, String>();
	}
	
	/**
	 * The request method.
	 * 
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * The URI of the request object.
	 * 
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * The version of the http request.
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * The key to value mapping in the request header fields.
	 * 
	 * @return the header
	 */
	public Map<String, String> getHeader() {
		// Lets return the unmodifable view of the header map
		return Collections.unmodifiableMap(header);
	}

	/**
	 * Reads raw data from the supplied input stream and constructs a 
	 * <tt>HttpRequest</tt> object out of the raw data.
	 * Does not read the body of the request off of the request.
	 * 
	 * @param inputStream The input stream to read from.
	 * @return A <tt>HttpRequest</tt> object.
	 * @throws Exception Throws either {@link ProtocolException} for bad request or 
	 * {@link IOException} for socket input stream read errors.
	 */
	public static HttpRequest read(InputStream inputStream) throws Exception {
		// We will fill this object with the data from input stream and return it
		HttpRequest request = new HttpRequest();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		//First Request Line: GET /somedir/page.html HTTP/1.1
		String line = reader.readLine(); // A line ends with either a \r, or a \n, or both
		
		if(line == null) {
			throw new ProtocolException(Protocol.BAD_REQUEST_CODE, Protocol.BAD_REQUEST_TEXT);
		}
		
		// We will break this line using space as delimeter into three parts
		StringTokenizer tokenizer = new StringTokenizer(line, " ");
		
		// Error checking the first line must have exactly three elements
		if(tokenizer.countTokens() != 3) {
			throw new ProtocolException(Protocol.BAD_REQUEST_CODE, Protocol.BAD_REQUEST_TEXT);
		}
		
		request.method = tokenizer.nextToken();		// GET
		request.uri = tokenizer.nextToken();		// /somedir/page.html
		request.version = tokenizer.nextToken();	// HTTP/1.1
		
		if (!request.version.equals(Protocol.VERSION)) {
			throw new ProtocolException(Protocol.NOT_SUPPORTED_CODE, Protocol.NOT_SUPPORTED_TEXT);
		}
		
		// Rest of the request is a header that maps keys to values
		// e.g. Host: www.rose-hulman.edu
		// We will convert both the strings to lower case to be able to search later
		line = reader.readLine().trim();
		
		while(!line.equals("")) {
			// THIS IS A PATCH 
			// Instead of a string tokenizer, we are using string split
			// Lets break the line into two part with first space as a separator 
			
			// Now, get index of the first occurrence of space
			int index = line.indexOf(' ');
			
			if(index > 0 && index < line.length()-1) {
				// Now lets break the string in two parts
				String key = line.substring(0, index).trim().toLowerCase(); // Get first part, e.g. "Host:" then trim and put it to lower
				String value = line.substring(index+1).trim(); // Get the rest, e.g. "www.rose-hulman.edu" then trim it
				
				// Lets also remove ":" from the key
				key = key.substring(0, key.length() - 1);
				
				// Now lets put the key=>value mapping to the header map
				request.header.put(key, value);
			}
			
			// Processed one more line, now lets read another header line and loop
			line = reader.readLine().trim();
		}
		
		int contentLength = 0;
		try {
			contentLength = Integer.parseInt(request.header.get(Protocol.CONTENT_LENGTH.toLowerCase()));
		}
		catch(Exception e){}
		
		if(contentLength > 0) {
			request.input = reader;
			request.EOS = false;
		}
		else {
			request.EOS = true;
		}
		
		request.remainingData = contentLength;
		
		return request;
	}
	
	public boolean hasBodyData() {
		return !EOS;
	}
	
	public int readBody(char[] buffer) throws IOException {
		if (input != null) {
			int read;
			if (buffer.length > remainingData) {
				read = input.read(buffer, 0, remainingData);
			}else {
				read = input.read(buffer,0,Protocol.BUFFER_SIZE);
			}
			remainingData -= read;
			if (read == -1 || remainingData == 0) {
				EOS = true;
			}
			return read;
		}
		return -1;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("----------- Header ----------------\n");
		buffer.append(this.method);
		buffer.append(Protocol.SPACE);
		buffer.append(this.uri);
		buffer.append(Protocol.SPACE);
		buffer.append(this.version);
		buffer.append(Protocol.LF);
		
		for(Map.Entry<String, String> entry : this.header.entrySet()) {
			buffer.append(entry.getKey());
			buffer.append(Protocol.SEPERATOR);
			buffer.append(Protocol.SPACE);
			buffer.append(entry.getValue());
			buffer.append(Protocol.LF);
		}
//		buffer.append("------------- Body ---------------\n");
//		buffer.append(this.body);
//		buffer.append("----------------------------------\n");
		return buffer.toString();
	}
}
