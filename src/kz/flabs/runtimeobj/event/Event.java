package kz.flabs.runtimeobj.event;

public class Event {
	public String eventType;
	public DropEventAfter dropEventAfter;

	public Event(String et, DropEventAfter dea){
		eventType = et;
		dropEventAfter = dea;
	}
}
