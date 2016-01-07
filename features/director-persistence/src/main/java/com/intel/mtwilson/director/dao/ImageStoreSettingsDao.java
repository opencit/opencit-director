package com.intel.mtwilson.director.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.intel.mtwilson.director.data.MwImageStoreSettings;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class ImageStoreSettingsDao {


	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageDao.class);
	
	Mapper mapper= new Mapper();

	public ImageStoreSettingsDao(EntityManagerFactory emf) {
		this.emf = emf;
	}

	private EntityManagerFactory emf = null;

	public EntityManager getEntityManager() {
		EntityManager em = emf.createEntityManager();
		em.clear();
		return em;
	}

	public MwImageStoreSettings createImageStoreSettings(MwImageStoreSettings imgStoreSettings) throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(imgStoreSettings);
			em.getTransaction().commit();
		} catch(Exception e){
			log.error("createImageStoreSettings failed",e);
			throw new DbException("ImageStoreSettingsDao,createImage method",e);
		}
		
		finally {
			em.close();
		}
		return imgStoreSettings;
	}

	public void updateImageStoreSettings(MwImageStoreSettings imgStoreSettings) throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.merge(imgStoreSettings);
			em.getTransaction().commit();
		} 
		catch(Exception e){
			log.error("updateImageStoreSettings failed",e);
			throw new DbException("ImageStoreSettingsDao,updateImage failed",e);
		}
		finally {
			em.close();
		}
	}

	public void destroyImageStoreSettings(MwImageStoreSettings imgStoreSettings) throws DbException {
		EntityManager em = getEntityManager();
		try {
			
			em.getTransaction().begin();
			MwImageStoreSettings mwImageStoreSettings = em.getReference(MwImageStoreSettings.class, imgStoreSettings.getId());
			em.remove(mwImageStoreSettings);
			
			em.getTransaction().commit();
		} 
		catch(Exception e){
			log.error("destroyImageStoreSettings failed",e);
			throw new DbException("ImageStoreSettingsDao,destroyImage failed",e);
		}
		finally {
			em.close();
		}
	}

	
	
	public List<MwImageStoreSettings> fetchImageStoreSettings(){
			EntityManager em = getEntityManager();
			CriteriaBuilder cb = em.getCriteriaBuilder();
	        CriteriaQuery<MwImageStoreSettings> cq = cb.createQuery(MwImageStoreSettings.class);
	        Root<MwImageStoreSettings> rootEntry = cq.from(MwImageStoreSettings.class);
	        CriteriaQuery<MwImageStoreSettings> all = cq.select(rootEntry);
	        TypedQuery<MwImageStoreSettings> allQuery = em.createQuery(all);
	        return allQuery.getResultList();
	}
	
	
	public MwImageStoreSettings fetchImageStoreSettingsByName(String name){
		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MwImageStoreSettings> cq = cb.createQuery(MwImageStoreSettings.class);
        Root<MwImageStoreSettings> rootEntry = cq.from(MwImageStoreSettings.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
		
		predicates.add(cb.equal(rootEntry.get("name"), name));
		cq.where(cb.and(predicates.toArray(new Predicate[] {})));
		 TypedQuery<MwImageStoreSettings> query = em.createQuery(cq);
		
		 return query.getSingleResult();
	}
	
	public MwImageStoreSettings fetchImageStoreSettingsById(String id){
		
		EntityManager em = getEntityManager();
     
		return em.find(MwImageStoreSettings.class, id);	 
	}
	

}
