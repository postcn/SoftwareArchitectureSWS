import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.ProtocolException;
import protocol.Servlet;

public class DeleteServlet extends Servlet {
	private PictureManager manager;

	public DeleteServlet(PictureManager manager) {
		this.manager = manager;
	}

	@Override
	public String getPath() {
		return "delete";
	}

	@Override
	public List<String> getAcceptedMethods() {
		LinkedList<String> methods = new LinkedList<String>();
		methods.add(Protocol.DELETE);
		return methods;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response)
			throws ProtocolException {
		String[] split_uri = request.getUri().split("/");
		if (split_uri.length != 4) {
			response.setStatus(Protocol.INTERNAL_SERVER_ERROR_CODE);
			return;
		}
		String filename = split_uri[3];

		JSONObject body = new JSONObject();
		File imageFile = manager.getPicture(filename);
		if (imageFile == null) {
			try {
				body.put("code", 404);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				body.put("status", manager.deletePicture(filename) ? 200 : 500);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		response.setFile(body.toString().toCharArray());
	}
}
