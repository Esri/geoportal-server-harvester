package com.esri.geoportal.base.security;

import java.util.List;

public class SecurityEndPointProp {
    private List<EndpointSecurityConfig> securedEndpoints;

    public List<EndpointSecurityConfig> getSecuredEndpoints() {
        return securedEndpoints;
    }

    public void setSecuredEndpoints(List<EndpointSecurityConfig> securedEndpoints) {
        this.securedEndpoints = securedEndpoints;
    }
}
