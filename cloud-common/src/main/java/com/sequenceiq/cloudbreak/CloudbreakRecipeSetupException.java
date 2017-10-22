package com.sequenceiq.cloudbreak;

import com.sequenceiq.cloudbreak.CloudbreakException;

public class CloudbreakRecipeSetupException extends CloudbreakException {
    public CloudbreakRecipeSetupException(String message) {
        super(message);
    }

    public CloudbreakRecipeSetupException(String message, Throwable cause) {
        super(message, cause);
    }

    public CloudbreakRecipeSetupException(Throwable cause) {
        super(cause);
    }
}
