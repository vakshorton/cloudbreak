package com.sequenceiq.cloudbreak.api.endpoint.v2;


import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sequenceiq.cloudbreak.api.endpoint.common.StackEndpoint;
import com.sequenceiq.cloudbreak.api.model.AmbariAddressJson;
import com.sequenceiq.cloudbreak.api.model.AutoscaleStackResponse;
import com.sequenceiq.cloudbreak.api.model.CertificateResponse;
import com.sequenceiq.cloudbreak.api.model.PlatformVariantsJson;
import com.sequenceiq.cloudbreak.api.model.StackResponse;
import com.sequenceiq.cloudbreak.api.model.StackValidationRequest;
import com.sequenceiq.cloudbreak.api.model.UpdateStackJson;
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
    @ApiOperation(value = OperationDescriptions.StackOpDescription.POST_PRIVATE, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "postPrivateStackV2")
    StackResponse postPrivate(@Valid StackV2Request stackRequest) throws Exception;

    @POST
    @Path("account")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = OperationDescriptions.StackOpDescription.POST_PUBLIC, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "postPublicStackV2")
    StackResponse postPublic(@Valid StackV2Request stackRequest) throws Exception;

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.GET_PRIVATE, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "getPrivatesStackV2")
    Set<StackResponse> getPrivates();

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.GET_PUBLIC, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "getPublicsStackV2")
    Set<StackResponse> getPublics();

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.GET_PRIVATE_BY_NAME, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "getPrivateStackV2")
    StackResponse getPrivate(@PathParam("name") String name, @QueryParam("entry") Set<String> entries);

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.GET_PUBLIC_BY_NAME, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "getPublicStackV2")
    StackResponse getPublic(@PathParam("name") String name, @QueryParam("entry") Set<String> entries);

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.GET_BY_ID, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "getStackV2")
    StackResponse get(@PathParam("id") Long id, @QueryParam("entry") Set<String> entries);

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.DELETE_PUBLIC_BY_NAME, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "deletePublicStackV2")
    void deletePublic(@PathParam("name") String name, @QueryParam("forced") @DefaultValue("false") Boolean forced,
            @QueryParam("deleteDependencies") @DefaultValue("false") Boolean deleteDependencies);

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.DELETE_PRIVATE_BY_NAME, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "deletePrivateStackV2")
    void deletePrivate(@PathParam("name") String name, @QueryParam("forced") @DefaultValue("false") Boolean forced,
            @QueryParam("deleteDependencies") @DefaultValue("false") Boolean deleteDependencies);

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.DELETE_BY_ID, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "deleteStackV2")
    void delete(@PathParam("id") Long id, @QueryParam("forced") @DefaultValue("false") Boolean forced,
            @QueryParam("deleteDependencies") @DefaultValue("false") Boolean deleteDependencies);

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.PUT_BY_ID, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "putStackV2")
    Response put(@PathParam("id") Long id, @Valid UpdateStackJson updateRequest);

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.GET_STATUS_BY_ID, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "statusStackV2")
    Map<String, Object> status(@PathParam("id") Long id);

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.GET_PLATFORM_VARIANTS, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "variantsStackV2")
    PlatformVariantsJson variants();

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.DELETE_INSTANCE_BY_ID, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "deleteInstanceStackV2")
    Response deleteInstance(@PathParam("stackId") Long stackId, @PathParam("instanceId") String instanceId);

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.GET_STACK_CERT, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "getCertificateStackV2")
    CertificateResponse getCertificate(@PathParam("id") Long stackId);

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.VALIDATE, produces = ContentType.JSON, notes = Notes.STACK_NOTES, nickname = "validateStackV2")
    Response validate(@Valid StackValidationRequest stackValidationRequest);

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.GET_BY_AMBARI_ADDRESS, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "getStackForAmbariV2")
    StackResponse getStackForAmbari(@Valid AmbariAddressJson json);

    @Override
    @ApiOperation(value = OperationDescriptions.StackOpDescription.GET_ALL, produces = ContentType.JSON,
            notes = Notes.STACK_NOTES, nickname = "getAllStackV2")
    Set<AutoscaleStackResponse> getAllForAutoscale();
}
