package kz.flabs.webrule.rulefile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import kz.flabs.runtimeobj.xml.Tag;
import kz.flabs.util.Util;
import kz.flabs.webrule.IRuleValue;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.scheduler.DaysOfWeek;
import kz.flabs.webrule.scheduler.ScheduleSettings;
import kz.flabs.webrule.scheduler.ScheduleType;

public class RuleTag extends Tag implements IRuleFileElement{
	public String name;
	public String value;
	public HashMap<String, String> attrs = new HashMap<String, String>();

	ArrayList<IRuleFileElement> tags =  new ArrayList<IRuleFileElement>();

	RuleTag(String tagName, String tagValue){
		super(tagName, tagValue);
	}

	RuleTag(String tagName, int tagValue){
		super(tagName, tagValue);
	}
	

	public void addComment(String comment){
		tags.add(new Comment(comment));
	}
		
	
	public void addSchedulerTag(ScheduleSettings schedule){
		Tag schedTag = addTag("scheduler");
		schedTag.setAttr("mode", schedule.isOn);
		
		if (schedule.schedulerType == ScheduleType.INTIME){			
			Tag schedTagTime =  schedTag.addTag("starttime");
			schedTagTime.value = Util.convertDataTimeToTimeString(schedule.startTimes.get(0));
			schedTag.addTag("nextstart", schedule.getStartTime().getTime());	
		}else if (schedule.schedulerType == ScheduleType.PERIODICAL){
			String days = "";
			Tag daysOfWeekTag =  schedTag.addTag("mininterval", schedule.minInterval);
			for(DaysOfWeek day: schedule.daysOfWeek){
				days += day + ",";
			}
			daysOfWeekTag.setAttr("daysofweek", days);	
			schedTag.addTag("nextstart", schedule.getStartTime().getTime());	
		}	
		
			
	}
	
	public Tag addTagWithSource(String tagName, IRuleValue tagValue){
		Tag valueTag =  addTag(tagName);
		if (tagValue.getSourceType() == ValueSourceType.SCRIPT || tagValue.getSourceType() == ValueSourceType.DOGFORMULA){
			valueTag.setTagValue("<![CDATA[" + tagValue.getValue() + "]]>");
			if (tagValue.getSourceType() == ValueSourceType.SCRIPT){
				valueTag.setAttr("type",FieldType.OBJECT);
			}else if(tagValue.getSourceType() == ValueSourceType.DOGFORMULA){
				valueTag.setAttr("type",FieldType.VECTOR);
			}
		}else{
			//valueTag.setTagValue(tagValue.getValue().replace("&","&amp;"));
			valueTag.setTagValue(tagValue.getValue());
			valueTag.setAttr("type",tagValue.getValueType());
		}
		valueTag.setAttr("source",tagValue.getSourceType());
		
		return valueTag;
	}

	public void setAttr(String attrName, Enum attrValue){
		attrs.put(attrName, attrValue.toString());			
	}
	
	public String toString(){
		String attrPiece = "";
		StringBuffer result = new StringBuffer(1000);
		for(Entry<String, String> entry: attrs.entrySet()){		
			String attrVal = entry.getValue();
			if (attrVal != null && (!attrVal.equalsIgnoreCase(""))){
				attrVal = attrVal.replace("&","&amp;");
			}else{
				attrVal = "";
			}
			attrPiece += " " + entry.getKey() + "=\"" + attrVal + "\""; 
		}			
		
		result.append("<" + name + attrPiece + ">" + value);
		for(IRuleFileElement tag: tags){				
			result.append(tag);
		}
		result.append("</" + name + ">");
		return result.toString();
	}
	
	

}