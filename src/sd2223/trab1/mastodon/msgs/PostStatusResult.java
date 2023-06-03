package sd2223.trab1.mastodon.msgs;

		import sd2223.trab1.api.Message;
		import sd2223.trab1.servers.Domain;

		import java.time.Instant;

public record PostStatusResult(String id, String content, String created_at, MastodonAccount account) {

	public long getId() {
		return Long.valueOf(id);
	}

	public long getCreationTime() {
		Instant instant = Instant.parse(created_at);
		return instant.getEpochSecond();
	}

	public String getText() {
		return content.replaceAll("<[^>]*>", "");
	}

	public Message toMessage() {
		var m = new Message(getId(), account.username(), Domain.get(), getText());
		m.setCreationTime(getCreationTime());
		return m;
	}

}