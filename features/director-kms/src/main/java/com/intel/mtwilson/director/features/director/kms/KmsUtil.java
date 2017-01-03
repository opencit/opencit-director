package com.intel.mtwilson.director.features.director.kms;

import static com.intel.mtwilson.configuration.ConfigurationFactory.getConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.crypto.file.RsaPublicKeyProtectedPemKeyEnvelopeOpener;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.kms.api.CreateKeyRequest;
import com.intel.kms.client.jaxrs2.Keys;
import com.intel.kms.ws.v2.api.Key;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.core.PasswordVaultFactory;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;

public class KmsUtil {
	RsaCredentialX509 wrappingKeyCertificate;
	String kmsLoginBasicUsername;
	Keys keys;
	Map<String, String> kmsprops = null;
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KmsUtil.class);
	private static final String DIRECTOR_ENVELOPE_ALIAS = "director.envelope.alias";
	private static final String DIRECTOR_KEYSTORE = "director.keystore";
	private static final String DIRECTOR_KEYSTORE_PASSWORD = "director.keystore.password";
	private static final String KMS_ENDPOINT_URL = "kms.endpoint.url";
	private static final String KMS_TLS_POLICY_CERTIFICATE_SHA256 = "kms.tls.policy.certificate.sha256";
	private static final String KMS_LOGIN_BASIC_USERNAME = "kms.login.basic.username";
	private static final String KMS_LOGIN_BASIC_PASSWORD = "kms.login.basic.password";
	private static final String KMS_PROP_FILE = "kms.properties";

	
	public KmsUtil(String user, String password, String url, String sha256) throws IOException, JAXBException, XMLStreamException, Exception {
		// Collect KMS configurations		
		if (StringUtils.isBlank(url)) {
			throw new NullPointerException("KMS endpoint URL not configured");
		}

		log.debug("Got the kms endpoint {}", url);
		if (StringUtils.isBlank(sha256)) {
			throw new NullPointerException("KMS TLS policy certificate digest not configured");
		}

		kmsLoginBasicUsername = user;
		if (StringUtils.isBlank(kmsLoginBasicUsername)) {
			throw new NullPointerException("KMS API username not configured");
		}

		String kmsLoginBasicPassword;

		try (PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
			if (passwordVault.contains(KMS_LOGIN_BASIC_PASSWORD)) {
				kmsLoginBasicPassword = new String(passwordVault.get(KMS_LOGIN_BASIC_PASSWORD).toCharArray());
			} else{
				kmsLoginBasicPassword = null;
				if(StringUtils.isNotBlank(password)) {
					kmsLoginBasicPassword = password;
				}
			}
		}
		
		
		// create KMS Keys API client
		Properties properties = new Properties();
		properties.setProperty("endpoint.url", url);
		properties.setProperty("tls.policy.certificate.sha256", sha256);
		properties.setProperty("login.basic.username", kmsLoginBasicUsername);
		properties.setProperty("login.basic.password", kmsLoginBasicPassword);
		keys = new Keys(properties);

		log.debug("INIT of Keys for validation complete");

	}
	
	public KmsUtil() throws Exception  {
		Password keystorePassword = null;
		PublicKey directorEnvelopePublicKey;
		String kmsEndpointUrl;
		String kmsTlsPolicyCertificateSha256;
		String kmsLoginBasicPassword;
		kmsprops = new Gson().fromJson(getProperties(KMS_PROP_FILE), new TypeToken<HashMap<String, Object>>() {
		}.getType());
		// Get director envelope key
		String directorEnvelopeAlias = getConfiguration().get(DIRECTOR_ENVELOPE_ALIAS, "director-envelope");
		if (StringUtils.isBlank(directorEnvelopeAlias)) {
			throw new NullPointerException("Trust Director Envelope alias not configured");
		}

		log.debug("KMSUTIL: Folders.configuration() : " + Folders.configuration());
		String keystorePath = getConfiguration().get(DIRECTOR_KEYSTORE,
				Folders.configuration() + File.separator + "keystore.jks");
		File keystoreFile = new File(keystorePath);
		if (!keystoreFile.exists()) {
			throw new FileNotFoundException("Director Keystore file does not exist");
		}
		log.debug("KMSUTIL: Got the keystore");
		try (PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
			if (passwordVault.contains(DIRECTOR_KEYSTORE_PASSWORD)) {
				keystorePassword = passwordVault.get(DIRECTOR_KEYSTORE_PASSWORD);
			}
		}
		log.debug("KMSUTIL: Got the keystore password");
		if (keystorePassword == null || keystorePassword.toCharArray().length == 0) {
			throw new NullPointerException("Director Keystore password is not configured");
		}

		SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
		wrappingKeyCertificate = keystore.getRsaCredentialX509(directorEnvelopeAlias, keystorePassword);
		log.debug("Found key {}", wrappingKeyCertificate.getCertificate().getSubjectX500Principal().getName());

		directorEnvelopePublicKey = wrappingKeyCertificate.getPublicKey();
		if (directorEnvelopePublicKey == null) {
			log.error("Trust Director envelope public key is not configured");
		}

		log.debug("Got the TD env key");

		// Collect KMS configurations
		kmsEndpointUrl = kmsprops.get(KMS_ENDPOINT_URL.replace('.', '_'));
		if (StringUtils.isBlank(kmsEndpointUrl)) {
			throw new NullPointerException("KMS endpoint URL not configured");
		}

		log.debug("Got the kms endpoint {}", kmsEndpointUrl);
		kmsTlsPolicyCertificateSha256 = kmsprops.get(KMS_TLS_POLICY_CERTIFICATE_SHA256.replace('.', '_'));
		if (StringUtils.isBlank(kmsTlsPolicyCertificateSha256)) {
			throw new NullPointerException("KMS TLS policy certificate digest not configured");
		}

		log.debug("Got the KMS SHA256");

		kmsLoginBasicUsername = kmsprops.get(KMS_LOGIN_BASIC_USERNAME.replace('.', '_'));
		if (StringUtils.isBlank(kmsLoginBasicUsername)) {
			throw new NullPointerException("KMS API username not configured");
		}
		log.debug("Got the KMS user name");

		try (PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
			if (passwordVault.contains(KMS_LOGIN_BASIC_PASSWORD)) {
				kmsLoginBasicPassword = new String(passwordVault.get(KMS_LOGIN_BASIC_PASSWORD).toCharArray());
			} else
				kmsLoginBasicPassword = null;
		}

		log.debug("Got the KMS password");
		if (StringUtils.isBlank(kmsLoginBasicPassword)) {
			log.info("KMS Password not set");
			kmsLoginBasicPassword = kmsprops.get(KMS_LOGIN_BASIC_PASSWORD.replace('.', '_'));
		}
		// create KMS Keys API client
		Properties properties = new Properties();
		properties.setProperty("endpoint.url", kmsEndpointUrl);
		properties.setProperty("tls.policy.certificate.sha256", kmsTlsPolicyCertificateSha256);
		properties.setProperty("login.basic.username", kmsLoginBasicUsername);
		properties.setProperty("login.basic.password", kmsLoginBasicPassword);
		keys = new Keys(properties);

		log.debug("INIT of Keys complete");

	}

	public KeyContainer createKey() throws CryptographyException {
		CreateKeyRequest createKeyRequest = new CreateKeyRequest();
		createKeyRequest.setAlgorithm("AES");
		createKeyRequest.setKeyLength(128);
		createKeyRequest.setMode("OFB");
		Key createKeyResponse = keys.createKey(createKeyRequest);
		// Request server to transfer the new key to us (encrypted)
		String transferKeyPemResponse = keys.transferKey(createKeyResponse.getId().toString());
		// decrypt the requested key
		RsaPublicKeyProtectedPemKeyEnvelopeOpener opener = new RsaPublicKeyProtectedPemKeyEnvelopeOpener(
				wrappingKeyCertificate.getPrivateKey(), kmsLoginBasicUsername);
		SecretKey secretKey = (SecretKey) opener.unseal(Pem.valueOf(transferKeyPemResponse));
		// package all these into a single container
		KeyContainer keyContainer = new KeyContainer();
		keyContainer.secretKey = secretKey;
		keyContainer.url = createKeyResponse.getTransferLink();
		keyContainer.attributes = createKeyResponse;
		return keyContainer;

	}

	private byte[] transferKey(String keyId) throws CryptographyException {
		String transferKeyPemResponse = keys.transferKey(keyId);
		// decrypt the requested key
		RsaPublicKeyProtectedPemKeyEnvelopeOpener opener = new RsaPublicKeyProtectedPemKeyEnvelopeOpener(
				wrappingKeyCertificate.getPrivateKey(), kmsLoginBasicUsername);
		SecretKey secretKey = (SecretKey) opener.unseal(Pem.valueOf(transferKeyPemResponse));
		byte[] key = secretKey.getEncoded();
		return key;
	}

	public String getKeyFromKMS(String id) {
		try {
			// new String(TransferKey.getKey(id), "UTF-8");
			byte[] key = transferKey(id);
			return new sun.misc.BASE64Encoder().encode(key);
		} catch (Exception e) {
			// TODO Handle Error
			log.error("Error in getKeyFromKMS() method" + e);
		}
		return null;
	}

	public static String getProperties(String path) throws IOException {
		File customFile = new File(Folders.configuration() + File.separator + path);
		ConfigurationProvider provider = ConfigurationFactory.createConfigurationProvider(customFile);
		Configuration loadedConfiguration = provider.load();
		Map<String, String> map = new HashMap<String, String>();
		for (String key : loadedConfiguration.keys()) {
			String value = loadedConfiguration.get(key);
			map.put(key.replace(".", "_"), value);
		}
		JSONObject json = new JSONObject(map);
		return json.toString();
	}

	public boolean getAllKeys() {
		WebTarget target = keys.getTarget();
		Response response = target.path("/v1/keys").request().accept(MediaType.APPLICATION_JSON).get(Response.class);
		return (200 == response.getStatus());
	}

	public String createAndDeleteKey() {
		CreateKeyRequest createKeyRequest = new CreateKeyRequest();
		createKeyRequest.setAlgorithm("AES");
		createKeyRequest.setKeyLength(128);
		createKeyRequest.setMode("OFB");
		Key createKeyResponse = keys.createKey(createKeyRequest);
		UUID id = createKeyResponse.getId();
		if (id != null) {
			keys.deleteKey(id.toString());
		}
		return id.toString();
	}
}
