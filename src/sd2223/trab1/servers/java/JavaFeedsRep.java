package sd2223.trab1.servers.java;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.FeedsPush;
import sd2223.trab1.api.java.FeedsRep;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.servers.Domain;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static sd2223.trab1.api.java.Result.ErrorCode.*;
import static sd2223.trab1.api.java.Result.error;
import static sd2223.trab1.api.java.Result.ok;
import static sd2223.trab1.clients.Clients.FeedsRepClients;

public class JavaFeedsRep  implements FeedsRep {
    private static final long FEEDS_MID_PREFIX= 1_000_000_000;
    private static final long FEEDS_CACHE_EXPIRATION = 3000;

    protected AtomicLong serial = new AtomicLong(Domain.uuid() * FEEDS_MID_PREFIX);

    final protected FeedsRep preconditions;

    public JavaFeedsRep( FeedsRep preconditions){
        this.preconditions = preconditions;
    }

    protected Map<Long, Message> messages = new ConcurrentHashMap<>();
    protected Map<String, JavaFeedsCommon.FeedInfo> feeds = new ConcurrentHashMap<>();

    final LoadingCache<JavaFeedsRep.FeedInfoKey, Result<List<Message>>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMillis(FEEDS_CACHE_EXPIRATION)).removalListener((e) -> {
            }).build(new CacheLoader<>() {
                @Override
                public Result<List<Message>> load(JavaFeedsRep.FeedInfoKey info) throws Exception {
                    var res = FeedsRepClients.get(info.domain()).pull_getTimeFilteredPersonalFeed(info.user(), info.time(), info.version());
                    if (res.error() == TIMEOUT)
                        return error(BAD_REQUEST);

                    return res;
                }
            });

    static protected record FeedInfo(String user, Set<Long> messages, Set<String> following, Set<String> followees) {
        public FeedInfo(String user) {
            this(user, new HashSet<>(), new HashSet<>(), ConcurrentHashMap.newKeySet());
        }
    }

    public Result<Long> postMessage(String user, String pwd, Message msg, long version) {

        var preconditionsResult = preconditions.postMessage(user, pwd, msg, version);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;

        if(msg.getId() == -1){

            Long mid = serial.incrementAndGet();
            msg.setId(mid);
            msg.setCreationTime(System.currentTimeMillis());
        }

        JavaFeedsCommon.FeedInfo ufi = feeds.computeIfAbsent(user, JavaFeedsCommon.FeedInfo::new );
        synchronized (ufi.user()) {
            ufi.messages().add(msg.getId());
            messages.putIfAbsent(msg.getId(), msg);
        }
        return Result.ok(msg.getId());
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd, long version) {

        var preconditionsResult = preconditions.removeFromPersonalFeed(user, mid, pwd, version);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;

        var ufi = feeds.get(user);
        if( ufi == null )
            return error(NOT_FOUND);

        synchronized (ufi.user()) {
            if (!ufi.messages().remove(mid))
                return error(NOT_FOUND);
        }

        deleteFromUserFeed( user, Set.of(mid), version );

        return ok();
    }


    protected List<Message> getTimeFilteredPersonalFeed(String user, long time, long version) {
        var ufi = feeds.computeIfAbsent(user, JavaFeedsCommon.FeedInfo::new );
        synchronized (ufi.user()) {
            return ufi.messages().stream().map(messages::get).filter(m -> m.getCreationTime() > time).toList();
        }
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd, long version) {

        var preconditionsResult = preconditions.subUser(user, userSub, pwd, version);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;

        var ufi = feeds.computeIfAbsent(user, JavaFeedsCommon.FeedInfo::new );
        synchronized (ufi.user()) {

            ufi.following().add(userSub);
        }
        return ok();
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd, long version) {

        var preconditionsResult = preconditions.unsubscribeUser(user, userSub, pwd, version);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;

        JavaFeedsCommon.FeedInfo ufi = feeds.computeIfAbsent(user, JavaFeedsCommon.FeedInfo::new);
        synchronized (ufi.user()) {

            if(!ufi.following().remove(userSub))
                return error(NOT_FOUND);

            ufi.following().remove(userSub);
        }
        return ok();
    }

    @Override
    public Result<List<String>> listSubs(String user, long version) {

        var preconditionsResult = preconditions.listSubs(user, version);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;

        JavaFeedsCommon.FeedInfo ufi = feeds.computeIfAbsent(user, JavaFeedsCommon.FeedInfo::new);
        synchronized (ufi.user()) {
            return ok(new ArrayList<>(ufi.following()));
        }
    }

    @Override
    public Result<Void> deleteUserFeed(String user, long version) {

        var preconditionsResult = preconditions.deleteUserFeed(user, version);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;

        JavaFeedsCommon.FeedInfo ufi = feeds.remove(user);
        if (ufi == null)
            return error(NOT_FOUND);

        synchronized (ufi.user()) {
            deleteFromUserFeed(user, ufi.messages(), version);
            for (var u : ufi.followees())
                ufi.following().remove(u);
        }
        return ok();
    }


    static public record FeedUser(String user, String name, String pwd, String domain, long version) {
        private static final String EMPTY_PASSWORD = "";

        public static JavaFeedsCommon.FeedUser from(String name, String pwd) {
            var idx = name.indexOf('@');
            var n = idx < 0 ? name : name.substring(0, idx);
            var d = idx < 0 ? Domain.get() : name.substring(idx + 1);
            return new JavaFeedsCommon.FeedUser(name, n, pwd, d);
        }

        public static JavaFeedsCommon.FeedUser from(String name) {
            return JavaFeedsCommon.FeedUser.from(name, EMPTY_PASSWORD);
        }

        boolean isLocalUser() {
            return domain.equals(Domain.get());
        }

        public boolean isRemoteUser() {
            return ! isLocalUser();
        }

    };

    protected void deleteFromUserFeed( String user, Set<Long> mids, long version){

    }
    @Override
    public Result<Message> getMessage(String user, long mid, long version) {
        var preconditionsResult = preconditions.getMessage(user, mid, version);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;

        var ufi = feeds.get(user);
        if (ufi == null)
            return error(NOT_FOUND);

        synchronized (ufi.user()) {
            if (!ufi.messages().contains(mid))
                return error(NOT_FOUND);

            return ok(messages.get(mid));
        }
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time, long version) {
        var preconditionsResult = preconditions.getMessages(user, time, version);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        var ufi = feeds.computeIfAbsent(user, JavaFeedsCommon.FeedInfo::new );
        synchronized (ufi.user()) {
            return ok(ufi.messages().stream().map(messages::get).filter(m -> m.getCreationTime() > time).toList());
        }

    }
    public Result<List<Message>> pull_getTimeFilteredPersonalFeed(String user, long time, long version) {

        var preconditionsResult = preconditions.pull_getTimeFilteredPersonalFeed(user, time, version);
        if( ! preconditionsResult.isOK() )
            return preconditionsResult;
        var ufi = feeds.computeIfAbsent(user, JavaFeedsCommon.FeedInfo::new );
        synchronized (ufi.user()) {
            return ok(ufi.messages().stream().map(messages::get).filter(m -> m.getCreationTime() > time).toList());
        }
    }

    private List<Message> getCachedPersonalFeed(String name, long time, long version) {
        try {
            if (JavaFeedsCommon.FeedUser.from(name).isRemoteUser())
                return cache.get( JavaFeedsRep.FeedInfoKey.from(name, time, version) ).value();
            else {
                var ufi = feeds.computeIfAbsent(name, JavaFeedsCommon.FeedInfo::new);
                synchronized (ufi.user()) {
                    return ufi.messages().stream().map(messages::get).filter(m -> m.getCreationTime() > time).toList();
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return Collections.emptyList();
    }

    static record FeedInfoKey(String user, String domain, long time, long version) {
        static JavaFeedsRep.FeedInfoKey from(String name, long time, long version) {
            var idx = name.indexOf('@');
            var domain = idx < 0 ? Domain.get() : name.substring(idx + 1);
            return new JavaFeedsRep.FeedInfoKey(name, domain, time, version);
        }
    }
}
