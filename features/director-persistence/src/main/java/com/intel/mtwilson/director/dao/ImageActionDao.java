package com.intel.mtwilson.director.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageActionTask;
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
			log.error("createImage failed",e);
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

	public List<ImageActionObject> getImageActionByImageID(String image_id)
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
	}

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

	@SuppressWarnings("unchecked")
	public List<MwImageAction> showAllAction() throws DbException {
		EntityManager em = getEntityManager();
		List<MwImageAction> li;
		try {

			Query query = em.createQuery("SELECT i FROM MwImageAction i");
			li = query.getResultList();
			System.out.println(li);
		} catch (Exception e) {
			log.error("showAllAction failed",e);
			throw new DbException("Show All Image Action Object Failed", e);
		}
		return li;
	}

}