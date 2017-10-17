package com.sequenceiq.cloudbreak.cluster.ambari.domain;

import java.io.Serializable;

import com.sequenceiq.cloudbreak.api.model.KerberosRequest;

public class KerberosValidationRequest implements Serializable {

    private Boolean enableSecurity;

    private KerberosRequest kerberosRequest;

    public KerberosValidationRequest(Boolean enableSecurity, KerberosRequest kerberosRequest) {
        this.enableSecurity = enableSecurity;
        this.kerberosRequest = kerberosRequest;
    }

    public Boolean getEnableSecurity() {
        return enableSecurity;
    }

    public KerberosRequest getKerberosRequest() {
        return kerberosRequest;
    }
}
