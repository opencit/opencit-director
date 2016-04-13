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

import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.api.ui.TrustPolicyFields;
import com.intel.director.api.ui.TrustPolicyFilter;
import com.intel.director.api.ui.TrustPolicyOrderBy;
import com.intel.mtwilson.director.data.MwImage;
import com.intel.mtwilson.director.data.MwTrustPolicy;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class TrustPolicyDao {


	 
	
	Mapper mapper= new Mapper();
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(TrustPolicyDao.class);
	
	public TrustPolicyDao(EntityManagerFactory emf) {
		this.emf = emf;
	}

	private EntityManagerFactory emf = null;

	public EntityManager getEntityManager() {
		EntityManager em = emf.createEntityManager();
		em.clear();
		return em;
	}

	public MwTrustPolicy createTrustPolicy(MwTrustPolicy mwTrustPolicy) throws DbException {
		EntityManager em = getEntityManager();
		try {
			  em.getTransaction().begin();
	            em.persist(mwTrustPolicy);
	            em.getTransaction().commit();
		} catch(Exception e){
			log.error("createTrustPolicy failed",e);
			throw new DbException("TrustPolicyDao,createTrustPolicy method",e);
		}
		
		finally {
			em.close();
		}
		return mwTrustPolicy;
	}

	public void updateTrustPolicy(MwTrustPolicy mwTrustPolicy) throws DbException {
		EntityManager em = getEntityManager();
		try {
			  em.getTransaction().begin();
	          em.merge(mwTrustPolicy);
	          em.getTransaction().commit();
		} 
		catch(Exception e){
			log.error("updateTrustPolicy failed",e);
			throw new DbException("TrustPolicyDao,updateTrustPolicy failed",e);
		}
		finally {
			em.close();
		}
	}

	public void destroyTrustPolicy(MwTrustPolicy trustPolicy) throws DbException {
		EntityManager em = getEntityManager();
		try {
			
			em.getTransaction().begin();
			MwTrustPolicy mwPolicyUpload = em.getReference(MwTrustPolicy.class, trustPolicy.getId());
            em.remove(mwPolicyUpload);;
			
			em.getTransaction().commit();
		} 
		catch(Exception e){
			log.error("destroyTrustPolicy failed",e);
			throw new DbException("TrustPolicyDao,destroyTrustPolicy failed",e);
		}
		finally {
			em.close();
		}
	}

	public List<MwTrustPolicy> findMwTrustPolicyEntities(TrustPolicyFilter trustPolicyFilter,
			TrustPolicyOrderBy orderBy) throws DbException {
		return findMwTrustPolicyEntities(true, -1, -1, trustPolicyFilter, orderBy);
	}

	public List<MwTrustPolicy> findMwTrustPolicyEntities(int firstResult, int maxResults,
			TrustPolicyFilter trustPolicyFilter,
			TrustPolicyOrderBy orderBy) throws DbException {
		return findMwTrustPolicyEntities(false, firstResult, maxResults, trustPolicyFilter,
				orderBy);
	}

	private List<MwTrustPolicy> findMwTrustPolicyEntities(boolean all, int firstResult,
			int maxResults, TrustPolicyFilter trustPolicyFilter,
			TrustPolicyOrderBy orderBy) throws DbException {
		EntityManager em = getEntityManager();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Object[]> cq = criteriaBuilder
					.createQuery(Object[].class);
	
			Root<MwTrustPolicy> mwTrustPolicy = cq.from(MwTrustPolicy.class);
			Join<MwTrustPolicy, MwImage> mwImage=mwTrustPolicy.join("image");
			cq.multiselect(mwTrustPolicy,mwImage);
	
			Map<TrustPolicyFields, String> policyAttributestoDataMapper =mapper.getPolicyAttributestoDataMapper();
			 

			 
			if (trustPolicyFilter != null) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (trustPolicyFilter.getId() != null) {
					predicates.add(criteriaBuilder.like(mwTrustPolicy
							.<String> get(policyAttributestoDataMapper
									.get(TrustPolicyFields.ID)), "%"
							+ trustPolicyFilter.getId() + "%"));
				}
				if (trustPolicyFilter.getName() != null) {
					predicates.add(criteriaBuilder.like(mwTrustPolicy
							.<String> get(policyAttributestoDataMapper
									.get(TrustPolicyFields.NAME)), "%"
							+ trustPolicyFilter.getName() + "%"));
				}

				if (trustPolicyFilter.getImage_name() != null) {
					predicates.add(criteriaBuilder.like(mwImage
							.<String> get(policyAttributestoDataMapper
									.get(TrustPolicyFields.IMAGE_NAME)), "%"
							+ trustPolicyFilter.getImage_name() + "%"));
				}

				if (trustPolicyFilter.getImage_id() != null) {
					predicates.add(criteriaBuilder.like(mwImage
							.<String> get(policyAttributestoDataMapper
									.get(TrustPolicyFields.IMAGE_ID)), "%"
							+ trustPolicyFilter.getImage_id() + "%"));
				}

				if (trustPolicyFilter.getImage_format() != null) {
					predicates.add(criteriaBuilder.like(mwImage
							.<String> get(policyAttributestoDataMapper
									.get(TrustPolicyFields.IMAGE_FORMAT)), "%"
							+ trustPolicyFilter.getImage_format() + "%"));
				}

				if (trustPolicyFilter.getCreated_by_user_id() != null) {
					predicates
							.add(criteriaBuilder.like(
									mwTrustPolicy
											.<String> get(policyAttributestoDataMapper
													.get(TrustPolicyFields.CREATED_BY_USER_ID)),
									trustPolicyFilter.getCreated_by_user_id()));
				}

				if (trustPolicyFilter.getEdited_by_user_id() != null) {
					predicates.add(criteriaBuilder.like(mwTrustPolicy
							.<String> get(policyAttributestoDataMapper
									.get(TrustPolicyFields.EDITED_BY_USER_ID)),
							trustPolicyFilter.getEdited_by_user_id()));
				}

				if (trustPolicyFilter.getFrom_created_date() != null) {
					predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwImage
							.<java.sql.Date> get(policyAttributestoDataMapper
									.get(TrustPolicyFields.CREATED_DATE)),
							new java.sql.Date(trustPolicyFilter
									.getFrom_created_date().getTime())));
				}
				if (trustPolicyFilter.getTo_created_date() != null) {
					predicates.add(criteriaBuilder.lessThanOrEqualTo(mwImage
							.<java.sql.Date> get(policyAttributestoDataMapper
									.get(TrustPolicyFields.CREATED_DATE)),
							new java.sql.Date(trustPolicyFilter
									.getTo_created_date().getTime())));
				}

		
				if (trustPolicyFilter.getFrom_image_created_date() != null) {
					predicates
							.add(criteriaBuilder.greaterThanOrEqualTo(
									mwTrustPolicy
											.<java.sql.Date> get(policyAttributestoDataMapper
													.get(TrustPolicyFields.IMAGE_CREATION_DATE)),
									new java.sql.Date(trustPolicyFilter
											.getFrom_image_created_date()
											.getTime())));
				}
				if (trustPolicyFilter.getTo_image_created_date() != null) {
					predicates
							.add(criteriaBuilder.lessThanOrEqualTo(
									mwTrustPolicy
											.<java.sql.Date> get(policyAttributestoDataMapper
													.get(TrustPolicyFields.IMAGE_CREATION_DATE)),
									new java.sql.Date(trustPolicyFilter
											.getTo_image_created_date()
											.getTime())));
				}

				if (trustPolicyFilter.getFrom_edited_date() != null) {
					predicates
							.add(criteriaBuilder.greaterThanOrEqualTo(
									mwTrustPolicy
											.<java.sql.Date> get(policyAttributestoDataMapper
													.get(TrustPolicyFields.EDITED_DATE)),
									new java.sql.Date(trustPolicyFilter
											.getFrom_edited_date().getTime())));
				}
				if (trustPolicyFilter.getTo_edited_date() != null) {
					predicates
							.add(criteriaBuilder.lessThanOrEqualTo(
									mwTrustPolicy
											.<java.sql.Date> get(policyAttributestoDataMapper
													.get(TrustPolicyFields.EDITED_DATE)),
									new java.sql.Date(trustPolicyFilter
											.getTo_edited_date().getTime())));
				}

				cq.where(criteriaBuilder.and(predicates
						.toArray(new Predicate[] {})));
			}
			
			
			 
			 if(orderBy!=null){
				if((orderBy.getTrustPolicyFields()==TrustPolicyFields.IMAGE_NAME )|| (orderBy.getTrustPolicyFields()==TrustPolicyFields.IMAGE_FORMAT ) || (orderBy.getTrustPolicyFields()==TrustPolicyFields.IMAGE_CREATION_DATE )){
				 if ((OrderByEnum.ASC)==(orderBy.getOrderBy())) {
						cq.orderBy(criteriaBuilder.asc(mwImage.get(policyAttributestoDataMapper.get(orderBy.getTrustPolicyFields()))));
					} else if ((OrderByEnum.DESC)==(orderBy.getOrderBy())) {
						cq.orderBy(criteriaBuilder.desc(mwImage.get(policyAttributestoDataMapper.get(orderBy.getTrustPolicyFields()))));
					}
				}else{
					if ((OrderByEnum.ASC)==(orderBy.getOrderBy())) {
						cq.orderBy(criteriaBuilder.asc(mwTrustPolicy.get(policyAttributestoDataMapper.get(orderBy.getTrustPolicyFields()))));
					} else if ((OrderByEnum.DESC)==(orderBy.getOrderBy())) {
						cq.orderBy(criteriaBuilder.desc(mwTrustPolicy.get(policyAttributestoDataMapper.get(orderBy.getTrustPolicyFields()))));
					}
				}
			 }
			 
			 
	
			Query q = em.createQuery(cq);

			
			if (!all) {
				if (firstResult != 0)
					q.setFirstResult(firstResult);
				if (maxResults != 0)
					q.setMaxResults(maxResults);
				
				q.setMaxResults(maxResults);
				q.setFirstResult(firstResult);
			}
			List<Object[]> result= q.getResultList();
			List<MwTrustPolicy> trustPolicyList= new ArrayList<MwTrustPolicy>();
			for(Object[] objArray : result){
				MwTrustPolicy tp=(MwTrustPolicy)objArray[0];
			
				tp.setImage((MwImage)(objArray[1]));
				trustPolicyList.add(tp);
				
			}
			return trustPolicyList;
		} catch(Exception e){
			log.error("findMwTrustPolicyEntities failed",e);
			throw new DbException("TrustPolicyDao,findMwTrustPolicyEntities failed",e);
		}
		
		finally {
			em.close();
		}
	}

	public MwTrustPolicy findMwTrustPolicy(String id) throws DbException {
		EntityManager em = getEntityManager();
		try {
			return em.find(MwTrustPolicy.class, id);
		}catch(Exception e){
			log.error("findMwTrustPolicy failed",e);
			throw new DbException("TrustPolicyDao,findMwTrustPolicy() failed",e);
		}
		
		finally {
			em.close();
		}
	}

	public int getMwTrustPolicyCount() throws DbException {
		EntityManager em = getEntityManager();
		try {
			CriteriaQuery<Long> cq = em.getCriteriaBuilder().createQuery(
					Long.class);
			Root<MwTrustPolicy> rt = cq.from(MwTrustPolicy.class);
			cq.select(em.getCriteriaBuilder().count(rt));
			Query q = em.createQuery(cq);
			return ((Long) q.getSingleResult()).intValue();
		}catch(Exception e){
			log.error("getMwTrustPolicyCount failed",e);
			throw new DbException("TrustPolicyDao,getMwTrustPolicyCount() failed",e);
		}
		
		finally {
			em.close();
		}
	}
	
	public int getMwTrustPolicyCount(TrustPolicyFilter trustPolicyFilter) throws DbException {
		EntityManager em = getEntityManager();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Long> cq = criteriaBuilder
					.createQuery(Long.class);
	
		
			Root<MwTrustPolicy> mwTrustPolicy = cq.from(MwTrustPolicy.class);
			Join<MwTrustPolicy, MwImage> mwImage=mwTrustPolicy.join("image");
			cq.select(em.getCriteriaBuilder().count(mwTrustPolicy));
	
			Map<TrustPolicyFields, String> policyAttributestoDataMapper =mapper.getPolicyAttributestoDataMapper();
			 

			 
			if(trustPolicyFilter!=null){	
			List<Predicate> predicates = new ArrayList<Predicate>();
			if(trustPolicyFilter.getId()!=null){
				predicates.add(criteriaBuilder.like(mwTrustPolicy.<String> get(policyAttributestoDataMapper.get(TrustPolicyFields.ID)),
						"%"+trustPolicyFilter.getId()+"%"));
			}
			if(trustPolicyFilter.getName()!=null){
				predicates.add(criteriaBuilder.like(mwTrustPolicy.<String> get(policyAttributestoDataMapper.get(TrustPolicyFields.NAME)),
						"%"+trustPolicyFilter.getName()+"%"));
			}
			
			
			if(trustPolicyFilter.getImage_name()!=null){
				predicates.add(criteriaBuilder.like(mwImage.<String> get(policyAttributestoDataMapper.get(TrustPolicyFields.IMAGE_NAME)),
						"%"+trustPolicyFilter.getImage_name()+"%"));
			}
			
			if(trustPolicyFilter.getImage_id()!=null){
				predicates.add(criteriaBuilder.like(mwImage.<String> get(policyAttributestoDataMapper.get(TrustPolicyFields.IMAGE_ID)),
						"%"+trustPolicyFilter.getImage_id()+"%"));
			}
			
			if(trustPolicyFilter.getImage_format()!=null){
				predicates.add(criteriaBuilder.like(mwImage.<String> get(policyAttributestoDataMapper.get(TrustPolicyFields.IMAGE_FORMAT)),
						"%"+trustPolicyFilter.getImage_format()+"%"));
			}
		
			
			if(trustPolicyFilter.getCreated_by_user_id()!=null){
				predicates.add(criteriaBuilder.like(mwTrustPolicy.<String> get(policyAttributestoDataMapper.get(TrustPolicyFields.CREATED_BY_USER_ID)),
						trustPolicyFilter.getCreated_by_user_id()));
			}
			
			if(trustPolicyFilter.getEdited_by_user_id()!=null){
				predicates.add(criteriaBuilder.like(mwTrustPolicy.<String> get(policyAttributestoDataMapper.get(TrustPolicyFields.EDITED_BY_USER_ID)),
						trustPolicyFilter.getEdited_by_user_id()));
			}

			if(trustPolicyFilter.getFrom_created_date()!=null){
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwImage.<java.sql.Date> get(policyAttributestoDataMapper.get(TrustPolicyFields.CREATED_DATE)),new java.sql.Date(trustPolicyFilter.getFrom_created_date().getTime()) ));
			}
			if(trustPolicyFilter.getTo_created_date()!=null){
				predicates.add(criteriaBuilder.lessThanOrEqualTo(mwImage.<java.sql.Date> get(policyAttributestoDataMapper.get(TrustPolicyFields.CREATED_DATE)),new java.sql.Date(trustPolicyFilter.getTo_created_date().getTime()) ));
			}
			
			if(trustPolicyFilter.getFrom_image_created_date()!=null){
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwTrustPolicy.<java.sql.Date> get(policyAttributestoDataMapper.get(TrustPolicyFields.IMAGE_CREATION_DATE)),new java.sql.Date(trustPolicyFilter.getFrom_image_created_date().getTime()) ));
			}
			if(trustPolicyFilter.getTo_image_created_date()!=null){
				predicates.add(criteriaBuilder.lessThanOrEqualTo(mwTrustPolicy.<java.sql.Date> get(policyAttributestoDataMapper.get(TrustPolicyFields.IMAGE_CREATION_DATE)),new java.sql.Date(trustPolicyFilter.getTo_image_created_date().getTime()) ));
			}
			
			
			if(trustPolicyFilter.getFrom_edited_date()!=null){
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwTrustPolicy.<java.sql.Date> get(policyAttributestoDataMapper.get(TrustPolicyFields.EDITED_DATE)),new java.sql.Date(trustPolicyFilter.getFrom_edited_date().getTime()) ));
			}
			if(trustPolicyFilter.getTo_edited_date()!=null){
				predicates.add(criteriaBuilder.lessThanOrEqualTo(mwTrustPolicy.<java.sql.Date> get(policyAttributestoDataMapper.get(TrustPolicyFields.EDITED_DATE)),new java.sql.Date(trustPolicyFilter.getTo_edited_date().getTime()) ));
			}
		
		
			
			cq.where(criteriaBuilder.and(predicates.toArray(new Predicate[] {})));
			}
			
			Query q = em.createQuery(cq);
			return ((Long) q.getSingleResult()).intValue();
		} catch(Exception e){
			log.error("getMwTrustPolicyCount failed",e);
			throw new DbException("TrustPolicyDao,getMwTrustPolicyCount(trustPolicyFilter) failed",e);
		}
		
		finally {
			em.close();
		}
	}

}
