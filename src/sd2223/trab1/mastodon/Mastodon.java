package sd2223.trab1.mastodon;

import static sd2223.trab1.api.java.Result.error;
import static sd2223.trab1.api.java.Result.ok;
import static sd2223.trab1.api.java.Result.ErrorCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.reflect.TypeToken;

import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.mastodon.msgs.PostStatusArgs;
import sd2223.trab1.mastodon.msgs.PostStatusResult;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import sd2223.trab1.servers.Domain;
import sd2223.trab1.servers.soap.SoapUsersServer;
import utils.JSON;

public class Mastodon implements Feeds {

    static String MASTODON_NOVA_SERVER_URI = "http://10.170.138.52:3000";
    static String MASTODON_SOCIAL_SERVER_URI = "https://mastodon.social";

    static String MASTODON_SERVER_URI = MASTODON_NOVA_SERVER_URI;

    private static final String clientKey = "hKcoCjq-Cjfgrs8E5GEBv2TdZSTU4acLrJL6JPepQlw";
    private static final String clientSecret = "28-azi3iCJNCo6Vy7AHYFwdXUsw363VoyL8Dcfk9Od0";
    private static final String accessTokenStr = "ztBowRMxYi5VR51vPlfj_ABMltXSOvuf_mFJw9Sx2KE";

    static final String STATUSES_PATH = "/api/v1/statuses";
    static final String TIMELINES_PATH = "/api/v1/timelines/home";
    static final String ACCOUNT_FOLLOWING_PATH = "/api/v1/accounts/%s/following";
    static final String VERIFY_CREDENTIALS_PATH = "/api/v1/accounts/verify_credentials";
    static final String SEARCH_ACCOUNTS_PATH = "/api/v1/accounts/search";
    static final String ACCOUNT_FOLLOW_PATH = "/api/v1/accounts/%s/follow";
    static final String ACCOUNT_UNFOLLOW_PATH = "/api/v1/accounts/%s/unfollow";


    private static final int HTTP_OK = 200;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_BAD_REQUEST = 400;


    protected OAuth20Service service;
    protected OAuth2AccessToken accessToken;

    private static Mastodon impl;

    private static Logger Log = Logger.getLogger(Mastodon.class.getName());


    protected Mastodon() {
        try {
            service = new ServiceBuilder(clientKey).apiSecret(clientSecret).build(MastodonApi.instance());
            accessToken = new OAuth2AccessToken(accessTokenStr);
        } catch (Exception x) {
            x.printStackTrace();
            System.exit(0);
        }
    }

    synchronized public static Mastodon getInstance() {
        if (impl == null)
            impl = new Mastodon();
        return impl;
    }

    private String getEndpoint(String path, Object... args) {
        var fmt = MASTODON_SERVER_URI + path;
        return String.format(fmt, args);
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        try {
            final OAuthRequest request = new OAuthRequest(Verb.POST, getEndpoint(STATUSES_PATH));

            JSON.toMap(new PostStatusArgs(msg.getText())).forEach((k, v) -> {
                request.addBodyParameter(k, v.toString());
            });
            service.signRequest(accessToken, request);

            Response response = service.execute(request);
            if (response.getCode() == HTTP_OK) {
                var res = JSON.decode(response.getBody(), PostStatusResult.class);
                return ok(res.getId());
            }
            if (response.getCode() == HTTP_NOT_FOUND) {
                return error(NOT_FOUND);
            }
            if (response.getCode() == HTTP_BAD_REQUEST) {
                return error(BAD_REQUEST);
            }

        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(INTERNAL_ERROR);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        try {
            final OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(TIMELINES_PATH));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            if (response.getCode() == HTTP_OK) {
                List<PostStatusResult> res = JSON.decode(response.getBody(), new TypeToken<List<PostStatusResult>>() {
                });

                List<Message> all = res.stream().map(PostStatusResult::toMessage).toList();
                List<Message> msgs = new ArrayList<>();
                for (Message m : all){
                    if(m.getUser().equals(user) && m.getCreationTime() >= time) msgs.add(m);
                }
                return ok(msgs);
            }

            if (response.getCode() == HTTP_NOT_FOUND) {
                return error(NOT_FOUND);
            }
            if (response.getCode() == HTTP_BAD_REQUEST) {
                return error(BAD_REQUEST);
            }

        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }


    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {

        try {
            final OAuthRequest request = new OAuthRequest(Verb.DELETE, getEndpoint(STATUSES_PATH + "/" + mid));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            if (response.getCode() == HTTP_OK) {
                return ok();
            }

            if (response.getCode() == HTTP_NOT_FOUND) {
                return error(NOT_FOUND);
            }
            if (response.getCode() == HTTP_BAD_REQUEST) {
                return error(BAD_REQUEST);
            }

        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }


    @Override
    public Result<Message> getMessage(String user, long mid) {
        try {
            final OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(STATUSES_PATH + "/" + mid));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            if (response.getCode() == HTTP_OK) {
                PostStatusResult res = JSON.decode(response.getBody(), new TypeToken<PostStatusResult>() {
                });

                return ok(res.toMessage());
            }

            if (response.getCode() == HTTP_NOT_FOUND) {
                return error(NOT_FOUND);
            }
            if (response.getCode() == HTTP_BAD_REQUEST) {
                return error(BAD_REQUEST);
            }

        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }
    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        try {
            final OAuthRequest request = new OAuthRequest(Verb.POST, getEndpoint(ACCOUNT_FOLLOW_PATH, user));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);
            if (response.getCode() == HTTP_OK) {

                return ok();
            }
            if (response.getCode() == HTTP_NOT_FOUND) {
                return error(NOT_FOUND);
            }
            if (response.getCode() == HTTP_BAD_REQUEST) {
                return error(BAD_REQUEST);
            }

        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(INTERNAL_ERROR);
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {

        try {
            final OAuthRequest request = new OAuthRequest(Verb.POST, getEndpoint(ACCOUNT_UNFOLLOW_PATH, user));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);
            if (response.getCode() == HTTP_OK) {

                return ok();
            }
            if (response.getCode() == HTTP_NOT_FOUND) {
                return error(NOT_FOUND);
            }
            if (response.getCode() == HTTP_BAD_REQUEST) {
                return error(BAD_REQUEST);
            }

        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(INTERNAL_ERROR);
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        try {
            final OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(ACCOUNT_FOLLOWING_PATH, user));

            service.signRequest(accessToken, request);

            Response response = service.execute(request);

            if (response.getCode() == HTTP_OK) {
                List<String> users = JSON.decode(response.getBody(), new TypeToken<List<String>>() {
                });

                return ok(users);
            }

            if (response.getCode() == HTTP_NOT_FOUND) {
                return error(NOT_FOUND);
            }
            if (response.getCode() == HTTP_BAD_REQUEST) {
                return error(BAD_REQUEST);
            }

        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }

    @Override
    public Result<Void> deleteUserFeed(String user) {
        try {


            Result<List<Message>> res = getMessages(user, 0);
            if(!res.isOK()) return error(res.error());
            List<Message> msgs = res.value();

            for (Message msg : msgs) {

                final OAuthRequest request1 = new OAuthRequest(Verb.DELETE, getEndpoint(STATUSES_PATH + "/" + msg.getId()));

                service.signRequest(accessToken, request1);

                Response response = service.execute(request1);

                if (response.getCode() != HTTP_OK) {
                    return error(Result.ErrorCode.INTERNAL_ERROR);
                }
            }

            return ok();

        } catch (Exception x) {
            x.printStackTrace();
        }
        return error(Result.ErrorCode.INTERNAL_ERROR);
    }

}
