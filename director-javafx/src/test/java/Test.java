/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.intel.mtwilson.director.javafx.utils.GenerateTrustPolicy;
import com.intel.mtwilson.director.javafx.utils.Sha256Digest;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.intel.mtwilson.director.javafx.utils.Sha256Digest;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
/**
 *
 * @author boskisha
 */
public class Test {
    
    public Test() {
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
    @Test
    public void testHash() throws Exception{
        String filepath = "C:\\Users\\boskisha\\Downloads\\TrustPolicy-201502261733.xml";
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String result = new GenerateTrustPolicy().computeHash(md, new File(filepath));
        String content ="hi\n" +
"test";
        String stringHash = new GenerateTrustPolicy().computeHash(md, content);
        System.out.println("Hash of a file is:"+result);
        System.out.println("Hash of a string is:"+stringHash);
        System.out.println(Sha256Digest.digestOf(content.getBytes("UTF-8")).toHexString());
        System.out.println(Sha256Digest.digestOf(FileUtils.readFileToByteArray(new File(filepath))).toHexString());
        System.out.println(Hex.encodeHexString(content.getBytes("UTF-8")));
        System.out.println(Hex.encodeHexString(FileUtils.readFileToByteArray(new File(filepath))));
        
    }
@Test
    public void testVirtualPcrExtension() { 
        Sha256Digest pcr = new Sha256Digest(new byte[] {0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0, 0,0});
        // repeat for each measurement
        pcr = pcr.extend(Sha256Digest.valueOfHex("72282991e99088498fa5a8f0497e474fb69dc5d94a2a7ff862f2ddd17e53b5fc").toByteArray());
        // final value:
        System.out.println("pcr value = {}"+ pcr.toHexString());
    }
    @Test
    public void testComputeHash() throws NoSuchAlgorithmException, Exception{
        String input="hi\n" +
"hello";
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String r1 = new GenerateTrustPolicy().computeHash(md, new File("C:\\Users\\boskisha\\Downloads\\tem.java"));
        String r2 = new GenerateTrustPolicy().computeHash(md, input);
        String r3 = new GenerateTrustPolicy().createSha256(new File("C:\\Users\\boskisha\\Downloads\\tem.java"));
        System.out.println(r1+" \n"+r2+"\n"+r3);
    }
    // TODO add Test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
