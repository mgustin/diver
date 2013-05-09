package labust.tritech;

import java.util.ArrayList;
import java.util.List;

public class DefaultMessageList {
	private List<DefaultMessage> defaultMessages;
	
	public DefaultMessageList(){
		this.defaultMessages = new ArrayList<DefaultMessage>();
		this.defaultMessages.add(new DefaultMessage(0, "Alert"));
		this.defaultMessages.add(new DefaultMessage(1, "Ok"));
		this.defaultMessages.add(new DefaultMessage(2, "No"));
		this.defaultMessages.add(new DefaultMessage(3, "SOS"));
	}
	
	public DefaultMessage getDefaultMessage(int messageNumber){
		DefaultMessage defaultMessage = null;
		try {	
			defaultMessage = this.defaultMessages.get(messageNumber);
		}
		catch(IndexOutOfBoundsException ex){
			defaultMessage = null;
		}
		return defaultMessage;
	}

	public List<DefaultMessage> getDefaultMessages() {
		return defaultMessages;
	}

	public void setDefaultMessages(List<DefaultMessage> defaultMessages) {
		this.defaultMessages = defaultMessages;
	}
	
	
}
