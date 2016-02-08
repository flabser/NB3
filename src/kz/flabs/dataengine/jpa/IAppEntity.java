package kz.flabs.dataengine.jpa;

import java.util.Date;

public interface IAppEntity extends ISimpleAppEntity {

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
