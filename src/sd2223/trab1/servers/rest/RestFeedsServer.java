package sd2223.trab1.servers.rest;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.servers.Domain;
import utils.Args;

import javax.net.ssl.SSLContext;


public class RestFeedsServer extends AbstractRestServer {
	public static final int PORT = 14566;
	static final String SERVER_URI_FMT = "https://%s:%s/rest";

	private static Logger Log = Logger.getLogger(RestFeedsServer.class.getName());

	RestFeedsServer() {
		super( Log, Feeds.SERVICENAME, PORT);
	}
	
	@Override
	void registerResources(ResourceConfig config) {
		config.register( Args.valueOf("-push", true) ? RestFeedsPushResource.class : RestFeedsPullResource.class ); 
	}
	
	public static void main(String[] args) throws Exception {
		Args.use( args );
		Domain.set( args[0], Long.valueOf(args[1]));
		var config = new ResourceConfig();
		config.register(RestUsersResource.class);

		var ip = InetAddress.getLocalHost().getHostAddress();
		var serverURI = URI.create(String.format(SERVER_URI_FMT, ip, PORT));
		JdkHttpServerFactory.createHttpServer( serverURI, config, SSLContext.getDefault());
		new RestFeedsServer().start();

	}	
}