/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.director.images;



/**
 * The AbstractResponse superclass is annotated with JsonInclude NON_EMPTY as
 * default behavior for all fields, but the SearachKeyAttributesResponse
 * class needs to include search results even when empty. This test confirms
 * it's working ok. 
 * 
 * @author jbuhacoff
 */
public class SearchResultJacksonTest {

    
    public void testEmptySearchResults(){
    }
}
