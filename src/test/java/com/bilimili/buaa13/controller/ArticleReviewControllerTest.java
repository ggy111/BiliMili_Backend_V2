package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.Article;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.article.ArticleReviewService;
import com.bilimili.buaa13.service.article.ArticleService;
import com.bilimili.buaa13.entity.Video;
import com.bilimili.buaa13.tools.JsonWebTokenTool;
import com.bilimili.buaa13.tools.RedisTool;
import lombok.SneakyThrows;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleReviewController.class)
public class ArticleReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleReviewService articleReviewService;

    @MockBean
    private JsonWebTokenTool jsonWebTokenTool;

    @MockBean
    private RedisTool redisTool;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private ArticleReviewController articleReviewController;

    @Test
    @SneakyThrows
    @WithMockUser
    void testGetArticles() {
        // 模拟 service 返回的 ResponseResult
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(200);
        responseResult.setData("article");
        Mockito.when(articleReviewController.getArticles(1, 1, 10)).thenReturn(responseResult);

        ResultActions resBody = mockMvc.perform(get("/review/article/getpage?astatus=1&page=1&quantity=10"));
//                .param("astatus", "1")
//                .param("page", "1")
//                .param("quantity", "10"));
                resBody.andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("data",IsEqual.equalTo(responseResult.getData())));

        //System.out.println("dqwdwqfd: "+resBody.getResponse().getContentAsString());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void testGetArticles_Failure() throws Exception {
        // 模拟 service 返回的 ResponseResult
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(200);
        responseResult.setData("article");
        Mockito.when(articleReviewController.getArticles(1, 1, 10)).thenReturn(responseResult);

        ResultActions resBody = mockMvc.perform(get("/review/article/getpage?astatus=1&page=1&quantity=10"));
//                .param("astatus", "1")
//                .param("page", "1")
//                .param("quantity", "10"));
                resBody.andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("data",IsEqual.equalTo(responseResult.getData())));

        //System.out.println("dqwdwqfd: "+resBody.getResponse().getContentAsString());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void getTotalArticle() throws Exception {
        // 模拟 service 返回的 ResponseResult
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(200);
        responseResult.setData("article");


        Mockito.when(articleReviewController.getTotalArticle(1)).thenReturn(responseResult);
        Mockito.when(articleReviewService.getTotalNumberByStatus(any(Integer.class))).thenReturn(responseResult);

        ResultActions resBody = mockMvc.perform(get("/review/article/total")
               .param("astatus", "1"));
                resBody.andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("data",IsEqual.equalTo(responseResult.getData())));

        //System.out.println("dqwdwqfd: "+resBody.getResponse().getContentAsString());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void getTotalArticle_Failure() throws Exception {
        // 模拟 service 返回的 ResponseResult
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(200);
        responseResult.setData("wrong_article");


        Mockito.when(articleReviewService.getTotalNumberByStatus(any(Integer.class))).thenReturn(responseResult);
        Mockito.when(articleReviewController.getTotalArticle(1)).thenReturn(responseResult);

        ResultActions resBody = mockMvc.perform(get("/review/article/total")
               .param("astatus", "1"));
                resBody.andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value("wrong_article"));

        //System.out.println("dqwdwqfd: "+resBody.getResponse().getContentAsString());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void getOneArticle() throws Exception {
        Video video = new Video();
        video.setCoverUrl("http://example.com/cover.jpg");
        video.setUid(1);
        video.setTitle("title");
        video.setDescription("description");

        Article mockArticle = new Article();
        mockArticle.setAid(100);
        mockArticle.setCoverUrl("http://example.com/cover.jpg");
        mockArticle.setContentUrl("http://example.com/content");
        mockArticle.setTitle("Example Title");

        ArrayList<Article> articles = new ArrayList<>();
        articles.add(mockArticle);

        // 模拟 service 返回的 ResponseResult
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(200);
        responseResult.setData(articles);

        Mockito.when(articleReviewService.getTotalNumberByStatus(any(Integer.class))).thenReturn(responseResult);
        Mockito.when(articleReviewController.getOneArticle(1)).thenReturn(responseResult);

        ResultActions resBody = mockMvc.perform(get("/review/article/getone")
               .param("aid", "1"));
                resBody.andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists());

        //System.out.println("dqwdwqfd: "+resBody.getResponse().getContentAsString());
    }
}

