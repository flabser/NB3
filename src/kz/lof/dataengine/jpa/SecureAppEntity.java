package kz.lof.dataengine.jpa;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.MappedSuperclass;

import kz.lof.user.IUser;

@MappedSuperclass
public abstract class SecureAppEntity extends AppEntity {
	@ElementCollection
	private Set<Long> editors = new HashSet<Long>();

	@ElementCollection
	private Set<Long> readers = new HashSet<Long>();

	public Set<Long> getEditors() {
		return editors;
	}

	public void setEditors(Set<Long> editors) {
		this.editors = editors;
	}

	public void addReaderEditor(IUser user) {
		long id = (long) user.getId();
		if (id != 0) {
			this.editors.add(id);
			addReader(user);
		}
	}

	public Set<Long> getReaders() {
		return readers;
	}

	public void setReaders(Set<Long> readers) {
		this.readers = readers;
	}

	public void addReader(IUser user) {
		long id = (long) user.getId();
		if (id != 0) {
			this.readers.add(id);
		}
	}

	public void addEditor(String role) {

	}

	@Override
	public void setAuthor(IUser user) {
		author = (Long) user.getId();
		if (author != 0) {
			addReader(user);
			addReaderEditor(user);
		}
	}

	@Override
	public boolean isEditable() {
		return isEditable;
	}

}
