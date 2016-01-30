package kz.flabs.runtimeobj.viewentry;

import java.util.HashSet;

public interface IViewEntryCollection {
	int getCount();

    int getUnreadCount();

    int getCurrentPage();
	int getMaxPage();
    HashSet<IViewEntry> getEntries();
	void add(IViewEntry entry);
	void setCount(int count);
	
}
