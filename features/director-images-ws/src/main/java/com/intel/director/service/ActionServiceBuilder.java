package com.intel.director.service;


import com.intel.director.api.ui.ImageInfo;
import com.intel.director.common.Constants;
import com.intel.director.images.exception.DirectorException;
import com.intel.director.service.impl.BMFetchActionsService;
import com.intel.director.service.impl.DockerFetchActionsService;
import com.intel.director.service.impl.ImageActionImpl;
import com.intel.director.service.impl.ImageServiceImpl;
import com.intel.director.service.impl.VMFetchActionsService;


public class ActionServiceBuilder {

	static ImageService imageService = new ImageServiceImpl();
	
	  public static ImageActionService build(String imageid) throws DirectorException {
		  ImageInfo image =null;
			try {
				 image = imageService.fetchImageById(imageid);
				
			} catch (DirectorException e) {
				throw new DirectorException(
						" fetch Image by Id failed",e);
				
			}
			ImageActionService actionService = null;
			
			
			switch (image.getImage_deployments()) {
			case Constants.DEPLOYMENT_TYPE_VM:
				actionService=new ImageActionImpl(new VMFetchActionsService());
				break;
			case Constants.DEPLOYMENT_TYPE_BAREMETAL:
				actionService=new ImageActionImpl(new BMFetchActionsService());
				break;
			case Constants.DEPLOYMENT_TYPE_DOCKER:
				actionService=new ImageActionImpl(new DockerFetchActionsService());
				break;
			
			}	
			
			return actionService;
	  
	  }
	  
	
}
