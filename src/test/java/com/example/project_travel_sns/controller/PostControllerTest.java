package com.example.project_travel_sns.controller;

import com.example.project_travel_sns.domain.dto.post.PostRequest;
import com.example.project_travel_sns.exception.AppException;
import com.example.project_travel_sns.exception.ErrorCode;
import com.example.project_travel_sns.service.PostService;
import com.example.project_travel_sns.util.ControllerAppInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@MockBean(JpaMetamodelMappingContext.class)
class PostControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    PostService postService;
    @Autowired
    ObjectMapper objectMapper;

    ControllerAppInfo controllerAppInfo = new ControllerAppInfo();

    @Test
    @DisplayName("포스트 작성 성공")
    @WithMockUser
    void post_write_SUCCESS() throws Exception {

        when(postService.write(any(), any(), any()))
                .thenReturn(controllerAppInfo.getPostResponse());

        mockMvc.perform(post("/api/v1/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostRequest("제목입니다", "내용입니다."))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.message").exists())
                .andExpect(jsonPath("$.result.postId").exists());
    }

    @Test
    @DisplayName("포스트 작성 실패 : 로그인을 하지 않은 경우")
    @WithAnonymousUser
    void post_write_FAILED_authentication() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostRequest("제목입니다", "내용입니다"))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("포스트 상세 조회 성공")
    @WithMockUser
    void post_get_detail_SUCCESS() throws Exception {

        when(postService.getPost(any()))
                .thenReturn(controllerAppInfo.getPostGetResponse());

        mockMvc.perform(get("/api/v1/posts/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").exists())
                .andExpect(jsonPath("$.result.title").exists())
                .andExpect(jsonPath("$.result.body").exists())
                .andExpect(jsonPath("$.result.userName").exists())
                .andExpect(jsonPath("$.result.createdAt").exists())
                .andExpect(jsonPath("$.result.lastModifiedAt").exists());
    }

    @Test
    @DisplayName("포스트 수정 성공")
    @WithMockUser
    void post_modify_SUCCESS() throws Exception {

        when(postService.modify(any(), any(), any(), any()))
                .thenReturn(controllerAppInfo.getPostResponse());

        mockMvc.perform(put("/api/v1/posts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostRequest("제목입니다", "내용입니다."))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.message").exists())
                .andExpect(jsonPath("$.result.postId").exists());
    }

    @Test
    @DisplayName("포스트 수정 실패(1) : 로그인을 하지 않은 경우")
    @WithAnonymousUser
    void post_modify_FAILED_authentication() throws Exception {
        mockMvc.perform(put("/api/v1/posts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostRequest("제목입니다", "내용입니다."))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("포스트 수정 실패(2) : 작성자 불일치")
    @WithMockUser
    void post_modify_FAILED_different() throws Exception {

        when(postService.modify(any(), any(), any(), any()))
                .thenThrow(new AppException(ErrorCode.INVALID_PERMISSION, ErrorCode.INVALID_PERMISSION.getMessage()));

        mockMvc.perform(put("/api/v1/posts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostRequest("제목입니다", "내용입니다."))))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result.message").exists())
                .andExpect(jsonPath("$.result.errorCode").exists());
    }

    @Test
    @DisplayName("포스트 수정 실패(3) : 데이터베이스 에러")
    @WithMockUser
    void post_modify_FAILED_db() throws Exception {

        when(postService.modify(any(), any(), any(), any()))
                .thenThrow(new AppException(ErrorCode.DATABASE_ERROR, ErrorCode.DATABASE_ERROR.getMessage()));

        mockMvc.perform(put("/api/v1/posts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostRequest("제목입니다", "내용입니다."))))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.result.message").exists())
                .andExpect(jsonPath("$.result.errorCode").exists());
    }

    @Test
    @DisplayName("포스트 삭제 성공")
    @WithMockUser
    void post_delete_SUCCESS() throws Exception {

        when(postService.delete(any(), any()))
                .thenReturn(controllerAppInfo.getPostResponse());

        mockMvc.perform(delete("/api/v1/posts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostRequest("제목입니다", "내용입니다."))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.message").exists())
                .andExpect(jsonPath("$.result.postId").exists());
    }

    @Test
    @DisplayName("포스트 삭제 실패(1) : 로그인을 하지 않은 경우")
    @WithAnonymousUser
    void post_delete_FAILED_authentication() throws Exception {

        mockMvc.perform(delete("/api/v1/posts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostRequest("제목입니다", "내용입니다."))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("포스트 삭제 실패(2) : 작성자 불일치")
    @WithMockUser
    void post_delete_FAILED_different() throws Exception {

        when(postService.delete(any(), any()))
                .thenThrow(new AppException(ErrorCode.INVALID_PERMISSION, ErrorCode.INVALID_PERMISSION.getMessage()));

        mockMvc.perform(delete("/api/v1/posts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostRequest("제목", "테스트입니다."))))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result.message").exists())
                .andExpect(jsonPath("$.result.errorCode").exists());
    }

    @Test
    @DisplayName("포스트 삭제 실패3_데이터베이스 에러")
    @WithMockUser
    void post_delete_FAILED_db() throws Exception {

        when(postService.delete(any(), any()))
                .thenThrow(new AppException(ErrorCode.DATABASE_ERROR, ErrorCode.DATABASE_ERROR.getMessage()));

        mockMvc.perform(delete("/api/v1/posts/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PostRequest("제목", "테스트입니다."))))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.result.message").exists())
                .andExpect(jsonPath("$.result.errorCode").exists());
    }

    @Test
    @DisplayName("pageable 파라미터 검증")
    @WithMockUser
    void post_get_list_SUCCESS() throws Exception {
        mockMvc.perform(get("/api/v1/posts")
                        .with(csrf())
                        .param("page", "0")
                        .param("size", "3")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(postService).getPosts(pageableArgumentCaptor.capture());
        PageRequest pageRequest = (PageRequest) pageableArgumentCaptor.getValue();

        assertEquals(0, pageRequest.getPageNumber());
        assertEquals(3, pageRequest.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "createdAt"), pageRequest.getSort());
    }

    @Test
    @DisplayName("마이피드 목록 성공")
    @WithMockUser
    void post_my_SUCCESS() throws Exception {

        when(postService.getMyPosts(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/posts/my")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("마이피드 목록 실패(1) : 로그인을 하지 않은 경우")
    @WithAnonymousUser
    void post_my_FAILED_login() throws Exception {

        mockMvc.perform(get("/api/v1/posts/my")
                        .with(csrf())
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isUnauthorized());
    }
}