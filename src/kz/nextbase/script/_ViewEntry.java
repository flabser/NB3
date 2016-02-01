package kz.nextbase.script;

import java.math.BigDecimal;
import java.util.ArrayList;

import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewText;

public class _ViewEntry {

	private ViewEntry entry;
	private _Session session;

	public _ViewEntry(ViewEntry entry, _Session session) {
		this.session = session;
		this.entry = entry;
	}

	public String getViewText() {
		ArrayList<ViewText> vt = entry.getViewTexts();
		try {
			return vt.get(0).getValueAsText();
		} catch (Exception e) {
			return "";
		}
	}

	public String getViewText(int pos) {
		return entry.getViewText(pos);
	}

	public void addViewText(String value, String tagName) {
		entry.addViewText(value, tagName);
	}

	public BigDecimal getViewNumberValue() {
		return entry.getViewNumberValue();
	}

	public String getID() {
		return entry.ddbID;
	}

	/*
	 * public _Document getDocument() throws _Exception { return
	 * session.getCurrentDatabase().getDocumentByID(entry.ddbID); }
	 */

	public String toXML() {
		return entry.toXML(session.getUser()).toString();
	}

	public _Tag toTag() {
		return new _Tag(entry.toTag());
	}

	public boolean hasAttachment() {
		if (entry.hasAttachment() > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return entry.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof _ViewEntry)) {
			return false;
		}

		_ViewEntry viewEntry = (_ViewEntry) o;

		if (!entry.equals(viewEntry.entry)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return entry.hashCode();
	}
}
