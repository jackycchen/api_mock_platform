package com.apimock.service.proxy;

import com.apimock.dto.proxy.CreateProxyConfigRequest;
import com.apimock.dto.proxy.ProxyConfigResponse;
import com.apimock.dto.proxy.UpdateProxyConfigRequest;
import com.apimock.entity.auth.User;
import com.apimock.entity.project.Project;
import com.apimock.entity.proxy.ProxyConfig;
import com.apimock.entity.proxy.ProxyMode;
import com.apimock.exception.InvalidRequestException;
import com.apimock.exception.ProjectNotFoundException;
import com.apimock.exception.UnauthorizedAccessException;
import com.apimock.repository.ProjectRepository;
import com.apimock.repository.proxy.ProxyConfigRepository;
import com.apimock.service.project.ProjectMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProxyConfigServiceImpl implements ProxyConfigService {

    @Autowired
    private ProxyConfigRepository proxyConfigRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberService projectMemberService;

    @Override
    @Transactional(readOnly = true)
    public List<ProxyConfigResponse> listProxyConfigs(Long projectId, User currentUser) {
        Project project = getAccessibleProject(projectId, currentUser, false);
        return proxyConfigRepository.findByProjectIdOrderByCreatedAtAsc(project.getId())
                .stream()
                .map(ProxyConfigResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public ProxyConfigResponse createProxyConfig(Long projectId, CreateProxyConfigRequest request, User currentUser) {
        Project project = getAccessibleProject(projectId, currentUser, true);

        validateRequest(projectId, null, request.getPathPattern(), request.getMode(), request.getTargetUrl());

        ProxyConfig config = new ProxyConfig();
        config.setProject(project);
        applyRequest(config, request);

        ProxyConfig saved = proxyConfigRepository.save(config);
        return new ProxyConfigResponse(saved);
    }

    @Override
    public ProxyConfigResponse updateProxyConfig(Long projectId, Long configId, UpdateProxyConfigRequest request, User currentUser) {
        Project project = getAccessibleProject(projectId, currentUser, true);

        ProxyConfig config = proxyConfigRepository.findByIdAndProjectId(configId, project.getId())
                .orElseThrow(() -> new ProjectNotFoundException("代理配置不存在"));

        validateRequest(projectId, configId, request.getPathPattern(), request.getMode(), request.getTargetUrl());

        applyRequest(config, request);

        ProxyConfig saved = proxyConfigRepository.save(config);
        return new ProxyConfigResponse(saved);
    }

    @Override
    public ProxyConfigResponse toggleProxyConfig(Long projectId, Long configId, boolean enabled, User currentUser) {
        Project project = getAccessibleProject(projectId, currentUser, true);

        ProxyConfig config = proxyConfigRepository.findByIdAndProjectId(configId, project.getId())
                .orElseThrow(() -> new ProjectNotFoundException("代理配置不存在"));

        config.setEnabled(enabled);
        ProxyConfig saved = proxyConfigRepository.save(config);
        return new ProxyConfigResponse(saved);
    }

    @Override
    public void deleteProxyConfig(Long projectId, Long configId, User currentUser) {
        Project project = getAccessibleProject(projectId, currentUser, true);

        ProxyConfig config = proxyConfigRepository.findByIdAndProjectId(configId, project.getId())
                .orElseThrow(() -> new ProjectNotFoundException("代理配置不存在"));

        proxyConfigRepository.delete(config);
    }

    private Project getAccessibleProject(Long projectId, User user, boolean requireWrite) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("项目不存在"));

        String permission = requireWrite ? "project:write" : "project:read";
        if (!projectMemberService.hasPermission(project, user, permission)) {
            throw new UnauthorizedAccessException("没有权限访问该项目");
        }
        return project;
    }

    private void validateRequest(Long projectId, Long excludeConfigId, String pathPattern, String modeValue, String targetUrl) {
        if (excludeConfigId == null) {
            if (proxyConfigRepository.existsByProjectIdAndPathPattern(projectId, pathPattern)) {
                throw new InvalidRequestException("路径规则已存在");
            }
        } else {
            if (proxyConfigRepository.existsByProjectIdAndPathPatternAndIdNot(projectId, pathPattern, excludeConfigId)) {
                throw new InvalidRequestException("路径规则已存在");
            }
        }

        ProxyMode mode;
        try {
            mode = ProxyMode.valueOf(modeValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("无效的代理模式");
        }

        boolean requiresTarget = mode == ProxyMode.PROXY || mode == ProxyMode.AUTO;
        if (requiresTarget) {
            if (!StringUtils.hasText(targetUrl)) {
                throw new InvalidRequestException("代理模式下目标地址不能为空");
            }
            if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                throw new InvalidRequestException("目标地址必须是合法的HTTP/HTTPS地址");
            }
        }
    }

    private void applyRequest(ProxyConfig config, CreateProxyConfigRequest request) {
        config.setName(request.getName());
        config.setPathPattern(request.getPathPattern());
        config.setMode(ProxyMode.valueOf(request.getMode().toUpperCase()));
        config.setTargetUrl(StringUtils.hasText(request.getTargetUrl()) ? request.getTargetUrl().trim() : null);

        if (request.getForwardHeaders() != null && !request.getForwardHeaders().isEmpty()) {
            String headers = request.getForwardHeaders().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.joining(","));
            config.setForwardHeaders(headers);
        } else {
            config.setForwardHeaders(null);
        }

        config.setPreserveHost(Boolean.TRUE.equals(request.getPreserveHost()));
        config.setEnabled(request.getEnabled() == null || request.getEnabled());
    }
}
