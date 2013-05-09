package labust.tritech;

public class DefaultMessage {
	
	private int messageNumber;
	private String description;
	
	public DefaultMessage(int messageNumber, String description){
		this.messageNumber = messageNumber;
		this.description = description;		
	}
	
	public int getMessageNumber() {
		return messageNumber;
	}

	public void setMessageNumber(int messageNumber) {
		this.messageNumber = messageNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return this.description;
	}
}
