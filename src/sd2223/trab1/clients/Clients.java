package sd2223.trab1.clients;

import sd2223.trab1.api.java.*;
import sd2223.trab1.clients.rest.*;
import sd2223.trab1.clients.soap.SoapFeedsClient;
import sd2223.trab1.clients.soap.SoapFeedsPullClient;
import sd2223.trab1.clients.soap.SoapFeedsPushClient;
import sd2223.trab1.clients.soap.SoapUsersClient;

public class Clients {
	public static final ClientFactory<Users> UsersClients = new ClientFactory<>(Users.SERVICENAME, RestUsersClient::new, SoapUsersClient::new);
	
	public static final ClientFactory<FeedsPull> FeedsPullClients = new ClientFactory<>(Feeds.SERVICENAME, RestFeedsPullClient::new, SoapFeedsPullClient::new);

	public static final ClientFactory<FeedsPush> FeedsPushClients = new ClientFactory<>(Feeds.SERVICENAME, RestFeedsPushClient::new, SoapFeedsPushClient::new);	
	
	public static final ClientFactory<Feeds> FeedsClients = new ClientFactory<>(Feeds.SERVICENAME, RestFeedsClient::new, SoapFeedsClient::new) ;

	public static final ClientFactory<FeedsRep> FeedsRepClients = new ClientFactory<>(Feeds.SERVICENAME, RestFeedsPullClientRep::new, null) ;

}
