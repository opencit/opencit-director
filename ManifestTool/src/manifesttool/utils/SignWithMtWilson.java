package manifesttool.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import java.io.BufferedReader;
import manifesttool.utils.GenerateHash;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import manifesttool.ui.Constants;
import manifesttool.ui.UserConfirmation;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author root
 */
public class SignWithMtWilson {
    private static final Logger logger = Logger.getLogger(SignWithMtWilson.class.getName());
    private String mtWilsonIP;
    private String mtWilsonPort;
    
    private String mtWilsonUserName = ConfigProperties.getProperty(Constants.Mt_WILSON_USER_NAME);
    private String mtWilsonPassword = ConfigProperties.getProperty(Constants.Mt_WILSON_PASSWORD);
    
    // Set FileHandler for logger
    static {
        LoggerUtility.setHandler(logger);
    }
    public String signManifest(String ip, String port, String imageID, String fileHash) {
        this.mtWilsonIP = ip;
        this.mtWilsonPort = port;
        String response = getMtWilsonResponse(imageID, fileHash);
        logger.info("Signed the manifest with Mt. Wilson\n" + "Mt. Wilson response is :\n" + response);
        
        return response;
        
    }

    private String getMtWilsonResponse(String imageID, String fileHash) {
        String mtWisontResponse = null;
        try {
            System.out.println("Manifest File Hash is : " + fileHash);
            
            String url = "https://" + this.mtWilsonIP + ":" + this.mtWilsonPort + "/mtwilson/v2/manifest-signature";
            
            ManifestSignatureInput input = new ManifestSignatureInput();
            input.setManifestHash(fileHash);
            input.setVmImageId(imageID);
            
            ObjectMapper mapper = new ObjectMapper();
            // System.out.println("\n" + mapper.writeValueAsString(input) + "\n");
            logger.info("Mt Wilson input : " + mapper.writeValueAsString(input));
            
            String json = mapper.writeValueAsString(input);
            JSONObject jsonObj = new JSONObject(json);
            
            String xml = "<manifest_signature_input>"+XML.toString(jsonObj)+"</manifest_signature_input>";
            logger.info("Manifest signature request to MtW \n Request Body is : " + xml);
            
                
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(url);
            HttpEntity entity = new ByteArrayEntity(xml.getBytes("UTF-8"));
                
            postRequest.setEntity(entity);
            postRequest.setHeader("Content-Type", "application/xml");
            postRequest.setHeader("Accept", "application/xml");
            if (mtWilsonUserName == null || mtWilsonPassword == null) {
                logger.warning("Mt Wilson credentials are not present in property file");
                return null;
            }
            String encryptedUserNameAndPassword = new FileUtilityOperation().base64Encode(mtWilsonUserName + ":" + mtWilsonPassword);
            postRequest.setHeader("Authorization", "Basic " + encryptedUserNameAndPassword);
            HttpResponse response = httpClient.execute(postRequest);
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.log(Level.SEVERE, null, new RuntimeException(response.getStatusLine().toString()));
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            String output = null;
            StringBuffer sb = new StringBuffer();
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
                
            //mapper = new ObjectMapper();
            //mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
            //jsonObj = new JSONObject(sb.toString());
            //mtWisontResponse = XML.toString(jsonObj);
            mtWisontResponse = sb.toString();
            System.out.println(mtWisontResponse);
                
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, null, e);
            return null;
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
            return null;
        } catch (JSONException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
            
/*        String response = "<manifest_signature>" +
                    "<vm_image_id>123456</vm_image_id>" +
                    "<manifest_hash>" + fileHash + "</manifest_hash>" +
                    "<customer_id>982734</customer_id>" +
                    "<signature>abcdef01234567890abcdef01234567890</signature>" +
                    "<document><vm_manifest><customer_id>1234</customer_id><image_id>1235289</image_id><manifest_hash>aaaaaa</manifest_hash></vm_manifest></document>" +
                    "</manifest_signature>";
*/
        return mtWisontResponse;
    }  
}
