package com.intel.director.images;

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
	

	@GET
	@Path("/abc")
	////@(MediaType.APPLICATION_JSON)
	public Response getMsg(@PathParam("param") String msg) {
		///// log.debug("##########SSSSUCCCESSSSSSSSSSSSSSSYY");

	///	Image img = new Image();
	///	img.setImageName("testImage");
		
		///return img;
		return Response.status(201).entity("Called successfuly").build();

	}

}
