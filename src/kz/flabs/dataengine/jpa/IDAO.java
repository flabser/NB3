package kz.flabs.dataengine.jpa;

import java.util.List;

public interface IDAO<T, K> {

	T findById(K id);

	T findById(String id);

	ViewPage findAllByIds(List<K> ids, int pageNum, int pageSize);

	List<T> findAll();

	Long getCount();

	List<T> findAll(int firstRec, int pageSize);

	T add(T entity);

	T update(T entity);

	void delete(T uuid);

}
