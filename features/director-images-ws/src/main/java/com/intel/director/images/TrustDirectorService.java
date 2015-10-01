package com.intel.director.images;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/*import com.intel.director.exception.ImageStoreNotAvailableException;
import com.intel.director.exception.KeyNotAvailableException;
import com.intel.director.model.Image;
import com.intel.director.model.ImageStore;
import com.intel.director.model.ImageStoreFactory;*/

@Path("/image")
public class TrustDirectorService {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustDirectorService.class);

	@GET
	@Path("/abc")
	////@(MediaType.APPLICATION_JSON)
	public Response getMsg(@PathParam("param") String msg) {
		///// log.debug("##########SSSSUCCCESSSSSSSSSSSSSSSYY");
		String output = "Jersey say : " + " Succes s"+msg;

		Map<String, Object> response = new HashMap<String, Object>();
	///	Image img = new Image();
	///	img.setImageName("testImage");
		
		///return img;
		return Response.status(201).entity("Called successfuly").build();

	}

}
