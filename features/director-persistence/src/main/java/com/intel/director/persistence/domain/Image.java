/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.persistence.domain;

/**
 *
 * @author GS-0681
 */
public class Image extends BaseDomain {

    public ImageDeployment imageDeployment;
    public ImageFormat imageFormat;
    public String name;
    public String location;
    public User mountedBy;
    public Boolean deleted;
}
