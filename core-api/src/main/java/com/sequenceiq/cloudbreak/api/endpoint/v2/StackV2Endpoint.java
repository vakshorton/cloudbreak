package com.sequenceiq.cloudbreak.api.endpoint.v2;


import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sequenceiq.cloudbreak.api.endpoint.common.StackEndpoint;
import com.sequenceiq.cloudbreak.api.model.StackResponse;
import com.sequenceiq.cloudbreak.api.model.v2.StackV2Request;
import com.sequenceiq.cloudbreak.doc.ContentType;
import com.sequenceiq.cloudbreak.doc.ControllerDescription;
import com.sequenceiq.cloudbreak.doc.Notes;
import com.sequenceiq.cloudbreak.doc.OperationDescriptions;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/v2/stacks")
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "/v2/stacks", description = ControllerDescription.STACK_DESCRIPTION, protocols = "http,https")
public interface StackV2Endpoint extends StackEndpoint {


    @POST
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = OperationDescriptions.StackOpDescription.POST_PRIVATE, produces = ContentType.JSON, notes = Notes.STACK_NOTES, nickname = "postPrivateStackV2")
    StackResponse postPrivate(@Valid StackV2Request stackRequest) throws Exception;

    @POST
    @Path("account")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = OperationDescriptions.StackOpDescription.POST_PUBLIC, produces = ContentType.JSON, notes = Notes.STACK_NOTES, nickname = "postPublicStackV2")
    StackResponse postPublic(@Valid StackV2Request stackRequest) throws Exception;

}
