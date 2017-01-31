package com.intel.director.images;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.spec.InvalidKeySpecException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.mtwilson.core.PasswordVaultFactory;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;
import org.apache.commons.lang.StringUtils;

import com.intel.director.api.GenericResponse;
import com.intel.director.api.MountWilsonSetting;
import com.intel.director.api.SettingsKMSObject;
import com.intel.director.api.ValidateKMSConfigurationRequest;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.director.service.impl.SettingImpl;
import com.intel.mtwilson.configuration.ConfigurationException;
import com.intel.mtwilson.launcher.ws.ext.V2;

import static com.intel.mtwilson.configuration.ConfigurationFactory.getConfiguration;

@V2
@Path("/setting")
public class Setting {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Setting.class);
	private static final String KMS_LOGIN_BASIC_PASSWORD = "kms.login.basic.password";

	SettingImpl impl = new SettingImpl();

	@GET
	@Path("/mtwilson")
	@Produces(MediaType.APPLICATION_JSON)
	public String getRecentMtWilson() throws IOException {
		log.debug("Setting -> getRecentMtWilson");
		return DirectorUtil.getPropertiesWithoutPassword(Constants.MTWILSON_PROP_FILE);
	}

	@POST
	@Path("/mtwilson")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String postRecentMtWilson(MountWilsonSetting request) throws ConfigurationException, IOException {
		log.debug("Setting -> updateMtWilsonProperties");
		if (!request.mtwilson_api_url.contains("https://")) {
			request.mtwilson_api_url = "https://" + request.mtwilson_api_url;
		}
		String validate = request.validate();
		if (StringUtils.isNotBlank(validate)) {
			return "Error: " + validate.replaceAll("_", " ");
		}
		DirectorUtil.editProperties(Constants.MTWILSON_PROP_FILE, request.toString());
		return DirectorUtil.getPropertiesWithoutPassword(Constants.MTWILSON_PROP_FILE);
	}

	@POST
	@Path("/mtwilson/validate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateMtWilsonConfiguration(MountWilsonSetting request)
			throws ConfigurationException, IOException {
		log.debug("Validate MTW config");
		SettingImpl settingImpl = new SettingImpl();
		GenericResponse genericResponse = settingImpl.validateMTW(request);
		return Response.status(Response.Status.OK).entity(genericResponse).build();
	}

	@GET
	@Path("/kms")
	@Produces(MediaType.APPLICATION_JSON)
	public String getKMSProperties() throws IOException {
		log.debug("Setting -> getKMSProperties");
		return DirectorUtil.getPropertiesWithoutPassword(Constants.KMS_PROP_FILE);
	}

	@POST
	@Path("/kms/validate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateKMSProperties(ValidateKMSConfigurationRequest request) throws IOException {
		log.debug("Validating KMS Properties");
		SettingImpl settingImpl = new SettingImpl();
		GenericResponse genericResponse = settingImpl.validateKMS(request);
		return Response.status(Response.Status.OK).entity(genericResponse).build();
	}

	@POST
	@Path("/kms")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String updateKMSProperties(SettingsKMSObject request) throws IOException, KeyStoreException, InvalidKeySpecException {
		log.debug("Setting -> updateKMSProperties");
		if (!request.kms_endpoint_url.contains("https://")) {
			request.kms_endpoint_url = "https://" + request.kms_endpoint_url;
		}
		DirectorUtil.editProperties(Constants.KMS_PROP_FILE, request.toString());
		if (StringUtils.isNotBlank(request.getKms_login_basic_password())) {
			try (PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
				passwordVault.set(KMS_LOGIN_BASIC_PASSWORD, new Password(request.getKms_login_basic_password().toCharArray()));
			}
		}

		return DirectorUtil.getPropertiesWithoutPassword(Constants.KMS_PROP_FILE);
	}

}
