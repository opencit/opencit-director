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

public class ImageStoreDao extends BaseDao{


	public ImageStoreDao(EntityManagerFactory emf) {
		super(emf);
	}

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageStoreDao.class);


	public MwImageStore createImageStore(MwImageStore mw_image_store)
			throws DbException {
		create(mw_image_store);
		return mw_image_store;
	}

	public MwImageStore getImageStoreByID(String id) throws DbException {
		MwImageStore mw_image_store = (MwImageStore) get(MwImageStore.class, id);		
		return mw_image_store;
	}

	public void updateImageStore(MwImageStore mw_image_store)
			throws DbException {
		update(mw_image_store);
	}

	public void deleteImageStore(MwImageStore mw_image_store)
			throws DbException {
		delete(MwImageStore.class, mw_image_store);
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
					StringBuffer bufferForArtifactString = new StringBuffer("");
					for (String artifact : artifact_types) {
						bufferForArtifactString = bufferForArtifactString.append(artifact).append("%");
					}
					String artifactString = bufferForArtifactString.toString().substring(0,
							bufferForArtifactString.length() - 1);
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
		MwImageStore imageStoreByID = getImageStoreByID(id);
		deleteImageStore(imageStoreByID);
	}
}