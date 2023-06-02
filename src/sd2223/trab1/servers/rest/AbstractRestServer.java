package sd2223.trab1.servers.rest;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import sd2223.trab1.discovery.Discovery;
import sd2223.trab1.servers.java.AbstractServer;
import utils.IP;
import utils.InsecureHostnameVerifier;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;


public abstract class AbstractRestServer extends AbstractServer {
	
	private static final String REST_CTX = "/rest";
	static final String SERVER_URI_FMT = "https://%s:%s/rest";
	private int port;

	protected AbstractRestServer(Logger log, String service, int port) {
		super(log, service, String.format(SERVER_BASE_URI, IP.hostAddress(), port, REST_CTX));

		this.port = port;
	}


	protected void start() throws NoSuchAlgorithmException, UnknownHostException {

		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
		ResourceConfig config = new ResourceConfig();

		registerResources(config);

		var ip = InetAddress.getLocalHost().getHostAddress();

		var serverURI = URI.create(String.format(SERVER_URI_FMT, ip, port));
		JdkHttpServerFactory.createHttpServer( serverURI, config, SSLContext.getDefault());
		
		registerResources( config );
		
		Discovery.getInstance().announce(service, super.serverURI);
		Log.info(String.format("%s Server ready @ %s\n",  service, serverURI));
	}
	
	abstract void registerResources( ResourceConfig config );
}
