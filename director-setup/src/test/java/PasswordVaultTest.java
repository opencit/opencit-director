/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */

import static com.intel.mtwilson.configuration.ConfigurationFactory.getConfiguration;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.spec.InvalidKeySpecException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.mtwilson.core.PasswordVaultFactory;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;

/**
 *
 * @author rksavino
 */
public class PasswordVaultTest {
    
    public PasswordVaultTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    @Test
    public void testPasswordVault() throws KeyStoreException, IOException, InvalidKeySpecException {
        String KMS_LOGIN_BASIC_PASSWORD = "kms.login.basic.password";
        String kmsLoginBasicPassword = "password";

        try (PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
            passwordVault.set(KMS_LOGIN_BASIC_PASSWORD, new Password(kmsLoginBasicPassword.toCharArray()));
        }
        try (PasswordKeyStore passwordVault = PasswordVaultFactory.getPasswordKeyStore(getConfiguration())) {
            if (passwordVault.contains(KMS_LOGIN_BASIC_PASSWORD)) {
                String newKmsLoginBasicPassword = new String(passwordVault.get(KMS_LOGIN_BASIC_PASSWORD).toCharArray());
                System.out.println("KMS LOGIN BASIC PASSWORD: " + newKmsLoginBasicPassword);
            }
        }
        
        
    }
}
