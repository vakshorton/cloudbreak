package com.sequenceiq.cloudbreak.cluster;

import java.io.Serializable;

public interface ClusterValidator <T extends Serializable, E extends RuntimeException>{

    void validate(T t) throws E;

    Class getRelatedClass();
}
