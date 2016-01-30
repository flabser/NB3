package kz.pchelka.server;

import java.awt.event.*;
import java.util.*;
import kz.pchelka.log.*;

public class CommandHandler implements ActionListener{//, MessageListener{						
	public void actionPerformed(ActionEvent ae){	
		
	}
/*
	public void processMessage(Chat chat, Message message) {
		processCommand(message.getBody());		
	}
*/
	public void processCommand(String command){
		try{
			Log4jLogger logger = (Log4jLogger)Server.logger;
			if (!command.equals("")){
				String respText = "";
				
					
			}
		}catch(Exception e){
			Server.logger.errorLogEntry(e);
		}
	}

}