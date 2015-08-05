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

import com.intel.director.api.ImageStoreUploadFields;
import com.intel.director.api.ui.ImageStoreUploadFilter;
import com.intel.director.api.ui.ImageStoreUploadOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.mtwilson.director.data.MwImage;
import com.intel.mtwilson.director.data.MwImageUpload;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class ImageStoreUploadDao {

    private Logger log = LoggerFactory.getLogger(getClass());
    Mapper mapper = new Mapper();

    public ImageStoreUploadDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        EntityManager em = emf.createEntityManager();
        em.clear();
        return em;
    }

    public MwImageUpload createImageUpload(MwImageUpload imgUpload) throws DbException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(imgUpload);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new DbException("ImageStoreUploadDao,createImageUpload method", e);
        } finally {
            em.close();
        }
        return imgUpload;
    }

    public void updateImageUpload(MwImageUpload imgUpload) throws DbException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(imgUpload);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new DbException("ImageStoreUploadDao,updateImageUpload failed", e);
        } finally {
            em.close();
        }
    }

    public void destroyImageUpload(MwImageUpload imgUpload) throws DbException {
        EntityManager em = getEntityManager();
        try {

            em.getTransaction().begin();
            MwImageUpload MwImageUpload = em.getReference(MwImageUpload.class, imgUpload.getId());
            em.remove(MwImageUpload);

            em.getTransaction().commit();
        } catch (Exception e) {
            throw new DbException("ImageStoreUploadDao,destroyImageUpload failed", e);
        } finally {
            em.close();
        }
    }

    public List<MwImageUpload> findMwImageUploadEntities(ImageStoreUploadFilter imgUploadFilter,
            ImageStoreUploadOrderBy orderBy) throws DbException {
        return findMwImageUploadEntities(true, -1, -1, imgUploadFilter, orderBy);
    }

    public List<MwImageUpload> findMwImageUploadEntities(int firstResult, int maxResults,
            ImageStoreUploadFilter imgUploadFilter,
            ImageStoreUploadOrderBy orderBy) throws DbException {
        return findMwImageUploadEntities(false, firstResult, maxResults, imgUploadFilter,
                orderBy);
    }

    private List<MwImageUpload> findMwImageUploadEntities(boolean all, int firstResult,
            int maxResults, ImageStoreUploadFilter imgUploadFilter,
            ImageStoreUploadOrderBy orderBy) throws DbException {
        EntityManager em = getEntityManager();
        try {

            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Object[]> cq = criteriaBuilder
                    .createQuery(Object[].class);

            Root<MwImageUpload> mwImageUpload = cq.from(MwImageUpload.class);
            Join<MwImageUpload, MwImage> mwImage = mwImageUpload.join("image");
            cq.multiselect(mwImageUpload, mwImage);

            Map<ImageStoreUploadFields, String> imageUploadAttributestoDataMapper = mapper.getImageUploadtAttributestoDataMapper();

            if (imgUploadFilter != null) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (imgUploadFilter.getId() != null) {
                    predicates.add(criteriaBuilder.like(mwImageUpload.<String>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.ID)),
                            "%" + imgUploadFilter.getId() + "%"));
                }
                if (imgUploadFilter.getStatus() != null) {
                    predicates.add(criteriaBuilder.like(mwImageUpload.<String>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.STATUS)),
                            "%" + imgUploadFilter.getStatus() + "%"));
                }
                if (imgUploadFilter.getImage_uri() != null) {
                    predicates.add(criteriaBuilder.like(mwImageUpload.<String>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.IMAGE_URI)),
                            "%" + imgUploadFilter.getImage_uri() + "%"));
                }

                if (imgUploadFilter.getImage_name() != null) {
                    predicates.add(criteriaBuilder.like(mwImage.<String>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.IMAGE_NAME)),
                            "%" + imgUploadFilter.getImage_name() + "%"));
                }

                if (imgUploadFilter.getImage_format() != null) {
                    predicates.add(criteriaBuilder.like(mwImage.<String>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.IMAGE_FORMAT)),
                            "%" + imgUploadFilter.getImage_format() + "%"));
                }

                if (imgUploadFilter.getImage_id() != null) {
                    predicates.add(criteriaBuilder.like(mwImage.<String>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.IMAGE_ID)),
                            "%" + imgUploadFilter.getImage_id() + "%"));
                }

                if (imgUploadFilter.getFrom_date() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwImageUpload.<java.sql.Date>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.DATE)), new java.sql.Date(imgUploadFilter.getFrom_date().getTime())));
                }
                if (imgUploadFilter.getTo_date() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(mwImageUpload.<java.sql.Date>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.DATE)), new java.sql.Date(imgUploadFilter.getTo_date().getTime())));
                }

                cq.where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));
            }

            if (orderBy != null) {
                if ((orderBy.getImgStoreUploadFields() == ImageStoreUploadFields.IMAGE_NAME) || (orderBy.getImgStoreUploadFields() == ImageStoreUploadFields.IMAGE_FORMAT) || (orderBy.getImgStoreUploadFields() == ImageStoreUploadFields.IMAGE_CREATION_DATE)) {
                    if ((OrderByEnum.ASC) == (orderBy.getOrderBy())) {
                        cq.orderBy(criteriaBuilder.asc(mwImage.get(imageUploadAttributestoDataMapper.get(orderBy.getImgStoreUploadFields()))));
                    } else if ((OrderByEnum.DESC) == (orderBy.getOrderBy())) {
                        cq.orderBy(criteriaBuilder.desc(mwImage.get(imageUploadAttributestoDataMapper.get(orderBy.getImgStoreUploadFields()))));
                    }
                } else {
                    if ((OrderByEnum.ASC) == (orderBy.getOrderBy())) {
                        cq.orderBy(criteriaBuilder.asc(mwImageUpload.get(imageUploadAttributestoDataMapper.get(orderBy.getImgStoreUploadFields()))));
                    } else if ((OrderByEnum.DESC) == (orderBy.getOrderBy())) {
                        cq.orderBy(criteriaBuilder.desc(mwImageUpload.get(imageUploadAttributestoDataMapper.get(orderBy.getImgStoreUploadFields()))));
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
            List<MwImageUpload> imageUploadList = new ArrayList<MwImageUpload>();
            List<MwImage> imageList = new ArrayList<MwImage>();
            for (Object[] objArray : result) {
                MwImageUpload tp = (MwImageUpload) objArray[0];

                tp.setImage((MwImage) (objArray[1]));
                imageUploadList.add(tp);

            }
            return imageUploadList;

        } catch (Exception e) {
            throw new DbException("ImageStoreUploadDao,findMwImageUploadEntities failed", e);
        } finally {
            em.close();
        }
    }

    public MwImageUpload findMwImageUpload(String id) throws DbException {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwImageUpload.class, id);
        } catch (Exception e) {
            throw new DbException("ImageStoreUploadDao,findMwImageUpload() failed", e);
        } finally {
            em.close();
        }
    }

    public int getMwImageUploadCount() throws DbException {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<Long> cq = em.getCriteriaBuilder().createQuery(
                    Long.class);
            Root<MwImageUpload> rt = cq.from(MwImageUpload.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } catch (Exception e) {
            throw new DbException("ImageStoreUploadDao,getMwImageUploadCount() failed", e);
        } finally {
            em.close();
        }
    }

    public int getMwImageUploadCount(ImageStoreUploadFilter imgUploadFilter) throws DbException {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = criteriaBuilder
                    .createQuery(Long.class);

            Root<MwImageUpload> mwImageUpload = cq.from(MwImageUpload.class);
            Join<MwImageUpload, MwImage> mwImage = mwImageUpload.join("image");
            cq.select(em.getCriteriaBuilder().count(mwImageUpload));

            Map<ImageStoreUploadFields, String> imageUploadAttributestoDataMapper = mapper.getImageUploadtAttributestoDataMapper();

            if (imgUploadFilter != null) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (imgUploadFilter.getId() != null) {
                    predicates.add(criteriaBuilder.like(mwImageUpload.<String>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.ID)),
                            "%" + imgUploadFilter.getId() + "%"));
                }
                if (imgUploadFilter.getStatus() != null) {
                    predicates.add(criteriaBuilder.like(mwImageUpload.<String>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.STATUS)),
                            "%" + imgUploadFilter.getStatus() + "%"));
                }
                if (imgUploadFilter.getImage_uri() != null) {
                    predicates.add(criteriaBuilder.like(mwImageUpload.<String>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.IMAGE_URI)),
                            "%" + imgUploadFilter.getImage_uri() + "%"));
                }

                if (imgUploadFilter.getImage_name() != null) {
                    predicates.add(criteriaBuilder.like(mwImage.<String>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.IMAGE_NAME)),
                            "%" + imgUploadFilter.getImage_name() + "%"));
                }

                if (imgUploadFilter.getImage_format() != null) {
                    predicates.add(criteriaBuilder.like(mwImage.<String>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.IMAGE_FORMAT)),
                            "%" + imgUploadFilter.getImage_format() + "%"));
                }

                if (imgUploadFilter.getImage_id() != null) {
                    predicates.add(criteriaBuilder.like(mwImage.<String>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.IMAGE_ID)),
                            "%" + imgUploadFilter.getImage_id() + "%"));
                }

                if (imgUploadFilter.getFrom_date() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(mwImageUpload.<java.sql.Date>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.DATE)), new java.sql.Date(imgUploadFilter.getFrom_date().getTime())));
                }
                if (imgUploadFilter.getTo_date() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(mwImageUpload.<java.sql.Date>get(imageUploadAttributestoDataMapper.get(ImageStoreUploadFields.DATE)), new java.sql.Date(imgUploadFilter.getTo_date().getTime())));
                }
                cq.where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));
            }

            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } catch (Exception e) {
            throw new DbException("ImageStoreUploadDao,getMwImageUploadCount(imageUploadFilter) failed", e);
        } finally {
            em.close();
        }
    }

}
