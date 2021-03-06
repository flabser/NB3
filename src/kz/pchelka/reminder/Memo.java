package kz.pchelka.reminder;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import kz.lof.env.Environment;
import kz.lof.server.Server;

public class Memo {

	private MimeMessage msg;
	private String smtpServer = Environment.SMTPHost;
	private String smtpPort = Environment.smtpPort;
	private String smtpUser = Environment.smtpUser;
	private String smtpPassword = Environment.smtpPassword;
	private boolean smtpAuth = Environment.smtpAuth;
	private boolean isValid;
	private boolean hasRecipients;

	public Memo(String sender, List<String> recipients, String subj, String body) {
		_Memo(sender, null, recipients, subj, body);
	}

	public Memo(String sender, String personal, List<String> recipients, String subj, String body) {
		_Memo(sender, personal, recipients, subj, body);
	}

	private void _Memo(String sender, String personal, List<String> recipients, String subj, String body) {
		if (Environment.mailEnable) {
			Properties props = new Properties();
			props.put("mail.smtp.host", smtpServer);
			Session ses;
			if (smtpAuth) {
				props.put("mail.smtp.auth", smtpAuth);
				props.put("mail.smtp.port", smtpPort);
				if ("465".equals(smtpPort)) {
					props.put("mail.smtp.ssl.enable", "true");
				}
				Authenticator auth = new SMTPAuthenticator();
				ses = Session.getInstance(props, auth);
			} else {
				ses = Session.getInstance(props, null);
			}

			msg = new MimeMessage(ses);
			hasRecipients = false;

			try {
				if (personal == null) {
					msg.setFrom(new InternetAddress(sender));
				} else {
					msg.setFrom(new InternetAddress(sender, personal));
				}

				for (String recipient : recipients) {
					try {
						msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
						hasRecipients = true;
					} catch (AddressException ae) {
						Server.logger.errorLogEntry("incorrect e-mail \"" + recipient + "\"");
						continue;
					}
				}

				if (hasRecipients) {
					msg.setSubject(subj, "utf-8");
					Multipart mp = new MimeMultipart();
					BodyPart htmlPart = new MimeBodyPart();
					htmlPart.setContent(body, "text/html; charset=utf-8");
					mp.addBodyPart(htmlPart);
					msg.setContent(mp);
					isValid = true;
				} else {
					Server.logger.errorLogEntry("unable to send the message. List of recipients is empty or consist is incorrect data");
				}
			} catch (MessagingException e) {
				Server.logger.errorLogEntry(e);
			} catch (UnsupportedEncodingException e) {
				Server.logger.errorLogEntry(e);
			}
		}
	}

	public boolean send() {
		try {
			if (Environment.mailEnable && isValid) {
				Transport.send(msg);
				return true;
			}
		} catch (SendFailedException se) {
			if (se.getMessage().contains("relay rejected for policy reasons")) {
				Server.logger.warningLogEntry("relay rejected for policy reasons by SMTP server. Message has not sent");
			} else {
				Server.logger.errorLogEntry("unable to send a message, probably SMTP host did not set");
				Server.logger.errorLogEntry(se);
			}
		} catch (MessagingException e) {
			Server.logger.errorLogEntry(e);
		}
		return false;
	}

	class SMTPAuthenticator extends javax.mail.Authenticator {

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(smtpUser, smtpPassword);
		}
	}
}
