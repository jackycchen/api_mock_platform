package com.apimock.controller.api;

import com.apimock.dto.ApiResponse;
import com.apimock.dto.api.*;
import com.apimock.entity.auth.User;
import com.apimock.service.api.ApiService;
import com.apimock.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/apis")
@CrossOrigin(origins = "*")
public class ApiController {

    @Autowired
    private ApiService apiService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApiResponseDto>> createApi(@Valid @RequestBody CreateApiRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        ApiResponseDto response = apiService.createApi(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("API接口创建成功", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiResponseDto>> getApiById(@PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        ApiResponseDto response = apiService.getApiById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("获取API接口详情成功", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiResponseDto>> updateApi(@PathVariable Long id,
                                                                  @Valid @RequestBody UpdateApiRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        ApiResponseDto response = apiService.updateApi(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("API接口更新成功", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApi(@PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        apiService.deleteApi(id, currentUser);
        return ResponseEntity.ok(ApiResponse.successMessage("删除API接口成功"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApiResponseDto>>> getProjectApis(@RequestParam Long projectId,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "20") int size,
                                                                            @RequestParam(defaultValue = "sortOrder") String sortBy,
                                                                            @RequestParam(defaultValue = "asc") String sortDir,
                                                                            @RequestParam(required = false) String keyword,
                                                                            @RequestParam(required = false) String method) {
        User currentUser = SecurityUtils.getCurrentUser();
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ApiResponseDto> responses = apiService.getProjectApis(projectId, method, keyword, currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success("获取API接口列表成功", responses));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<Page<ApiResponseDto>>> getGroupApis(@PathVariable Long groupId,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "20") int size,
                                                                          @RequestParam(defaultValue = "sortOrder") String sortBy,
                                                                          @RequestParam(defaultValue = "asc") String sortDir) {
        User currentUser = SecurityUtils.getCurrentUser();
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ApiResponseDto> responses = apiService.getGroupApis(groupId, currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success("获取分组API接口列表成功", responses));
    }

    @GetMapping("/check-uniqueness")
    public ResponseEntity<ApiResponse<Boolean>> checkApiUniqueness(@RequestParam Long projectId,
                                                                    @RequestParam String path,
                                                                    @RequestParam String method,
                                                                    @RequestParam(required = false) Long excludeApiId) {
        boolean unique = apiService.isApiPathAndMethodUnique(projectId, path, method, excludeApiId);
        String message = unique ? "API路径和方法可以使用" : "API路径和方法已存在";
        return ResponseEntity.ok(ApiResponse.success(message, unique));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchDeleteApis(@RequestBody List<Long> apiIds) {
        User currentUser = SecurityUtils.getCurrentUser();
        apiService.batchDeleteApis(apiIds, currentUser);
        return ResponseEntity.ok(ApiResponse.successMessage("批量删除API接口成功"));
    }

    @PutMapping("/batch/move")
    public ResponseEntity<ApiResponse<Void>> batchMoveApisToGroup(@RequestBody Map<String, Object> payload) {
        User currentUser = SecurityUtils.getCurrentUser();
        @SuppressWarnings("unchecked")
        List<Long> apiIds = (List<Long>) payload.get("apiIds");
        Long groupId = payload.get("groupId") != null ? Long.valueOf(payload.get("groupId").toString()) : null;
        apiService.batchMoveApisToGroup(apiIds, groupId, currentUser);
        return ResponseEntity.ok(ApiResponse.successMessage("批量移动API接口成功"));
    }

    @PutMapping("/{id}/sort")
    public ResponseEntity<ApiResponse<Void>> updateApiSortOrder(@PathVariable Long id,
                                                                 @RequestBody Map<String, Integer> payload) {
        User currentUser = SecurityUtils.getCurrentUser();
        Integer sortOrder = payload.getOrDefault("sortOrder", 0);
        apiService.updateApiSortOrder(id, sortOrder, currentUser);
        return ResponseEntity.ok(ApiResponse.successMessage("更新API接口排序成功"));
    }

    @PostMapping("/{id}/copy")
    public ResponseEntity<ApiResponse<ApiResponseDto>> copyApi(@PathVariable Long id,
                                                                @RequestBody(required = false) Map<String, String> payload) {
        User currentUser = SecurityUtils.getCurrentUser();
        String newName = payload != null ? payload.get("newName") : null;
        ApiResponseDto response = apiService.copyApi(id, newName, currentUser);
        return ResponseEntity.ok(ApiResponse.success("复制API接口成功", response));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getApiStatistics(@RequestParam Long projectId) {
        User currentUser = SecurityUtils.getCurrentUser();
        Object stats = apiService.getApiStatistics(projectId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("获取API统计信息成功", stats));
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<ApiResponse<List<ApiVersionDto>>> listVersions(@PathVariable Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        List<ApiVersionDto> versions = apiService.listVersions(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("获取API版本列表成功", versions));
    }

    @GetMapping("/{id}/versions/{versionNumber}")
    public ResponseEntity<ApiResponse<ApiVersionDetailDto>> getVersionDetail(@PathVariable Long id,
                                                                             @PathVariable Integer versionNumber) {
        User currentUser = SecurityUtils.getCurrentUser();
        ApiVersionDetailDto detail = apiService.getVersionDetail(id, versionNumber, currentUser);
        return ResponseEntity.ok(ApiResponse.success("获取API版本详情成功", detail));
    }

    @PostMapping("/{id}/versions/{versionNumber}/restore")
    public ResponseEntity<ApiResponse<ApiResponseDto>> restoreVersion(@PathVariable Long id,
                                                                       @PathVariable Integer versionNumber,
                                                                       @RequestBody(required = false) Map<String, String> payload) {
        User currentUser = SecurityUtils.getCurrentUser();
        String changeSummary = payload != null ? payload.get("changeSummary") : null;
        ApiResponseDto response = apiService.restoreVersion(id, versionNumber, changeSummary, currentUser);
        return ResponseEntity.ok(ApiResponse.success("回滚API版本成功", response));
    }

    @GetMapping("/{id}/versions/compare")
    public ResponseEntity<ApiResponse<ApiVersionCompareResponse>> compareVersions(@PathVariable Long id,
                                                                                   @RequestParam Integer version1,
                                                                                   @RequestParam Integer version2) {
        User currentUser = SecurityUtils.getCurrentUser();
        ApiVersionCompareResponse response = apiService.compareVersions(id, version1, version2, currentUser);
        return ResponseEntity.ok(ApiResponse.success("比较API版本成功", response));
    }
}
