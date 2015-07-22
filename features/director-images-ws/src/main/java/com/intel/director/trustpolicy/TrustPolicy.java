package com.intel.director.trustpolicy;


import com.intel.director.api.CreateTrustPolicyRequest;
import com.intel.director.api.CreateTrustPolicyResponse;
import com.intel.director.api.SignTrustPolicyResponse;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author GS-0681
 */
@Path("/trustpolicies")
public class TrustPolicy { 
    
    @POST
    @Produces(CryptoMediaType.APPLICATION_X_PEM_FILE)
    @Path("/")
    public CreateTrustPolicyResponse createTrustPolicy( @FormParam("") CreateTrustPolicyRequest createTrustPolicyRequest){
        return null;
    }
    
    @POST
    @Path("/trustPolicyId: [0-9a-zA-Z_-]+}/sign")
    public SignTrustPolicyResponse signTrustPolicy(@PathParam("trustPolicyId") String trustPolicyId, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse){
        return null;
    }
}
