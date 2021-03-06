package com.sequenceiq.periscope.service.security;

import javax.inject.Inject;

import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.model.CertificateResponse;
import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.periscope.domain.Cluster;
import com.sequenceiq.periscope.domain.SecurityConfig;
import com.sequenceiq.periscope.model.TlsConfiguration;
import com.sequenceiq.periscope.repository.SecurityConfigRepository;

@Service
public class TlsSecurityService {

    @Inject
    private CloudbreakClient cloudbreakClient;

    @Inject
    private SecurityConfigRepository securityConfigRepository;

    public SecurityConfig prepareSecurityConfig(Long stackId) {
        CertificateResponse response = cloudbreakClient.stackV1Endpoint().getCertificate(stackId);
        byte[] serverCert = Base64.encode(response.getServerCert());
        byte[] clientKey = Base64.encode(response.getClientKey());
        byte[] clientCert = Base64.encode(response.getClientCert());
        return new SecurityConfig(clientKey, clientCert, serverCert);
    }

    public TlsConfiguration getConfiguration(Cluster cluster) {
        SecurityConfig securityConfig = cluster.getSecurityConfig();
        if (securityConfig != null) {
            return new TlsConfiguration(securityConfig.getClientKeyDecoded(), securityConfig.getClientCertDecoded(), securityConfig.getServerCertDecoded());
        }
        securityConfig = securityConfigRepository.findByClusterId(cluster.getId());
        if (securityConfig == null) {
            securityConfig = prepareSecurityConfig(cluster.getStackId());
        }
        return new TlsConfiguration(securityConfig.getClientKeyDecoded(), securityConfig.getClientCertDecoded(), securityConfig.getServerCertDecoded());
    }

}
