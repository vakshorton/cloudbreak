package com.sequenceiq.cloudbreak.cloud;

import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;

/**
 * Stack validation
 */
public interface Validator {

    /**
     * Validates the given stack
     *
     * @param cloudStack stack
     */
    void validate(AuthenticatedContext ac, CloudStack cloudStack);
}
