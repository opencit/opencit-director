/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.director.setup;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.director.api.SettingsKMSObject;
import com.intel.director.common.Constants;
import com.intel.director.common.DirectorUtil;
import com.intel.kms.client.jaxrs2.Users;
import com.intel.kms.user.User;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.core.PasswordVaultFactory;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;

/**
 *
 * @author rksavino
 */
public class DirectorEnvelopeKeyRegistration extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DirectorEnvelopeKeyRegistration.class);
    
    Map<String, String> kmsprops;
    
    // configuration keys
    private static final String DIRECTOR_ENVELOPE_ALIAS = "director.envelope.alias";
    private static final String DIRECTOR_KEYSTORE = "director.keystore";
    private static final String DIRECTOR_KEYSTORE_PASSWORD = "director.keystore.password";
    private static final String KMS_ENDPOINT_URL = "kms.endpoint.url";
    private static final String KMS_TLS_POLICY_CERTIFICATE_SHA1 = "kms.tls.policy.certificate.sha1";
    private static final String KMS_LOGIN_BASIC_USERNAME = "kms.login.basic.username";
    private static final String KMS_LOGIN_BASIC_PASSWORD = "kms.login.basic.password";
    
//    private String directorEnvelopeAlias;
//    private File keystoreFile;
    private Password keystorePassword;
    private PublicKey directorEnvelopePublicKey;
    private String kmsEndpointUrl;
    private String kmsTlsPolicyCertificateSha1;
    private String kmsLoginBasicUsername;
    private String kmsLoginBasicPassword;
    private Users users;
    private User user;
    
    @Override
    protected void configure() throws Exception {
    	kmsprops = new Gson().fromJson(DirectorUtil.getProperties(Constants.KMS_PROP_FILE), new TypeToken<HashMap<String, Object>>() {}.getType());
        String directorEnvelopeAlias = getConfiguration().get(DIRECTOR_ENVELOPE_ALIAS, "director-envelope");
        if( directorEnvelopeAlias == null || directorEnvelopeAlias.isEmpty() ) {
            configuration("Trust Director Envelope alias not configured");
        }
        
        String keystorePath = getConfiguration().get(DIRECTOR_KEYSTORE, Folders.configuration() + File.separator + "envelope-keystore.jks");
        File keystoreFile = new File(keystorePath);
        if( !keystoreFile.exists() ) {
            configuration("Keystore file does not exist");
            return;
        }
        
        // to avoid putting any passwords in the configuration file, we
        // get the password from the password vault
        try(PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
            if( passwordVault.contains(DIRECTOR_KEYSTORE_PASSWORD)) {
                keystorePassword = passwordVault.get(DIRECTOR_KEYSTORE_PASSWORD);
            }
        }
        if( keystorePassword == null || keystorePassword.toCharArray().length == 0) {
            configuration("Keystore password is not configured");
            return;
        }
        
        SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
        RsaCredentialX509 credential;
        try {
            credential = keystore.getRsaCredentialX509(directorEnvelopeAlias, keystorePassword);
            log.debug("Found key {}", credential.getCertificate().getSubjectX500Principal().getName());
            
            directorEnvelopePublicKey = credential.getPublicKey();
            if ( directorEnvelopePublicKey == null ) {
                configuration("Trust Director envelope public key is not configured");
            }
        } catch (FileNotFoundException e) {
            log.warn("Keystore does not contain the specified key [{}]", directorEnvelopeAlias);
            configuration("Keystore does not contain the specified key %s", directorEnvelopeAlias);
        }
        catch(java.security.UnrecoverableKeyException e) {
            log.debug("Incorrect password for existing key: {}", e.getMessage());
            configuration("Key must be recreated");
        }
        catch(NullPointerException e) {
            log.debug("Invalid certificate");
            configuration("Invalid certificate");
        }
        
        kmsEndpointUrl = kmsprops.get(KMS_ENDPOINT_URL.replace('.', '_'));
        if( kmsEndpointUrl == null || kmsEndpointUrl.isEmpty() ) {
            configuration("KMS endpoint URL not configured");
        }
        
        kmsTlsPolicyCertificateSha1 = kmsprops.get(KMS_TLS_POLICY_CERTIFICATE_SHA1.replace('.', '_'));
        if( kmsTlsPolicyCertificateSha1 == null || kmsTlsPolicyCertificateSha1.isEmpty() ) {
            configuration("KMS TLS policy certificate digest not configured");
        }
        
        kmsLoginBasicUsername = kmsprops.get(KMS_LOGIN_BASIC_USERNAME.replace('.', '_'));
        if( kmsLoginBasicUsername == null || kmsLoginBasicUsername.isEmpty() ) {
            configuration("KMS API username not configured");
        }
        
        kmsLoginBasicPassword = getConfiguration().get(KMS_LOGIN_BASIC_PASSWORD, null);
        if (kmsLoginBasicPassword == null || kmsLoginBasicPassword.isEmpty()) {
            try (PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
                if (passwordVault.contains(KMS_LOGIN_BASIC_PASSWORD)) {
                    kmsLoginBasicPassword = new String(passwordVault.get(KMS_LOGIN_BASIC_PASSWORD).toCharArray());
                }
            }
        }
        if( kmsLoginBasicPassword == null || kmsLoginBasicPassword.isEmpty() ) {
            configuration("KMS API password not configured");
        }
        
        // create KMS Users API client
        Properties properties = new Properties();
        if (kmsEndpointUrl != null && !kmsEndpointUrl.isEmpty()
                && kmsTlsPolicyCertificateSha1 != null && !kmsTlsPolicyCertificateSha1.isEmpty()
                && kmsLoginBasicUsername != null && !kmsLoginBasicUsername.isEmpty()
                && kmsLoginBasicPassword != null && !kmsLoginBasicPassword.isEmpty()) {
            properties.setProperty("endpoint.url", kmsEndpointUrl);
            properties.setProperty("tls.policy.certificate.sha1", kmsTlsPolicyCertificateSha1);
            properties.setProperty("login.basic.username", kmsLoginBasicUsername);
            properties.setProperty("login.basic.password", kmsLoginBasicPassword);
            users = new Users(properties);
        }
    }
    
    @Override
    protected void validate() throws Exception {
        try {
            user = users.findUserByUsername(kmsLoginBasicUsername);

            if (user == null) {
                //log.warn("KMS API user does not exist");
                validation("KMS API user does not exist");
            }
            if (directorEnvelopePublicKey == null || directorEnvelopePublicKey.getEncoded() == null) {
                log.error("New envelope public key is null");
                validation("New envelope public key is null");
            } else if (user.getTransferKey() == null || user.getTransferKey().getEncoded() == null) {
                //log.warn("Existing envelope public key is null");
                validation("Existing envelope public key is null");
            } else if (! Arrays.equals(user.getTransferKey().getEncoded(), directorEnvelopePublicKey.getEncoded())) {
                log.warn("New director envelope public key does not match the existing KMS user public key");
                validation("New director envelope public key does not match the existing KMS user public key");
            }   
        } catch (Exception e) {
            log.error("Attempt to retreive user from the KMS failed: {}", e.getMessage());
            validation("Attempt to retreive user from the KMS failed");
        }
    }
    
    @Override
    protected void execute() throws Exception {
        if (kmsLoginBasicPassword != null && kmsLoginBasicPassword.toCharArray().length != 0) {
            try (PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
                passwordVault.set(KMS_LOGIN_BASIC_PASSWORD, new Password(kmsLoginBasicPassword.toCharArray()));
            }
        }
        
        // save the settings in configuration;  DO NOT SAVE MASTER KEY
//        getConfiguration().set(KMS_ENDPOINT_URL, kmsEndpointUrl);
//        getConfiguration().set(KMS_TLS_POLICY_CERTIFICATE_SHA1, kmsTlsPolicyCertificateSha1);
//        getConfiguration().set(KMS_LOGIN_BASIC_USERNAME, kmsLoginBasicUsername);
        
        SettingsKMSObject settingskms= new SettingsKMSObject();
        settingskms.setKms_endpoint_url(kmsEndpointUrl);
        settingskms.setKms_login_basic_username(kmsLoginBasicUsername);
        settingskms.setKms_tls_policy_certificate_sha1(kmsTlsPolicyCertificateSha1);
        DirectorUtil.editProperties(Constants.KMS_PROP_FILE, settingskms.toString());
        
        try {
            if (user == null) {
                log.error("KMS client call failed to retrieve user");
                throw new IllegalArgumentException("KMS client call failed to retrieve user");
            }
            user.setTransferKey(directorEnvelopePublicKey);
            User edited = users.editUser(user);
            // confirmation:
            log.debug("Successfully registered Trust Director envelope key with KMS user {}", edited.getUsername());
        } catch (Exception e) {
            log.error("Attempt to set the director envelope key for the KMS user failed: {}", e.getMessage());
            throw new IllegalArgumentException("Attempt to set the director envelope key for the KMS user failed");
        }
    }
}
