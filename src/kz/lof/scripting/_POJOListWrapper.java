package kz.lof.scripting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import kz.flabs.localization.LanguageType;
import kz.lof.webserver.servlet.IOutcomeObject;

public class _POJOListWrapper<T extends IPOJOObject> implements IOutcomeObject {
	private String entityType = "undefined";
	private int maxPage;
	private long count;
	private int currentPage;
	private List<T> list;
	private LanguageType lang;
	private String keyWord = "";

	public _POJOListWrapper(List<T> list, int maxPage, long count, int currentPage, LanguageType lang) {
		this.maxPage = maxPage;
		this.count = count;
		this.currentPage = currentPage;
		this.list = list;
		this.lang = lang;
		recognizeName();
	}

	public _POJOListWrapper(List<T> list, int maxPage, long count, int currentPage, LanguageType lang, String keyWord) {
		this.maxPage = maxPage;
		this.count = count;
		this.currentPage = currentPage;
		this.list = list;
		this.lang = lang;
		this.keyWord = " keyword=\"" + keyWord + "\" ";
		recognizeName();
	}

	public _POJOListWrapper(List<T> list, LanguageType lang) {
		this.count = list.size();
		this.list = list;
		maxPage = 1;
		currentPage = 1;
		this.lang = lang;
		recognizeName();
	}

	public _POJOListWrapper(List<T> list, LanguageType lang, String en) {
		this.count = list.size();
		this.list = list;
		maxPage = 1;
		currentPage = 1;
		this.lang = lang;
		entityType = en;
	}

	public _POJOListWrapper(String msg, String keyWord) {
		this.count = 0;
		List<T> l = new ArrayList<T>();
		l.add((T) new SimplePOJO(msg));
		this.list = l;
		this.lang = lang;
		this.keyWord = " keyword=\"" + keyWord + "\" ";
	}

	public int getMaxPage() {
		return maxPage;
	}

	public List<T> getList() {
		return list;
	}

	@Override
	public String toXML() {

		String result = "<query entity=\"" + entityType + "\"  maxpage=\"" + maxPage + "\" count=\"" + count + "\" currentpage=\"" + currentPage
		        + "\"" + keyWord + ">";
		for (T val : list) {
			result += "<entry isread=\"1\" hasattach=\"0\" id=\"" + val.getId() + "\" " + "url=\"" + val.getURL() + "\"><viewcontent>";
			result += val.getShortXMLChunk(lang) + "</viewcontent></entry>";
		}
		return result + "</query>";
	}

	@Override
	public String toJSON() {
		return null;

	}

	private void recognizeName() {
		try {
			final Class<T> listClass = (Class<T>) list.get(0).getClass();
			entityType = listClass.getSimpleName().toLowerCase();
		} catch (ArrayIndexOutOfBoundsException e) {

		}
	}

	class SimplePOJO implements IPOJOObject {
		private String msg;

		SimplePOJO(String msg) {
			this.msg = msg;
		}

		@Override
		public UUID getId() {
			return null;
		}

		@Override
		public String getURL() {
			return msg;

		}

		@Override
		public boolean isEditable() {
			return false;
		}

		@Override
		public String getFullXMLChunk(LanguageType lang) {
			return "<message>" + msg + "</message>";
		}

		@Override
		public String getShortXMLChunk(LanguageType lang) {
			return getFullXMLChunk(lang);
		}

	}

}
