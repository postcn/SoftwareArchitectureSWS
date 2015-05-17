import java.util.LinkedList;
import java.util.List;

import protocol.Plugin;
import protocol.Servlet;


public class PicturePlugin implements Plugin {
	PictureManager manager;

	@Override
	public String getLocation() {
		return "photo";
	}

	@Override
	public List<Servlet> getServlets() {
		LinkedList<Servlet> servlets = new LinkedList<>();
		servlets.add(new GetServlet(manager));
		servlets.add(new PostServlet(manager));
		servlets.add(new PutServlet(manager));
		servlets.add(new DeleteServlet(manager));
		servlets.add(new ListServlet(manager));
		
		for (Servlet s: servlets) {
			s.setPlugin(this);
		}
		return servlets;
	}

	@Override
	public void load() {
		try {
			manager = new PictureManager();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

}
