package kz.nextbase.script.outline;

import java.util.ArrayList;
import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.flabs.webrule.constants.RunMode;
import kz.nextbase.script._IPOJOObject;
import kz.nextbase.script._URL;

public class _Outline implements _IPOJOObject {
	public RunMode isOn = RunMode.ON;
	public String caption = "";
	public String hint = "";
	public String customID;

	private ArrayList<_Outline> outlines = new ArrayList<_Outline>();
	private ArrayList<_OutlineEntry> entries = new ArrayList<_OutlineEntry>();

	public _Outline(String caption, String hint, String customID) {
		this.caption = caption;
		this.hint = hint;
		this.customID = customID;
	}

	public _Outline(String caption, String customID) {
		this.caption = caption;
		this.hint = caption;
		this.customID = customID;
	}

	void addOutline(_Outline outl) {
		outlines.add(outl);
	}

	public void addEntry(_OutlineEntry entry) {
		entries.add(entry);
	}

	@Override
	public UUID getId() {
		return null;
	}

	@Override
	public _URL getURL() {
		return null;
	}

	@Override
	public String getFullXMLChunk(LanguageType lang) {
		String a = "";

		for (_Outline o : outlines) {
			a += o.getFullXMLChunk(lang);
		}

		for (_OutlineEntry e : entries) {
			a += e.toXML();
		}

		return "<outline mode=\"" + isOn + "\" id=\"" + customID + "\" caption=\"" + caption + "\" hint=\"" + hint + "\">" + a + "</outline>";

	}

	@Override
	public String getShortXMLChunk(LanguageType lang) {
		return getFullXMLChunk(lang);
	}

	@Override
	public boolean isEditable() {
		return false;
	}

}
