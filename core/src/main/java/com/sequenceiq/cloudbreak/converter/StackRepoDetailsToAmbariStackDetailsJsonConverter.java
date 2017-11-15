package com.sequenceiq.cloudbreak.converter;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.StackRepositoryResponse;
import com.sequenceiq.cloudbreak.cloud.model.component.StackRepoDetails;

@Component
public class StackRepoDetailsToAmbariStackDetailsJsonConverter extends AbstractConversionServiceAwareConverter<StackRepoDetails, StackRepositoryResponse> {

    @Override
    public StackRepositoryResponse convert(StackRepoDetails source) {
        StackRepositoryResponse ambariRepoDetailsJson = new StackRepositoryResponse();
        ambariRepoDetailsJson.setHdpVersion(source.getHdpVersion());
        ambariRepoDetailsJson.setVerify(source.isVerify());
        ambariRepoDetailsJson.setKnox(source.getKnox());
        ambariRepoDetailsJson.setStack(source.getStack());
        ambariRepoDetailsJson.setUtil(source.getUtil());
        return ambariRepoDetailsJson;
    }
}
