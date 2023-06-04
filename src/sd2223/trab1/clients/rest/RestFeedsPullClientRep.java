package sd2223.trab1.clients.rest;

import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.FeedsPull;
import sd2223.trab1.api.java.FeedsRep;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.api.rest.FeedsServiceRep;

public class RestFeedsPullClientRep extends RestFeedsClient implements FeedsRep {
    protected static final String PERSONAL = "personal";

    final protected WebTarget target;
    public RestFeedsPullClientRep( String serverURI ) {
        super( serverURI );
        target = client.target( serverURI ).path( FeedsService.PATH );
    }

    @Override
    public Result<List<Message>> pull_getTimeFilteredPersonalFeed(String user, long time, long version) {
        return super.reTry(() -> clt_getTimeFilteredPersonalFeed(user, time, version));
    }

    public Result<List<Message>> clt_getTimeFilteredPersonalFeed(String user, long time, long version) {
        Response r = target.path(PERSONAL).path( user )
                .queryParam(FeedsServiceRep.TIME, time)
                .request()
                .header(FeedsServiceRep.HEADER_VERSION, version)
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, new GenericType<List<Message>>() {});
    }


    @Override
    public Result<Void> deleteUserFeed(String user, long version) {
        return super.reTry(() -> clt_deleteUserFeed(user, version));
    }

    @Override
    public Result<Message> getMessage(String user, long mid, long version) {
        return super.reTry(() -> clt_getMessage(user, mid, version));
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time, long version) {
        return super.reTry(() -> clt_getMessages(user, time, version));
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg, long version) {
        return super.reTry(() -> clt_postMessage(user, pwd, msg, version));
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd, long version) {
        return super.reTry(() -> clt_removeFromPersonalFeed(user, mid, pwd, version));
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd, long version) {
        return super.reTry(() -> clt_subUser(user, userSub, pwd, version));
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd, long version) {
        return super.reTry(() -> clt_unsubscribeUser(user, userSub, pwd, version));
    }

    @Override
    public Result<List<String>> listSubs(String user, long version) {
        return super.reTry(() -> clt_listSubs(user, version));
    }


    private Result<Message> clt_getMessage(String user, long mid, long version) {
        Response r = target.path(user).path( Long.toString(mid) )
                .request()
                .get();

        return super.toJavaResult(r, Message.class);
    }

    private Result<List<Message>> clt_getMessages(String user, long time, long version) {
        Response r = target.path( user )
                .queryParam(FeedsServiceRep.TIME, time)
                .request()
                .header(FeedsServiceRep.HEADER_VERSION, version)
                .get();

        return super.toJavaResult(r, new GenericType<List<Message>>() {});
    }

    public Result<Void> clt_deleteUserFeed(String user, long version) {
        Response r = target.path(PERSONAL).path( user )
                .request()
                .header(FeedsServiceRep.HEADER_VERSION, version)
                .delete();

        return super.toJavaResult(r, Void.class);
    }

    private Result<Long> clt_postMessage(String username, String pwd, Message msg, long version) {
        Response r = target.path(username)
                .queryParam(FeedsServiceRep.PWD, pwd).request()
                .header(FeedsServiceRep.HEADER_VERSION, version)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(msg, MediaType.APPLICATION_JSON));

        return super.toJavaResult(r, Long.class);
    }

    private Result<Void> clt_removeFromPersonalFeed(String user, long mid, String pwd, long version) {
        Response r = target.path(user + "/" + mid)
                .queryParam(FeedsServiceRep.PWD, pwd).request()
                .header(FeedsServiceRep.HEADER_VERSION, version)
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        return super.toJavaResult(r, Void.class);

    }
    private Result<Void> clt_subUser(String username, String userSub, String pwd, long version) {
        Response r = target.path("sub/" + username + "/" + userSub)
                .queryParam(FeedsServiceRep.PWD, pwd).request()
                .header(FeedsServiceRep.HEADER_VERSION, version)
                .accept(MediaType.APPLICATION_JSON)
                .post(null);

        return super.toJavaResult(r, Void.class);
    }
    private Result<Void> clt_unsubscribeUser(String username, String userSub, String pwd, long version) {
        Response r = target.path("sub/" + username + "/" + userSub)
                .queryParam(FeedsServiceRep.PWD, pwd).request()
                .header(FeedsServiceRep.HEADER_VERSION, version)
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        return super.toJavaResult(r, Void.class);
    }

    private Result<List<String>> clt_listSubs(String user, long version) {
        Response r = target.path("sub/list/" + user)
                .request()
                .header(FeedsServiceRep.HEADER_VERSION, version)
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        return super.toJavaResult(r, new GenericType<List<String>>() {});
    }
}
