package com.sequenceiq.cloudbreak.cluster.ambari.validator;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cluster.ClusterValidator;
import com.sequenceiq.cloudbreak.cluster.ambari.AmbariValidationException;
import com.sequenceiq.cloudbreak.cluster.ambari.domain.KerberosValidationRequest;

@Service
public class KerberosValidator implements ClusterValidator<KerberosValidationRequest, AmbariValidationException> {

    @Override
    public void validate(KerberosValidationRequest request) throws AmbariValidationException {
        if (request.getEnableSecurity() && request.getKerberosRequest() == null) {
            throw new AmbariValidationException("If the security is enabled the kerberos parameters cannot be empty");
        }
    }

    @Override
    public Class getRelatedClass() {
        return KerberosValidationRequest.class;
    }
}
