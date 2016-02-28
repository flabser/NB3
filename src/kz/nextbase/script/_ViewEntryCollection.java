package kz.nextbase.script;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import kz.flabs.runtimeobj.viewentry.IViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.RunTimeParameters.Filter;
import kz.flabs.users.RunTimeParameters.Sorting;
import kz.flabs.users.User;
import kz.lof.scripting._Session;
import kz.pchelka.scheduler.IProcessInitiator;

public class _ViewEntryCollection implements _IXMLContent, IProcessInitiator {

	private ViewEntryCollection parentObj;
	private LinkedHashSet<_ViewEntry> _entries = new LinkedHashSet<_ViewEntry>();
	private _Session session;
	private static final String viewTextFileds[] = { "viewtext", "viewtext1", "viewtext2", "viewtext3", "viewtext4", "viewtext5", "viewtext6",
	        "viewtext7", "viewnumber", "viewdate" };

	public _ViewEntryCollection(User user, ViewEntryCollection parentObj) {
		session = new _Session(user.getAppEnv(), user);
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

	@Override
	public String toXML() {

		RunTimeParameters pars = parentObj.getRunTimePameters();
		String vtResult = "";
		if (pars != null) {
			for (String val : viewTextFileds) {
				vtResult += "<" + val + ">";
				Sorting sorting = pars.sortingMap.get(val);
				if (sorting != null) {
					vtResult += "<sorting mode=\"ON\" order=\"" + sorting.sortingDirection + "\"/>";
				} else {
					vtResult += "<sorting mode=\"OFF\"/>";
				}

				Filter filter = pars.filtersMap.get(val);
				if (filter != null) {
					vtResult += "<filter mode=\"ON\" keyword=\"" + filter.keyWord.replace("\"", "'") + "\"/>";
				} else {
					vtResult += "<filter mode=\"OFF\"/>";
				}

				vtResult += "</" + val + ">";
			}
		}

		String result = "<query maxpage=\"" + parentObj.getMaxPage() + "\" count=\"" + parentObj.getCount() + "\" currentpage=\""
		        + parentObj.getCurrentPage() + "\">";
		result += "<columns>" + vtResult + "</columns>";
		for (_ViewEntry val : _entries) {
			result += val.toXML();
		}
		return result + "</query>";
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
	public String getOwnerID() {
		return this.getClass().getSimpleName();
	}

	@Override
	public Object toJSON() {
		// TODO Auto-generated method stub
		return null;
	}
}
