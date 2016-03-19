package kz.lof.administrator.dao;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import kz.lof.administrator.model.Application;
import kz.lof.dataengine.jpa.DAO;
import kz.lof.scripting._Session;

public class ApplicationDAO extends DAO<Application, UUID> {

	public ApplicationDAO(_Session session) {
		super(Application.class, session);
	}

	public Application findByName(String name) {
		EntityManager em = getEntityManagerFactory().createEntityManager();
		try {
			String jpql = "SELECT m FROM Application AS m WHERE m.name = :name";
			TypedQuery<Application> q = em.createQuery(jpql, Application.class);
			q.setParameter("name", name);
			List<Application> res = q.getResultList();
			return res.get(0);
		} catch (IndexOutOfBoundsException e) {
			return null;
		} finally {
			em.close();
		}

	}

}
