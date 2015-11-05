/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.director.setup;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.core.PasswordVaultFactory;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;

/**
 *
 * @author rksavino
 */
public class DirectorEnvelopeKey extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DirectorEnvelopeKey.class);

    // configuration keys
    private static final String DIRECTOR_ENVELOPE_ALIAS = "director.envelope.alias";
    private static final String DIRECTOR_ENVELOPE_CERT_DN = "director.envelope.cert.dn";
    private static final String DIRECTOR_KEYSTORE = "director.keystore";
    private static final String DIRECTOR_KEYSTORE_PASSWORD = "director.keystore.password";
    
    private static final String directorEnvelopeAlias = "director-envelope";
    private String directorEnvelopeCertDN;
    private File keystoreFile;
    private Password keystorePassword;
    private String dn;
    
    @Override
    protected void configure() throws Exception {
        directorEnvelopeCertDN = getConfiguration().get(DIRECTOR_ENVELOPE_CERT_DN, null);
        
        String keystorePath = getConfiguration().get(DIRECTOR_KEYSTORE, null);
        if( keystorePath == null ) {
            keystorePath = Folders.configuration() + File.separator + "envelope-keystore.jks";
        }
        keystoreFile = new File(keystorePath);
        
        // to avoid putting any passwords in the configuration file, we
        // get the password from the password vault
        try(PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
            if( passwordVault.contains(DIRECTOR_KEYSTORE_PASSWORD)) {
                keystorePassword = passwordVault.get(DIRECTOR_KEYSTORE_PASSWORD);
            }
        }
        
        // if we already have a keystore file, then we need to know the existing keystore password
        // otherwise it's ok for password to be missing (new install, or creating new keystore) and
        // we'll generate one in execute()
        if( keystoreFile.exists() ) {
            if( keystorePassword == null || keystorePassword.toCharArray().length == 0 ) { configuration("Keystore password has not been generated"); }
        }
        
        if( directorEnvelopeCertDN == null ) {
            directorEnvelopeCertDN = String.format("director.%s", new UUID().toHexString());
        }
        
        // if a specific DN is not configured, use "director" with a random UUID to avoid collisions when multiple director instances
        // register with the same kms
        dn = getConfiguration().get(DIRECTOR_ENVELOPE_CERT_DN, String.format("CN=%s", directorEnvelopeCertDN));
        
        if( dn == null || dn.isEmpty() ) { configuration("DN not configured"); }
    }
    
    @Override
    protected void validate() throws Exception {
        if( !keystoreFile.exists() ) {
            validation("Keystore file was not created");
            return;
        }
        if( keystorePassword == null || keystorePassword.toCharArray().length == 0) {
            validation("Keystore password has not been generated");
            return;
        }
        SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
        RsaCredentialX509 credential;
        try {
            credential = keystore.getRsaCredentialX509(directorEnvelopeAlias, keystorePassword);
            log.debug("Found key {}", credential.getCertificate().getSubjectX500Principal().getName());
        } catch (FileNotFoundException e) {
            log.warn("Keystore does not contain the specified key [{}]", directorEnvelopeAlias);
            validation("Keystore does not contain the specified key %s", directorEnvelopeAlias);
        }
        catch(java.security.UnrecoverableKeyException e) {
            log.debug("Incorrect password for existing key; will create new key: {}", e.getMessage());
            validation("Key must be recreated");
        }
        catch(NullPointerException e) {
            log.debug("Invalid certificate");
            validation("Certificate must be recreated");
        }
    }
    
    @Override
    protected void execute() throws Exception {
        if (keystorePassword == null || keystorePassword.toCharArray().length == 0) {
            // generate a keystore password
            keystorePassword = new Password(RandomUtil.randomBase64String(16).toCharArray());
            log.info("Generated random keystore password");
        }

        // ensure directories exist
        if (!keystoreFile.getParentFile().exists()) {
            if (keystoreFile.getParentFile().mkdirs()) {
                log.debug("Created directory {}", keystoreFile.getParentFile().getAbsolutePath());
            }
        }

        // create the keypair
        KeyPair keypair = RsaUtil.generateRsaKeyPair(2048);
        X509Builder builder = X509Builder.factory()
                .selfSigned(dn, keypair)
                .expires(3650, TimeUnit.DAYS)
                .keyUsageKeyEncipherment();
        // NOTE:  right now we are creating a self-signed cert but if we have
        //        the kms api url, username, and password, we could submit
        //        a certificate signing request there and have our cert signed
        //        by the kms's ca, and then the ssl policy for this director in 
        //        kms could be "signed by trusted ca" instead of
        //        "that specific cert"

        X509Certificate cert = builder.build();
        
        // look for an existing keypair and delete it
        SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
        try {
            String alias = directorEnvelopeAlias;
            List<String> aliases = Arrays.asList(keystore.aliases());
            if( aliases.contains(alias) ) {
                keystore.delete(alias);
            }
        }
        catch(KeyStoreException | KeyManagementException e) {
            log.debug("Cannot remove existing tls keypair", e);
        }
        // store it in the keystore
        keystore.addKeyPairX509(keypair.getPrivate(), cert, directorEnvelopeAlias, keystorePassword);
        keystore.save();
        
        // save the password to the password vault
        try (PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
            passwordVault.set(DIRECTOR_KEYSTORE_PASSWORD, keystorePassword);
        }

        // save the settings in configuration;  DO NOT SAVE MASTER KEY
        getConfiguration().set(DIRECTOR_ENVELOPE_ALIAS, directorEnvelopeAlias);
        getConfiguration().set(DIRECTOR_ENVELOPE_CERT_DN, directorEnvelopeCertDN);
        getConfiguration().set(DIRECTOR_KEYSTORE, keystoreFile.getAbsolutePath());
    }
}
