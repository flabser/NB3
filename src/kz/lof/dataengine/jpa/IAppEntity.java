package kz.lof.dataengine.jpa;

import java.util.Date;
import java.util.UUID;

public interface IAppEntity extends ISimpleAppEntity<UUID> {

	long getAuthor();

	void setAuthor(long author);

	Date getRegDate();

	void setRegDate(Date regDate);

	public String getForm();

	public void setForm(String form);

	public String getDefaultFormName();

	public String getDefaultViewName();

	public boolean isEditable();

	public void setEditable(boolean isEditable);

}
