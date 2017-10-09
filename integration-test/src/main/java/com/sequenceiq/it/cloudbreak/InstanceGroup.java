package com.sequenceiq.it.cloudbreak;

import com.sequenceiq.cloudbreak.api.model.TemplateRequest;

public class InstanceGroup {

    private final TemplateRequest templateRequest;

    private final String name;

    private final int nodeCount;

    private final String type;

    public InstanceGroup(TemplateRequest templateRequest, String name, int nodeCount, String type) {
        this.templateRequest = templateRequest;
        this.name = name;
        this.nodeCount = nodeCount;
        this.type = type;
    }

    public TemplateRequest getTemplateRequest() {
        return templateRequest;
    }

    public String getName() {
        return name;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public String getType() {
        return type;
    }
}
