package kz.lof.dataengine.jpa;

import java.util.List;

import kz.lof.exception.SecureException;

import org.eclipse.persistence.exceptions.DatabaseException;

public interface IDAO<T, K> {

	T findById(K id);

	T findById(String id);

	ViewPage findAllByIds(List<K> ids, int pageNum, int pageSize);

	List<T> findAll();

	Long getCount();

	List<T> findAll(int firstRec, int pageSize);

	T add(T entity) throws DatabaseException, SecureException;

	T update(T entity) throws SecureException;

	void delete(T uuid) throws SecureException;

}
