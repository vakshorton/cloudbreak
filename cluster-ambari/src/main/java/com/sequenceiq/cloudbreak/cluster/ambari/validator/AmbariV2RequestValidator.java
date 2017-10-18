package com.sequenceiq.cloudbreak.cluster.ambari.validator;

import com.sequenceiq.cloudbreak.api.model.v2.AmbariV2Request;
import com.sequenceiq.cloudbreak.cluster.ClusterValidator;
import com.sequenceiq.cloudbreak.cluster.ambari.AmbariValidationException;

public class AmbariV2RequestValidator implements ClusterValidator<AmbariV2Request, AmbariValidationException> {

    @Override
    public void validate(AmbariV2Request ambariV2Request) throws AmbariValidationException {

    }

    @Override
    public Class getRelatedClass() {
        return AmbariV2Request.class;
    }
}
