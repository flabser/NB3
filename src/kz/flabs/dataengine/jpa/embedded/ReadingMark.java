package kz.flabs.dataengine.jpa.embedded;

import java.util.Date;

import javax.persistence.Embeddable;

/**
 * Created by Kaira on 27/12/15.
 */

@Embeddable
public class ReadingMark {

	private Long user;

	public Long getUser() {
		return user;
	}

	public void setUser(Long user) {
		this.user = user;
	}

	public Date getMarkDate() {
		return markDate;
	}

	public void setMarkDate(Date markDate) {
		this.markDate = markDate;
	}

	private Date markDate;

}
