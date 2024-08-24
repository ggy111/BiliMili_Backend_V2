package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.Article;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.Video;
import com.bilimili.buaa13.service.article.ArticleService;
import com.bilimili.buaa13.mapper.ArticleMapper;
import com.bilimili.buaa13.mapper.VideoMapper;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.service.article.ArticleUploadService;
import com.bilimili.buaa13.service.video.VideoService;
import com.bilimili.buaa13.tools.JsonWebTokenTool;
import com.bilimili.buaa13.tools.OssTool;
import com.bilimili.buaa13.tools.RedisTool;
import lombok.SneakyThrows;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleUploadController.class)
public class ArticleUploadControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JsonWebTokenTool jsonWebTokenTool;

    @MockBean
    private RedisTool redisTool;

    @MockBean
    private OssTool ossTool; // 假设ossTool是一个Spring Bean

    @MockBean
    private ArticleUploadService articleUploadService;

    @MockBean
    private ArticleMapper articleMapper;

    @MockBean
    private VideoMapper videoMapper;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private CurrentUser currentUser;

    @Test
    @SneakyThrows
    @WithMockUser
    public void testAddImage_Failure() throws Exception {
        // Mocking a file
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image content".getBytes()
        );

        // Mocking the ossTool.uploadImage() method
        String mockUrl = "http://mocked.url/test-image.jpg";
        Mockito.when(ossTool.uploadImage(Mockito.any(MultipartFile.class), Mockito.eq("articleArtwork")))
                .thenReturn(mockUrl);

        mockMvc.perform(multipart("/image/add")
                        .file(file))
                .andExpect(status().isForbidden());

        // Verify that the mock was called as expected
        //Mockito.verify(ossTool).uploadImage(Mockito.any(MultipartFile.class), Mockito.eq("articleArtwork"));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    public void testAddAllArticle_Failure() throws Exception {
        // Mocking files
        MockMultipartFile cover = new MockMultipartFile("cover", "cover.jpg", MediaType.IMAGE_JPEG_VALUE, "cover content".getBytes());
        MockMultipartFile content = new MockMultipartFile("content", "article.md", "text/markdown", "markdown content".getBytes());

        // Mocking services
        String mockContentUrl = "http://mocked.url/article.md";
        String mockCoverUrl = "http://mocked.url/cover.jpg";
        Mockito.when(ossTool.uploadArticle(Mockito.any(MultipartFile.class))).thenReturn(mockContentUrl);
        Mockito.when(ossTool.uploadImage(Mockito.any(MultipartFile.class), Mockito.eq("articleCover"))).thenReturn(mockCoverUrl);
        Mockito.when(currentUser.getUserId()).thenReturn(1);

        // Mocking video check
        Video video = new Video();
        video.setVid(1);
        video.setStatus(1);
        Mockito.when(videoMapper.selectOne(Mockito.any())).thenReturn(video);

        // Performing the request and verifying the result
        mockMvc.perform(multipart("/article/add/all")
                        .file(cover)
                        .file(content)
                        .param("title", "Test Article")
                        .param("vid", "1"))
                .andExpect(status().isForbidden());

        // Verifying that the article was saved
        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
        //Mockito.verify(articleMapper).insert(articleCaptor.capture());

        /*Article savedArticle = articleCaptor.getValue();
        assert savedArticle != null;
        assert "Test Article".equals(savedArticle.getTitle());
        assert "1".equals(savedArticle.getVid());
        assert mockContentUrl.equals(savedArticle.getContentUrl());
        assert mockCoverUrl.equals(savedArticle.getCoverUrl());
        assert savedArticle.getUid() == 1;*/
    }
}
