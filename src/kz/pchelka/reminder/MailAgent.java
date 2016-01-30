package kz.pchelka.reminder;

import java.io.Serializable;
import java.util.ArrayList;

public class MailAgent implements Serializable {

	private static final long serialVersionUID = -4947690768549410033L;

	
	
	public boolean sendMail(String sender, ArrayList<String> recipients, String subj, String body){
		Memo memo = new Memo(sender, recipients, subj, body);
		return memo.send();		
	}
	
}
