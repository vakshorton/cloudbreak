package com.sequenceiq.cloudbreak.converter.v2;

import java.util.HashSet;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.ConstraintJson;
import com.sequenceiq.cloudbreak.api.model.HostGroupRequest;
import com.sequenceiq.cloudbreak.api.model.v2.InstanceGroupV2Request;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;

@Component
public class InstanceGroupV2RequestToHostGroupRequestConverter extends AbstractConversionServiceAwareConverter<InstanceGroupV2Request, HostGroupRequest> {

    @Override
    public HostGroupRequest convert(InstanceGroupV2Request instanceGroupV2Request) {
        HostGroupRequest hostGroupRequest = new HostGroupRequest();
        hostGroupRequest.setName(instanceGroupV2Request.getGroup());
        hostGroupRequest.setRecipeNames(instanceGroupV2Request.getRecipeNames());
        hostGroupRequest.setRecipes(new HashSet<>());
        ConstraintJson constraintJson = new ConstraintJson();
        constraintJson.setHostCount(instanceGroupV2Request.getNodeCount());
        constraintJson.setInstanceGroupName(instanceGroupV2Request.getGroup());
        hostGroupRequest.setConstraint(constraintJson);
        hostGroupRequest.setRecipeIds(instanceGroupV2Request.getRecipeIds());
        hostGroupRequest.setRecoveryMode(instanceGroupV2Request.getRecoveryMode());
        return hostGroupRequest;
    }
}
