package com.sequenceiq.cloudbreak;

import com.sequenceiq.cloudbreak.CloudbreakException;

public class CloudbreakSecuritySetupException extends CloudbreakException {

    public CloudbreakSecuritySetupException(String message) {
        super(message);
    }

    public CloudbreakSecuritySetupException(String message, Throwable cause) {
        super(message, cause);
    }

    public CloudbreakSecuritySetupException(Throwable cause) {
        super(cause);
    }
}
