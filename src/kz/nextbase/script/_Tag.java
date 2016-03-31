package kz.nextbase.script;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import kz.flabs.runtimeobj.xml.Tag;
import kz.flabs.util.Util;
import kz.lof.scripting.IPOJOObject;
import kz.lof.scripting._Session;

@Deprecated
public class _Tag<UUID> implements IPOJOObject {

	private Tag runtimeTag;

	public _Tag() {
		runtimeTag = new Tag("", "");
	}

	public _Tag(String tagName) {
		runtimeTag = new Tag(tagName, "");
	}

	public _Tag(String tagName, String tagValue) {
		runtimeTag = new Tag(tagName, tagValue);
	}

	public _Tag(String tagName, Object tagValue) {
		runtimeTag = new Tag(tagName, tagValue.toString());
	}

	public _Tag(String tagName, int tagValue) {
		runtimeTag = new Tag(tagName, tagValue);
	}

	public _Tag(String tagName, Collection<_Tag> tags) {
		runtimeTag = new Tag(tagName, "");
		for (_Tag t : tags) {
			addTag(t);
		}
	}

	_Tag(Tag tag) {
		runtimeTag = tag;
	}

	public Tag getRuntimeTag() {
		return runtimeTag;
	}

	public _Tag addTag(_Tag tag) {
		runtimeTag.tags.add(tag.runtimeTag);
		return tag;
	}

	public _Tag addTag(String tagName) {
		Tag tag = new Tag(tagName, "");
		runtimeTag.tags.add(tag);
		return new _Tag(tag);
	}

	public _Tag addTag(String tagName, BigDecimal tagValue) {
		Tag tag = new Tag(tagName, tagValue);
		runtimeTag.tags.add(tag);
		return new _Tag(tag);
	}

	public _Tag addTag(String tagName, int tagValue) {
		Tag tag = new Tag(tagName, tagValue);
		runtimeTag.tags.add(tag);
		return new _Tag(tag);
	}

	public _Tag addTag(String tagName, Date tagValue) {
		Tag tag = new Tag(tagName, Util.dateTimeFormat.format(tagValue));
		runtimeTag.tags.add(tag);
		return new _Tag(tag);
	}

	public void addTag(String tagName, Collection<_Tag> tagsList) {
		for (_Tag t : tagsList) {
			runtimeTag.tags.add(t.runtimeTag);
		}
	}

	public _Tag addTag(String tagName, String tagValue) {
		Tag tag = new Tag(tagName, tagValue);
		runtimeTag.tags.add(tag);
		return new _Tag(tag);
	}

	public _Tag addCDATATag(String tagName, String tagValue) {
		Tag tag = new Tag(tagName, "<![CDATA[" + tagValue + "]]>");
		runtimeTag.tags.add(tag);
		return new _Tag(tag);
	}

	public void setTagValue(String tagValue) {
		runtimeTag.value = tagValue;
	}

	public void setTagValue(int tagValue) {
		runtimeTag.value = Integer.toString(tagValue);
	}

	public void setAttr(String attrName, String attrValue) {
		runtimeTag.attrs.put(attrName, attrValue);
	}

	public void setAttr(String attrName, boolean attrValue) {
		runtimeTag.attrs.put(attrName, Boolean.toString(attrValue));
	}

	public void setAttr(String attrName, int attrValue) {
		runtimeTag.attrs.put(attrName, Integer.toString(attrValue));
	}

	@Override
	public String getURL() {
		return null;
	}

	@Override
	public String getFullXMLChunk(_Session ses) {
		return runtimeTag.toXML();
	}

	@Override
	public String getShortXMLChunk(_Session ses) {
		return getFullXMLChunk(null);
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public String toString() {
		return runtimeTag.name + "=" + runtimeTag.value;
	}

	@Override
	public Object getJSONObj(_Session ses) {
		return this;
	}

	@Override
	public String getIdentifier() {

		return "null";
	}

}
