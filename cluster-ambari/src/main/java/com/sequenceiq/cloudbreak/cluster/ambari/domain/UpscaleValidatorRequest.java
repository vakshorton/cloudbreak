package com.sequenceiq.cloudbreak.cluster.ambari.domain;

import java.io.Serializable;
import java.util.Optional;

import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.HostGroup;

public class UpscaleValidatorRequest implements Serializable {

    private Blueprint blueprint;

    private Optional<HostGroup> hostGroup;

    private Integer adjustment;

    public UpscaleValidatorRequest(Blueprint blueprint, Optional<HostGroup> hostGroup, Integer adjustment) {
        this.blueprint = blueprint;
        this.hostGroup = hostGroup;
        this.adjustment = adjustment;
    }

    public Blueprint getBlueprint() {
        return blueprint;
    }

    public Optional<HostGroup> getHostGroup() {
        return hostGroup;
    }

    public Integer getAdjustment() {
        return adjustment;
    }
}
