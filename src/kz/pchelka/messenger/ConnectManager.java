package kz.pchelka.messenger;

import kz.pchelka.env.Environment;
import kz.pchelka.scheduler.Scheduler;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class ConnectManager extends Thread {
	public ChatManager chatmanager;
	public  XMPPConnection connection;

	private ConnectionConfiguration config;
	private String module = "IM";


	public ConnectManager(){
		config = new ConnectionConfiguration(Environment.XMPPServer, Environment.XMPPServerPort);
        config.setReconnectionAllowed(true);
		SASLAuthentication.supportSASLMechanism("PLAIN");
		config.setCompressionEnabled(true);
		config.setSASLAuthenticationEnabled(true);
	}

	public void run(){
		while(true){
			try{
				if(connection == null || (!connection.isConnected())){
					connection = new XMPPConnection(config);
					//System.setProperty("smack.debugEnabled", "true");
					//connection.DEBUG_ENABLED = true;
					SASLAuthentication.supportSASLMechanism("PLAIN", 0);
				    connection.connect();
					connection.login(Environment.XMPPLogin, Environment.XMPPPwd, Environment.XMPPLogin);
					InstMessageListener listener = new InstMessageListener();
					chatmanager = connection.getChatManager();
					chatmanager.addChatListener(listener);
					Environment.logger.normalLogEntry(module,"Connected...");
					Environment.XMPPServerEnable = true;
					Environment.connection = connection;
					Environment.chatmanager = chatmanager;	
				}
			}catch(XMPPException xmppe ){	
				if (xmppe.getMessage().contains("not-authorized")){
					Environment.logger.warningLogEntry(module,"Error while authorization to XMPP server, InstantMessengerAgent have to shutdown");
				}else{
					Environment.logger.errorLogEntry(module,"XMPPE Error while connect to XMPP server, InstantMessengerAgent have to shutdown");
					Environment.logger.errorLogEntry(module,xmppe);
				}
			}catch(Exception e ){			
				Environment.logger.normalLogEntry(module,"Error while connect to XMPP server, InstantMessengerAgent have to shutdown");
				Environment.logger.errorLogEntry(module,e);
				Environment.XMPPServerEnable = false;
				Environment.connection = null;
				Environment.chatmanager = null;
			}  
			try {
				Thread.sleep(5 * Scheduler.minuteInterval);
			} catch (InterruptedException e) {			
				Environment.logger.errorLogEntry(module,e);
			}
		}  
	}
}
