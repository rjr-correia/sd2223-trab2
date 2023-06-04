package utils;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import sd2223.trab1.api.rest.FeedsServiceKafka;
import sd2223.trab1.servers.kafka.KafkaResource;

import java.io.IOException;

@Provider
public class VersionFilter implements ContainerResponseFilter {

    static long version;

    public VersionFilter(KafkaResource resource) {

        this.version = resource.getVersion();
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response)
            throws IOException {
        response.getHeaders().add(FeedsServiceKafka.HEADER_VERSION, version);
    }
}

