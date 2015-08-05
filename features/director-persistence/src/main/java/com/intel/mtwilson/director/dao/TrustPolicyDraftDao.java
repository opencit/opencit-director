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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.api.TrustPolicyDraftFields;
import com.intel.director.api.TrustPolicyDraftFilter;
import com.intel.director.api.ui.TrustPolicyDraftOrderBy;
import com.intel.mtwilson.director.data.MwImage;
import com.intel.mtwilson.director.data.MwTrustPolicyDraft;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class TrustPolicyDraftDao {

    private Logger log = LoggerFactory.getLogger(getClass());
    Mapper mapper = new Mapper();

    public TrustPolicyDraftDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        EntityManager em = emf.createEntityManager();
        em.clear();
        return em;
    }

    public MwTrustPolicyDraft createTrustPolicyDraft(MwTrustPolicyDraft trustPolicyDraft) throws DbException {
        EntityManager em = getEntityManager();
        try {

            em.getTransaction().begin();
            String imageId = trustPolicyDraft.getImage().getId();
            MwImage mwImage = em.find(MwImage.class, imageId);
            trustPolicyDraft.setId((new UUID()).toString());
            mwImage.setTrustPolicyDraft(trustPolicyDraft);
            em.merge(mwImage);
            em.getTransaction().commit();

        } catch (Exception e) {
            throw new DbException("TrustPolicyDraftDao,createTrustPolicyDraft method", e);
        } finally {
            em.close();
        }
        return trustPolicyDraft;
    }

    public void updateTrustPolicyDraft(MwTrustPolicyDraft trustPolicyDraft) throws DbException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            String imageId = trustPolicyDraft.getImage().getId();
            MwImage mwImage = em.find(MwImage.class, imageId);
            mwImage.setTrustPolicyDraft(trustPolicyDraft);
            em.merge(trustPolicyDraft);
            em.merge(mwImage);

            em.merge(trustPolicyDraft);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new DbException("TrustPolicyDraftDao,updateTrustPolicyDraft failed", e);
        } finally {
            em.close();
        }
    }

    public void destroyPolicyDraft(MwTrustPolicyDraft trustPolicyDraft) throws DbException {
        EntityManager em = getEntityManager();
        try {

            em.getTransaction().begin();
            MwTrustPolicyDraft MwTrustPolicyDraft = em.getReference(MwTrustPolicyDraft.class, trustPolicyDraft.getId());
            em.remove(MwTrustPolicyDraft);

            em.getTransaction().commit();
        } catch (Exception e) {
            throw new DbException("TrustPolicyDraftDao,destroyPolicyDraft failed", e);
        } finally {
            em.close();
        }
    }

    public List<MwTrustPolicyDraft> findMwTrustPolicyDraftEntities(TrustPolicyDraftFilter trustPolicyDraftFilter,
            TrustPolicyDraftOrderBy orderBy) throws DbException {
        return findMwTrustPolicyDraftEntities(true, -1, -1, trustPolicyDraftFilter, orderBy);
    }

    public List<MwTrustPolicyDraft> findMwTrustPolicyDraftEntities(int firstResult, int maxResults,
            TrustPolicyDraftFilter trustPolicyDraftFilter,
            TrustPolicyDraftOrderBy orderBy) throws DbException {
        return findMwTrustPolicyDraftEntities(false, firstResult, maxResults, trustPolicyDraftFilter,
                orderBy);
    }

    private List<MwTrustPolicyDraft> findMwTrustPolicyDraftEntities(boolean all, int firstResult,
            int maxResults, TrustPolicyDraftFilter trustPolicyDraftFilter,
            TrustPolicyDraftOrderBy orderBy) throws DbException {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Object[]> cq = criteriaBuilder
                    .createQuery(Object[].class);

            Root<MwTrustPolicyDraft> mwTrustPolicyDraft = cq.from(MwTrustPolicyDraft.class);
            Join<MwTrustPolicyDraft, MwImage> mwImage = mwTrustPolicyDraft.join("image");
            cq.multiselect(mwTrustPolicyDraft, mwImage);

            Map<TrustPolicyDraftFields, String> policyAttributestoDataMapper = mapper.getPolicyDraftAttributestoDataMapper();

            if (trustPolicyDraftFilter != null) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (trustPolicyDraftFilter.getId() != null) {
                    predicates.add(criteriaBuilder.like(mwTrustPolicyDraft.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.ID)),
                            "%" + trustPolicyDraftFilter.getId() + "%"));
                }
                if (trustPolicyDraftFilter.getName() != null) {
                    predicates.add(criteriaBuilder.like(mwTrustPolicyDraft.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.NAME)),
                            "%" + trustPolicyDraftFilter.getName() + "%"));
                }

                if (trustPolicyDraftFilter.getImage_name() != null) {
                    predicates.add(criteriaBuilder.like(mwImage.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.IMAGE_NAME)),
                            "%" + trustPolicyDraftFilter.getImage_name() + "%"));
                }

                if (trustPolicyDraftFilter.getImage_format() != null) {
                    predicates.add(criteriaBuilder.like(mwImage.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.IMAGE_FORMAT)),
                            "%" + trustPolicyDraftFilter.getImage_format() + "%"));
                }

                if (trustPolicyDraftFilter.getImage_id() != null) {
                    predicates.add(criteriaBuilder.like(mwImage.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.IMAGE_ID)),
                            "%" + trustPolicyDraftFilter.getImage_id() + "%"));
                }

                if (trustPolicyDraftFilter.getCreated_by_user_id() != null) {
                    predicates.add(criteriaBuilder.like(mwTrustPolicyDraft.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.CREATED_BY_USER_ID)),
                            trustPolicyDraftFilter.getCreated_by_user_id()));
                }

                if (trustPolicyDraftFilter.getEdited_by_user_id() != null) {
                    predicates.add(criteriaBuilder.like(mwTrustPolicyDraft.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.EDITED_BY_USER_ID)),
                            trustPolicyDraftFilter.getEdited_by_user_id()));
                }

                if (trustPolicyDraftFilter.getFrom_created_date() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwImage.<java.sql.Date>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.CREATED_DATE)), new java.sql.Date(trustPolicyDraftFilter.getFrom_created_date().getTime())));
                }
                if (trustPolicyDraftFilter.getTo_created_date() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(mwImage.<java.sql.Date>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.CREATED_DATE)), new java.sql.Date(trustPolicyDraftFilter.getTo_created_date().getTime())));
                }

                if (trustPolicyDraftFilter.getFrom_image_created_date() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwTrustPolicyDraft.<java.sql.Date>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.IMAGE_CREATION_DATE)), new java.sql.Date(trustPolicyDraftFilter.getFrom_image_created_date().getTime())));
                }
                if (trustPolicyDraftFilter.getTo_image_created_date() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(mwTrustPolicyDraft.<java.sql.Date>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.IMAGE_CREATION_DATE)), new java.sql.Date(trustPolicyDraftFilter.getTo_image_created_date().getTime())));
                }

                if (trustPolicyDraftFilter.getFrom_edited_date() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwTrustPolicyDraft.<java.sql.Date>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.EDITED_DATE)), new java.sql.Date(trustPolicyDraftFilter.getFrom_edited_date().getTime())));
                }
                if (trustPolicyDraftFilter.getTo_edited_date() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(mwTrustPolicyDraft.<java.sql.Date>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.EDITED_DATE)), new java.sql.Date(trustPolicyDraftFilter.getTo_edited_date().getTime())));
                }

                cq.where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));
            }

            if (orderBy != null) {
                if ((orderBy.getTrustPolicyDraftFields() == TrustPolicyDraftFields.IMAGE_NAME) || (orderBy.getTrustPolicyDraftFields() == TrustPolicyDraftFields.IMAGE_FORMAT) || (orderBy.getTrustPolicyDraftFields() == TrustPolicyDraftFields.IMAGE_CREATION_DATE)) {
                    if ((OrderByEnum.ASC) == (orderBy.getOrderBy())) {
                        cq.orderBy(criteriaBuilder.asc(mwImage.get(policyAttributestoDataMapper.get(orderBy.getTrustPolicyDraftFields()))));
                    } else if ((OrderByEnum.DESC) == (orderBy.getOrderBy())) {
                        cq.orderBy(criteriaBuilder.desc(mwImage.get(policyAttributestoDataMapper.get(orderBy.getTrustPolicyDraftFields()))));
                    }
                } else {
                    if ((OrderByEnum.ASC) == (orderBy.getOrderBy())) {
                        cq.orderBy(criteriaBuilder.asc(mwTrustPolicyDraft.get(policyAttributestoDataMapper.get(orderBy.getTrustPolicyDraftFields()))));
                    } else if ((OrderByEnum.DESC) == (orderBy.getOrderBy())) {
                        cq.orderBy(criteriaBuilder.desc(mwTrustPolicyDraft.get(policyAttributestoDataMapper.get(orderBy.getTrustPolicyDraftFields()))));
                    }
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
            List<MwTrustPolicyDraft> trustPolicyDraftList = new ArrayList<MwTrustPolicyDraft>();
            List<MwImage> imageList = new ArrayList<MwImage>();
            for (Object[] objArray : result) {
                MwTrustPolicyDraft tpd = (MwTrustPolicyDraft) objArray[0];

                tpd.setImage((MwImage) (objArray[1]));
                trustPolicyDraftList.add(tpd);

            }
            return trustPolicyDraftList;
        } catch (Exception e) {
            throw new DbException("TrustPolicyDraftDao,findMwTrustPolicyDraftEntities failed", e);
        } finally {
            em.close();
        }
    }

    public MwTrustPolicyDraft findMwTrustPolicyDraft(String id) throws DbException {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwTrustPolicyDraft.class, id);
        } catch (Exception e) {
            throw new DbException("TrustPolicyDraftDao,findMwTrustPolicyDraft() failed", e);
        } finally {
            em.close();
        }
    }

    public int getMwTrustPolicyDraftCount() throws DbException {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<Long> cq = em.getCriteriaBuilder().createQuery(
                    Long.class);
            Root<MwTrustPolicyDraft> rt = cq.from(MwTrustPolicyDraft.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } catch (Exception e) {
            throw new DbException("TrustPolicyDraftDao,getMwTrustPolicyDraftCount() failed", e);
        } finally {
            em.close();
        }
    }

    public int getMwTrustPolicyDraftCount(TrustPolicyDraftFilter trustPolicyDraftFilter) throws DbException {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = criteriaBuilder
                    .createQuery(Long.class);

            Root<MwTrustPolicyDraft> mwTrustPolicyDraft = cq.from(MwTrustPolicyDraft.class);
            Join<MwTrustPolicyDraft, MwImage> mwImage = mwTrustPolicyDraft.join("image");
            cq.select(em.getCriteriaBuilder().count(mwTrustPolicyDraft));

            Map<TrustPolicyDraftFields, String> policyAttributestoDataMapper = mapper.getPolicyDraftAttributestoDataMapper();

            if (trustPolicyDraftFilter != null) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (trustPolicyDraftFilter.getId() != null) {
                    predicates.add(criteriaBuilder.like(mwTrustPolicyDraft.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.ID)),
                            "%" + trustPolicyDraftFilter.getId() + "%"));
                }
                if (trustPolicyDraftFilter.getName() != null) {
                    predicates.add(criteriaBuilder.like(mwTrustPolicyDraft.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.NAME)),
                            "%" + trustPolicyDraftFilter.getName() + "%"));
                }

                if (trustPolicyDraftFilter.getImage_name() != null) {
                    predicates.add(criteriaBuilder.like(mwImage.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.IMAGE_NAME)),
                            "%" + trustPolicyDraftFilter.getImage_name() + "%"));
                }

                if (trustPolicyDraftFilter.getImage_format() != null) {
                    predicates.add(criteriaBuilder.like(mwImage.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.IMAGE_FORMAT)),
                            "%" + trustPolicyDraftFilter.getImage_format() + "%"));
                }

                if (trustPolicyDraftFilter.getImage_id() != null) {
                    predicates.add(criteriaBuilder.like(mwImage.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.IMAGE_ID)),
                            "%" + trustPolicyDraftFilter.getImage_id() + "%"));
                }

                if (trustPolicyDraftFilter.getCreated_by_user_id() != null) {
                    predicates.add(criteriaBuilder.like(mwTrustPolicyDraft.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.CREATED_BY_USER_ID)),
                            trustPolicyDraftFilter.getCreated_by_user_id()));
                }

                if (trustPolicyDraftFilter.getEdited_by_user_id() != null) {
                    predicates.add(criteriaBuilder.like(mwTrustPolicyDraft.<String>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.EDITED_BY_USER_ID)),
                            trustPolicyDraftFilter.getEdited_by_user_id()));
                }

                if (trustPolicyDraftFilter.getFrom_created_date() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwImage.<java.sql.Date>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.CREATED_DATE)), new java.sql.Date(trustPolicyDraftFilter.getFrom_created_date().getTime())));
                }
                if (trustPolicyDraftFilter.getTo_created_date() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(mwImage.<java.sql.Date>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.CREATED_DATE)), new java.sql.Date(trustPolicyDraftFilter.getTo_created_date().getTime())));
                }

                if (trustPolicyDraftFilter.getFrom_image_created_date() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwTrustPolicyDraft.<java.sql.Date>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.IMAGE_CREATION_DATE)), new java.sql.Date(trustPolicyDraftFilter.getFrom_image_created_date().getTime())));
                }
                if (trustPolicyDraftFilter.getTo_image_created_date() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(mwTrustPolicyDraft.<java.sql.Date>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.IMAGE_CREATION_DATE)), new java.sql.Date(trustPolicyDraftFilter.getTo_image_created_date().getTime())));
                }

                if (trustPolicyDraftFilter.getFrom_edited_date() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwTrustPolicyDraft.<java.sql.Date>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.EDITED_DATE)), new java.sql.Date(trustPolicyDraftFilter.getFrom_edited_date().getTime())));
                }
                if (trustPolicyDraftFilter.getTo_edited_date() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(mwTrustPolicyDraft.<java.sql.Date>get(policyAttributestoDataMapper.get(TrustPolicyDraftFields.EDITED_DATE)), new java.sql.Date(trustPolicyDraftFilter.getTo_edited_date().getTime())));
                }

                cq.where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));
            }

            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } catch (Exception e) {
            throw new DbException("TrustPolicyDraftDao,getMwTrustPolicyDraftCount(policyDraftFilter) failed", e);
        } finally {
            em.close();
        }
    }

}
