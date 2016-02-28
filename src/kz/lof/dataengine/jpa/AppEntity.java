package kz.lof.dataengine.jpa;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.lof.appenv.AppEnv;
import kz.lof.dataengine.jpa.util.UUIDConverter;
import kz.lof.scripting.IPOJOObject;
import kz.lof.scripting._Session;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.internal.indirection.jdk8.IndirectList;

import com.fasterxml.jackson.annotation.JsonIgnore;

@MappedSuperclass
@Converter(name = "uuidConverter", converterClass = UUIDConverter.class)
@UuidGenerator(name = "uuid-gen")
public abstract class AppEntity implements IAppEntity, IPOJOObject {
	@Id
	@GeneratedValue(generator = "uuid-gen")
	@Convert("uuidConverter")
	@Column(name = "id", nullable = false)
	protected UUID id;

	@Column(name = "author", nullable = false, updatable = false)
	protected Long author;

	@Transient
	private String authorName;

	@Column(name = "reg_date", nullable = false, updatable = false)
	protected Date regDate;

	@Column(name = "form", nullable = false, updatable = false, length = 64)
	protected String form;

	@JsonIgnore
	@Transient
	protected boolean isEditable = true;

	@PrePersist
	private void prePersist() {
		regDate = new Date();
	}

	@Override
	public void setId(UUID id) {
		this.id = id;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@JsonIgnore
	@Override
	public long getAuthor() {
		return author;
	}

	@Override
	public void setAuthor(long author) {
		this.author = author;
	}

	public void setAuthor(User user) {
		author = (long) user.docID;
	}

	@JsonIgnore
	public void setAuthorName(long author) {
		this.author = author;
	}

	@JsonIgnore
	@Override
	public Date getRegDate() {
		return regDate;
	}

	@Override
	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}

	@JsonIgnore
	@Override
	public String toString() {
		return getId().toString();
	}

	/**
	 * To more faster processing the method should be reloaded in real entity
	 * object
	 */
	@Override
	public String getFullXMLChunk(_Session ses) {
		Class<?> noparams[] = {};
		StringBuilder value = new StringBuilder(1000);
		try {
			for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors()) {
				Method method = propertyDescriptor.getReadMethod();
				if (method != null && !method.getName().equals("getShortXMLChunk") && !method.getName().equals("getFullXMLChunk")
				        && !method.getName().equals("getClass")) {
					String methodValue = "";

					String methodName = method.getName().toLowerCase();
					String fieldName;
					if (methodName.startsWith("get")) {
						fieldName = methodName.substring(3);
					} else {
						fieldName = methodName;
					}
					try {
						Object val = method.invoke(this, noparams);
						// System.out.println(val.getClass().getName());
						if (val instanceof Date) {
							methodValue = Util.simpleDateFormat.format((Date) val);
						} else if (val instanceof IndirectList) {
							List<IPOJOObject> list = (List<IPOJOObject>) val;
							for (IPOJOObject nestedValue : list) {
								// methodValue += nestedValue.toXML();
								methodValue = nestedValue.getClass().getName();
							}
						} else if (val.getClass().isInstance(IPOJOObject.class)) {
							methodValue = ((IPOJOObject) val).getFullXMLChunk(null);
						} else {
							methodValue = val.toString();
						}
					} catch (Exception e) {
						// AppEnv.logger.errorLogEntry(e);
					}
					value.append("<" + fieldName + ">" + XMLUtil.getAsTagValue(methodValue) + "</" + fieldName + ">");
				}
			}
		} catch (IntrospectionException e) {
			AppEnv.logger.errorLogEntry(e);
		}

		return value.toString();
	}

	/**
	 * To more faster processing the method during showing in a view should be
	 * reloaded in real entity object
	 */
	@JsonIgnore
	@Override
	public String getShortXMLChunk(_Session ses) {
		return getFullXMLChunk(ses);
	}

	@Override
	public Object getJSONObj(_Session ses) {
		return this;
	}

	@Override
	public String getURL() {
		return "Provider?id=" + this.getClass().getSimpleName().toLowerCase() + "-form&amp;docid=" + getId();
	}

	@JsonIgnore
	@Override
	public String getDefaultFormName() {
		return this.getClass().getSimpleName().toLowerCase() + "-form";
	}

	@JsonIgnore
	@Override
	public String getDefaultViewName() {
		return this.getClass().getSimpleName().toLowerCase() + "-view";
	}

	@JsonIgnore
	@Override
	public String getForm() {
		return form;
	}

	@Override
	public void setForm(String form) {
		this.form = form;
	}

	@Override
	public boolean isEditable() {
		return isEditable;
	}

	@Override
	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (!(getClass() == obj.getClass())) {
			return false;
		} else {
			AppEntity tmp = (AppEntity) obj;
			if (tmp.id.equals(this.id)) {
				return true;
			} else {
				return false;
			}
		}
	}
}
