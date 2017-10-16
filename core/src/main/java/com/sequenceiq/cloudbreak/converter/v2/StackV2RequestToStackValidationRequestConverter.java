package com.sequenceiq.cloudbreak.converter.v2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.sequenceiq.cloudbreak.api.model.HostGroupRequest;
import com.sequenceiq.cloudbreak.api.model.InstanceGroupRequest;
import com.sequenceiq.cloudbreak.api.model.StackValidationRequest;
import com.sequenceiq.cloudbreak.api.model.v2.ClusterV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.InstanceGroupV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.StackV2Request;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.Credential;
import com.sequenceiq.cloudbreak.service.blueprint.BlueprintService;
import com.sequenceiq.cloudbreak.service.credential.CredentialService;

@Component
public class StackV2RequestToStackValidationRequestConverter extends AbstractConversionServiceAwareConverter<StackV2Request, StackValidationRequest> {

    @Inject
    private CredentialService credentialService;

    @Inject
    private BlueprintService blueprintService;

    @Override
    public StackValidationRequest convert(StackV2Request source) {
        ClusterV2Request clusterRequest = source.getClusterRequest();
        StackValidationRequest stackValidationRequest = new StackValidationRequest();
        stackValidationRequest.setBlueprintId(clusterRequest.getAmbariRequest().getBlueprintId());
        stackValidationRequest.setCredentialId(source.getCredentialId());
        stackValidationRequest.setNetworkId(source.getNetworkId());
        stackValidationRequest.setPlatform(source.getCloudPlatform());
        if (!Strings.isNullOrEmpty(source.getCredentialName())) {
            Credential credential = credentialService.get(source.getCredentialName(), source.getAccount());
            stackValidationRequest.setCredentialName(source.getCredentialName());
            stackValidationRequest.setCredentialId(credential.getId());
        }
        if (!Strings.isNullOrEmpty(clusterRequest.getAmbariRequest().getBlueprintName())) {
            Blueprint blueprint = blueprintService.get(clusterRequest.getAmbariRequest().getBlueprintName(), source.getAccount());
            stackValidationRequest.setBlueprintId(blueprint.getId());
        }
        stackValidationRequest.setFileSystem(source.getClusterRequest().getFileSystem());
        stackValidationRequest.setHostGroups(hostGroupRequests(source.getInstanceGroups()));
        stackValidationRequest.setInstanceGroups(instanceGroupRequests(new HashSet<>(source.getInstanceGroups())));
        return stackValidationRequest;
    }

    private Set<HostGroupRequest> hostGroupRequests(List<InstanceGroupV2Request> instanceGroupV2Requests){
        return instanceGroupV2Requests.stream().map(instanceGroupV2Request -> getConversionService()
                .convert(instanceGroupV2Request, HostGroupRequest.class)).collect(Collectors.toSet());
    }

    private Set<InstanceGroupRequest> instanceGroupRequests(Set<InstanceGroupV2Request> instanceGroupV2Requests){
        return instanceGroupV2Requests.stream().map(instanceGroupV2Request -> getConversionService()
                .convert(instanceGroupV2Request, InstanceGroupRequest.class)).collect(Collectors.toSet());
    }
}
