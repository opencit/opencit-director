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

import com.intel.director.api.ui.OrderByEnum;
import com.intel.director.api.ui.UserFields;
import com.intel.director.api.ui.UserFilter;
import com.intel.director.api.ui.UserOrderBy;
import com.intel.mtwilson.director.data.MwUser;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class UserDao {

    Mapper mapper = new Mapper();

    public UserDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        EntityManager em = emf.createEntityManager();
        em.clear();
        return em;
    }

    public MwUser createUser(MwUser user) throws DbException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new DbException("UserDao,createUser method", e);
        } finally {
            em.close();
        }
        return user;
    }

    public void updateUser(MwUser user) throws DbException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new DbException("UserDao,updateUser failed", e);
        } finally {
            em.close();
        }
    }

    public void destroyUser(MwUser user) throws DbException {
        EntityManager em = getEntityManager();
        try {

            em.getTransaction().begin();
            MwUser MwUser = em.getReference(MwUser.class, user.getId());
            em.remove(MwUser);

            em.getTransaction().commit();
        } catch (Exception e) {
            throw new DbException("UserDao,destroyUser failed", e);
        } finally {
            em.close();
        }
    }

    public List<MwUser> findMwUserEntities(UserFilter userFilter,
            UserOrderBy orderBy) throws DbException {
        return findMwUserEntities(true, -1, -1, userFilter, orderBy);
    }

    public List<MwUser> findMwUserEntities(int firstResult, int maxResults,
            UserFilter userFilter,
            UserOrderBy orderBy) throws DbException {
        return findMwUserEntities(false, firstResult, maxResults, userFilter,
                orderBy);
    }

    private List<MwUser> findMwUserEntities(boolean all, int firstResult,
            int maxResults, UserFilter userFilter,
            UserOrderBy orderBy) throws DbException {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<MwUser> cq = criteriaBuilder
                    .createQuery(MwUser.class);

            Root<MwUser> root = cq.from(MwUser.class);

            Map<UserFields, String> UserAttributestoDataMapper = mapper.getUserAttributestoDataMapper();

            if (userFilter != null) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (userFilter.getId() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(UserAttributestoDataMapper.get(UserFields.ID)),
                            "%" + userFilter.getId() + "%"));
                }
                if (userFilter.getDisplayname() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(UserAttributestoDataMapper.get(UserFields.DISPLAYNAME)),
                            "%" + userFilter.getDisplayname() + "%"));
                }
                if (userFilter.getUsername() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(UserAttributestoDataMapper.get(UserFields.USERNAME)),
                            "%" + userFilter.getUsername() + "%"));
                }
                if (userFilter.getEmail() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(UserAttributestoDataMapper.get(UserFields.EMAIL)),
                            "%" + userFilter.getEmail() + "%"));
                }

                cq.where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));
            }

            if (orderBy != null) {
                if ((OrderByEnum.ASC) == (orderBy.getOrderBy())) {
                    cq.orderBy(criteriaBuilder.asc(root.get(UserAttributestoDataMapper.get(orderBy.getUserFields()))));
                } else if ((OrderByEnum.DESC) == (orderBy.getOrderBy())) {
                    cq.orderBy(criteriaBuilder.desc(root.get(UserAttributestoDataMapper.get(orderBy.getUserFields()))));
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
            throw new DbException("UserDao,findMwUserEntities failed", e);
        } finally {
            em.close();
        }
    }

    public MwUser findMwUser(String id) throws DbException {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwUser.class, id);
        } catch (Exception e) {
            throw new DbException("UserDao,findMwUser() failed", e);
        } finally {
            em.close();
        }
    }

    public int getMwUserCount() throws DbException {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<Long> cq = em.getCriteriaBuilder().createQuery(
                    Long.class);
            Root<MwUser> rt = cq.from(MwUser.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } catch (Exception e) {
            throw new DbException("UserDao,getMwUserCount() failed", e);
        } finally {
            em.close();
        }
    }

    public int getMwUserCount(UserFilter userFilter) throws DbException {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = criteriaBuilder
                    .createQuery(Long.class);

            Root<MwUser> root = cq.from(MwUser.class);
            cq.select(em.getCriteriaBuilder().count(root));

            Map<UserFields, String> UserAttributestoDataMapper = mapper.getUserAttributestoDataMapper();

            if (userFilter != null) {
                List<Predicate> predicates = new ArrayList<Predicate>();
                if (userFilter.getId() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(UserAttributestoDataMapper.get(UserFields.ID)),
                            "%" + userFilter.getId() + "%"));
                }
                if (userFilter.getDisplayname() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(UserAttributestoDataMapper.get(UserFields.DISPLAYNAME)),
                            "%" + userFilter.getDisplayname() + "%"));
                }
                if (userFilter.getUsername() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(UserAttributestoDataMapper.get(UserFields.USERNAME)),
                            "%" + userFilter.getUsername() + "%"));
                }
                if (userFilter.getEmail() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get(UserAttributestoDataMapper.get(UserFields.EMAIL)),
                            "%" + userFilter.getEmail() + "%"));
                }

                cq.where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));
            }

            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } catch (Exception e) {
            throw new DbException("UserDao,getMwUserCount(UserFilter) failed", e);
        } finally {
            em.close();
        }
    }

}
