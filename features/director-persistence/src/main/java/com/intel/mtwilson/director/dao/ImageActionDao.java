package com.intel.mtwilson.director.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intel.director.api.ImageActionActions;
import com.intel.director.api.ImageActionObject;
import com.intel.mtwilson.director.data.MwImageAction;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.director.mapper.Mapper;


public class ImageActionDao {
 
 private Logger log = LoggerFactory.getLogger(getClass());
 Mapper mapper= new Mapper();

 public ImageActionDao(EntityManagerFactory emf) {
  this.emf = emf;
 }

 private EntityManagerFactory emf = null;

 public EntityManager getEntityManager() {
  EntityManager em = emf.createEntityManager();
  em.clear();
  return em;
 }
 
 public MwImageAction createImageAction(MwImageAction img) throws DbException {
  EntityManager em = getEntityManager();
  try {
   em.getTransaction().begin();
   em.persist(img);
   em.getTransaction().commit();
  } catch(Exception e){
   throw new DbException("ImageActionDao,createImageAction method",e);
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
  } catch(Exception e){
   throw new DbException("ImageActionDao,getImageActionByID method",e);
  }
  
  finally {
   em.close();
  }
  return img;  
 }
 
 public List<ImageActionObject> getImageActionByImageID(String image_id) throws DbException {
  List<ImageActionObject> imageActionObjectList= new ArrayList<ImageActionObject>();
  Gson gson=new Gson();
  EntityManager em = getEntityManager();
  try {
   Query query= emf.createEntityManager().createNativeQuery("SELECT id,image_id,action,action_count,action_completed,action_size,action_size_max " +
                  "FROM mw_image_action " +
                     "WHERE image_id = '" + image_id + "';");
   
   
List<Object[]> imageObjList =query.getResultList();
   for(Object[] imageObj:imageObjList){
    ImageActionObject imgAction= new ImageActionObject();
    
    imgAction.setId((String)imageObj[0]);
    imgAction.setImage_id((String)imageObj[1]);
    TypeToken<List<ImageActionActions>> token = new TypeToken<List<ImageActionActions>>(){};
    List<ImageActionActions> actionlist = gson.fromJson((String)imageObj[2], token.getType());
    imgAction.setAction(actionlist);
    imgAction.setAction_count(Integer.parseInt(imageObj[3].toString().trim()));
    imgAction.setAction_completed(Integer.parseInt(imageObj[4].toString().trim()));
    imgAction.setAction_size(Integer.parseInt(imageObj[5].toString().trim()));
    imgAction.setAction_size(Integer.parseInt(imageObj[6].toString().trim()));
    imageActionObjectList.add(imgAction);
   }
  } catch(Exception e){
   System.out.println("*****************************");
   System.out.println(e);
   throw new DbException("ImageActionDao,getImageActionByImageID method",e);
  }
  
  finally {
   em.close();
  }
  return imageActionObjectList;  
 }
 
 
 public List<ImageActionActions> searchIncompleteTask(String id)throws DbException {
  List<MwImageAction> imageActionObjectList= new ArrayList<MwImageAction>();
  imageActionObjectList=showAllAction();
  
  List<ImageActionActions> imageActionActions= new ArrayList<ImageActionActions>();
  List<ImageActionActions> newImageAction=new ArrayList<ImageActionActions>();
  for(int index=0;index<imageActionObjectList.size();index++){
   if(imageActionObjectList.get(index).getId()==id){
    ImageActionObject imageActionObject=mapper.toTransferObject(getImageActionByID(id));
    imageActionActions=imageActionObject.getAction();
    for(int i=0;i<imageActionActions.size();i++){
     if(imageActionActions.get(i).getStatus()=="INCOMPLETE"){
      newImageAction.add(imageActionActions.get(i));
     }
    }
   }
  }
  return newImageAction; 
 }
 
 
 public void updateImageAction(MwImageAction img) throws DbException {
  EntityManager em = getEntityManager();
  try {
   em.getTransaction().begin();
   em.merge(img);
   em.getTransaction().commit();
  } 
  catch(Exception e){

   throw new DbException("ImageDaoAction,updateImageAction failed",e);
  }
  finally {
   em.close();
  }
 }

 public void deleteImageAction(MwImageAction img) throws DbException {
  EntityManager em = getEntityManager();
  try {
   
   em.getTransaction().begin();
   em.remove(img);
   em.getTransaction().commit();
  } 
  catch(Exception e){
   throw new DbException("ImageDaoAction, deleteImageAction failed",e);
  }
  finally {
   em.close();
  }
 }
 
 public void deleteImageActionByID(String id) throws DbException {
  EntityManager em = getEntityManager();
  try {
   
   em.getTransaction().begin();
   MwImageAction mwImageAction = em.getReference(MwImageAction.class, id);
   em.remove(mwImageAction);
   em.getTransaction().commit();
  } 
  catch(Exception e){
   throw new DbException("ImageDaoAction, deleteImageAction failed",e);
  }
  finally {
   em.close();
  }
 }
 
 @SuppressWarnings("unchecked")
public  List<MwImageAction>  showAllAction() throws DbException {
   EntityManager em = getEntityManager();
   List<MwImageAction> li;
      try{
       
        Query query = em.createQuery("SELECT i FROM MwImageAction i");
        li=query.getResultList();
        System.out.println(li);
      }
      catch(Exception e){
       throw new DbException("Show All Image Action Object Failed", e);
      }  
      return li;
 }
 
 
 
}