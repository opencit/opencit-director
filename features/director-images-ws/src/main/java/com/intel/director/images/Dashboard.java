/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.director.images;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.intel.director.api.ImageActionObject;
import com.intel.director.api.ImageUploadRequest;
import com.intel.director.api.ImagesReadyToDeployResponse;
import com.intel.director.api.TrustPolicyDraft;
import com.intel.director.api.TrustPolicyDraftRequest;
import com.intel.director.api.ui.ImageCountPieChart;
import com.intel.director.api.ui.ImageInfo;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.DashboardService;
import com.intel.director.service.impl.DashboardImpl;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.launcher.ws.ext.V2;

/**
 * 
 * @author Sayee
 */
@V2
@Path("/dashboard")
public class Dashboard {

	/**
	 * Method to get the trust polices edited by the logged in user
	 */
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(Dashboard.class);


	DashboardService dashboard = new DashboardImpl();

	@GET
	@Path("/trustpolicies/{id: [0-9a-zA-Z_-]+}/currentuser")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TrustPolicyDraftRequest> getRecentEditedTrustPolicies(
			@PathParam("id") String id) throws DbException {
		log.debug("Dashboard -> trustpolicies for user : "+id);
		return dashboard.getRecentUserPolicy(id);

	}

	/**
	 * Method to get the trust policies edited by all users
	 */
	@GET
	@Path("/trustpolicies")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TrustPolicyDraft> getAllRecentEditedTrustPolicies()
			throws DbException {
		log.debug("Dashboard -> trustpolicies all"); 
		return dashboard.getRecentPolicy();

	}

	@GET
	@Path("/images/withouttrustpolicies")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ImageInfo> getAllImagesWithoutPolicy() throws DbException {
		log.debug("Dashboard -> trustpolicies withouttrustpolicies");
		return dashboard.getImagesWithoutPolicy();

	}

	/**
	 * Method to retrieve the images uploaded to TD which do not have any
	 * policies
	 */
	/*
	 * @GET
	 * 
	 * @Path("/newImages")
	 * 
	 * @Consumes(MediaType.APPLICATION_JSON)
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public void
	 * getRecentTrustDirectorImages() { }
	 *//**
	 * Images who have polices created but not yet uploaded to any store
	 * 
	 * @throws DbException
	 * @throws DirectorException
	 */
	@GET
	@Path("/imagesToUpload")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ImagesReadyToDeployResponse> getImagesReadyToUpload()
			throws DbException, DirectorException {
		log.debug("Dashboard -> trustpolicies imagesToUpload");

		return dashboard.getImagesReadyToDeploy();
	}

	/**
	 * Method to retrieve the images that were recently uploaded to image store
	 * 
	 * @return
	 * @throws DbException
	 */
	@GET
	@Path("/uploadedImages")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ImageUploadRequest> getRecentUploadedImages()
			throws DbException {
		log.debug("Dashboard -> trustpolicies uploadedImages");

		return dashboard.getRecentlyDeployedImages();
	}

	/**
	 * Method to retrieve the update on the progress of the images that are
	 * uploaded to the image store
	 * 
	 * @throws DbException
	 */
	@GET
	@Path("/uploadProgress")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ImageActionObject> getUploadProgress() throws DbException {
		log.debug("Dashboard -> trustpolicies uploadProgress");

		return dashboard.uploadInProgress();
	}

	@GET
	@Path("/pieChart")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<ImageCountPieChart> getPieChartData() throws DbException {
		log.debug("Dashboard -> trustpolicies pieChart");

		return dashboard.pieChart();
	}
}
