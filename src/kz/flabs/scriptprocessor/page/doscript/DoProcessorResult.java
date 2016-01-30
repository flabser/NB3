package kz.flabs.scriptprocessor.page.doscript;

import java.util.ArrayList;

import kz.flabs.webrule.form.IShowField;

public class DoProcessorResult {
	public boolean continueOpen;
	public ArrayList<IShowField> toPublish = new ArrayList<IShowField>();
	
	DoProcessorResult(boolean cs, ArrayList<IShowField> toPublish){
		continueOpen = cs;
		this.toPublish = toPublish;
	}
}
