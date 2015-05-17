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


public class PutServlet extends Servlet {
	private PictureManager manager;
	
	public PutServlet(PictureManager manager) {
		this.manager = manager;
	}

	@Override
	public List<String> getAcceptedMethods() {
		LinkedList<String> methods = new LinkedList<String>();
		methods.add(Protocol.PUT);
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
		
		try {
			 JSONObject o = new JSONObject(build.toString());
			 String filename = (String) o.get("filename");
			 String encoded = (String) o.get("image");
			 BASE64Decoder decode = new BASE64Decoder();
			 byte[] contents = decode.decodeBuffer(encoded);
			 if (manager.editPicture(filename, contents)) {
				 response.setStatus(Protocol.OK_CODE);
			 } else {
				 response.setStatus(Protocol.NOT_FOUND_CODE);
			 }
		} catch (JSONException e) {
			response.setStatus(Protocol.INTERNAL_SERVER_ERROR_CODE);
		} catch (IOException e) {
			response.setStatus(Protocol.INTERNAL_SERVER_ERROR_CODE);
		}
	}

	@Override
	public String getPath() {
		return "edit";
	}

}
