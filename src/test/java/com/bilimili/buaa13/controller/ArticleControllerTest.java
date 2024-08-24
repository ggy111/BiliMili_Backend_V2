package com.bilimili.buaa13.controller;
import com.bilimili.buaa13.entity.Favorite;
import com.bilimili.buaa13.im.handler.NoticeHandler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bilimili.buaa13.entity.Article;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.mapper.FavoriteMapper;
import com.bilimili.buaa13.mapper.ArticleMapper;
import com.bilimili.buaa13.service.article.ArticleService;
import com.bilimili.buaa13.service.video.FavoriteService;
import com.bilimili.buaa13.tools.JsonWebTokenTool;
import com.bilimili.buaa13.tools.RedisTool;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.mockito.Mockito.when;

@WebMvcTest(ArticleController.class)
//@ComponentScan(basePackages = "com.bilimili.buaa13.tools")
class ArticleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleController articleController;

    @MockBean
    private JsonWebTokenTool jsonWebTokenTool;

    @MockBean
    private RedisTool redisTool;

    @MockBean
    private ArticleMapper articleMapper;

    @MockBean
    private FavoriteMapper favoriteMapper;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private NoticeHandler noticeHandler;

    @Test
    @SneakyThrows
    @WithMockUser
    void testGetArticleById() throws Exception {
        Article mockArticle = new Article();
        mockArticle.setAid(1);
        mockArticle.setCoverUrl("http://example.com/cover.jpg");
        mockArticle.setContentUrl("http://example.com/content");
        mockArticle.setTitle("Example Title");

        // 当articleMapper.selectOne被调用时，返回mockArticle
        when(articleController.getArticleById(1)).thenReturn(new ResponseResult(200,"OK",mockArticle));

        // 模拟发送GET请求，并验证返回结果
        ResultActions resBody = mockMvc.perform(MockMvcRequestBuilders.get("/article/get")
                        .param("aid", "1"));
        resBody.andDo(result -> System.out.println("请求响应："+result.getResponse().getContentAsString()));
        resBody.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.coverUrl").value("http://example.com/cover.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.contentUrl").value("http://example.com/content"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("Example Title"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.aid").value(1));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void testGetArticleById_Failure() throws Exception {
        Article mockArticle = new Article();
        mockArticle.setAid(100);
        mockArticle.setCoverUrl("http://example.com/cover.jpg");
        mockArticle.setContentUrl("http://example.com/content");
        mockArticle.setTitle("Example Title");

        // 当articleMapper.selectOne被调用时，返回mockArticle
        when(articleController.getArticleById(1)).thenReturn(new ResponseResult(200,"OK",mockArticle));

        // 模拟发送GET请求，并验证返回结果
        ResultActions resBody = mockMvc.perform(MockMvcRequestBuilders.get("/article/get")
                        .param("aid", "100"));
        resBody.andDo(result -> System.out.println("请求响应："+result.getResponse().getContentAsString()));
        resBody.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").doesNotExist());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    public void testUpdateStatus() throws Exception {
        // 模拟 ResponseResult 对象
        ResponseResult expectedResponse = new ResponseResult();
        expectedResponse.setCode(200);
        expectedResponse.setMessage("Success");

        // 模拟 articleService.updateArticleStatus 的行为
        given(articleService.updateArticleStatus(1, 1)).willReturn(expectedResponse);

        // 模拟 Article 对象
        Article article = new Article();
        article.setAid(1);
        article.setUid(100);
        article.setStatus(1);

        // 模拟 articleMapper.selectOne 的行为
        given(articleMapper.selectOne(any(QueryWrapper.class))).willReturn(article);


        // 发送 POST 请求并验证结果
        mockMvc.perform(post("/article/change/status")
                .param("aid", "1")
                .param("status", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void testGetArticleContentByVid() throws Exception {
        Article mockArticle = new Article();
        mockArticle.setAid(100);
        mockArticle.setCoverUrl("http://example.com/cover.jpg");
        mockArticle.setContentUrl("http://example.com/content");
        mockArticle.setTitle("Example Title");

        // 当articleMapper.selectOne被调用时，返回mockArticle
        when(articleController.getArticleContentByVid(1)).thenReturn(new ResponseResult(200,"OK",mockArticle));

        // 模拟发送GET请求，并验证返回结果
        ResultActions resBody = mockMvc.perform(MockMvcRequestBuilders.get("/column/markdown")
                        .param("aid", "1"));
        resBody.andDo(result -> System.out.println("请求响应："+result.getResponse().getContentAsString()));
        resBody.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.coverUrl").value("http://example.com/cover.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.contentUrl").value("http://example.com/content"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("Example Title"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.aid").value(100));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void testFavoriteRelatedVideo() throws Exception {
        Article mockArticle = new Article();
        mockArticle.setAid(100);
        mockArticle.setCoverUrl("http://example.com/cover.jpg");
        mockArticle.setContentUrl("http://example.com/content");
        mockArticle.setTitle("Example Title");

        Favorite favorite = new Favorite();
        favorite.setCount(10);
        favorite.setTitle("Example Title");
        favorite.setUid(1);
        favorite.setFid(10);

        // 当articleMapper.selectOne被调用时，返回mockArticle

        when(favoriteMapper.selectOne(any(QueryWrapper.class))).thenReturn(favorite);
        when(articleController.favoriteRelatedVideo(1, 1)).thenReturn(new ResponseResult(200,"OK",favorite));

        // 模拟发送GET请求，并验证返回结果
        ResultActions resBody = mockMvc.perform(MockMvcRequestBuilders.get("/column/favoriteVideo")
                        .param("aid", "1")
                .param("uid", "1"));
        resBody.andDo(result -> System.out.println("请求响应："+result.getResponse().getContentAsString()));
        resBody.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.count").value("10"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("Example Title"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.uid").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.fid").value(10));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void testFavoriteRelatedVideo_Failure() throws Exception {
        Article mockArticle = new Article();
        mockArticle.setAid(100);
        mockArticle.setCoverUrl("http://example.com/cover.jpg");
        mockArticle.setContentUrl("http://example.com/content");
        mockArticle.setTitle("Example Title");

        Favorite favorite = new Favorite();
        favorite.setCount(10);
        favorite.setTitle("Example Title");
        favorite.setUid(1);
        favorite.setFid(10);

        when(favoriteMapper.selectOne(any(QueryWrapper.class))).thenReturn(favorite);
        when(articleController.favoriteRelatedVideo(1, 1)).thenReturn(new ResponseResult(200,"OK",favorite));

        // 模拟发送GET请求，并验证返回结果
        ResultActions resBody = mockMvc.perform(MockMvcRequestBuilders.get("/column/favoriteVideo")
                        .param("aid", "1")
                .param("uid", "1"));
        resBody.andDo(result -> System.out.println("请求响应："+result.getResponse().getContentAsString()));
        resBody.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.count").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("Example Title"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.uid").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.fid").value(10));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void testGetArticlesByUid() throws Exception {
        Article mockArticle = new Article();
        mockArticle.setAid(100);
        mockArticle.setCoverUrl("http://example.com/cover.jpg");
        mockArticle.setContentUrl("http://example.com/content");
        mockArticle.setTitle("Example Title");

        ArrayList<Article> articles = new ArrayList<Article>();
        articles.add(mockArticle);

        Favorite favorite = new Favorite();
        favorite.setCount(10);
        favorite.setTitle("Example Title");
        favorite.setUid(1);
        favorite.setFid(10);

        when(articleService.getArticlesByPage(1, 1, 10)).thenReturn(new ResponseResult(200,"OK",articles));
        when(articleController.getArticlesByUid(1, 1, 10)).thenReturn(new ResponseResult(200,"OK",mockArticle));

        // 模拟发送GET请求，并验证返回结果
        ResultActions resBody = mockMvc.perform(MockMvcRequestBuilders.get("/article/user-works")
                        .param("uid", "1")
                .param("page", "1")
                .param("quantity", "10"));
        resBody.andDo(result -> System.out.println("请求响应："+result.getResponse().getContentAsString()));
        resBody.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.coverUrl").value("http://example.com/cover.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.contentUrl").value("http://example.com/content"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("Example Title"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.aid").value(100));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void testGetArticlesByUid_Failure() throws Exception {
        Article mockArticle = new Article();
        mockArticle.setAid(100);
        mockArticle.setCoverUrl("http://example.com/cover.jpg");
        mockArticle.setContentUrl("http://example.com/content");
        mockArticle.setTitle("Example Title");

        ArrayList<Article> articles = new ArrayList<Article>();
        articles.add(mockArticle);

        Favorite favorite = new Favorite();
        favorite.setCount(10);
        favorite.setTitle("Example Title");
        favorite.setUid(1);
        favorite.setFid(10);

        when(articleService.getArticlesByPage(1, 1, 10)).thenReturn(new ResponseResult(200,"OK",articles));
        when(articleController.getArticlesByUid(1, 1, 10)).thenReturn(new ResponseResult(200,"OK",mockArticle));

        // 模拟发送GET请求，并验证返回结果
        ResultActions resBody = mockMvc.perform(MockMvcRequestBuilders.get("/article/user-works")
                        .param("uid", "1")
                .param("page", "2")
                .param("quantity", "10"));
        resBody.andDo(result -> System.out.println("请求响应："+result.getResponse().getContentAsString()));
        resBody.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").doesNotExist());
    }
}
