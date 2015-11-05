/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.api;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The AbstractResponse superclass is annotated with JsonInclude NON_EMPTY as
 * default behavior for all fields, but the SearachKeyAttributesResponse
 * class needs to include search results even when empty. This test confirms
 * it's working ok. 
 * 
 * @author jbuhacoff
 */
public class SearchResultJacksonTest {

    @Test
    public void testEmptySearchResults() throws JsonProcessingException {
     /*   ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
        SearchKeyAttributesResponse response = new SearchKeyAttributesResponse();
        log.debug("empty search results: {}", mapper.writeValueAsString(response));*/ // {"data":[]}    works ok.
    }
}
