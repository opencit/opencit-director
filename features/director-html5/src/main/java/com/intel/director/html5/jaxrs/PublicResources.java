/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.director.html5.jaxrs;

/**
 * Serves static content from classpath resources. The resources must be under a
 * "public" directory in order to be found. Example jar structure:
 * <pre>
 * META-INF/services
 * public/*
 * com/example/*.class
 * </pre>
 *
 * To serve files from the filesystem, use the default servlet that comes with
 * Jetty or any other web server.
 *
 * @author jbuhacoff
 */
public class PublicResources {
}
