package kz.nextbase.script;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import kz.flabs.runtimeobj.viewentry.IViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;
import kz.flabs.users.User;
import kz.lof.scripting._Session;
import kz.lof.user.AnonymousUser;

public class _ViewEntryCollection implements _IXMLContent {

	private ViewEntryCollection parentObj;
	private LinkedHashSet<_ViewEntry> _entries = new LinkedHashSet<_ViewEntry>();
	private _Session session;
	private static final String viewTextFileds[] = { "viewtext", "viewtext1", "viewtext2", "viewtext3", "viewtext4", "viewtext5", "viewtext6",
	        "viewtext7", "viewnumber", "viewdate" };

	public _ViewEntryCollection(User user, ViewEntryCollection parentObj) {
		session = new _Session(user.getAppEnv(), new AnonymousUser());
		this.parentObj = parentObj;
		HashSet<IViewEntry> entries = parentObj.getEntries();
		for (IViewEntry entry : entries) {
			_ViewEntry _entry = new _ViewEntry((ViewEntry) entry, session);
			_entries.add(_entry);
		}
	}

	public _ViewEntryCollection(_Session ses, ViewEntryCollection parentObj) {
		session = ses;
		this.parentObj = parentObj;
		HashSet<IViewEntry> entries = parentObj.getEntries();
		for (IViewEntry entry : entries) {
			_ViewEntry _entry = new _ViewEntry((ViewEntry) entry, session);
			_entries.add(_entry);
		}
	}

	public _Session getSession() {
		return session;
	}

	public Set<_ViewEntry> getEntries() {
		return _entries;
	}

	public int getCount() {
		return parentObj.getCount();
	}

	public int getUnreadCount() {
		return parentObj.getUnreadCount();
	}

	public BigDecimal getViewNumberSum() {
		return parentObj.getViewNumberSum();
	}

	public BigDecimal[] getViewNumberTotal() {
		return parentObj.getViewNumberTotal();
	}

	public int getCurrentPage() {
		return parentObj.getCurrentPage();
	}

	public int getMaxPage() {
		return parentObj.getMaxPage();
	}

	public void add(_ViewEntry entry) {
		_entries.add(entry);

	}

	@Override
	public Object toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toXML() throws _Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
