/*
 * Protocol.java
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

/**
 * This class is a collection of HTTP protocol related constants, 
 * that can be used uniformly across the classes of this project.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class Protocol {
	// Escape characters
    public static final char SPACE = ' ';
    public static final char SEPERATOR = ':';
    public static final char SLASH = '/';
    public static final char CR = '\r';
    public static final char LF = '\n';
    public static final String CRLF = "" + CR + LF;

    // Some useful protocol elements
    public static final String VERSION = "HTTP/1.1";
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    
    public static final int MISSING_METHOD_CODE = -1;
    public static final String MISSING_METHOD_TEXT = "Missing Method. Improper HTTPResponse";
    
    // Some useful http codes and text
    public static final int OK_CODE = 200;
    public static final String OK_TEXT = "OK";
    
    public static final int CREATED_CODE = 201;
    public static final String CREATED_TEXT = "Created";
    
    public static final int ACCEPTED_CODE = 202;
    public static final String ACCEPTED_TEXT = "Accepted";
    
    public static final int NO_CONTENT_CODE = 204;
    public static final String NO_CONTENT_TEXT = "No Content";
    
    public static final int MOVED_PERMANENTLY_CODE = 301;
    public static final String MOVED_PERMANENTLY_TEXT = "Moved Permanently";
    
    public static final int BAD_REQUEST_CODE = 400;
    public static final String BAD_REQUEST_TEXT = "Bad Request";
    
    public static final int NOT_FOUND_CODE = 404;
    public static final String NOT_FOUND_TEXT = "Not Found";
    
    public static final int GONE_CODE = 410;
    public static final String GONE_TEXT = "GONE";
    
    public static final int METHOD_NOT_ALLOWED_CODE = 405;
    public static final String METHOD_NOT_ALLOWED_TEXT = "Method Not Allowed";
    
    public static final int NOT_SUPPORTED_CODE = 505;
    public static final String NOT_SUPPORTED_TEXT = "HTTP Version Not Supported";
    
    public static final int INTERNAL_SERVER_ERROR_CODE = 500;
    public static final String INTERNAL_SERVER_ERROR_TEXT = "Internal Server Error";
    
    public static final int NOT_MODIFIED_CODE = 304;
    public static final String NOT_MODIFIED_TEXT = "Not Modified";

    // Some useful header elements in request
    public static final String HOST = "Host";
    public static final String CONNECTION = "connection";
    public static final String USER_AGENT = "user-agent";

    // Some useful header elements in response
    public static final String DATE = "date";
    public static final String Server = "server";
    public static final String LAST_MODIFIED = "last-modified";
    public static final String CONTENT_LENGTH = "content-length";
    public static final String CONTENT_TYPE = "content-type";
    public static final String LOCATION = "location";
    
    /**
     * A chunk size to be used when reading a file and sending it to a socket. 
     * Rather than reading the whole file at once, we divide the reading of the file
     * into number of small chunk of bytes and send each chunk to the socket to 
     * utilize the memory better.
     */
    public static final int BUFFER_SIZE = 262144; //256 KB
    
    // Server information that we want to send in "Server:" header field
    public static final String SERVER_INFO = "SimpleWebServer(SWS)/1.0.0";
    public static final String PROVIDER = "Provider";
    public static final String AUTHOR = "Chandan R. Rupakheti";
    public static final String CLOSE = "Close";
    public static final String OPEN = "Keep-Alive";
    public static final String DEFAULT_FILE = "index.html";
    public static final String MIME_TEXT = "text";
    
    /**
     * Returns a formatted String containing server information.<br/>
     * e.g. <tt>SimpleWebServer(SWS)/1.0.0 (Mac OS X/10.5.8/i386)</tt>
     * @return
     */
	public static String getServerInfo() {
		String os = System.getProperty("os.name"); // e.g. Mac OSX, Ubuntu, etc.
		String osVersion = System.getProperty("os.version"); // e.g. 10.5, 10.0.4, etc
		String architecture = System.getProperty("os.arch"); // e.g. i386, x86_64, etc
		String serverInfo = Protocol.SERVER_INFO + Protocol.SPACE + 
				"(" + os + Protocol.SLASH + osVersion + Protocol.SLASH + architecture + ")";
		return serverInfo;
	}
}
