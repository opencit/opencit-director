package com.intel.mtwilson.director.data;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;





public class Main {

  /**
   * @param args
 * @throws SQLException 
   */
  public static void main(String[] args) throws SQLException {
	  
	
	  
	  Connection connection = null;
	  connection = DriverManager.getConnection(
	     "jdbc:postgresql://hostname:port/dbname","username", "password");
	  connection.close();
    EntityManager em = Persistence.createEntityManagerFactory("DirectorDataPU").createEntityManager();
    em.getTransaction().begin();

    
    MwImage mwImage= new MwImage();
    mwImage.setDeleted(false);
    mwImage.setImageDeploymentType("VM");
    em.persist(mwImage);
    
    MwTrustPolicy mwTrustPolicy= new MwTrustPolicy(); 
    mwTrustPolicy.setImage(mwImage);
    em.persist(mwTrustPolicy);
    
    MwImageUpload mwimageupload = new MwImageUpload();
   /// mwimageupload.setImageUri("http:/www.imageuri.com".toCharArray());;
    mwimageupload.setChecksum("sadfasds");
    mwimageupload.setStatus("Active");
    mwimageupload.setTarballUpload(true);
    mwimageupload.setImage(mwImage);
  ///  mwimageupload.setTrustPolicy(mwTrustPolicy);
    em.persist(mwimageupload);
    
    MwPolicyUpload mwPolicyUpload= new MwPolicyUpload();
  ///  mwPolicyUpload.setPolicyUri("http://policyuri/".toCharArray());
    mwPolicyUpload.setDate(new java.sql.Date((new Date()).getTime()));
    mwPolicyUpload.setStatus("ACTIVE");
    mwPolicyUpload.setTrustPolicy(mwTrustPolicy);
    em.persist(mwPolicyUpload);
    em.getTransaction().commit();
    
    
}
}
