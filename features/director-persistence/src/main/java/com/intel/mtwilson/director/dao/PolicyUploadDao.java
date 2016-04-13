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

import com.intel.director.api.ui.ImageStoreUploadFields;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.api.ui.PolicyUploadFields;
import com.intel.director.api.ui.PolicyUploadFilter;
import com.intel.director.api.ui.PolicyUploadOrderBy;
import com.intel.mtwilson.director.data.MwPolicyUpload;
import com.intel.mtwilson.director.data.MwTrustPolicy;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class PolicyUploadDao {

    Mapper mapper = new Mapper();

    public PolicyUploadDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(PolicyUploadDao.class);
    
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        EntityManager em = emf.createEntityManager();
        em.clear();
        return em;
    }

    public MwPolicyUpload createPolicyUpload(MwPolicyUpload polUpload) throws DbException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(polUpload);
            em.getTransaction().commit();
        } catch (Exception e) {
        	log.error("createPolicyUpload failed",e);
            throw new DbException("PolicyUploadDao,createPolicyUpload method", e);
        } finally {
            em.close();
        }
        return polUpload;
    }

    public void updatePolicyUpload(MwPolicyUpload polUpload) throws DbException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(polUpload);
            em.getTransaction().commit();
        } catch (Exception e) {
        	log.error("updatePolicyUpload failed",e);
            throw new DbException("PolicyUploadDao,updatePolicyUpload failed", e);
        } finally {
            em.close();
        }
    }

    public void destroyPolicyUpload(MwPolicyUpload polUpload) throws DbException {
        EntityManager em = getEntityManager();
        try {

            em.getTransaction().begin();
            MwPolicyUpload MwPolicyUpload = em.getReference(MwPolicyUpload.class, polUpload.getId());
            em.remove(MwPolicyUpload);

            em.getTransaction().commit();
        } catch (Exception e) {
        	log.error("destroyPolicyUpload failed",e);
            throw new DbException("PolicyUploadDao,destroyPolicyUpload failed", e);
        } finally {
            em.close();
        }
    }

    public List<MwPolicyUpload> findMwPolicyUploadEntities(PolicyUploadFilter polUploadFilter,
            PolicyUploadOrderBy orderBy) throws DbException {
        return findMwPolicyUploadEntities(true, -1, -1, polUploadFilter, orderBy);
    }

    public List<MwPolicyUpload> findMwPolicyUploadEntities(int firstResult, int maxResults,
            PolicyUploadFilter polUploadFilter,
            PolicyUploadOrderBy orderBy) throws DbException {
        return findMwPolicyUploadEntities(false, firstResult, maxResults, polUploadFilter,
                orderBy);
    }

    private List<MwPolicyUpload> findMwPolicyUploadEntities(boolean all, int firstResult,
            int maxResults, PolicyUploadFilter polUploadFilter,
            PolicyUploadOrderBy orderBy) throws DbException {
        EntityManager em = getEntityManager();
        try {

            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Object[]> cq = criteriaBuilder
                    .createQuery(Object[].class);

            Root<MwPolicyUpload> mwPolicyUpload = cq.from(MwPolicyUpload.class);
            Join<MwPolicyUpload, MwTrustPolicy> mwPolicy = mwPolicyUpload.join("trustPolicy");
            cq.multiselect(mwPolicyUpload, mwPolicy);

            Map<PolicyUploadFields, String> policyUploadAttributestoDataMapper = mapper.getPolicyUploadtAttributestoDataMapper();

            if (polUploadFilter != null) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (polUploadFilter.getId() != null) {
                    predicates.add(criteriaBuilder.like(mwPolicyUpload.<String>get(policyUploadAttributestoDataMapper.get(PolicyUploadFields.ID)),
                            "%" + polUploadFilter.getId() + "%"));
                }
                if (polUploadFilter.getStatus() != null) {
                    predicates.add(criteriaBuilder.like(mwPolicyUpload.<String>get(policyUploadAttributestoDataMapper.get(PolicyUploadFields.STATUS)),
                            "%" + polUploadFilter.getStatus() + "%"));
                }
                if (polUploadFilter.getPolicy_uri() != null) {
                    predicates.add(criteriaBuilder.like(mwPolicyUpload.<String>get(policyUploadAttributestoDataMapper.get(PolicyUploadFields.POLICY_URI)),
                            "%" + polUploadFilter.getPolicy_uri() + "%"));
                }

            	if (polUploadFilter.getTrust_policy_id()!= null) {
					predicates.add(criteriaBuilder.like(mwPolicy
							.<String> get(policyUploadAttributestoDataMapper
									.get(PolicyUploadFields.TRUST_POLICY_ID)), "%"
							+ polUploadFilter.getTrust_policy_id() + "%"));
				}

                if (polUploadFilter.getFrom_date() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwPolicyUpload.<java.sql.Date>get(policyUploadAttributestoDataMapper.get(PolicyUploadFields.DATE)), new java.sql.Date(polUploadFilter.getFrom_date().getTime())));
                }
                if (polUploadFilter.getTo_date() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(mwPolicyUpload.<java.sql.Date>get(policyUploadAttributestoDataMapper.get(PolicyUploadFields.DATE)), new java.sql.Date(polUploadFilter.getTo_date().getTime())));
                }

                cq.where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));
            }

            if (orderBy != null) {
                
                    if ((OrderByEnum.ASC) == (orderBy.getOrderBy())) {
                        cq.orderBy(criteriaBuilder.asc(mwPolicyUpload.get(policyUploadAttributestoDataMapper.get(orderBy.getPolicyUploadFields()))));
                    } else if ((OrderByEnum.DESC) == (orderBy.getOrderBy())) {
                        cq.orderBy(criteriaBuilder.desc(mwPolicyUpload.get(policyUploadAttributestoDataMapper.get(orderBy.getPolicyUploadFields()))));
                    }
                
            }

            Query q = em.createQuery(cq);

            if (!all) {
                if (firstResult != 0) {
                    q.setFirstResult(firstResult);
                }
                if (maxResults != 0) {
                    q.setMaxResults(maxResults);
                }

                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            List<Object[]> result = q.getResultList();
            List<MwPolicyUpload> policyUploadList = new ArrayList<MwPolicyUpload>();

            for (Object[] objArray : result) {
                MwPolicyUpload tp = (MwPolicyUpload) objArray[0];

                tp.setTrustPolicy((MwTrustPolicy) (objArray[1]));
                policyUploadList.add(tp);

            }
            return policyUploadList;

        } catch (Exception e) {
        	log.error("findMwPolicyUploadEntities failed",e);
            throw new DbException("PolicyUploadDao,findMwPolicyUploadEntities failed", e);
        } finally {
            em.close();
        }
    }

    public MwPolicyUpload findMwPolicyUpload(String id) throws DbException {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwPolicyUpload.class, id);
        } catch (Exception e) {
        	log.error("findMwPolicyUpload failed",e);
            throw new DbException("PolicyUploadDao,findMwPolicyUpload() failed", e);
        } finally {
            em.close();
        }
    }

    public int getMwPolicyUploadCount() throws DbException {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<Long> cq = em.getCriteriaBuilder().createQuery(
                    Long.class);
            Root<MwPolicyUpload> rt = cq.from(MwPolicyUpload.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } catch (Exception e) {
        	log.error("getMwPolicyUploadCount failed",e);
            throw new DbException("PolicyUploadDao,getMwPolicyUploadCount() failed", e);
        } finally {
            em.close();
        }
    }

    public int getMwPolicyUploadCount(PolicyUploadFilter polUploadFilter) throws DbException {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = criteriaBuilder
                    .createQuery(Long.class);

            Root<MwPolicyUpload> mwPolicyUpload = cq.from(MwPolicyUpload.class);
            Join<MwPolicyUpload, MwTrustPolicy> mwPolicy = mwPolicyUpload.join("trustPolicy");
            cq.select(em.getCriteriaBuilder().count(mwPolicyUpload));

            Map<PolicyUploadFields, String> policyUploadAttributestoDataMapper = mapper.getPolicyUploadtAttributestoDataMapper();

            if (polUploadFilter != null) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (polUploadFilter.getId() != null) {
                    predicates.add(criteriaBuilder.like(mwPolicyUpload.<String>get(policyUploadAttributestoDataMapper.get(PolicyUploadFields.ID)),
                            "%" + polUploadFilter.getId() + "%"));
                }
                if (polUploadFilter.getStatus() != null) {
                    predicates.add(criteriaBuilder.like(mwPolicyUpload.<String>get(policyUploadAttributestoDataMapper.get(PolicyUploadFields.STATUS)),
                            "%" + polUploadFilter.getStatus() + "%"));
                }
                if (polUploadFilter.getPolicy_uri() != null) {
                    predicates.add(criteriaBuilder.like(mwPolicyUpload.<String>get(policyUploadAttributestoDataMapper.get(PolicyUploadFields.POLICY_URI)),
                            "%" + polUploadFilter.getPolicy_uri() + "%"));
                }

                if (polUploadFilter.getStoreArtifactId() != null) {
                    predicates.add(criteriaBuilder.equal(mwPolicyUpload.<String>get(policyUploadAttributestoDataMapper.get(ImageStoreUploadFields.STORE_ARTIFACT_ID)),
                    		polUploadFilter.getStoreArtifactId() ));
                }
                
                if (polUploadFilter.isEnableDeletedCheck()) {
                    predicates.add(criteriaBuilder.equal(mwPolicyUpload.<Boolean>get(policyUploadAttributestoDataMapper.get(ImageStoreUploadFields.IS_DELETED)),
                    		polUploadFilter.isDeleted() ));
                }

                if (polUploadFilter.getFrom_date() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwPolicyUpload.<java.sql.Date>get(policyUploadAttributestoDataMapper.get(PolicyUploadFields.DATE)), new java.sql.Date(polUploadFilter.getFrom_date().getTime())));
                }
                if (polUploadFilter.getTo_date() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(mwPolicyUpload.<java.sql.Date>get(policyUploadAttributestoDataMapper.get(PolicyUploadFields.DATE)), new java.sql.Date(polUploadFilter.getTo_date().getTime())));
                }

                cq.where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));
            }

            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } catch (Exception e) {
        	log.error("getMwPolicyUploadCount failed",e);
            throw new DbException("PolicyUploadDao,getMwPolicyUploadCount(policyUploadFilter) failed", e);
        } finally {
            em.close();
        }
    }

}
