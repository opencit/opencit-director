/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.director.features.director.kms;

import java.net.URL;

import javax.crypto.SecretKey;

import com.intel.kms.ws.v2.api.Key;

/**
 *
 * @author boskisha
 */
public class KeyContainer {
    public SecretKey secretKey;
    public URL url;
    public Key attributes;
}
