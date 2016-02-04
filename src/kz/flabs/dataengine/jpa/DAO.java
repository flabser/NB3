package kz.flabs.dataengine.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import kz.flabs.dataengine.Const;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.users.User;
import kz.nextbase.script._Session;

public abstract class DAO<T extends IAppEntity, K> implements IDAO<T, K> {
	public User user;
	protected final Class<T> entityClass;
	private EntityManagerFactory emf;
	private _Session ses;

	public DAO(Class<T> entityClass, _Session session) {
		this.entityClass = entityClass;
		ses = session;
		emf = session.getCurrentDatabase().getEntityManagerFactory();
		user = session.getUser();
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return emf;
	}

	@Override
	public T findById(K id) {
		EntityManager em = getEntityManagerFactory().createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		boolean isSecureEntity = false;
		try {
			CriteriaQuery<T> cq = cb.createQuery(entityClass);
			Root<T> c = cq.from(entityClass);
			cq.select(c);
			Predicate condition = c.get("id").in(id);
			cq.where(condition);
			Query query = em.createQuery(cq);
			if (!user.getUserID().equals(Const.sysUser) && SecureAppEntity.class.isAssignableFrom(getEntityClass())) {
				condition = cb.and(c.get("readers").in((long) user.docID), condition);
				isSecureEntity = true;
			}
			T entity = (T) query.getSingleResult();
			if (isSecureEntity) {
				if (!((SecureAppEntity) entity).getEditors().contains(user.docID)) {
					entity.setEditable(false);
				}
			}
			return entity;
		} catch (NoResultException e) {
			return null;
		} finally {
			em.close();
		}
	}

	@Override
	public ViewPage<T> findAllByIds(List<K> value, int pageNum, int pageSize) {
		return findAllin("id", value, pageNum, pageSize);
	}

	@Override
	public List<T> findAll(int firstRec, int pageSize) {
		EntityManager em = getEntityManagerFactory().createEntityManager();
		try {
			TypedQuery<T> q = em.createNamedQuery(getQueryNameForAll(), entityClass);
			q.setFirstResult(firstRec);
			q.setMaxResults(pageSize);
			return q.getResultList();
		} finally {
			em.close();
		}
	}

	@Override
	public List<T> findAll() {
		EntityManager em = getEntityManagerFactory().createEntityManager();
		try {
			TypedQuery<T> q = em.createNamedQuery(getQueryNameForAll(), entityClass);
			return q.getResultList();
		} finally {
			em.close();
		}
	}

	@Override
	public T add(T entity) {
		EntityManager em = getEntityManagerFactory().createEntityManager();
		try {
			EntityTransaction t = em.getTransaction();
			try {
				t.begin();
				entity.setAuthor(user.docID);
				entity.setForm(entity.getDefaultFormName());
				em.persist(entity);
				t.commit();
				update(entity);
				return entity;
			} finally {
				if (t.isActive()) {
					t.rollback();
				}
			}
		} finally {
			em.close();

		}

	}

	@Override
	public T update(T entity) {
		EntityManager em = getEntityManagerFactory().createEntityManager();
		try {
			EntityTransaction t = em.getTransaction();
			try {
				t.begin();
				em.merge(entity);
				t.commit();
				return entity;
			} finally {
				if (t.isActive()) {
					t.rollback();
				}
			}
		} finally {
			em.close();
		}
	}

	@Override
	public void delete(T entity) {
		EntityManager em = getEntityManagerFactory().createEntityManager();
		try {
			EntityTransaction t = em.getTransaction();
			try {
				t.begin();
				entity = em.merge(entity);
				em.remove(entity);
				t.commit();
			} finally {
				if (t.isActive()) {
					t.rollback();
				}
			}
		} finally {
			em.close();
		}
	}

	@Override
	public Long getCount() {
		EntityManager em = getEntityManagerFactory().createEntityManager();
		try {
			Query q = em.createQuery("SELECT count(m) FROM " + entityClass.getName() + " AS m");
			return (Long) q.getSingleResult();
		} finally {
			em.close();
		}
	}

	public ViewPage<T> findAllequal(String fieldName, String value, int pageNum, int pageSize) {
		EntityManager em = getEntityManagerFactory().createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		try {
			CriteriaQuery<T> cq = cb.createQuery(entityClass);
			CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
			Root<T> c = cq.from(entityClass);
			cq.select(c);
			countCq.select(cb.count(c));
			Predicate condition = cb.equal(c.get(fieldName), value);
			if (!user.getUserID().equals(Const.sysUser) && SecureAppEntity.class.isAssignableFrom(getEntityClass())) {
				condition = cb.and(c.get("readers").in((long) user.docID), condition);
			}
			cq.where(condition);
			countCq.where(condition);
			TypedQuery<T> typedQuery = em.createQuery(cq);
			Query query = em.createQuery(countCq);
			long count = (long) query.getSingleResult();
			int maxPage = RuntimeObjUtil.countMaxPage(count, pageSize);
			if (pageNum == 0) {
				pageNum = maxPage;
			}
			int firstRec = RuntimeObjUtil.calcStartEntry(pageNum, pageSize);
			typedQuery.setFirstResult(firstRec);
			typedQuery.setMaxResults(pageSize);
			List<T> result = typedQuery.getResultList();

			ViewPage<T> r = new ViewPage<T>(result, count, maxPage, pageNum);
			return r;
		} finally {
			em.close();
		}
	}

	public ViewPage<T> findAllin(String fieldName, List value, int pageNum, int pageSize) {
		EntityManager em = getEntityManagerFactory().createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		try {
			CriteriaQuery<T> cq = cb.createQuery(entityClass);
			CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
			Root<T> c = cq.from(entityClass);
			cq.select(c);
			countCq.select(cb.count(c));
			Predicate condition = c.get(fieldName).in(value);
			if (!user.getUserID().equals(Const.sysUser) && SecureAppEntity.class.isAssignableFrom(getEntityClass())) {
				condition = cb.and(c.get("readers").in((long) user.docID), condition);
			}
			cq.where(condition);
			countCq.where(condition);
			TypedQuery<T> typedQuery = em.createQuery(cq);
			Query query = em.createQuery(countCq);
			long count = (long) query.getSingleResult();
			int maxPage = RuntimeObjUtil.countMaxPage(count, pageSize);
			if (pageNum == 0) {
				pageNum = maxPage;
			}
			int firstRec = RuntimeObjUtil.calcStartEntry(pageNum, pageSize);
			typedQuery.setFirstResult(firstRec);
			typedQuery.setMaxResults(pageSize);
			List<T> result = typedQuery.getResultList();

			ViewPage<T> r = new ViewPage<T>(result, count, maxPage, pageNum);
			return r;
		} finally {
			em.close();
		}
	}

	public String getQueryNameForAll() {
		String queryName = entityClass.getSimpleName() + ".findAll";
		return queryName;
	}

	public _Session getSession() {
		return ses;
	}
}