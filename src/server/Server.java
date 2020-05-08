package server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Server extends Service {
	
	private HttpServer _server;
	private List<Service> _services;
	
	public Server(int port) throws IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, CertificateException, KeyManagementException {
		log("Create server on port " + port);
		_services = new LinkedList<Service>();
		_server = HttpServer.create(new InetSocketAddress(port), 0);
		
		_server.createContext("/", this);
		/*
		 * setExecutor(null) makes a single-threaded server. 
		 * To have a multithreaded server you need to provide a proper executor. 
		 * For example: httpsServer.setExecutor(new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100)));
		*/
		_server.setExecutor(null); //TODO creates a default executor
		_server.start();
	}
	
	public void addService(Service service) {
		log("Add service '" + service.getName() + "'");
		service.init();
		_server.createContext("/" + service.getName().toLowerCase(), service);
		_services.add(service);
	}
	
	
	//------------------------------------------------------------------------------


	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		Map<String, String> query = Utils.queryToMap(httpExchange);
		String action = query.get("action");		

		boolean stopServer = false;
		String response;
		
		if ((action != null) && ("stop".compareTo(action) == 0)) {
			log("Get action stop");
			response = "Stopping server";
			stopServer = true;
		} else {
			log("Get action list services");
			response = "Available services:\n";
			for (Service service : _services) {
				response += " - " + service.getName() + " at : " + service.getName().toLowerCase() + "\n";
			}
			
		}
		
		
		httpExchange.getResponseHeaders().add("Content-Type", "text/plain");
		httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        
        if (stopServer) {
        	log("Stopping server...");
        	_server.stop(5);
        	log("Server stopped!");
        	System.exit(0);
        }
	}

	@Override
	protected void init() {
	}

	@Override
	public String getName() {
		return "root";
	}
	
}
