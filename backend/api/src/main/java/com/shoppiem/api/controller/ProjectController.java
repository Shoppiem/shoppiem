//package com.shoppiem.api.controller;
//
//import com.shoppiem.api.ProjectApi;
//import com.shoppiem.api.service.project.ProjectService;
//import com.shoppiem.api.ContentPreviewResponse;
//import com.shoppiem.api.CreateProjectRequest;
//import com.shoppiem.api.CreateProjectResponse;
//import com.shoppiem.api.GenericResponse;
//import com.shoppiem.api.MediaContent;
//import com.shoppiem.api.PageableResponse;
//import com.shoppiem.api.ProjectDetail;
//import com.shoppiem.api.ProjectJobStatus;
//import com.shoppiem.api.UpdateProjectContentRequest;
//import com.shoppiem.api.UserProjectsResponse;
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * @author Biz Melesse
// * created on 01/29/23
// */
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//public class ProjectController implements ProjectApi {
//  private final ProjectService projectService;
//
//  @Override
//  public ResponseEntity<CreateProjectResponse> createProject(CreateProjectRequest createProjectRequest) {
//    return ResponseEntity.ok(projectService.createProject(createProjectRequest));
//  }
//
//  @Override
//  public ResponseEntity<GenericResponse> deleteProject(String projectId) {
//    return ResponseEntity.ok(projectService.deleteProject(projectId));
//  }
//
//  @Override
//  public ResponseEntity<PageableResponse> getAllProjects() {
//    return ResponseEntity.ok(projectService.getAllProjects());
//  }
//
//  @Override
//  public ResponseEntity<UserProjectsResponse> getAllUserProjects(Integer page, Integer limit) {
//    return ResponseEntity.ok(projectService.getAllUserProjects(page, limit));
//  }
//
//  @Override
//  public ResponseEntity<ContentPreviewResponse> getContentPreview(String url) {
//    return ResponseEntity.ok(projectService.getContentPreview(url));
//  }
//
//  @Override
//  public ResponseEntity<MediaContent> getMediaContent(String projectId) {
//    return ResponseEntity.ok(projectService.getMediaContent(projectId));
//  }
//
//  @Override
//  public ResponseEntity<ProjectDetail> getProjectById(String id) {
//    return ResponseEntity.ok(projectService.getProjectById(id));
//  }
//
//  @Override
//  public ResponseEntity<ProjectJobStatus> getProjectJobStatus(String projectId) {
//    return ResponseEntity.ok(projectService.getProjectJobStatus(projectId));
//  }
//
//  @Override
//  public ResponseEntity<List<String>> getSampledImages(String projectId) {
//    return ResponseEntity.ok(projectService.getSampledImages(projectId));
//  }
//
//  @Override
//  public ResponseEntity<GenericResponse> updateProjectContent(
//      UpdateProjectContentRequest updateProjectContentRequest) {
//    return ResponseEntity.ok(projectService.updateProjectContent(updateProjectContentRequest));
//  }
//}
