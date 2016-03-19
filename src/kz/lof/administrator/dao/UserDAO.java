package kz.lof.administrator.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.lof.administrator.model.User;
import kz.lof.dataengine.jpa.ViewPage;
import kz.lof.env.Environment;
import kz.lof.scripting._Session;
import kz.lof.user.IUser;
import kz.lof.util.StringUtil;

import org.eclipse.persistence.exceptions.DatabaseException;

public class UserDAO {
	public User user;
	private EntityManagerFactory emf;
	protected _Session ses;

	public UserDAO(_Session ses) {
		this.ses = ses;
		IDatabase db = Environment.dataBase;
		emf = db.getEntityManagerFactory();
	}

	public UserDAO(IDatabase db) {
		emf = db.getEntityManagerFactory();
	}

	public UserDAO() {
		IDatabase db = Environment.dataBase;
		emf = db.getEntityManagerFactory();
	}

	public List<User> findAll() {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<User> q = em.createNamedQuery("User.findAll", User.class);
			return q.getResultList();
		} finally {
			em.close();
		}
	}

	public List<User> findAll(int firstRec, int pageSize) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<User> q = em.createNamedQuery("User.findAll", User.class);
			q.setFirstResult(firstRec);
			q.setMaxResults(pageSize);
			return q.getResultList();
		} finally {
			em.close();
		}
	}

	public long getCount() {
		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createQuery("SELECT count(m) FROM User AS m");
			return (Long) q.getSingleResult();
		} finally {
			em.close();
		}
	}

	public ViewPage<User> findAll(String keyword, int pageNum, int pageSize) {
		EntityManager em = emf.createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		try {
			CriteriaQuery<User> cq = cb.createQuery(User.class);
			CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
			Root<User> c = cq.from(User.class);
			cq.select(c);
			countCq.select(cb.count(c));
			if (!keyword.isEmpty()) {
				Predicate condition = cb.like(cb.lower(c.<String> get("login")), "%" + keyword.toLowerCase() + "%");
				cq.where(condition);
				countCq.where(condition);
			}
			TypedQuery<User> typedQuery = em.createQuery(cq);
			Query query = em.createQuery(countCq);
			long count = (long) query.getSingleResult();
			int maxPage = RuntimeObjUtil.countMaxPage(count, pageSize);
			if (pageNum == 0) {
				pageNum = maxPage;
			}
			int firstRec = RuntimeObjUtil.calcStartEntry(pageNum, pageSize);
			typedQuery.setFirstResult(firstRec);
			typedQuery.setMaxResults(pageSize);
			List<User> result = typedQuery.getResultList();
			return new ViewPage<User>(result, count, maxPage, pageNum);
		} finally {
			em.close();
		}
	}

	public User findById(long id) {
		EntityManager em = emf.createEntityManager();
		try {
			String jpql = "SELECT m FROM User AS m WHERE m.id = :id";
			TypedQuery<User> q = em.createQuery(jpql, User.class);
			q.setParameter("id", id);
			List<User> res = q.getResultList();
			return res.get(0);
		} catch (IndexOutOfBoundsException e) {
			return null;
		} finally {
			em.close();
		}
	}

	public IUser<Long> findByLogin(String login) {
		EntityManager em = emf.createEntityManager();
		try {
			String jpql = "SELECT m FROM User AS m WHERE m.login = :login";
			TypedQuery<User> q = em.createQuery(jpql, User.class);
			q.setParameter("login", login);
			List<User> res = q.getResultList();
			return res.get(0);
		} catch (IndexOutOfBoundsException e) {
			return null;
		} finally {
			em.close();
		}

	}

	// TODO need to secure by ACL
	public User add(User entity) throws DatabaseException {
		EntityManager em = emf.createEntityManager();
		try {
			EntityTransaction t = em.getTransaction();
			try {
				t.begin();
				normalizePwd(entity);
				em.persist(entity);
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

	// TODO need to secure by ACL
	public User update(User entity) {
		EntityManager em = emf.createEntityManager();
		try {
			EntityTransaction t = em.getTransaction();
			try {
				t.begin();
				normalizePwd(entity);
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

	public void delete(User entity) {
		EntityManager em = emf.createEntityManager();
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

	public static void normalizePwd(User user) {
		if (user != null && user.getPwd() != null && !user.getPwd().isEmpty()) {
			String pwdHash = StringUtil.encode(user.getPwd());
			user.setPwdHash(pwdHash);
			user.setPwd("");
		}
	}
}
