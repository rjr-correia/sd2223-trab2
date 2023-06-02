package sd2223.trab1.servers.mastodon;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.servers.Domain;
import sd2223.trab1.servers.rest.AbstractRestServer;
import sd2223.trab1.servers.rest.RestUsersResource;
import utils.Args;

import javax.net.ssl.SSLContext;


public class ProxyServer extends AbstractRestServer {
    public static final int PORT = 15790;
    static final String SERVER_URI_FMT = "https://%s:%s/mastodon";

    private static Logger Log = Logger.getLogger(ProxyServer.class.getName());

    ProxyServer() {
        super( Log, Feeds.SERVICENAME, PORT);
    }

    @Override
    public void registerResources(ResourceConfig config) {
        config.register( ProxyResource.class );
    }

    public static void main(String[] args) throws Exception {
        Args.use( args );
        Domain.set( args[0], Long.valueOf(args[1]));
        new ProxyServer().start();

    }
}