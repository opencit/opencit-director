package com.intel.mtwilson.director.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.intel.director.api.ImageStoreDetailsFilter;
import com.intel.director.api.ui.ImageStoreDetailsField;
import com.intel.mtwilson.director.data.MwImageStore;
import com.intel.mtwilson.director.data.MwImageStoreDetails;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class ImageStoreDetailsDao {


	Mapper mapper = new Mapper();

	public ImageStoreDetailsDao(EntityManagerFactory emf) {
		this.emf = emf;
	}
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageStoreDetailsDao.class);

	private EntityManagerFactory emf = null;

	public EntityManager getEntityManager() {
		EntityManager em = emf.createEntityManager();
		em.clear();
		return em;
	}

	public MwImageStoreDetails createImageStoreDetail(MwImageStoreDetails mwImageStoreDetails)
			throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(mwImageStoreDetails);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("createImageStoreDetail failed",e);
			throw new DbException("ImageStoreDetailsDao,createImageStoreDetail method", e);
		}
		finally {
			em.close();
		}
		return mwImageStoreDetails;
	}

	public MwImageStoreDetails getImageStoreDetailByID(String id) throws DbException {
		MwImageStoreDetails mwImageStoreDetails;
		EntityManager em = getEntityManager();
		try {
			mwImageStoreDetails = em.find(MwImageStoreDetails.class, id);
		} catch (Exception e) {
			log.error("getImageStoreDetailByID failed",e);
			throw new DbException("ImageStoreDetailsDao,getImageStoreDetailByID method", e);
		}
		finally {
			em.close();
		}
		return mwImageStoreDetails;
	}

	public void updateImageStoreDetails(MwImageStoreDetails mwImageStoreDetails) throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.merge(mwImageStoreDetails);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("updateImageStoreDetails failed",e);
			throw new DbException("ImageStoreDetailsDao,updateImageStoreDetails failed", e);
		} finally {
			em.close();
		}
	}

	public void deleteImageStoreDetail(MwImageStoreDetails mwImageStoreDetails) throws DbException {
		EntityManager em = getEntityManager();
		try {

			em.getTransaction().begin();
			em.remove(mwImageStoreDetails);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("deleteImageStoreDetail failed",e);
			throw new DbException("ImageStoreDetailsDao, deleteImageStoreDetail failed", e);
		} finally {
			em.close();
		}
	}

	public void deleteImageStoreDetailByID(String id) throws DbException {
		EntityManager em = getEntityManager();
		try {

			em.getTransaction().begin();
			MwImageStoreDetails mwImageStoreDetails = em.getReference(MwImageStoreDetails.class,
					id);
			em.remove(mwImageStoreDetails);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("deleteImageStoreDetailByID failed",e);
			throw new DbException("ImageStoreDetailsDao, deleteImageStoreDetailByID failed", e);
		} finally {
			em.close();
		}
	}
	
	public List<MwImageStoreDetails> getImageStoreDetail(
			ImageStoreDetailsFilter imageStoreDetailsFilter) throws DbException {

		EntityManager em = getEntityManager();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Object[]> cq = criteriaBuilder
					.createQuery(Object[].class);

			Root<MwImageStoreDetails> mwImageStoreDetails = cq
					.from(MwImageStoreDetails.class);
			Join<MwImageStoreDetails, MwImageStore> mwImageStore = mwImageStoreDetails
					.join("image_store_id");
			cq.multiselect(mwImageStoreDetails, mwImageStore);

			Map<ImageStoreDetailsField, String> imageStoreDetailsAttributestoDataMapper = mapper
					.getImageStoreDetailsAttributesMapper();

			if (imageStoreDetailsFilter != null) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (imageStoreDetailsFilter.getId() != null) {
					predicates
							.add(criteriaBuilder.like(
									mwImageStoreDetails
											.<String> get(imageStoreDetailsAttributestoDataMapper
													.get(ImageStoreDetailsField.ID)),
									"%" + imageStoreDetailsFilter.getId() + "%"));
				}

				if (imageStoreDetailsFilter.getImage_store_id() != null) {
					predicates
							.add(criteriaBuilder.like(
									mwImageStore
											.<String> get(imageStoreDetailsAttributestoDataMapper
													.get(ImageStoreDetailsField.ID)),
									"%"
											+ imageStoreDetailsFilter
													.getImage_store_id() + "%"));
				}

				if (imageStoreDetailsFilter.getKey() != null) {
					predicates
							.add(criteriaBuilder.like(
									mwImageStoreDetails
											.<String> get(imageStoreDetailsAttributestoDataMapper
													.get(ImageStoreDetailsField.KEY)),
									"%" + imageStoreDetailsFilter.getKey()
											+ "%"));
				}

				if (imageStoreDetailsFilter.getValue() != null) {
					predicates
							.add(criteriaBuilder.like(
									mwImageStoreDetails
											.<String> get(imageStoreDetailsAttributestoDataMapper
													.get(ImageStoreDetailsField.VALUE)),
									"%" + imageStoreDetailsFilter.getValue()
											+ "%"));
				}

				cq.where(criteriaBuilder.and(predicates
						.toArray(new Predicate[] {})));
				
				log.info("Query :: " + cq.toString());
			}

			Query q = em.createQuery(cq);

			List<Object[]> result = q.getResultList();
			List<MwImageStoreDetails> mwImageStoreDetailsList = new ArrayList<MwImageStoreDetails>();
			for (Object[] objArray : result) {
				MwImageStoreDetails isd = (MwImageStoreDetails) objArray[0];
				isd.setImage_store((MwImageStore) (objArray[1]));
				mwImageStoreDetailsList.add(isd);
			}
			return mwImageStoreDetailsList;
		} catch (Exception e) {
			log.error("findMwImageStoreDetails failed", e);
			throw new DbException(
					"ImageStoreDetailsDao,findMwImageStoreDetails failed", e);
		}

		finally {
			em.close();
		}
	}
}
