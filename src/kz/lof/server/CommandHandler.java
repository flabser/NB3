package kz.lof.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import kz.lof.log.Log4jLogger;

public class CommandHandler implements ActionListener {// , MessageListener{
	@Override
	public void actionPerformed(ActionEvent ae) {

	}

	/*
	 * public void processMessage(Chat chat, Message message) {
	 * processCommand(message.getBody()); }
	 */
	public void processCommand(String command) {
		try {
			Log4jLogger logger = (Log4jLogger) Server.logger;
			if (!command.equals("")) {
				String respText = "";

			}
		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
		}
	}

}