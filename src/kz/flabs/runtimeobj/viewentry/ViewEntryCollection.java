package kz.flabs.runtimeobj.viewentry;

import java.math.BigDecimal;
import java.util.HashSet;

import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.users.User;
import kz.lof.appenv.AppEnv;
import kz.lof.scripting._Session;
import kz.nextbase.script._ViewEntry;
import kz.nextbase.script._ViewEntryCollection;

public class ViewEntryCollection implements IViewEntryCollection {

	private int count = -1;
	private int unreadcount = 0;
	private HashSet<IViewEntry> entries = new HashSet<>();
	private int currentPage = 1;
	private int maxPage = 1;
	private int pageSize;
	private _ViewEntryCollection scriptingObj;

	public ViewEntryCollection(int pageSize, User user, Object p) {
		entries = new HashSet<IViewEntry>();
		this.pageSize = pageSize;
		scriptingObj = new _ViewEntryCollection(user, this);

	}

	public ViewEntryCollection(int pageSize) {
		entries = new HashSet<IViewEntry>();
		this.pageSize = pageSize;
		scriptingObj = null;
	}

	public ViewEntryCollection(_Session session, int pageSize) {
		entries = new HashSet<IViewEntry>();
		this.pageSize = pageSize;
		scriptingObj = new _ViewEntryCollection(session, this);
	}

	public ViewEntryCollection(HashSet<IViewEntry> entries, int pageSize, AppEnv env) {
		this.entries = entries;
		this.pageSize = pageSize;
		scriptingObj = new _ViewEntryCollection(new User(env), this);
	}

	public ViewEntryCollection(int pageSize2, User user, String[] strings, String[] strings2) {
		entries = new HashSet<IViewEntry>();
		this.pageSize = pageSize2;
		scriptingObj = new _ViewEntryCollection(user, this);

	}

	@Override
	public void setCount(int count) {
		this.count = count;
		maxPage = RuntimeObjUtil.countMaxPage(getCount(), pageSize);

	}

	@Override
	public int getCount() {
		if (count < 0) {
			return entries.size();
		} else {
			return count;
		}
	}

	@Override
	public int getUnreadCount() {
		return unreadcount;
	}

	public void setCurrentPage(int cp) {
		currentPage = cp;
	}

	@Override
	public int getCurrentPage() {
		return currentPage;
	}

	public void setMaxPage(int cp) {
		maxPage = cp;
	}

	@Override
	public int getMaxPage() {
		return maxPage;
	}

	@Override
	public HashSet<IViewEntry> getEntries() {
		return entries;
	}

	@Override
	public void add(IViewEntry entry) {
		entries.add(entry);
		if (!((ViewEntry) entry).isRead()) {
			unreadcount++;
		}
		scriptingObj.add(new _ViewEntry((ViewEntry) entry, scriptingObj.getSession()));
	}

	public _ViewEntryCollection getScriptingObj() {
		return scriptingObj;
	}

	public BigDecimal getViewNumberSum() {
		BigDecimal result = new BigDecimal(0);
		for (IViewEntry e : entries) {
			result = result.add(e.getViewNumberValue());
		}
		return result;
	}

	public BigDecimal[] getViewNumberTotal() {
		int total = 0;
		int plus = 1;
		int minus = 2;

		BigDecimal result[] = { null, new BigDecimal(0), new BigDecimal(0) };
		for (IViewEntry e : entries) {
			int sign = e.getViewNumberValue().signum();
			if (sign == 1) {
				result[plus] = result[plus].add(e.getViewNumberValue());
			} else if (sign == -1) {
				result[minus] = result[minus].add(e.getViewNumberValue());
			}
		}
		result[total] = result[plus].add(result[minus]);
		return result;
	}

}
