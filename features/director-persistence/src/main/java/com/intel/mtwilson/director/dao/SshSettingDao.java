package com.intel.mtwilson.director.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.intel.director.api.SshSetting;
import com.intel.director.api.SshSettingInfo;
import com.intel.mtwilson.director.data.MwHost;
import com.intel.mtwilson.director.data.MwImage;
import com.intel.mtwilson.director.data.MwImageStoreSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class SshSettingDao {

	private Logger log = LoggerFactory.getLogger(getClass());
	Mapper mapper = new Mapper();

	public SshSettingDao(EntityManagerFactory emf) {
		this.emf = emf;
	}

	private EntityManagerFactory emf = null;

	public EntityManager getEntityManager() {
		EntityManager em = emf.createEntityManager();
		em.clear();
		return em;
	}

	public MwHost createSshSetting(MwHost mwHost) throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(mwHost);
			em.getTransaction().commit();
		} catch (Exception e) {
			throw new DbException("SshSettingDao,createSshSetting method", e);
		} finally {
			em.close();
		}
		return mwHost;
	}

	public MwHost fetchSshSettingById(String id) {
		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<MwHost> cq = cb.createQuery(MwHost.class);
		Root<MwHost> rootEntry = cq.from(MwHost.class);
		List<Predicate> predicates = new ArrayList<Predicate>();

		predicates.add(cb.equal(rootEntry.get("id"), id));
		cq.where(cb.and(predicates.toArray(new Predicate[] {})));
		TypedQuery<MwHost> query = em.createQuery(cq);

		return query.getSingleResult();
	}

	public void updateSshSetting(MwHost mwHost) throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			String id = mwHost.getId();
			MwHost demo = em.getReference(MwHost.class, id);
			em.merge(mwHost);
			em.getTransaction().commit();
		} catch (Exception e) {
			throw new DbException("SshSettingUploadDao,updatesshUpload failed",
					e);
		} finally {
			em.close();
		}
	}

	public void destroySshSetting(MwHost mwHost) throws DbException {
		EntityManager em = getEntityManager();
		try {

			em.getTransaction().begin();
			MwHost demo = em.getReference(MwHost.class, mwHost.getId());
			em.remove(demo);

			em.getTransaction().commit();
		} catch (Exception e) {
			throw new DbException("SshSettingDao,destroySshSetting failed", e);
		} finally {
			em.close();
		}
	}

	public void destroySshSettingById(String sshId) throws DbException {
		EntityManager em = getEntityManager();
		try {

			em.getTransaction().begin();
			MwHost demo = em.getReference(MwHost.class, sshId);
			em.remove(demo);

			em.getTransaction().commit();
		} catch (Exception e) {
			throw new DbException("SshSettingDao,destroySshSetting failed", e);
		} finally {
			em.close();
		}
	}

	public MwHost getMwHost(String id) throws DbException {
		EntityManager em = getEntityManager();
		try {
			MwHost mwHost = em.find(MwHost.class, id);

			return mwHost;
		} catch (Exception e) {
			throw new DbException("ImageDao,findMwImage() failed", e);
		}

		finally {
			em.close();
		}

	}

	public MwHost getMwHostByImageId(String image_id) throws DbException {
		EntityManager em = getEntityManager();

		try {
			Query query = em
					.createQuery("SELECT m FROM MwHost AS m WHERE m.imageId.id = '"
							+ image_id + "'");

			MwHost mwHost = (MwHost) query.getSingleResult();
			return mwHost;
		} catch (Exception e) {
			throw new DbException("ImageDao,findMwImageByImageId() failed", e);
		}

		finally {
			em.close();
		}

	}

	public List<MwHost> ShowAll() throws DbException {

		EntityManager em = getEntityManager();
		List<MwHost> list;
		try {
			Query query = em.createQuery("SELECT m FROM MwHost m");
			list = query.getResultList();

		}

		catch (Exception e) {
			throw new DbException("SshSettingDao,ScalarSshSetting failed", e);
		}
		return list;
	}
}
