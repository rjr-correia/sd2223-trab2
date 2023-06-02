package sd2223.trab1.servers.soap;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import jakarta.xml.ws.Endpoint;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.servers.Domain;

import javax.net.ssl.SSLContext;

public class SoapUsersServer extends AbstractSoapServer<SoapUsersWebService> {

	public static final int PORT = 13456;
	private static Logger Log = Logger.getLogger(SoapUsersServer.class.getName());

	protected SoapUsersServer() {
		super(false, Log, Users.SERVICENAME, PORT,  new SoapUsersWebService() );
	}

	public static void main(String[] args) throws Exception {		
		Domain.set( args[0], 0);
		Log.setLevel(Level.INFO);

		var ip = InetAddress.getLocalHost().getHostAddress();

		var server = HttpsServer.create(new InetSocketAddress(ip, PORT), 0);

		server.setExecutor(Executors.newCachedThreadPool());
		server.setHttpsConfigurator(new HttpsConfigurator(SSLContext.getDefault()));

		var endpoint = Endpoint.create(new SoapUsersWebService());
		endpoint.publish(server.createContext("/soap"));

		server.start();

		new SoapUsersServer().start();

	}
}
