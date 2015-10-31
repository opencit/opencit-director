package com.intel.director.images;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.intel.director.api.MountWilsonSetting;
import com.intel.director.api.SettingsKMSObject;
import com.intel.director.api.SshSettingRequest;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.service.impl.SettingImpl;
import com.intel.mtwilson.configuration.ConfigurationException;
import com.intel.mtwilson.director.db.exception.DbException;
import com.intel.mtwilson.launcher.ws.ext.V2;

@V2
@Path("/setting")
public class Setting {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(Setting.class);

	SettingImpl impl = new SettingImpl();

	@GET
	@Path("/sshsettings/getdata")
	@Produces(MediaType.APPLICATION_JSON)
	public List<SshSettingRequest> getRecentSsh() throws DbException {
		List<SshSettingRequest> newdata = impl.sshData();
		return newdata;
	}

	@POST
	@Path("/sshsettings/postdata")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<SshSettingRequest> postRecentSsh(SshSettingRequest sshSettingRequest)
			throws DbException, IOException {
		log.debug("Dashboard -> postRecentSsh");
		impl.postSshData(sshSettingRequest);
		return impl.sshData();
	}

	@DELETE
	@Path("/sshsettings/{id: [0-9a-zA-Z_-]+}/delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<SshSettingRequest> deleteSsh(@PathParam("id") String sshId)
			throws DbException {
		log.debug("Dashboard -> deleteSsh");
		impl.deleteSshSetting(sshId);
		return impl.sshData();
		// return "Deleted successfully";
	}



	@PUT
	@Path("/sshsettings/update")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<SshSettingRequest> updateSsh(SshSettingRequest sshSettingRequest)
			throws DbException, IOException {
		log.debug("Setting -> updateSsh");
		impl.updateSshData(sshSettingRequest);
		return impl.sshData();
		// return "Update Success";

	}
	
	@GET
    @Path("/mtwilson/getproperties")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRecentMtWilson() throws IOException {
		log.debug("Setting -> getRecentMtWilson");
		return DirectorUtil.getProperties(Constants.MTWILSON_PROP_FILE);
    }
	
	@POST
    @Path("/mtwilson/updateproperties")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
    public String  postRecentMtWilson(MountWilsonSetting request) throws ConfigurationException, IOException {
		log.debug("Setting -> updateMtWilsonProperties");
		if(!request.mtwilson_api_url.contains("https://")){
			request.mtwilson_api_url= "https://" + request.mtwilson_api_url;
		}
		return DirectorUtil.editProperties(Constants.MTWILSON_PROP_FILE,request.toString());
		
	 }
	
	@GET
	@Path("/kms/getproperties")
	@Produces(MediaType.APPLICATION_JSON)
	public String getKMSProperties() throws IOException
	{
		log.debug("Setting -> getKMSProperties");		
		return DirectorUtil.getProperties(Constants.KMS_PROP_FILE);
	}
	
	@POST
	@Path("/kms/updateproperties")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String updateKMSProperties(SettingsKMSObject request) throws IOException
	{
		log.debug("Setting -> updateKMSProperties");
		if(!request.kms_endpoint_url.contains("https://")){
			request.kms_endpoint_url = "https://" + request.kms_endpoint_url;
		}
		return DirectorUtil.editProperties(Constants.KMS_PROP_FILE,request.toString());
		
	}

}
