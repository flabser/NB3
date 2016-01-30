package kz.pchelka.messenger;

import kz.pchelka.env.Environment;
import kz.pchelka.server.Server;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPException;

public class InstMessengerAgent {
	private boolean isValid;
	private ChatManager chatmanager;
	private InstMessageListener listener;

	public InstMessengerAgent(){
		
		if (Environment.XMPPServerEnable && Environment.chatmanager != null){
			chatmanager = Environment.chatmanager;
			isValid = true;
		}
	}


	public boolean sendMessage(String receiver, String msg){

		if (isValid){
			Chat newChat = chatmanager.createChat(receiver, listener);
			
			try {
				newChat.sendMessage(msg);
				return true;
			}
			catch (XMPPException xe) {
				Server.logger.errorLogEntry(xe);
				return false;
			}
			catch (Exception e) {
				Server.logger.errorLogEntry(e);
				return false;
			}
		}else{
			Server.logger.warningLogEntry("XMPP agent has turn off or not set");
			return false;
		}
	}
}
