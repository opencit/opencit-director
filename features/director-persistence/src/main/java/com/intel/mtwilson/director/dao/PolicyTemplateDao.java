package com.intel.mtwilson.director.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import com.intel.director.api.PolicyTemplateInfo;
import com.intel.mtwilson.director.data.MwPolicyTemplate;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class PolicyTemplateDao {
	Mapper mapper = new Mapper();

	 private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
				.getLogger(PolicyTemplateDao.class);
	public PolicyTemplateDao(EntityManagerFactory emf) {
		this.emf = emf;
	}

	private EntityManagerFactory emf = null;

	public EntityManager getEntityManager() {
		EntityManager em = emf.createEntityManager();
		em.clear();
		return em;
	}

	public MwPolicyTemplate createPolicyTemplate(MwPolicyTemplate policytemplate)
			throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(policytemplate);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("createPolicyTemplate failed",e);
			throw new DbException("PolicyTemplateDao,createPolicyTemplate method", e);
		}

		finally {
			em.close();
		}
		return policytemplate;
	}

	public void updatePolicyTemplate(MwPolicyTemplate policytemplate)
			throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.merge(policytemplate);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("updatePolicyTemplate failed",e);
			throw new DbException("PolicyTemplateDao,updatePolicyTemplate failed", e);
		} finally {
			em.close();
		}
	}

	public void destroyPolicyTemplate(MwPolicyTemplate policytemplate)
			throws DbException {
		EntityManager em = getEntityManager();
		try {

			em.getTransaction().begin();
			MwPolicyTemplate MwPolicyTemplate = em.getReference(
					MwPolicyTemplate.class, policytemplate.getId());
			em.remove(MwPolicyTemplate);

			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("destroyPolicyTemplate failed",e);
			throw new DbException("PolicyTemplateDao,destroyPolicyTemplate failed", e);
		} finally {
			em.close();
		}
	}

	
	
	
	
	
	
	
	
	
	
	public List<MwPolicyTemplate> findDeploymentType(String deployment_type)
			throws DbException {
		EntityManager em = getEntityManager();
		try {

			String queryString = "select id, name, deployment_type, policy_type, content, active, deployment_type_identifier"
					+ " from mw_policy_template where active=true ";
			if (deployment_type != null) {
				queryString = queryString + "and deployment_type='"
						+ deployment_type + "'";
			}
			Query query = emf.createEntityManager().createNativeQuery(
					queryString);
			List<MwPolicyTemplate> mwPolicyTemplate = new ArrayList<MwPolicyTemplate>();
			if (query.getResultList() != null) {
				List<Object[]> policyTemplateObjList = query.getResultList();
				for (Object[] imageObj : policyTemplateObjList) {
					MwPolicyTemplate info = new MwPolicyTemplate();

					info.setId((String) imageObj[0]);
					info.setName((String) imageObj[1]);
					info.setDeployment_type((String) imageObj[2]);
					info.setPolicy_type((String) imageObj[3]);
					if(imageObj[4]!=null){
						info.setContent((new Mapper()).toCharacterArray((String)imageObj[4]));
					}
					info.setActive((boolean) imageObj[5]);
					info.setDeployment_type_identifier((String)imageObj[6]);
					mwPolicyTemplate.add(info);
				}

			}
			return mwPolicyTemplate;
		} catch (Exception e) {
			log.error("findDeploymentType failed",e);
			throw new DbException(
					"PolicyTemplateDao,findDeploymentType() failed", e);
		}

		finally {
			em.close();
		}
	}

	public List<MwPolicyTemplate> findDeploymentTypeByFilter(
			PolicyTemplateInfo filter) throws DbException {
		EntityManager em = getEntityManager();
		List<MwPolicyTemplate> mwPolicyTemplate = new ArrayList<MwPolicyTemplate>();
		try {

			String queryString = "select id, name, deployment_type, policy_type, content, active"
					+ "from mw_policy_template where active=true ";

			if (filter.deployment_type != null) {
				queryString = queryString + " and deployment_type='"
						+ filter.deployment_type + "'";
			}
			if (filter.deployment_type_identifier != null) {
				queryString = queryString + " and deployment_type_identifier='"
						+ filter.deployment_type_identifier + "'";
			}
			if (filter.getPolicy_type() != null) {
				queryString = queryString + " and policy_type='"
						+ filter.policy_type + "'";
			}
			if (filter.getName() != null) {
				queryString = queryString + " and name='" + filter.name + "'";
			}
			
			Query query = emf.createEntityManager().createNativeQuery(
					queryString);

			if (query.getResultList() != null) {
				List<Object[]> policyTemplateObjList = query.getResultList();
				for (Object[] imageObj : policyTemplateObjList) {
					MwPolicyTemplate info = new MwPolicyTemplate();

					info.setId((String) imageObj[0]);
					info.setName((String) imageObj[1]);
					info.setDeployment_type((String) imageObj[2]);
					info.setPolicy_type((String) imageObj[3]);
					if(imageObj[4]!=null){
						info.setContent((new Mapper()).toCharacterArray((String)imageObj[4]));
					}					
					info.setActive((boolean) imageObj[5]);
					mwPolicyTemplate.add(info);
				}

			}
			return mwPolicyTemplate;
		} catch (Exception e) {
			log.error("findDeploymentTypeByFilter failed",e);
			throw new DbException("PolicyTemplateDao,findDeploymentTypeByFilter() failed", e);
		}

		finally {
			em.close();
		}
	}

}
