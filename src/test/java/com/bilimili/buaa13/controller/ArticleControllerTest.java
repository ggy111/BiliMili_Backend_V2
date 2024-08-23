package com.bilimili.buaa13.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bilimili.buaa13.entity.Article;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.mapper.ArticleMapper;
import com.bilimili.buaa13.tools.JsonWebTokenTool;
import com.bilimili.buaa13.tools.RedisTool;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

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

    @Test
    @SneakyThrows
    @WithMockUser
    void testGetArticleById() throws Exception {
        Article mockArticle = new Article();
        mockArticle.setAid(100);
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("Example Title"));
    }
}
