package lab.ex2022.messsages;

public class PublishMsg extends Msg{

	private String topic;
	private String value;
	
	public PublishMsg (String topic, String value) {
		this.topic = topic;
		this.value = value;
	}

	public String getTopic() {
		return topic;
	}

	public String getValue() {
		return value;
	}
}
