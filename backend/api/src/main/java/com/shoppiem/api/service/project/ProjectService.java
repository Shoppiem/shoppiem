//package com.shoppiem.api.service.project;
//
//import com.shoppiem.api.ContentPreviewResponse;
//import com.shoppiem.api.CreateProjectRequest;
//import com.shoppiem.api.CreateProjectResponse;
//import com.shoppiem.api.GenericResponse;
//import com.shoppiem.api.GifRequest;
//import com.shoppiem.api.GifResponse;
//import com.shoppiem.api.MediaContent;
//import com.shoppiem.api.PageableResponse;
//import com.shoppiem.api.ProjectDetail;
//import com.shoppiem.api.ProjectJobStatus;
//import com.shoppiem.api.UpdateProjectContentRequest;
//import com.shoppiem.api.UserProjectsResponse;
//import java.util.List;
//
///**
// * @author Biz Melesse created on 1/29/23
// */
//public interface ProjectService {
//
//  /**
//   * Create a new project
//   *
//   * @param createProjectRequest project metadata and an optional uploaded media content
//   * @return
//   */
//  CreateProjectResponse createProject(CreateProjectRequest createProjectRequest);
//  GenericResponse deleteProject(String projectUid);
//  PageableResponse getAllProjects();
//  ProjectDetail getProjectById(String projectUid);
//
//  GenericResponse updateProjectContent(
//      UpdateProjectContentRequest updateProjectContentRequest);
//
//  GenericResponse deleteGif(String url);
//
//  GifResponse generateGif(GifRequest gifRequest);
//
//  List<String> getSampledImages(String projectUid);
//
//  MediaContent getMediaContent(String projectUid);
//
//  ProjectJobStatus getProjectJobStatus(String projectUid);
//
//  /**
//   * Get OpenGraph social preview links for the given content
//   * @param url
//   * @return
//   */
//  ContentPreviewResponse getContentPreview(String url);
//
//  /**
//   * Get all user projects for an admin
//   *
//   * @param page
//   * @param limit
//   * @return
//   */
//  UserProjectsResponse getAllUserProjects(Integer page, Integer limit);
//
//}
