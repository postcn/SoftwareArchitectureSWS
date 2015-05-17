import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.Protocol;
import protocol.ProtocolException;
import protocol.Servlet;

public class ListServlet extends Servlet {
	private PictureManager manager;

	public ListServlet(PictureManager manager) {
		this.manager = manager;
	}

	@Override
	public List<String> getAcceptedMethods() {
		LinkedList<String> methods = new LinkedList<String>();
		methods.add(Protocol.GET);
		methods.add(Protocol.DELETE);
		return methods;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response)
			throws ProtocolException {
		if (request.getMethod().equals(Protocol.GET)) {
			handleGet(request, response);
		} else if (request.getMethod().equals(Protocol.DELETE)) {
			handleDelete(request, response);
		}
	}

	public void handleGet(HttpRequest request, HttpResponse response) {
		String[] split_uri = request.getUri().split("/");
		JSONObject ret = new JSONObject();
		if (split_uri.length != 4 && split_uri.length != 3) {
			response.setStatus(Protocol.INTERNAL_SERVER_ERROR_CODE);
			return;
		}
		List<String> names = new ArrayList<String>();
		if (split_uri.length == 4) {
			names = manager.getAllNames(split_uri[3]);
		} else if (split_uri.length == 3) {
			names = manager.getAllNames();
		}
		String[] namesConv = names.toArray(new String[names.size()]);
		try {
			ret.put("files", namesConv);
			ret.put("count", namesConv.length);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		response.setStatus(Protocol.OK_CODE);
		response.setFile(ret.toString().toCharArray());
	}

	public void handleDelete(HttpRequest request, HttpResponse response) {
		String[] split_uri = request.getUri().split("/");
		JSONObject ret = new JSONObject();
		if (split_uri.length != 4 && split_uri.length != 3) {
			response.setStatus(Protocol.INTERNAL_SERVER_ERROR_CODE);
			return;
		}
		int count = 0;
		if (split_uri.length == 4) {
			count = manager.deleteAllMatching((split_uri[3]));
		} else if (split_uri.length == 3) {
			count = manager.deleteAll();
		}
		try {
			ret.put("count", count);
			ret.put("status", 200);
		} catch (JSONException e) {
			e.printStackTrace();
			try {
				ret.put("status", 500);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		response.setStatus(Protocol.OK_CODE);
		response.setFile(ret.toString().toCharArray());
	}

	@Override
	public String getPath() {
		return "list";
	}

}
