package com.intel.mtwilson.director.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.intel.director.api.ui.ImageActionFields;
import com.intel.director.api.ui.ImageActionFilter;
import com.intel.director.api.ui.ImageActionOrderBy;
import com.intel.director.api.ui.OrderByEnum;
import com.intel.mtwilson.director.data.MwImageAction;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;

public class ImageActionDao {

	Mapper mapper = new Mapper();

	public ImageActionDao(EntityManagerFactory emf) {
		this.emf = emf;
	}
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(ImageActionDao.class);

	private EntityManagerFactory emf = null;

	public EntityManager getEntityManager() {
		EntityManager em = emf.createEntityManager();
		em.clear();
		return em;
	}

	public MwImageAction createImageAction(MwImageAction img)
			throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(img);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("createImageAction failed",e);
			throw new DbException("ImageActionDao,createImageAction method", e);
		}

		finally {
			em.close();
		}
		return img;
	}

	public MwImageAction getImageActionByID(String id) throws DbException {
		MwImageAction img;
		EntityManager em = getEntityManager();
		try {
			img = em.find(MwImageAction.class, id);
		} catch (Exception e) {
			log.error("createImage failed",e);
			throw new DbException("ImageActionDao,getImageActionByID method", e);
		}

		finally {
			em.close();
		}
		return img;
	}

	/*public List<ImageActionObject> getImageActionByImageID(String image_id)
			throws DbException {
		List<ImageActionObject> imageActionObjectList = new ArrayList<ImageActionObject>();
		Gson gson = new Gson();
		EntityManager em = getEntityManager();
		try {
			Query query = emf
					.createEntityManager()
					.createNativeQuery(
							"SELECT id,image_id,action,action_count,action_completed,action_size,action_size_max "
									+ "FROM mw_image_action "
									+ "WHERE image_id = '" + image_id + "';");

			List<Object[]> imageObjList = query.getResultList();
			for (Object[] imageObj : imageObjList) {
				ImageActionObject imgAction = new ImageActionObject();

				imgAction.setId((String) imageObj[0]);
				imgAction.setImage_id((String) imageObj[1]);
				TypeToken<List<ImageActionTask>> token = new TypeToken<List<ImageActionTask>>() {
				};
				List<ImageActionTask> actionlist = gson.fromJson(
						(String) imageObj[2], token.getType());
				imgAction.setActions(actionlist);
				imgAction.setAction_count(Integer.parseInt(imageObj[3]
						.toString().trim()));
				imgAction.setAction_completed(Integer.parseInt(imageObj[4]
						.toString().trim()));
				imgAction.setAction_size(Integer.parseInt(imageObj[5]
						.toString().trim()));
				imgAction.setAction_size(Integer.parseInt(imageObj[6]
						.toString().trim()));
				imageActionObjectList.add(imgAction);
			}
		} catch (Exception e) {
			log.error("getImageActionByImageID failed",e);
			throw new DbException(
					"ImageActionDao,getImageActionByImageID method", e);
		}

		finally {
			em.close();
		}
		return imageActionObjectList;
	}*/

	public void updateImageAction(MwImageAction img) throws DbException {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.merge(img);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("updateImageAction failed",e);
			throw new DbException("ImageDaoAction,updateImageAction failed", e);
		} finally {
			em.close();
		}
	}

	public void deleteImageAction(MwImageAction img) throws DbException {
		EntityManager em = getEntityManager();
		try {

			em.getTransaction().begin();
			em.remove(img);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("deleteImageAction failed",e);
			throw new DbException("ImageDaoAction, deleteImageAction failed", e);
		} finally {
			em.close();
		}
	}

	public void deleteImageActionByID(String id) throws DbException {
		EntityManager em = getEntityManager();
		try {

			em.getTransaction().begin();
			MwImageAction mwImageAction = em.getReference(MwImageAction.class,
					id);
			em.remove(mwImageAction);
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error("deleteImageActionByID failed",e);
			throw new DbException("ImageDaoAction, deleteImageAction failed", e);
		} finally {
			em.close();
		}
	}


	public List<MwImageAction> showAllAction() throws DbException {
		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MwImageAction> cq = cb.createQuery(MwImageAction.class);
        Root<MwImageAction> rootEntry = cq.from(MwImageAction.class);
        CriteriaQuery<MwImageAction> all = cq.select(rootEntry);
        TypedQuery<MwImageAction> allQuery = em.createQuery(all);
        if(allQuery==null){
        	return null;
        }
        return allQuery.getResultList();
		
		
	/*	EntityManager em = getEntityManager();
		List<MwImageAction> li;
		try {

			Query query = em.createQuery("SELECT i FROM MwImageAction i");
			li = query.getResultList();
			System.out.println(li);
		} catch (Exception e) {
			log.error("showAllAction failed",e);
			throw new DbException("Show All Image Action Object Failed", e);
		}
		return li;*/
	}
	
	
	public List<MwImageAction> findMwImageAction(ImageActionFilter imageActionFilter, ImageActionOrderBy imageActionOrderBy)
			throws DbException {

		EntityManager em = getEntityManager();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<Object> criteriaQuery  = criteriaBuilder
					.createQuery();
			Root<MwImageAction> mwImageAction = criteriaQuery.from(MwImageAction.class);

			Map<ImageActionFields, String> imageActionAttributestoDataMapper = mapper
					.getImageActionsAttributesMapper();
			
			List<Predicate> predicates = new ArrayList<Predicate>();
			if (imageActionFilter != null) {
				
				if(imageActionFilter.getId()!=null){
					predicates.add(criteriaBuilder.equal(mwImageAction.<String> get(imageActionAttributestoDataMapper.get(ImageActionFields.ID)),
							imageActionFilter.getId()));
				}
				if(imageActionFilter.getImage_id()!=null){
					predicates.add(criteriaBuilder.equal(mwImageAction.<String> get(imageActionAttributestoDataMapper.get(ImageActionFields.IMAGE_ID)),
							imageActionFilter.getImage_id()));
				}
				if(imageActionFilter.getCurrent_task_name()!=null){
					predicates.add(criteriaBuilder.like(mwImageAction.<String> get(imageActionAttributestoDataMapper.get(ImageActionFields.CURRENT_TASK_NAME)),
							"%"+imageActionFilter.getCurrent_task_name()+"%"));
				}
				if(imageActionFilter.getCurrent_task_status()!=null){
					predicates.add(criteriaBuilder.like(mwImageAction.<String> get(imageActionAttributestoDataMapper.get(ImageActionFields.CURRENT_TASK_STATUS)),
							"%"+imageActionFilter.getCurrent_task_status()+"%"));
				}
				

			}
			
			criteriaQuery.where(criteriaBuilder.and(predicates
					.toArray(new Predicate[] {})));
			
			 if (imageActionOrderBy != null) {
	               
	                    if ((OrderByEnum.ASC) == (imageActionOrderBy.getOrderBy())) {
	                    	criteriaQuery.orderBy(criteriaBuilder.asc(mwImageAction.get(imageActionAttributestoDataMapper.get(imageActionOrderBy.getImageActionFields()))));
	                    } else if ((OrderByEnum.DESC) == (imageActionOrderBy.getOrderBy())) {
	                    	criteriaQuery.orderBy(criteriaBuilder.desc(mwImageAction.get(imageActionAttributestoDataMapper.get(imageActionOrderBy.getImageActionFields()))));
	                    }
	                
	            }
			
			Query q = em.createQuery(criteriaQuery);
			log.info("Query :: " + q.toString());

			List<Object[]> result = q.getResultList();
			List<MwImageAction> mwImageActionList = new ArrayList<MwImageAction>();
			for (Object objArray : result) {
				MwImageAction isd = (MwImageAction) objArray;
				mwImageActionList.add(isd);
			}
			return mwImageActionList;
		} catch (Exception e) {
			log.error("findMwImageAction failed", e);
			throw new DbException("ImageActionDao,findMwImageAction failed", e);
		}

		finally {
			em.close();
		}
	}

}