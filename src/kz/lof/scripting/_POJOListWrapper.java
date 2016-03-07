package kz.lof.scripting;

import java.util.ArrayList;
import java.util.List;

import kz.lof.webserver.servlet.IOutcomeObject;

public class _POJOListWrapper<T extends IPOJOObject> implements IOutcomeObject {
	private String entityType = "undefined";
	private int maxPage;
	private long count;
	private int currentPage;
	private List<T> list;
	private List<Object> jsList;
	private String keyWord = "";
	private _Session ses;

	public _POJOListWrapper(List<T> list, int maxPage, long count, int currentPage, _Session ses) {
		this.maxPage = maxPage;
		this.count = count;
		this.currentPage = currentPage;
		this.list = list;
		this.ses = ses;
		recognizeName();
	}

	public _POJOListWrapper(List<T> list, int maxPage, long count, int currentPage, _Session ses, String keyWord) {
		this.maxPage = maxPage;
		this.count = count;
		this.currentPage = currentPage;
		this.list = list;
		this.ses = ses;
		this.keyWord = " keyword=\"" + keyWord + "\" ";
		recognizeName();
	}

	public _POJOListWrapper(List<T> list, _Session ses) {
		this.count = list.size();
		this.list = list;
		maxPage = 1;
		currentPage = 1;
		this.ses = ses;
		recognizeName();
	}

	public _POJOListWrapper(List<T> list, _Session ses, String en) {
		this.count = list.size();
		this.list = list;
		maxPage = 1;
		currentPage = 1;
		this.ses = ses;
		entityType = en;
	}

	public _POJOListWrapper(String msg, String keyWord) {
		this.count = 0;
		List<T> l = new ArrayList<T>();
		l.add((T) new SimplePOJO(msg));
		this.list = l;
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
			result += "<entry isread=\"1\" hasattach=\"0\" id=\"" + val.getIdentifier() + "\" " + "url=\"" + val.getURL() + "\"><viewcontent>";
			result += val.getShortXMLChunk(ses) + "</viewcontent></entry>";
		}
		return result + "</query>";
	}

	@Override
	public Object toJSON() {
		jsList = new ArrayList<Object>();
		for (T obj : list) {
			jsList.add(obj.getJSONObj(ses));
		}
		return jsList;
	}

	private void recognizeName() {
		try {
			if (list.size() > 0) {
				final Class<T> listClass = (Class<T>) list.get(0).getClass();
				entityType = listClass.getSimpleName().toLowerCase();
			}
		} catch (ArrayIndexOutOfBoundsException e) {

		}
	}

	class SimplePOJO implements IPOJOObject {
		private String msg;

		SimplePOJO(String msg) {
			this.msg = msg;
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
		public String getFullXMLChunk(_Session ses) {
			return "<message>" + msg + "</message>";
		}

		@Override
		public String getShortXMLChunk(_Session ses) {
			return getFullXMLChunk(ses);
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

}
