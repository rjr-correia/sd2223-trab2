package sd2223.trab1.servers.mastodon;

import java.util.logging.Logger;
import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.servers.Domain;
import sd2223.trab1.servers.rest.AbstractRestServer;
import utils.Args;


public class ProxyServer extends AbstractRestServer {
    public static final int PORT = 15790;

    private static Logger Log = Logger.getLogger(ProxyServer.class.getName());

    ProxyServer() {
        super( Log, Feeds.SERVICENAME, PORT);
    }

    @Override
    public void registerResources(ResourceConfig config) {
        System.out.println("In proxy server");
        config.register( ProxyResource.class );
    }

    public static void main(String[] args) throws Exception {
        Args.use( args );
        Domain.set( args[0], Long.valueOf(args[1]));
        new ProxyServer().start();

    }
}