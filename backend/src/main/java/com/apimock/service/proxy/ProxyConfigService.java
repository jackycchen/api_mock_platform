package com.apimock.service.proxy;

import com.apimock.dto.proxy.CreateProxyConfigRequest;
import com.apimock.dto.proxy.ProxyConfigResponse;
import com.apimock.dto.proxy.UpdateProxyConfigRequest;
import com.apimock.entity.auth.User;

import java.util.List;

public interface ProxyConfigService {

    List<ProxyConfigResponse> listProxyConfigs(Long projectId, User currentUser);

    ProxyConfigResponse createProxyConfig(Long projectId, CreateProxyConfigRequest request, User currentUser);

    ProxyConfigResponse updateProxyConfig(Long projectId, Long configId, UpdateProxyConfigRequest request, User currentUser);

    ProxyConfigResponse toggleProxyConfig(Long projectId, Long configId, boolean enabled, User currentUser);

    void deleteProxyConfig(Long projectId, Long configId, User currentUser);
}
