package com.intel.director.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.director.api.ErrorCode;
import com.intel.director.api.GenericResponse;
import com.intel.director.api.MountWilsonSetting;
import com.intel.director.api.ValidateKMSConfigurationRequest;
import com.intel.director.common.Constants;
import com.intel.director.service.Setting;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.api.ClientException;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.attestation.client.jaxrs.Hosts;
import com.intel.mtwilson.director.features.director.kms.KmsUtil;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.user.management.client.jaxrs.Users;
import com.intel.mtwilson.user.management.rest.v2.model.UserCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserFilterCriteria;
import com.intel.mtwilson.v2.client.MwClientUtil;

public class SettingImpl implements Setting {
	// private ImageStoreManager imageStoreManager;
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SettingImpl.class);

	@Override
	public GenericResponse validateKMS(ValidateKMSConfigurationRequest request) {
		GenericResponse genericResponse = new GenericResponse();
		KmsUtil kmsUtil = null;
		String errMsg = "Error connecting to KMS";
		genericResponse.status = Constants.ERROR;
		try {
			kmsUtil = new KmsUtil(request.getUser(), request.getUrl(), request.getSha1());
		} catch (IOException e) {
			genericResponse.error = errMsg;
		} catch (JAXBException e) {
			genericResponse.error = errMsg;
		} catch (XMLStreamException e) {
			genericResponse.error = errMsg;
		} catch (Exception e) {
			genericResponse.error = errMsg;
		}
		if (StringUtils.isNotBlank(genericResponse.error)) {
			genericResponse.errorCode = ErrorCode.VALIDATION_FAILED;
			return genericResponse;
		}

		boolean status = kmsUtil.getAllKeys();
		if (status) {
			genericResponse.error = null;
			genericResponse.status = Constants.SUCCESS;
		} else {
			genericResponse.error = "Error connecting to KMS";
		}
		return genericResponse;
	}

	@SuppressWarnings("deprecation")
	@Override
	public GenericResponse validateMTW(MountWilsonSetting request) {
		GenericResponse genericResponse = new GenericResponse();
		genericResponse.status = Constants.ERROR;
		Properties mtwProperties = new Properties();

		try {
			Extensions.register(TlsPolicyCreator.class,
					com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.class);

			String apiUrl = request.getMtwilson_api_url();
			String apiPassword = request.getMtwilson_api_password();
			String apiUser = request.getMtwilson_api_username();
			String sha256 = request.getMtwilson_api_tls_policy_certificate_sha256();

			String keystore = Folders.configuration() + File.separator + apiUser + ".jks";

			log.debug("Keystore path = {}", keystore);
			mtwProperties.setProperty(Constants.MTWILSON_PROP_SERVER_API_PASSWORD, apiPassword);
			mtwProperties.setProperty(Constants.MTWILSON_PROP_SERVER_SHA256, sha256);
			mtwProperties.setProperty(Constants.MTWILSON_PROP_SERVER_API_IP, apiUrl);
			mtwProperties.setProperty(Constants.MTWILSON_PROP_SERVER_API_USER, apiUser);

			UserCollection users = null;
			try {
				Users client = new Users(mtwProperties);
				UserFilterCriteria criteria = new UserFilterCriteria();
				criteria.filter = true;
				criteria.nameEqualTo = apiUser;
				users = client.searchUsers(criteria);
			} catch (Exception e) {
				log.error("Unable to check if the user already exists in MTW", e);
			}

			File keystoreFile = new File(keystore);
			if (users != null && users.getUsers().size() > 0 && keystoreFile.exists()) {
				log.info("User: {} already created in MTW. Not creating again", apiUser);
			} else {
				log.info("Creating user: {} in MTW", apiUser);
				Properties properties = new Properties();
				File folder = new File(Folders.configuration());
				properties.setProperty("mtwilson.api.tls.policy.certificate.sha256",
						sha256);
				String comment = formatCommentRequestedRoles("Attestation", "Challenger");
				URL server = new URL(apiUrl);

				MwClientUtil.createUserInDirectoryV2(folder, apiUser, apiPassword, server, comment, properties);
			}

		} catch (IOException e) {
			String errorMsg = "Error reading configuration for MTW client";
			log.error(errorMsg, e);
			genericResponse.error = "Error registering API user";
			mtwProperties = null;
		} catch (ApiException e) {
			String errorMsg = "Error reading configuration for MTW client";
			log.error(errorMsg, e);
			genericResponse.error = "Error registering API user";
			mtwProperties = null;
		} catch (CryptographyException e) {
			String errorMsg = "Error reading configuration for MTW client";
			log.error(errorMsg, e);
			genericResponse.error = "Error registering API user";
			mtwProperties = null;
		} catch (ClientException e) {
			String errorMsg = "Error reading configuration for MTW client";
			log.error(errorMsg, e);
			genericResponse.error = "Error registering API user";
			mtwProperties = null;
		}
		if (genericResponse.error != null) {
			return genericResponse;
		}
		// Fetch the hosts to validate the connection

		try {
			Hosts hostsService = new Hosts(mtwProperties);
			HostFilterCriteria criteria = new HostFilterCriteria();
			criteria.filter = false;

			try {
				hostsService.searchHosts(criteria);
			} catch (Exception e) {
				log.error("Error while fetching hosts from Attestation Service ", e);
				genericResponse.error = "Access to Attestation Service API failed";
				return genericResponse;
			}
			genericResponse.status = Constants.SUCCESS;
		} catch (Exception e) {
			String errorMsg = "Error creating client for MTW ";
			log.error(errorMsg, e);
			genericResponse.error = "Access to Attestation Service API failed";
			return genericResponse;
		}

		return genericResponse;
	}

	private static String formatCommentRequestedRoles(String... roles) throws JsonProcessingException {
		UserComment userComment = new UserComment();
		userComment.roles.addAll(Arrays.asList(roles));
		ObjectMapper yaml = createYamlMapper();
		return yaml.writeValueAsString(userComment);
	}

	private static class UserComment {
		public HashSet<String> roles = new HashSet<>();
	}

	private static ObjectMapper createYamlMapper() {
		YAMLFactory yamlFactory = new YAMLFactory();
		yamlFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		yamlFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		ObjectMapper mapper = new ObjectMapper(yamlFactory);
		mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
		return mapper;
	}

}
