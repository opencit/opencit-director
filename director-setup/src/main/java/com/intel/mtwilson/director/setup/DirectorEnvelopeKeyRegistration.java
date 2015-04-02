/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.director.setup;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.kms.client.jaxrs2.Users;
import com.intel.kms.user.User;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.PasswordVaultFactory;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;
import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Properties;

/**
 *
 * @author rksavino
 */
public class DirectorEnvelopeKeyRegistration extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DirectorEnvelopeKeyRegistration.class);
    
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
        String directorEnvelopeAlias = getConfiguration().get(DIRECTOR_ENVELOPE_ALIAS, "director-envelope");
        if( directorEnvelopeAlias == null || directorEnvelopeAlias.isEmpty() ) {
            configuration("Trust Director Envelope alias not configured");
        }
        
        String keystorePath = getConfiguration().get(DIRECTOR_KEYSTORE, Folders.configuration() + File.separator + "keystore.jks");
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
        
        kmsEndpointUrl = getConfiguration().get(KMS_ENDPOINT_URL, null);
        if( kmsEndpointUrl == null || kmsEndpointUrl.isEmpty() ) {
            configuration("KMS endpoint URL not configured");
        }
        
        kmsTlsPolicyCertificateSha1 = getConfiguration().get(KMS_TLS_POLICY_CERTIFICATE_SHA1, null);
        if( kmsTlsPolicyCertificateSha1 == null || kmsTlsPolicyCertificateSha1.isEmpty() ) {
            configuration("KMS TLS policy certificate digest not configured");
        }
        
        kmsLoginBasicUsername = getConfiguration().get(KMS_LOGIN_BASIC_USERNAME, null);
        if( kmsLoginBasicUsername == null || kmsLoginBasicUsername.isEmpty() ) {
            configuration("KMS API username not configured");
        }
        
        try(PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
            if( passwordVault.contains(KMS_LOGIN_BASIC_PASSWORD)) {
                kmsLoginBasicPassword = passwordVault.get(KMS_LOGIN_BASIC_PASSWORD).toString();
            }
        }
        kmsLoginBasicPassword = getConfiguration().get(KMS_LOGIN_BASIC_PASSWORD, kmsLoginBasicPassword);
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
            user = users.findUserByUsername(kmsLoginBasicUsername);

            if (user == null) {
                configuration("KMS API user does not exist");
            }
        }
    }
    
    @Override
    protected void validate() throws Exception {
//        user = users.findUserByUsername(kmsLoginBasicUsername);
//        
//        if ( user == null ) {
//            validation("KMS API user does not exist");
//        }
    }
    
    @Override
    protected void execute() throws Exception {
        if (kmsLoginBasicPassword != null && kmsLoginBasicPassword.toCharArray().length != 0) {
            try (PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
                passwordVault.set(KMS_LOGIN_BASIC_PASSWORD, new Password(kmsLoginBasicPassword.toCharArray()));
            }
        }
        
        user.setTransferKey(directorEnvelopePublicKey);
        User edited = users.editUser(user);
        // confirmation:
        log.debug("Successfully registered Trust Director envelope key with KMS user {}", edited.getUsername());
        
        // save the settings in configuration;  DO NOT SAVE MASTER KEY
        getConfiguration().set(KMS_ENDPOINT_URL, kmsEndpointUrl);
        getConfiguration().set(KMS_TLS_POLICY_CERTIFICATE_SHA1, kmsTlsPolicyCertificateSha1);
        getConfiguration().set(KMS_LOGIN_BASIC_USERNAME, kmsLoginBasicUsername);
    }
}
