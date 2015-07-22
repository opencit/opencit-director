/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.director.images.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Siddharth
 *
 */
@Provider
public class ExceptionHttpStatusResolver implements
        ExceptionMapper {

    @Override
    public Response toResponse(Throwable e) {
        Response.Status httpStatus = Response.Status.INTERNAL_SERVER_ERROR;

        if (e instanceof DirectorException) {
            httpStatus = Response.Status.BAD_REQUEST;
        }

        return Response.status(httpStatus).entity(e.getMessage())
                .build();
    }
}
