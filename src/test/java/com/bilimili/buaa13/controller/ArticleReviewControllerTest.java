package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.article.ArticleReviewService;
import com.bilimili.buaa13.service.article.ArticleService;
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
}

