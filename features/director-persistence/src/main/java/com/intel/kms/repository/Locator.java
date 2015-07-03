/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.repository;

/**
 * A type transformation specific to locating items in a repository.
 * Given an item of type T, the locator is responsible for creating
 * a corresponding instance of key type K which can be used by the
 * repository to locate the item.
 * 
 * An example might be Locator<CustomType,UUID> where the locator
 * implements UUID locate(CustomType item) and knows which attribute
 * of CustomType contains a UUID or a UUID representation that can
 * be instantiated as a UUID. The repository in this example
 * would know how to locate
 * items by UUID.
 * 
 * @author jbuhacoff
 */
public interface Locator<T,K> {
    K locate(T item);
}
