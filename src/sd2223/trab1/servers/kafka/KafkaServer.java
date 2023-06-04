package sd2223.trab1.servers.kafka;

import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.servers.Domain;
import sd2223.trab1.servers.rest.AbstractRestServer;
import utils.Args;
import utils.VersionFilter;
import java.util.logging.Logger;

public class KafkaServer extends AbstractRestServer {
        public static final int PORT = 17321;

        private static Logger Log = Logger.getLogger(KafkaServer.class.getName());

        KafkaServer() {
            super( Log, Feeds.SERVICENAME, PORT);
        }

        @Override
        public void registerResources(ResourceConfig config) {

            config.register( new KafkaResource());
            config.register(new VersionFilter(new KafkaResource()));
        }

        public static void main(String[] args) throws Exception {
            Args.use( args );
            Domain.set( args[0], Long.valueOf(args[1]));
            new KafkaServer().start();

    }
}
