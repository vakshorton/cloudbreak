package com.sequenceiq.cloudbreak.service.decorator;

import java.io.Serializable;

import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;

/**
 * Decortor service interface for domain objects.
 * Implementers are expected to decorate passed in domain objects with data from other services.
 *
 * @param <P> the type of the object to be decorated
 */
public interface Decorator<P extends Serializable, J extends Serializable> {

    /**
     * Performs the decorator logic.
     *
     * @param subject the object to be decorated
     * @param data    additional data
     * @return the decorated object
     */
    P decorate(P subject, J request, IdentityUser identityUser, Object... data);
}
