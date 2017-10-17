package com.sequenceiq.cloudbreak.cluster.ambari.validator;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cluster.ClusterValidator;
import com.sequenceiq.cloudbreak.cluster.ambari.AmbariValidationException;
import com.sequenceiq.cloudbreak.cluster.ambari.domain.UpscaleValidatorRequest;

@Service
public class StackScaleBlueprintValidator implements ClusterValidator<UpscaleValidatorRequest, AmbariValidationException> {

    @Inject
    private BlueprintValidator blueprintValidator;

    @Override
    public void validate(UpscaleValidatorRequest request) throws AmbariValidationException {
        blueprintValidator.validateHostGroupScalingRequest(request.getBlueprint(), request.getHostGroup().orElse(null), request.getAdjustment());
    }

    @Override
    public Class getRelatedClass() {
        return UpscaleValidatorRequest.class;
    }
}
