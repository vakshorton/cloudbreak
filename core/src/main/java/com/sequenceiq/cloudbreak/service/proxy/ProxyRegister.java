package com.sequenceiq.cloudbreak.service.proxy;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.ecwid.consul.v1.ConsulClient;

@Service
public class ProxyRegister {

    private final static String BACKEND_URL = "traefik/backends/%s/servers/gw/url";
    private final static String FRONTED_BASE = "traefik/frontends/%s/";
    private final static String FRONTED_WIRE_TO_BACKEND = FRONTED_BASE + "backend";
    private final static String FRONTED_PASS_HOST_HEADER = FRONTED_BASE + "passHostHeader";
    private final static String FRONTED_RULE = FRONTED_BASE + "routes/gw/rule";

    @Inject
    private ConsulClient consulClient;

    public void register(String clusterName, String gatewayHost) {
        if ("shared1".equals(clusterName)) {
            registerKeys(clusterName, String.format("https://%s:8443", gatewayHost), String.format("/gateway/%s/", clusterName));
        } else {
            registerKeys(clusterName, String.format("https://%s:8443", gatewayHost), String.format("/gateway/shared1/%s/", clusterName));
        }
    }

    public void remove(String clusterName) {
        removeKeys(clusterName);
    }

    private void registerKeys(String clusterName, String baseUrl, String context) {
        consulClient.setKVValue(String.format(BACKEND_URL, clusterName), baseUrl + context);
        consulClient.setKVValue(String.format(FRONTED_WIRE_TO_BACKEND, clusterName), clusterName);
        consulClient.setKVValue(String.format(FRONTED_PASS_HOST_HEADER, clusterName), "true");
        consulClient.setKVValue(String.format(FRONTED_RULE, clusterName), String.format("PathPrefix:%s", context));
    }

    private void removeKeys(String name) {
        consulClient.deleteKVValues(String.format("traefik/backends/%s", name));
        consulClient.deleteKVValues(String.format("traefik/frontends/%s", name));
        consulClient.deleteKVValues(String.format("traefik/backends/%s-sso", name));
        consulClient.deleteKVValues(String.format("traefik/frontends/%s-sso", name));
    }


    public void removeAll() {
        consulClient.deleteKVValues("traefik/backends");
        consulClient.deleteKVValues("traefik/frontends");
    }

}
