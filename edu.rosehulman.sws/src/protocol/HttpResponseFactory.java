/*
 * HttpResponseFactory.java
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

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import export.HttpResponse;
import export.Protocol;

/**
 * This is a factory to produce various kind of HTTP responses.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class HttpResponseFactory {
	/**
	 * Convenience method for adding general header to the supplied response object.
	 * 
	 * @param response The {@link HttpResponse} object whose header needs to be filled in.
	 * @param connection Supported values are {@link Protocol#OPEN} and {@link Protocol#CLOSE}.
	 */
	private static void fillGeneralHeader(HttpResponse response, String connection) {
		// Lets add Connection header
		response.put(Protocol.CONNECTION, connection);

		// Lets add current date
		Date date = Calendar.getInstance().getTime();
		response.put(Protocol.DATE, date.toString());
		
		// Lets add server info
		response.put(Protocol.Server, Protocol.getServerInfo());

		// Lets add extra header with provider info
		response.put(Protocol.PROVIDER, Protocol.AUTHOR);
	}
	
	/**
	 * Creates a {@link HttpResponse} object for sending the supplied file with supplied connection
	 * parameter.
	 * 
	 * @param file The {@link File} to be sent.
	 * @param connection Supported values are {@link Protocol#OPEN} and {@link Protocol#CLOSE}.
	 * @return A {@link HttpResponse} object represent 200 status.
	 */
	private static HttpResponse create200OK(File file, String connection) {
		HttpResponse response = new HttpResponse(Protocol.VERSION, Protocol.OK_CODE, 
				Protocol.OK_TEXT, new HashMap<String, String>(), file);
		
		// Lets fill up header fields with more information
		fillGeneralHeader(response, connection);
		
		// Lets add last modified date for the file
		if (file != null && file.exists()) {
			long timeSinceEpoch = file.lastModified();
			Date modifiedTime = new Date(timeSinceEpoch);
			response.put(Protocol.LAST_MODIFIED, modifiedTime.toString());
			
			// Lets get content length in bytes
			long length = file.length();
			response.put(Protocol.CONTENT_LENGTH, length + "");
			
			// Lets get MIME type for the file
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			String mime = fileNameMap.getContentTypeFor(file.getName());
			// The fileNameMap cannot find mime type for all of the documents, e.g. doc, odt, etc.
			// So we will not add this field if we cannot figure out what a mime type is for the file.
			// Let browser do this job by itself.
			if(mime != null) { 
				response.put(Protocol.CONTENT_TYPE, mime);
			}
		}
		
		return response;
	}
	
	private static HttpResponse create201Created(File newFile, String connection) {
		HttpResponse response = new HttpResponse(Protocol.VERSION, Protocol.CREATED_CODE, 
				Protocol.CREATED_TEXT, new HashMap<String, String>(), null);
		
		// Lets fill up header fields with more information
		fillGeneralHeader(response, connection);
		
		// Lets add last modified date for the file
		if (newFile != null) {
			response.put(Protocol.LOCATION, newFile.getPath());
			
			// Lets get MIME type for the file
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			String mime = fileNameMap.getContentTypeFor(newFile.getName());
			// The fileNameMap cannot find mime type for all of the documents, e.g. doc, odt, etc.
			// So we will not add this field if we cannot figure out what a mime type is for the file.
			// Let browser do this job by itself.
			if(mime != null) { 
				response.put(Protocol.CONTENT_TYPE, mime);
			}
		}
		
		return response;
	}
	
	private static HttpResponse create202Accepted(String connection) {
		HttpResponse response = new HttpResponse(Protocol.VERSION, Protocol.ACCEPTED_CODE, 
				Protocol.ACCEPTED_TEXT, new HashMap<String, String>(), null);
		
		// Lets fill up header fields with more information
		fillGeneralHeader(response, connection);
		
		return response;
	}
	
	/**
	 * Creates a {@link HttpResponse} object for sending bad request response.
	 * 
	 * @param connection Supported values are {@link Protocol#OPEN} and {@link Protocol#CLOSE}.
	 * @return A {@link HttpResponse} object represent 400 status.
	 */
	private static HttpResponse create400BadRequest(String connection) {
		HttpResponse response = new HttpResponse(Protocol.VERSION, Protocol.BAD_REQUEST_CODE, 
				Protocol.BAD_REQUEST_TEXT, new HashMap<String, String>(), null);
		
		// Lets fill up header fields with more information
		fillGeneralHeader(response, connection);
		
		return response;
	}
	
	/**
	 * Creates a {@link HttpResponse} object for sending not found response.
	 * 
	 * @param connection Supported values are {@link Protocol#OPEN} and {@link Protocol#CLOSE}.
	 * @return A {@link HttpResponse} object represent 404 status.
	 */
	private static HttpResponse create404NotFound(String connection) {
		HttpResponse response = new HttpResponse(Protocol.VERSION, Protocol.NOT_FOUND_CODE, 
				Protocol.NOT_FOUND_TEXT, new HashMap<String, String>(), null);
		
		// Lets fill up the header fields with more information
		fillGeneralHeader(response, connection);
		
		return response;	
	}
	
	/**
	 * Creates a {@link HttpResponse} object for sending version not supported response.
	 * 
	 * @param connection Supported values are {@link Protocol#OPEN} and {@link Protocol#CLOSE}.
	 * @return A {@link HttpResponse} object represent 505 status.
	 */
	private static HttpResponse create505NotSupported(String connection) {
		HttpResponse response = new HttpResponse(Protocol.VERSION, Protocol.NOT_SUPPORTED_CODE, 
				Protocol.NOT_SUPPORTED_TEXT, new HashMap<String, String>(), null);
		
		// Lets fill up the header fields with more information
		fillGeneralHeader(response, connection);
		
		return response;
	}
	
	/**
	 * Creates a {@link HttpResponse} object for sending file not modified response.
	 * 
	 * @param connection Supported values are {@link Protocol#OPEN} and {@link Protocol#CLOSE}.
	 * @return A {@link HttpResponse} object represent 304 status.
	 */
	private static HttpResponse create304NotModified(String connection) {
		// TODO fill in this method
		return null;
	}
	
	private static HttpResponse create500InternalServerError(String connection) {
		HttpResponse response = new HttpResponse(Protocol.VERSION, Protocol.INTERNAL_SERVER_ERROR_CODE, 
				Protocol.INTERNAL_SERVER_ERROR_TEXT, new HashMap<String, String>(), null);
		
		// Lets fill up the header fields with more information
		fillGeneralHeader(response, connection);
		
		return response;
	}
	
	private static HttpResponse create405MethodNotAllowed(String connection) {
		HttpResponse response = new HttpResponse(Protocol.VERSION, Protocol.METHOD_NOT_ALLOWED_CODE, 
				Protocol.METHOD_NOT_ALLOWED_TEXT, new HashMap<String, String>(), null);
		
		// Lets fill up the header fields with more information
		fillGeneralHeader(response, connection);
		
		return response;
	}
	
	private static HttpResponse create410Gone(String connection) {
		HttpResponse response = new HttpResponse(Protocol.VERSION, Protocol.GONE_CODE, 
				Protocol.GONE_TEXT, new HashMap<String, String>(), null);
		
		// Lets fill up the header fields with more information
		fillGeneralHeader(response, connection);
		
		return response;
	}
	
	public static HttpResponse createResponse(int code, String connection, File file) {
		switch (code) {
		case Protocol.NOT_MODIFIED_CODE:
			return create304NotModified(connection);
		case Protocol.NOT_FOUND_CODE:
			return create404NotFound(connection);
		case Protocol.BAD_REQUEST_CODE:
			return create400BadRequest(connection);
		case Protocol.OK_CODE:
			return create200OK(file, connection);
		case Protocol.NOT_SUPPORTED_CODE:
			return create505NotSupported(connection);
		case Protocol.METHOD_NOT_ALLOWED_CODE:
			return create405MethodNotAllowed(connection);
		case Protocol.CREATED_CODE:
			return create201Created(file, connection);
		case Protocol.ACCEPTED_CODE:
			return create202Accepted(connection);
		case Protocol.GONE_CODE:
			return create410Gone(connection);
		default:
			return create500InternalServerError(connection);
		}
	}
}
