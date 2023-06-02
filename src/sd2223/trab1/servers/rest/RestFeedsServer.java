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
	public static final int PORT = 4566;
	static final String SERVER_URI_FMT = "https://%s:%s/rest";

	private static Logger Log = Logger.getLogger(RestFeedsServer.class.getName());

	RestFeedsServer() {
		super( Log, Feeds.SERVICENAME, PORT);
	}
	
	@Override
	public void registerResources(ResourceConfig config) {
		config.register( Args.valueOf("-push", true) ? RestFeedsPushResource.class : RestFeedsPullResource.class ); 
	}
	
	public static void main(String[] args) throws Exception {
		Args.use( args );
		Domain.set( args[0], Long.valueOf(args[1]));
		new RestFeedsServer().start();

	}	
}