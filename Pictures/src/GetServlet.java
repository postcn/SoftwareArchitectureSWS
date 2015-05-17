
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;


import org.json.JSONException;
import org.json.JSONObject;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.ProtocolException;
import protocol.Servlet;
import sun.misc.BASE64Encoder;


public class GetServlet extends Servlet {
	private static List<String> acceptedMethods;
	private PictureManager manager;
	
	static {
		acceptedMethods = new LinkedList<String>();
		acceptedMethods.add("exists");
		acceptedMethods.add("return");
	}
	
	public GetServlet(PictureManager manager) {
		this.manager = manager;
	}

	@Override
	public String getPath() {
		return "get";
	}

	@Override
	public List<String> getAcceptedMethods() {
		LinkedList<String> methods = new LinkedList<String>();
		methods.add(Protocol.GET);
		return methods;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response)
			throws ProtocolException {
		String[] split_uri = request.getUri().split("/");
		if (split_uri.length != 5) {
			response.setStatus(Protocol.INTERNAL_SERVER_ERROR_CODE);
			return;
		}
		String method = split_uri[3];
		String filename = split_uri[4];
		
		if (!acceptedMethods.contains(method)) {
			response.setStatus(Protocol.INTERNAL_SERVER_ERROR_CODE);	
		}
		else {
			JSONObject body = new JSONObject();
			if (method.equals("exists")) {
				boolean found = manager.getPicture(filename) != null;
				try {
					body.put("code", found ? 200 : 404);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} 
			else if (method.equals("return")) {
				File imageFile = manager.getPicture(filename);
				if (imageFile == null) {
					try {
						body.put("code", 404);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				else {
					try {
						byte[] data = Files.readAllBytes(imageFile.toPath());
						String imageString = new BASE64Encoder().encode(data);
						body.put("code", 200);
						body.put("image", imageString);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			response.setFile(body.toString().toCharArray());
		}
	}

}
