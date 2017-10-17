package com.sequenceiq.cloudbreak.api.endpoint.common;


import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sequenceiq.cloudbreak.api.model.AmbariAddressJson;
import com.sequenceiq.cloudbreak.api.model.AutoscaleStackResponse;
import com.sequenceiq.cloudbreak.api.model.CertificateResponse;
import com.sequenceiq.cloudbreak.api.model.PlatformVariantsJson;
import com.sequenceiq.cloudbreak.api.model.StackResponse;
import com.sequenceiq.cloudbreak.api.model.StackValidationRequest;
import com.sequenceiq.cloudbreak.api.model.UpdateStackJson;

public interface StackEndpoint {

    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    Set<StackResponse> getPrivates();

    @GET
    @Path("account")
    @Produces(MediaType.APPLICATION_JSON)
    Set<StackResponse> getPublics();

    @GET
    @Path("user/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    StackResponse getPrivate(@PathParam("name") String name, @QueryParam("entry") Set<String> entries);

    @GET
    @Path("account/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    StackResponse getPublic(@PathParam("name") String name, @QueryParam("entry") Set<String> entries);

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    StackResponse get(@PathParam("id") Long id, @QueryParam("entry") Set<String> entries);

    @DELETE
    @Path("account/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    void deletePublic(@PathParam("name") String name, @QueryParam("forced") @DefaultValue("false") Boolean forced,
            @QueryParam("deleteDependencies") @DefaultValue("false") Boolean deleteDependencies);

    @DELETE
    @Path("user/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    void deletePrivate(@PathParam("name") String name, @QueryParam("forced") @DefaultValue("false") Boolean forced,
            @QueryParam("deleteDependencies") @DefaultValue("false") Boolean deleteDependencies);

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    void delete(@PathParam("id") Long id, @QueryParam("forced") @DefaultValue("false") Boolean forced,
            @QueryParam("deleteDependencies") @DefaultValue("false") Boolean deleteDependencies);

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Response put(@PathParam("id") Long id, @Valid UpdateStackJson updateRequest);

    @GET
    @Path("{id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> status(@PathParam("id") Long id);

    @GET
    @Path("platformVariants")
    @Produces(MediaType.APPLICATION_JSON)
    PlatformVariantsJson variants();

    @DELETE
    @Path("{stackId}/{instanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response deleteInstance(@PathParam("stackId") Long stackId, @PathParam("instanceId") String instanceId);

    @GET
    @Path("{id}/certificate")
    @Produces(MediaType.APPLICATION_JSON)
    CertificateResponse getCertificate(@PathParam("id") Long stackId);

    @POST
    @Path("validate")
    @Produces(MediaType.APPLICATION_JSON)
    Response validate(@Valid StackValidationRequest stackValidationRequest);

    @POST
    @Path("ambari")
    @Produces(MediaType.APPLICATION_JSON)
    StackResponse getStackForAmbari(@Valid AmbariAddressJson json);

    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    Set<AutoscaleStackResponse> getAllForAutoscale();
}
