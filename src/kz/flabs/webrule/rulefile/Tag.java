package kz.flabs.webrule.rulefile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import kz.flabs.util.Util;
import kz.flabs.webrule.IRuleValue;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.scheduler.DaysOfWeek;
import kz.flabs.webrule.scheduler.ScheduleSettings;
import kz.flabs.webrule.scheduler.ScheduleType;

public class Tag implements IRuleFileElement{
	public String name;
	public String value;
	public HashMap<String, String> attrs = new HashMap<String, String>();

	ArrayList<IRuleFileElement> tags =  new ArrayList<IRuleFileElement>();

	Tag(String tagName, String tagValue){
		name = tagName;
		value = tagValue;
	}

	Tag(String tagName, int tagValue){
		name = tagName;
		value = Integer.toString(tagValue);
	}

	public Tag addTag(String tagName){
		Tag tag = new Tag(tagName, "");
		tags.add(tag);
		return tag;
	}


	public Tag addTag(String tagName, int tagValue){
		Tag tag = new Tag(tagName, tagValue);
		tags.add(tag);
		return tag;
	}
	
	public Tag addTag(String tagName, Enum tagValue){
		Tag tag = new Tag(tagName, tagValue.toString());
		tags.add(tag);
		return tag;
	}

	public Tag addTag(String tagName, Date tagValue){
		Tag tag = new Tag(tagName, Util.dateTimeFormat.format(tagValue));
		tags.add(tag);
		return tag;
	}

	public Tag addTag(String tagName, String tagValue){
		Tag tag = new Tag(tagName, tagValue);
		tags.add(tag);
		return tag;
	}

	public void addComment(String comment){
		tags.add(new Comment(comment));
	}
	
	public Tag addCDATATag(String tagName, String tagValue){
		Tag tag = new Tag(tagName, "<![CDATA[" + tagValue + "]]>");
		tags.add(tag);
		return tag;
	}
	
	public void addSchedulerTag(ScheduleSettings schedule){
		Tag schedTag = addTag("scheduler");
		schedTag.setAttr("mode", schedule.isOn);
		
		if (schedule.schedulerType == ScheduleType.INTIME){			
			Tag schedTagTime = schedTag.addTag("starttime");
			schedTagTime.value = Util.convertDataTimeToTimeString(schedule.startTimes.get(0));
			schedTag.addTag("nextstart", schedule.getStartTime().getTime());	
		}else if (schedule.schedulerType == ScheduleType.PERIODICAL){
			String days = "";
			Tag daysOfWeekTag = schedTag.addTag("mininterval", schedule.minInterval);
			for(DaysOfWeek day: schedule.daysOfWeek){
				days += day + ",";
			}
			daysOfWeekTag.setAttr("daysofweek", days);	
			schedTag.addTag("nextstart", schedule.getStartTime().getTime());	
		}	
		
			
	}
	
	public Tag addTagWithSource(String tagName, IRuleValue tagValue){
		Tag valueTag = addTag(tagName);
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

	public void setTagValue(String tagValue){
		value = tagValue;
	}

	public void setTagValue(int tagValue){
		value = Integer.toString(tagValue);
	}

	public void setAttr(String attrName, String attrValue){
		attrs.put(attrName, attrValue);			
	}

	public void setAttr(String attrName, Enum attrValue){
		attrs.put(attrName, attrValue.toString());			
	}
	
			
	public void setAttr(String attrName, boolean attrValue){
		attrs.put(attrName, Boolean.toString(attrValue));			
	}

	public void setAttr(String attrName, int attrValue){
		attrs.put(attrName, Integer.toString(attrValue));			
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