package staff.dao;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import kz.flabs.dataengine.jpa.DAO;
import kz.flabs.dataengine.jpa.ViewPage;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.nextbase.script._Session;
import staff.model.Organization;

public class OrganizationDAO extends DAO<Organization, UUID> {

	public OrganizationDAO(_Session session) {
		super(Organization.class, session);
	}

	public Organization findPrimaryOrg() {
		EntityManager em = getEntityManagerFactory().createEntityManager();
		try {
			String jpql = "SELECT m FROM Organization AS m WHERE m.isPrimary = true";
			TypedQuery<Organization> q = em.createQuery(jpql, Organization.class);
			return q.getResultList().get(0);
		} catch (IndexOutOfBoundsException e) {
			return null;
		} finally {
			em.close();
		}
	}

	public ViewPage<Organization> findAllByKeyword(String keyword, int pageNum, int pageSize) {
		EntityManager em = getEntityManagerFactory().createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		try {
			CriteriaQuery<Organization> cq = cb.createQuery(getEntityClass());
			CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
			Root<Organization> c = cq.from(getEntityClass());
			cq.select(c);
			countCq.select(cb.count(c));
			Predicate condition = cb.like(cb.lower(c.<String> get("name")), "%" + keyword.toLowerCase() + "%");
			cq.where(condition);
			countCq.where(condition);
			TypedQuery<Organization> typedQuery = em.createQuery(cq);
			Query query = em.createQuery(countCq);
			long count = (long) query.getSingleResult();
			int maxPage = RuntimeObjUtil.countMaxPage(count, pageSize);
			if (pageNum == 0) {
				pageNum = maxPage;
			}
			int firstRec = RuntimeObjUtil.calcStartEntry(pageNum, pageSize);
			typedQuery.setFirstResult(firstRec);
			typedQuery.setMaxResults(pageSize);
			List<Organization> result = typedQuery.getResultList();
			return new ViewPage<Organization>(result, count, maxPage, pageNum);
		} finally {
			em.close();
		}
	}

}
