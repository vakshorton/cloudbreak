package com.sequenceiq.cloudbreak.cluster.ambari;

public class AmbariValidationException extends ValidatorException {

    public AmbariValidationException(String message) {
        super(message);
    }

    public AmbariValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmbariValidationException(Throwable cause) {
        super(cause);
    }
}
