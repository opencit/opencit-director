/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.repository;

//import java.util.Collection;
//import java.util.Map;

/**
 * TODO: there is overlap between this interface and the repository used 
 * in the v2 jaxrs APIs (mtwilson-repository-api);  
 * maybe refactor into mtwilson-util-repository?   the mtwilson-repository-api
 * has an abstraction problem because it has create(T item) which means 
 * there can be no generic implementation that can get a locator out of item
 * and know what to do with it.  it maybe should have been create(item, locator)
 * and store(item, locator)  to match  retrieve(locator) and delete(locator).
 * 
 * @author jbuhacoff
 */
public interface Repository<T,L> {
    void create(L id, T item);
    void store(L id, T item);
    T retrieve(L id);
    void delete(L id);
//    Collection<T> search(Map<String,String> criteria);  // should be in SearchableRepository interface
}
