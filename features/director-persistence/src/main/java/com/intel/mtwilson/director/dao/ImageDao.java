package com.intel.mtwilson.director.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.director.api.ImageAttributeFields;
import com.intel.director.api.ui.ImageAttributesFilter;
import com.intel.director.api.ui.ImageAttributesOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.mtwilson.director.data.MwImage;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class ImageDao {

    private Logger log = LoggerFactory.getLogger(getClass());
    Mapper mapper = new Mapper();

    public ImageDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        EntityManager em = emf.createEntityManager();
        em.clear();
        return em;
    }

    public MwImage createImage(MwImage img) throws DbException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(img);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new DbException("ImageDao,createImage method", e);
        } finally {
            em.close();
        }
        return img;
    }

    public void updateImage(MwImage img) throws DbException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(img);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new DbException("ImageDao,updateImage failed", e);
        } finally {
            em.close();
        }
    }

    public void destroyImage(MwImage img) throws DbException {
        EntityManager em = getEntityManager();
        try {

            em.getTransaction().begin();
            MwImage mwImage = em.getReference(MwImage.class, img.getId());
            em.remove(mwImage);

            em.getTransaction().commit();
        } catch (Exception e) {
            throw new DbException("ImageDao,destroyImage failed", e);
        } finally {
            em.close();
        }
    }

    public List<MwImage> findMwImageEntities(ImageAttributesFilter imgFilter,
            ImageAttributesOrderBy orderBy) throws DbException {
        return findMwImageEntities(true, -1, -1, imgFilter, orderBy);
    }

    public List<MwImage> findMwImageEntities(int firstResult, int maxResults,
            ImageAttributesFilter imgFilter,
            ImageAttributesOrderBy orderBy) throws DbException {
        return findMwImageEntities(false, firstResult, maxResults, imgFilter,
                orderBy);
    }

    private List<MwImage> findMwImageEntities(boolean all, int firstResult,
            int maxResults, ImageAttributesFilter imgFilter,
            ImageAttributesOrderBy orderBy) throws DbException {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<MwImage> cq = criteriaBuilder
                    .createQuery(MwImage.class);

            Root<MwImage> root = cq.from(MwImage.class);

            Map<ImageAttributeFields, String> imageAttributestoDataMapper = mapper.getImageAttributesToDataMapper();

            if (imgFilter != null) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (imgFilter.getId() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(imageAttributestoDataMapper.get(ImageAttributeFields.ID)),
                            "%" + imgFilter.getId() + "%"));
                }
                if (imgFilter.getImage_deployments() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(imageAttributestoDataMapper.get(ImageAttributeFields.IMAGE_DEPLOYMENTS)),
                            "%" + imgFilter.getImage_deployments() + "%"));
                }
                if (imgFilter.getImage_format() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(imageAttributestoDataMapper.get(ImageAttributeFields.IMAGE_FORMAT)),
                            "%" + imgFilter.getImage_format() + "%"));
                }
                if (imgFilter.getName() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(imageAttributestoDataMapper.get(ImageAttributeFields.NAME)),
                            "%" + imgFilter.getName() + "%"));
                }

                if (imgFilter.getCreated_by_user_id() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(imageAttributestoDataMapper.get(ImageAttributeFields.CREATED_BY_USER_ID)),
                            imgFilter.getCreated_by_user_id()));
                }

                if (imgFilter.getEdited_by_user_id() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(imageAttributestoDataMapper.get(ImageAttributeFields.EDITED_BY_USER_ID)),
                            imgFilter.getEdited_by_user_id()));
                }

                if (imgFilter.getFrom_created_date() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<java.sql.Date>get(imageAttributestoDataMapper.get(ImageAttributeFields.CREATED_DATE)), new java.sql.Date(imgFilter.getFrom_created_date().getTime())));
                }
                if (imgFilter.getTo_created_date() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<java.sql.Date>get(imageAttributestoDataMapper.get(ImageAttributeFields.CREATED_DATE)), new java.sql.Date(imgFilter.getTo_created_date().getTime())));
                }

                cq.where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));
            }

            if (orderBy != null) {
                if ((OrderByEnum.ASC) == (orderBy.getOrderBy())) {
                    cq.orderBy(criteriaBuilder.asc(root.get(imageAttributestoDataMapper.get(orderBy.getImgFields()))));
                } else if ((OrderByEnum.DESC) == (orderBy.getOrderBy())) {
                    cq.orderBy(criteriaBuilder.desc(root.get(imageAttributestoDataMapper.get(orderBy.getImgFields()))));
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
            return q.getResultList();
        } catch (Exception e) {
            throw new DbException("ImageDao,findMwImageEntities failed", e);
        } finally {
            em.close();
        }
    }

    public MwImage findMwImage(String id) throws DbException {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwImage.class, id);
        } catch (Exception e) {
            throw new DbException("ImageDao,findMwImage() failed", e);
        } finally {
            em.close();
        }
    }

    public int getMwImageCount() throws DbException {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<Long> cq = em.getCriteriaBuilder().createQuery(
                    Long.class);
            Root<MwImage> rt = cq.from(MwImage.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } catch (Exception e) {
            throw new DbException("ImageDao,getMwImageCount() failed", e);
        } finally {
            em.close();
        }
    }

    public int getMwImageCount(ImageAttributesFilter imgFilter) throws DbException {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = criteriaBuilder
                    .createQuery(Long.class);

            Root<MwImage> root = cq.from(MwImage.class);
            cq.select(em.getCriteriaBuilder().count(root));

            Map<ImageAttributeFields, String> imageAttributestoDataMapper = mapper.getImageAttributesToDataMapper();

            if (imgFilter != null) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (imgFilter.getId() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(imageAttributestoDataMapper.get(ImageAttributeFields.ID)),
                            "%" + ImageAttributeFields.ID + "%"));
                }
                if (imgFilter.getImage_deployments() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(imageAttributestoDataMapper.get(ImageAttributeFields.IMAGE_DEPLOYMENTS)),
                            "%" + imgFilter.getImage_deployments() + "%"));
                }
                if (imgFilter.getImage_format() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(imageAttributestoDataMapper.get(ImageAttributeFields.IMAGE_FORMAT)),
                            "%" + imgFilter.getImage_format() + "%"));
                }
                if (imgFilter.getName() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(imageAttributestoDataMapper.get(ImageAttributeFields.NAME)),
                            "%" + imgFilter.getName() + "%"));
                }
                if (imgFilter.getCreated_by_user_id() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(imageAttributestoDataMapper.get(ImageAttributeFields.CREATED_BY_USER_ID)),
                            imgFilter.getCreated_by_user_id()));
                }

                if (imgFilter.getEdited_by_user_id() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(imageAttributestoDataMapper.get(ImageAttributeFields.EDITED_BY_USER_ID)),
                            imgFilter.getEdited_by_user_id()));
                }

                if (imgFilter.getFrom_created_date() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<java.sql.Date>get(imageAttributestoDataMapper.get(ImageAttributeFields.CREATED_DATE)), new java.sql.Date(imgFilter.getFrom_created_date().getTime())));
                }
                if (imgFilter.getTo_created_date() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<java.sql.Date>get(imageAttributestoDataMapper.get(ImageAttributeFields.CREATED_DATE)), new java.sql.Date(imgFilter.getTo_created_date().getTime())));
                }

                cq.where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));
            }

            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } catch (Exception e) {
            throw new DbException("ImageDao,getMwImageCount(imageFilter) failed", e);
        } finally {
            em.close();
        }
    }

}
