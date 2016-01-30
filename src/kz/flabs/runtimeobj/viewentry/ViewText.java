package kz.flabs.runtimeobj.viewentry;

import kz.flabs.dataengine.h2.Database;
import kz.flabs.runtimeobj.xml.Tag;
import kz.flabs.util.XMLUtil;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ViewText{
	private String valueAsText;
	private ViewTextType type;
	private String tagName;
	
	ViewText(String value, String tagName){		
		this.valueAsText = value;
		this.tagName = tagName;
		type = ViewTextType.TEXT;
	}
		
	ViewText(BigDecimal value, String tagName){
		if (value != null) {
			this.valueAsText = String.valueOf(value.stripTrailingZeros().toPlainString());
			this.tagName = tagName;
			type = ViewTextType.NUMBER;
		}
	}

    ViewText(Integer value, String tagName){
       this(BigDecimal.valueOf(value), tagName);
    }

	@Deprecated
	ViewText(Date value, String tagName){
		 if (value != null){
			 this.valueAsText = Database.dateTimeFormat.format(value);
		 }
		this.tagName = tagName;
		type = ViewTextType.DATE;
	}
	
	public ViewText(Date value, String tagName, SimpleDateFormat simpleDateFormat) {
		 if (value != null){
			 this.valueAsText = simpleDateFormat.format(value);
		 }
		this.tagName = tagName;
		type = ViewTextType.DATE;
	}

	public String getValueAsText() {
		return valueAsText;
	}

	public String toXML() {
		return "<" + tagName + ">" + XMLUtil.getAsTagValue(valueAsText) + "</" + tagName + ">";
	}
	
	public ViewTextType getType() {
		return type;
	}
	
	public Tag getTag() {
		return new Tag(tagName, valueAsText);
	}

}