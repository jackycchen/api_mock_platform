package com.apimock.controller.project;

import com.apimock.dto.ApiResponse;
import com.apimock.dto.project.CreateProjectRequest;
import com.apimock.dto.project.ProjectResponse;
import com.apimock.dto.project.UpdateProjectRequest;
import com.apimock.entity.auth.User;
import com.apimock.entity.project.ProjectType;
import com.apimock.service.project.ProjectService;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 创建项目
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {

        try {
            User currentUser = SecurityUtils.getCurrentUser();
            ProjectResponse response = projectService.createProject(request, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("项目创建成功", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "创建项目失败：" + e.getMessage()));
        }
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable Long id) {
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            ProjectResponse response = projectService.getProjectById(id, currentUser);

            return ResponseEntity.ok(ApiResponse.success("获取项目详情成功", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取项目详情失败：" + e.getMessage()));
        }
    }

    /**
     * 更新项目信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {

        try {
            User currentUser = SecurityUtils.getCurrentUser();
            ProjectResponse response = projectService.updateProject(id, request, currentUser);

            return ResponseEntity.ok(ApiResponse.success("项目更新成功", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "更新项目失败：" + e.getMessage()));
        }
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        try {
            User currentUser = SecurityUtils.getCurrentUser();
            projectService.deleteProject(id, currentUser);

            return ResponseEntity.ok(ApiResponse.success());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "删除项目失败：" + e.getMessage()));
        }
    }

    /**
     * 获取项目列表（用户有权限访问的项目）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword) {

        try {
            User currentUser = SecurityUtils.getCurrentUser();

            // 创建分页和排序参数
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<ProjectResponse> projects;
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 搜索项目
                projects = projectService.searchProjects(keyword, currentUser, pageable);
            } else {
                // 获取所有有权限的项目
                projects = projectService.getAccessibleProjects(currentUser, pageable);
            }

            return ResponseEntity.ok(ApiResponse.success("获取项目列表成功", projects));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取项目列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取我创建的项目列表
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getMyProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            User currentUser = SecurityUtils.getCurrentUser();

            // 创建分页和排序参数
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<ProjectResponse> projects = projectService.getUserProjects(currentUser, pageable);

            return ResponseEntity.ok(ApiResponse.success("获取我的项目列表成功", projects));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取我的项目列表失败：" + e.getMessage()));
        }
    }

    /**
     * 检查项目名称是否可用
     */
    @GetMapping("/check-name")
    public ResponseEntity<ApiResponse<Boolean>> checkProjectName(
            @RequestParam String name) {

        try {
            boolean available = projectService.isProjectNameAvailable(name);
            String message = available ? "项目名称可以使用" : "项目名称已被占用";

            return ResponseEntity.ok(ApiResponse.success(message, available));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "检查项目名称失败：" + e.getMessage()));
        }
    }

    /**
     * 获取项目类型选项
     */
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<Object>> getProjectTypes() {
        try {
            List<Map<String, String>> types = Arrays.stream(ProjectType.values())
                    .map(type -> {
                        Map<String, String> item = new HashMap<>();
                        item.put("code", type.name());
                        item.put("name", type.getDisplayName());
                        item.put("description", type.getDescription());
                        return item;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("获取项目类型成功", types));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取项目类型失败：" + e.getMessage()));
        }
    }

    /**
     * 更新项目统计信息
     */
    @PostMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<Void>> updateProjectStats(@PathVariable Long id) {
        try {
            User currentUser = SecurityUtils.getCurrentUser();

            // 检查权限
            if (!projectService.hasProjectAccess(id, currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(403, "无权限访问此项目"));
            }

            projectService.updateProjectStats(id);

            return ResponseEntity.ok(ApiResponse.success());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "更新项目统计信息失败：" + e.getMessage()));
        }
    }
}
