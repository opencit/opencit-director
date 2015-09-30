/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.intel.dcsg.cpg.crypto.file.RsaPublicKeyProtectedPemKeyEnvelopeOpener;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.kms.api.CreateKeyRequest;
import com.intel.kms.client.jaxrs2.Keys;
import com.intel.kms.client.jaxrs2.Users;
import com.intel.kms.user.User;
import com.intel.kms.ws.v2.api.Key;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import java.util.Properties;
import javax.crypto.SecretKey;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author boskisha
 */
public class KmsClientTest {
    
    public KmsClientTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.class); 
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

    @Test
    public void testKmsPassword() throws Exception {
        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.class);
        Properties properties = new Properties();
        properties.setProperty("endpoint.url", "https://10.1.68.58");
        properties.setProperty("tls.policy.certificate.sha1", "3d006b84f7e4c82ad04da777d26b476158b4fae2 ");
        properties.setProperty("login.basic.username", "td");
        properties.setProperty("login.basic.password", "password");
        Keys keys = new Keys(properties);

        // Request server to create a new key        
        CreateKeyRequest createKeyRequest = new CreateKeyRequest();
        createKeyRequest.setAlgorithm("AES");
        createKeyRequest.setKeyLength(128);
        createKeyRequest.setMode("OFB");
        Key createKeyResponse = keys.createKey(createKeyRequest);
        // Request server to transfer the new key to us (encrypted)
        String transferKeyPemResponse = keys.transferKey(createKeyResponse.getId().toString());
        // decrypt the requested key
//        RsaPublicKeyProtectedPemKeyEnvelopeOpener opener = new RsaPublicKeyProtectedPemKeyEnvelopeOpener(wrappingKeyCertificate.getPrivateKey(), kmsLoginBasicUsername);
//        SecretKey secretKey = (SecretKey) opener.unseal(Pem.valueOf(transferKeyPemResponse));
//        // package all these into a single container
//        KeyContainer keyContainer = new KeyContainer();
//        keyContainer.secretKey = secretKey;
//        keyContainer.url = createKeyResponse.getTransferLink();
//        keyContainer.attributes = createKeyResponse;
//        return keyContainer;
        
    }
    @Test
    public void testKmsUser() throws Exception{
        Properties properties = new Properties();        
        properties.setProperty("endpoint.url", "https://10.1.68.58");
        properties.setProperty("tls.policy.certificate.sha1", "3d006b84f7e4c82ad04da777d26b476158b4fae2");
        properties.setProperty("login.basic.username", "admin");
        properties.setProperty("login.basic.password", "password");
        Users users = new Users(properties);
        User user = users.findUserByUsername("admin");
        System.out.println(user);
    }   
}
