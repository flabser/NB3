package kz.lof.dataengine.jpa;

import java.util.List;

public class ViewPage<T> {
	private List<T> result;
	private long count;
	private int maxPage;
	private int pageNum;
	private String keyWord;

	public ViewPage(List<T> result, long count2, int maxPage, int pageNum) {
		this.result = result;
		this.count = count2;
		this.maxPage = maxPage;
		this.pageNum = pageNum;
	}

	public long getCount() {
		return count;
	}

	public List<T> getResult() {
		return result;
	}

	public int getMaxPage() {
		return maxPage;
	}

	public int getPageNum() {
		return pageNum;
	}

	public String getKeyWord() {
		return keyWord;
	}

}
