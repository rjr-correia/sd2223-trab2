package sd2223.trab1.servers.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.FeedsPull;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.rest.FeedsServiceKafka;
import sd2223.trab1.kafka.KafkaPublisher;
import sd2223.trab1.kafka.KafkaSubscriber;
import sd2223.trab1.kafka.RecordProcessor;
import sd2223.trab1.kafka.sync.SyncPoint;
import sd2223.trab1.servers.Domain;
import sd2223.trab1.servers.java.JavaFeedsPull;
import sd2223.trab1.servers.rest.RestResource;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class KafkaResource extends RestResource implements FeedsServiceKafka, RecordProcessor {

    static final String FROM_BEGINNING = "earliest";
    static final String KAFKA_BROKERS = "kafka:9092";
    final KafkaPublisher sender;
    final KafkaSubscriber receiver;
    final SyncPoint<Result> sync;
    private static Logger Log = Logger.getLogger(KafkaServer.class.getName());

    final protected FeedsPull impl;

    private static final long FEEDS_MID_PREFIX= 1_000_000_000;

    protected AtomicLong serial = new AtomicLong(Domain.uuid() * FEEDS_MID_PREFIX);

    static long version;

    KafkaResource() {

        this.sender = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        this.receiver = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(Domain.get()), FROM_BEGINNING);
        this.receiver.start(false, this);
        this.sync = new SyncPoint<>();
        this.impl = new JavaFeedsPull();
    }


    @Override
    public void onReceive(ConsumerRecord<String, String> r) {

        Result result = Result.ok();
        //"postMessage" -> "user,pwd,msg"
        try {
            String methodName = r.key();
            String[] parameters = r.value().split(",");
            switch (methodName) {
                case "postMessage":

                    if (parameters[5].equals("null")){
                        parameters[5] = null;
                    }
                    result = impl.postMessage(parameters[0], parameters[1], //user, pwd, msg.id, msg.user, msg.domain, msg.text, msg.creationTime
                            new Message(Long.parseLong(parameters[2]), parameters[3], parameters[4], parameters[5], Long.parseLong(parameters[6])));
                    break;
                case "removeFromPersonalFeed":
                    result = impl.removeFromPersonalFeed(parameters[0], Long.parseLong(parameters[1]), parameters[2]);
                    break;
                case "subUser":
                    result = impl.subUser(parameters[0], parameters[1], parameters[2]); break;
                case "unsubscribeUser":
                    result = impl.unsubscribeUser(parameters[0], parameters[1], parameters[2]); break;
                case "deleteUserFeed":
                    result = impl.deleteUserFeed(parameters[0]); break;
                case "pull_getTimeFilteredPersonalFeed":
                    result = impl.pull_getTimeFilteredPersonalFeed(parameters[0], Long.parseLong(parameters[1]));
                    break;
                default:
                    Log.info("Unknown command in KafkaResource\n");
            }
            sync.setResult(r.offset(),result);

        } catch (Exception e){
            Log.info("Error in KafkaResource\n");
        }
    }

    @Override
    public long postMessage(String user, String pwd, Message msg) {

        Long mid = serial.incrementAndGet();
        msg.setId(mid);
        msg.setCreationTime(System.currentTimeMillis());

        KafkaResource.version = sender.publish(Domain.get(), "postMessage", user + "," + pwd + "," + msg.getId() + "," + msg.getUser() + "," + msg.getDomain() + "," + msg.getText() + "," + msg.getCreationTime());

        Result<Long> r = sync.waitForResult(KafkaResource.version);

        return super.fromJavaResult(r);
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {

        KafkaResource.version = sender.publish(Domain.get(), "removeFromPersonalFeed", user + "," + mid + "," + pwd);

        sync.waitForResult(KafkaResource.version);
    }

    @Override
    public Message getMessage(String user, long mid, Long version) {

        sync.waitForResult(version);

        return super.fromJavaResult( impl.getMessage(user, mid));
    }

    @Override
    public List<Message> getMessages(String user, long time, Long version) {

        sync.waitForResult(version);

        return super.fromJavaResult( impl.getMessages(user, time));

    }

    @Override
    public void subUser(String user, String userSub, String pwd) {

        KafkaResource.version = sender.publish(Domain.get(), "subUser", user + "," + userSub + "," + pwd);

        sync.waitForResult(KafkaResource.version);
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) {

        KafkaResource.version = sender.publish(Domain.get(), "unsubscribeUser", user + "," + userSub + "," + pwd);

        sync.waitForResult(KafkaResource.version);
    }

    @Override
    public List<String> listSubs(String user, Long version) {

        sync.waitForResult(version);

        return super.fromJavaResult( impl.listSubs(user));
    }

    @Override
    public void deleteUserFeed(String user) {

        KafkaResource.version = sender.publish(Domain.get(), "deleteUserFeed", user);

        sync.waitForResult(KafkaResource.version);

    }

    @Override
    public List<Message> pull_getTimeFilteredPersonalFeed(String user, long time, Long version) {

        sync.waitForResult(version);

        return super.fromJavaResult( impl.pull_getTimeFilteredPersonalFeed(user, time));
    }

    public static long getVersion() {
        return version;
    }
}


