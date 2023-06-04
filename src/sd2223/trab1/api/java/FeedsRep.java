package sd2223.trab1.api.java;

import java.util.List;

import sd2223.trab1.api.Message;

public interface FeedsRep{
    static String SERVICENAME = "feeds";

    Result<Long> postMessage(String user, String pwd, Message msg, long version);

    Result<Void> removeFromPersonalFeed(String user, long mid, String pwd, long version);

    Result<Message> getMessage(String user, long mid, long version);

    Result<List<Message>> getMessages(String user, long time, long version);

    Result<Void> subUser(String user, String userSub, String pwd, long version);

    Result<Void> unsubscribeUser(String user, String userSub, String pwd, long version);

    Result<List<String>> listSubs(String user, long version);

    Result<Void> deleteUserFeed(String user, long version);

    Result<List<Message>> pull_getTimeFilteredPersonalFeed(String user, long time, long version);
}
