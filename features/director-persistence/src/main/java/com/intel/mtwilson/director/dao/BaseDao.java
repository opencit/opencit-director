package com.intel.mtwilson.director.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.intel.mtwilson.director.data.BaseDomainInterface;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class BaseDao {
	protected EntityManagerFactory emf = null;
	protected Mapper mapper = new Mapper();
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BaseDao.class);

	public BaseDao(EntityManagerFactory emf) {
		this.emf = emf;
	}

	public EntityManager getEntityManager() {
		EntityManager em = emf.createEntityManager();
		em.clear();
		return em;
	}

	protected Object create(Object objectToBeSaved) throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(objectToBeSaved);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("createImage failed", e);
			throw new DbException("Create method failed", e);
		}

		finally {
			em.close();
		}
		return objectToBeSaved;

	}

	public void update(Object objectToBeUpdated) throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.merge(objectToBeUpdated);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("update failed", e);
			throw new DbException("Update failed", e);
		} finally {
			em.close();
		}
	}

	public void delete(Class domainClass, BaseDomainInterface objectTobeDeleted) throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			Object reference = em.getReference(domainClass, objectTobeDeleted.getId());
			em.remove(reference);

			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("Delete failed", e);
			throw new DbException("Delete failed", e);
		} finally {
			em.close();
		}
	}

	public Object get(Class domainClass, String id) throws DbException {
		EntityManager em = getEntityManager();
		try {
			Object obj = em.find(domainClass, id);
			return obj;
		} catch (Exception e) {
			log.error("Get failed", e);
			throw new DbException("Get failed", e);
		}

		finally {
			em.close();
		}

	}

}
