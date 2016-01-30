package kz.pchelka.messenger;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.MessageListener;

public class InstMessageListener implements MessageListener, ChatManagerListener{
	
	  public void processMessage(Chat chat, Message message) {	    
		 
	        System.out.println(message.getBody());
	    }

	@Override
	public void chatCreated(Chat chat, boolean arg1) {
		chat.addMessageListener(this);
		
	}

}
