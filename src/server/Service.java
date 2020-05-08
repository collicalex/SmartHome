package server;

import com.sun.net.httpserver.HttpHandler;

public abstract class Service implements HttpHandler {
	
	abstract protected void init();
	
	//Return the name of the service.
	//The name (to lower case) will be used to build the requested url.
	//Example: if the server is on url http://127.0.0.1:2806/
	//         returning 'Test' will make the service be requested via url http://127.0.0.1:2806/test/
	abstract public String getName();
	
	protected void log(String msg) {
		commons.Utils.log("["+getName()+"] " + msg);
	}
	
	protected void ler(String msg) {
		commons.Utils.ler("["+getName()+"] " + msg);
	}	
	
}
