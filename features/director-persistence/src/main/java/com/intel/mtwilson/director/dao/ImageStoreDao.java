package com.intel.mtwilson.director.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.intel.director.api.ImageStoreFilter;
import com.intel.director.api.ui.ImageStoreFields;
import com.intel.mtwilson.director.data.MwImageStore;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class ImageStoreDao {

	Mapper mapper = new Mapper();

	public ImageStoreDao(EntityManagerFactory emf) {
		this.emf = emf;
	}

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageStoreDao.class);

	private EntityManagerFactory emf = null;

	public EntityManager getEntityManager() {
		EntityManager em = emf.createEntityManager();
		em.clear();
		return em;
	}

	public MwImageStore createImageStore(MwImageStore mw_image_store)
			throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(mw_image_store);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("createImageStore failed", e);
			throw new DbException("ImageStoreDao,createImageStore method", e);
		}

		finally {
			em.close();
		}
		return mw_image_store;
	}

	public MwImageStore getImageStoreByID(String id) throws DbException {
		MwImageStore mw_image_store;
		EntityManager em = getEntityManager();
		try {
			mw_image_store = em.find(MwImageStore.class, id);
		} catch (Exception e) {
			log.error("getImageStoreByID failed", e);
			throw new DbException("ImageStoreDao,getImageStoreByID method", e);
		} finally {
			em.close();
		}
		return mw_image_store;
	}

	public void updateImageStore(MwImageStore mw_image_store)
			throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.merge(mw_image_store);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("updateImageStore failed", e);
			throw new DbException("ImageStoreDao,updateImageStore failed", e);
		} finally {
			em.close();
		}
	}

	public void deleteImageStore(MwImageStore mw_image_store)
			throws DbException {
		EntityManager em = getEntityManager();
		try {

			em.getTransaction().begin();
			em.remove(mw_image_store);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("deleteImageStore failed", e);
			throw new DbException("ImageStoreDao, deleteImageStore failed", e);
		} finally {
			em.close();
		}
	}

	public List<MwImageStore> findMwImageStore(ImageStoreFilter imageStoreFilter)
			throws DbException {

		EntityManager em = getEntityManager();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Object> criteriaQuery  = criteriaBuilder
					.createQuery();
			Root<MwImageStore> mwImageStore = criteriaQuery.from(MwImageStore.class);

			Map<ImageStoreFields, String> imageStoreAttributestoDataMapper = mapper
					.getImageStoreAttributesMapper();
			
			List<Predicate> predicates = new ArrayList<Predicate>();
			if (imageStoreFilter != null) {
				
				if (imageStoreFilter.getId() != null) {
					predicates.add(criteriaBuilder.like(mwImageStore
							.<String> get(imageStoreAttributestoDataMapper
									.get(ImageStoreFields.ID)), "%"
							+ imageStoreFilter.getId() + "%"));
				}

				if (imageStoreFilter.getName() != null) {
					predicates.add(criteriaBuilder.like(mwImageStore
							.<String> get(imageStoreAttributestoDataMapper
									.get(ImageStoreFields.NAME)), "%"
							+ imageStoreFilter.getName() + "%"));
				}

				if (imageStoreFilter.getArtifact_types() != null && imageStoreFilter.getArtifact_types().length != 0) {
					String[] artifact_types = imageStoreFilter.getArtifact_types();
					Arrays.sort(artifact_types);
					String artifactString = "";
					for (String artifact : artifact_types) {
						artifactString += artifact;
						artifactString += "%";
					}
					artifactString = artifactString.substring(0, artifactString.length() - 1);
					predicates.add(criteriaBuilder.like(mwImageStore
							.<String> get(imageStoreAttributestoDataMapper
									.get(ImageStoreFields.ARTIFACT_TYPE)), "%"
							+ artifactString + "%"));
				}

				if (imageStoreFilter.getConnector() != null) {
					predicates.add(criteriaBuilder.like(mwImageStore
							.<String> get(imageStoreAttributestoDataMapper
									.get(ImageStoreFields.CONNECTOR)), "%"
							+ imageStoreFilter.getConnector() + "%"));
				}
				

			}
			
			criteriaQuery.where(criteriaBuilder.and(predicates
					.toArray(new Predicate[] {})));
			
			Query q = em.createQuery(criteriaQuery);
			log.info("Query :: " + q.toString());

			List<Object[]> result = q.getResultList();
			List<MwImageStore> mwImageStoreList = new ArrayList<MwImageStore>();
			for (Object objArray : result) {
				MwImageStore isd = (MwImageStore) objArray;
				mwImageStoreList.add(isd);
			}
			return mwImageStoreList;
		} catch (Exception e) {
			log.error("findMwImageStore failed", e);
			throw new DbException("ImageStoreDao,findMwImageStore failed", e);
		}

		finally {
			em.close();
		}
	}

	public void deleteImageStoreByID(String id) throws DbException {
		EntityManager em = getEntityManager();
		try {

			em.getTransaction().begin();
			MwImageStore mwImageStore = em.getReference(MwImageStore.class, id);
			em.remove(mwImageStore);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("deleteImageStoreByID failed", e);
			throw new DbException("ImageStoreDao, deleteImageStoreByID failed",
					e);
		} finally {
			em.close();
		}
	}
}