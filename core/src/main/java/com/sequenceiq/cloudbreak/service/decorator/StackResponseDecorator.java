package com.sequenceiq.cloudbreak.service.decorator;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.model.StackResponse;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.service.decorator.responseprovider.ResponseProvider;
import com.sequenceiq.cloudbreak.service.decorator.responseprovider.ResponseProviders;

@Service
public class StackResponseDecorator implements Decorator<StackResponse, Stack> {
    @Inject
    private ResponseProviders responseProviders;

    private enum DecorationData {
        ENTRY,
        STACK
    }

    @Override
    public StackResponse decorate(StackResponse stackResponse, Stack stack, IdentityUser user, Object... data) {
        Set<String> entries = (Set<String>) data[DecorationData.ENTRY.ordinal()];
        if (entries != null && !entries.isEmpty()) {
            for (String entry : entries) {
                ResponseProvider responseProvider = responseProviders.get(entry);
                stackResponse = (responseProvider == null) ? stackResponse : responseProvider.providerEntriesToStackResponse(stack, stackResponse);
            }
        }
        return stackResponse;
    }
}
