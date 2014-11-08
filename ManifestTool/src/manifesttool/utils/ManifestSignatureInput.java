/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package manifesttool.utils;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 *
 * @author root
 */

@JacksonXmlRootElement(localName="manifest_signature_input")
public class ManifestSignatureInput {
    private String vmImageId;
    private String manifestHash;
 
    //getter and setter methods  
    public String getVmImageId() {
        return this.vmImageId;
    }
        
    public void setVmImageId(String vm_image_id) {
        this.vmImageId = vm_image_id;
    }
        
    public String getManifestHash() {
        return this.manifestHash;
    }
        
    public void setManifestHash(String manifest_hash) {
        this.manifestHash = manifest_hash;
    }
}
