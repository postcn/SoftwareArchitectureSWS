import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.ProtocolException;
import protocol.Servlet;
import sun.misc.BASE64Decoder;


public class PostServlet extends Servlet {
	private PictureManager manager;
	
	public PostServlet(PictureManager manager) {
		this.manager = manager;
	}

	@Override
	public List<String> getAcceptedMethods() {
		LinkedList<String> methods = new LinkedList<String>();
		methods.add(Protocol.POST);
		return methods;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response)
			throws ProtocolException {
		int length = request.getHeader().get(Protocol.CONTENT_LENGTH) == null ? 100 :Integer.parseInt(request.getHeader().get(Protocol.CONTENT_LENGTH));
		char[] body = new char[length];
		StringBuilder build = new StringBuilder();
		try {
			while (request.hasBodyData()) {
				int read = request.readBody(body);
				build.append(Arrays.copyOfRange(body, 0, read));
			}
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatus(Protocol.INTERNAL_SERVER_ERROR_CODE);
			return;
		}
		
		JSONObject ret = new JSONObject();
		try {
			 JSONObject o = new JSONObject(build.toString());
			 String filename = (String) o.get("filename");
			 String encoded = (String) o.get("image");
			 BASE64Decoder decode = new BASE64Decoder();
			 byte[] contents = decode.decodeBuffer(encoded);
			 if (manager.createPicture(filename, contents)) {
				 response.setStatus(Protocol.OK_CODE);
				 ret.put("status", 200);
			 } else {
				 response.setStatus(Protocol.NOT_MODIFIED_CODE);
				 ret.put("status", 403);
			 }
		} catch (JSONException e) {
			response.setStatus(Protocol.INTERNAL_SERVER_ERROR_CODE);
			try {
				ret.put("status", 500);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			response.setStatus(Protocol.INTERNAL_SERVER_ERROR_CODE);
			try {
				ret.put("status", 500);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		response.setFile(ret.toString().toCharArray());
	}

	@Override
	public String getPath() {
		return "create";
	}

}
