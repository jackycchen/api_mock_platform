package com.apimock.controller.project;

import com.apimock.dto.project.CreateProjectRequest;
import com.apimock.dto.project.ProjectResponse;
import com.apimock.dto.project.UpdateProjectRequest;
import com.apimock.entity.project.ProjectVisibility;
import com.apimock.service.project.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProjectResponse projectResponse;
    private CreateProjectRequest createRequest;
    private UpdateProjectRequest updateRequest;

    @BeforeEach
    void setUp() {
        projectResponse = new ProjectResponse();
        projectResponse.setId(1L);
        projectResponse.setName("Test Project");
        projectResponse.setDescription("Test Description");
        projectResponse.setVisibility(ProjectVisibility.PRIVATE);
        projectResponse.setCreatedAt(LocalDateTime.now());

        ProjectResponse.OwnerInfo ownerInfo = new ProjectResponse.OwnerInfo(1L, "testuser", "test@example.com");
        projectResponse.setOwner(ownerInfo);

        createRequest = new CreateProjectRequest();
        createRequest.setName("New Project");
        createRequest.setDescription("New Description");
        createRequest.setVisibility(ProjectVisibility.PRIVATE);

        updateRequest = new UpdateProjectRequest();
        updateRequest.setName("Updated Project");
        updateRequest.setDescription("Updated Description");
        updateRequest.setVisibility(ProjectVisibility.PUBLIC);
    }

    @Test
    void testCreateProject_Success() throws Exception {
        // Given
        when(projectService.createProject(any(CreateProjectRequest.class), any()))
                .thenReturn(projectResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Project"));
    }

    @Test
    void testCreateProject_ValidationError() throws Exception {
        // Given - Invalid request with empty name
        createRequest.setName("");

        // When & Then
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetProjectById_Success() throws Exception {
        // Given
        when(projectService.getProjectById(eq(1L), any()))
                .thenReturn(projectResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Project"));
    }

    @Test
    void testUpdateProject_Success() throws Exception {
        // Given
        when(projectService.updateProject(eq(1L), any(UpdateProjectRequest.class), any()))
                .thenReturn(projectResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testDeleteProject_Success() throws Exception {
        // Given
        doNothing().when(projectService).deleteProject(eq(1L), any());

        // When & Then
        mockMvc.perform(delete("/api/v1/projects/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProjects_Success() throws Exception {
        // Given
        Page<ProjectResponse> projectPage = new PageImpl<>(Arrays.asList(projectResponse));
        when(projectService.getAccessibleProjects(any(), any()))
                .thenReturn(projectPage);

        // When & Then
        mockMvc.perform(get("/api/v1/projects")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "updatedAt")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }
}