package kz.pchelka.reminder;

import java.io.Serializable;
import java.util.List;

public class MailAgent implements Serializable {

	private static final long serialVersionUID = -4947690768549410033L;

	public boolean sendMail(String sender, List<String> recipients, String subj, String body) {
		Memo memo = new Memo(sender, recipients, subj, body);
		return memo.send();
	}

}
