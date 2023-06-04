package sd2223.trab1.servers.rest;

import jakarta.inject.Singleton;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.PushMessage;
import sd2223.trab1.api.java.FeedsPush;
import sd2223.trab1.api.java.FeedsRep;
import sd2223.trab1.api.rest.FeedsServicePush;
import sd2223.trab1.api.rest.FeedsServiceRep;
import sd2223.trab1.servers.java.JavaFeedsPull;
import sd2223.trab1.servers.java.JavaFeedsPush;
import sd2223.trab1.servers.java.JavaFeedsRep;
import sd2223.trab1.servers.java.JavaFeedsRepPreconditions;


import java.util.List;

@Singleton
public class RestFeedsRepResource extends RestResource implements FeedsServiceRep {

    final FeedsRep impl;

    public RestFeedsRepResource() {
        this.impl = new JavaFeedsRep(new JavaFeedsRepPreconditions());
    }

    @Override
    public long postMessage(String user, String pwd, Message msg, Long version) {
        return super.fromJavaResult( impl.postMessage(user, pwd, msg, version));
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd, Long version) {
        super.fromJavaResult( impl.removeFromPersonalFeed(user, mid, pwd, version));
    }

    @Override
    public Message getMessage(String user, long mid, Long version) {
        return super.fromJavaResult( impl.getMessage(user, mid, version));
    }

    @Override
    public List<Message> getMessages(String user, long time, Long version) {
        return super.fromJavaResult( impl.getMessages(user, time, version));
    }
    @Override
    public void subUser(String user, String userSub, String pwd, Long version) {
        super.fromJavaResult( impl.subUser(user, userSub, pwd, version));
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd, Long version) {
        super.fromJavaResult( impl.unsubscribeUser(user, userSub, pwd, version));
    }

    @Override
    public List<String> listSubs(String user, Long version) {
        return super.fromJavaResult( impl.listSubs(user, version));
    }

    @Override
    public void deleteUserFeed(String user, Long version) {
        super.fromJavaResult( impl.deleteUserFeed(user, version));
    }
}
