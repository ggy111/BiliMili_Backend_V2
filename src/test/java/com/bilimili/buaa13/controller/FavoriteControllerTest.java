package com.bilimili.buaa13.controller;

import com.alibaba.druid.support.http.stat.WebAppStat;
import com.bilimili.buaa13.config.DruidConfig;
import com.bilimili.buaa13.config.WebSocketConfig;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.video.FavoriteService;
import com.bilimili.buaa13.tools.JsonWebTokenTool;
import com.bilimili.buaa13.tools.RedisTool;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class FavoriteControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FavoriteController favoriteController;
    @MockBean
    private FavoriteService favoriteService;
    @MockBean
    private JsonWebTokenTool jsonWebTokenTool;
    @MockBean
    private RedisTool redisTool;

    @Test
    @WithMockUser
    @SneakyThrows
    void getFavoriteDetailsP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getFavoriteDetailsP");
        when(favoriteController.getFavoriteDetails(2)).thenReturn(responseResult);
        mockMvc.perform(get("/favorite/get")
                .param("fid", String.valueOf(2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()));
    }

    @Test
    @WithMockUser
    @SneakyThrows
    void getFavoriteDetailsN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getFavoriteDetailsN");
        responseResult.setCode(404);
        when(favoriteController.getFavoriteDetails(-200)).thenReturn(responseResult);
        mockMvc.perform(get("/favorite/Get")
                        .param("fid", String.valueOf(-200)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @SneakyThrows
    void deleteFavoriteP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("deleteFavoriteP");
        responseResult.setMessage("删除成功");
        when(favoriteController.deleteFavorite(2)).thenReturn(responseResult);
        mockMvc.perform(post("/favorite/delete")
                        .param("fid", String.valueOf(2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("删除成功"));
    }

    @Test
    @WithMockUser
    @SneakyThrows
    void deleteFavoriteN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("deleteFavoriteN");
        responseResult.setMessage("删除失败");
        responseResult.setCode(404);
        when(favoriteController.deleteFavorite(-2)).thenReturn(responseResult);
        mockMvc.perform(post("/favorite/Delete")
                        .param("fid", String.valueOf(-2)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @SneakyThrows
    void updateFavoriteP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("updateFavoriteP");
        responseResult.setMessage("更新成功");
        when(favoriteController.updateFavorite(4,"测试","描述",1)).thenReturn(responseResult);
        mockMvc.perform(post("/favorite/update")
                        .param("fid", String.valueOf(4))
                        .param("title","测试")
                        .param("desc","描述")
                        .param("visible","1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("更新成功"));
    }

    @Test
    @WithMockUser
    @SneakyThrows
    void updateFavoriteN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("updateFavoriteN");
        responseResult.setMessage("更新失败");
        responseResult.setCode(404);
        when(favoriteController.updateFavorite(-4,"test","description",-3)).thenReturn(responseResult);
        mockMvc.perform(post("/Favorite/Update")
                        .param("fid", String.valueOf(-4))
                        .param("title","test")
                        .param("desc","description")
                        .param("visible","-3"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @SneakyThrows
    void getAllFavoritiesForUserP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getAllFavoritiesForUserP");
        responseResult.setMessage("获取成功");
        responseResult.setCode(200);
        when(favoriteController.getAllFavoritiesForUser(6)).thenReturn(responseResult);
        mockMvc.perform(get("/favorite/get-all/user")
                        .param("uid", String.valueOf(6)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取成功"));
    }

    @Test
    @WithMockUser
    @SneakyThrows
    void getAllFavoritiesForUserN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getAllFavoritiesForUserN");
        responseResult.setMessage("获取失败");
        responseResult.setCode(404);
        when(favoriteController.getAllFavoritiesForUser(-6)).thenReturn(responseResult);
        mockMvc.perform(get("/favorite/get_all/user")
                        .param("uid", String.valueOf(-6)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @SneakyThrows
    void getAllFavoritiesForVisitorP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getAllFavoritiesForVisitorP");
        responseResult.setMessage("获取成功");
        responseResult.setCode(200);
        when(favoriteController.getAllFavoritiesForVisitor(6)).thenReturn(responseResult);
        mockMvc.perform(get("/favorite/get-all/visitor")
                        .param("uid", String.valueOf(6)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取成功"));
    }
    @Test
    @WithMockUser
    @SneakyThrows
    void getAllFavoritiesForVisitorN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getAllFavoritiesForVisitorN");
        responseResult.setMessage("获取失败");
        responseResult.setCode(404);
        when(favoriteController.getAllFavoritiesForVisitor(-6)).thenReturn(responseResult);
        mockMvc.perform(get("/favorite/get-all/visitor")
                        .param("uid", String.valueOf(-6)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("404"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取失败"));
    }

    @Test
    @WithMockUser
    @SneakyThrows
    void createFavoriteP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("createFavoriteP");
        responseResult.setMessage("创建成功");
        responseResult.setCode(200);
        when(favoriteController.createFavorite("创建测试","创建描述",1)).thenReturn(responseResult);
        mockMvc.perform(post("/favorite/create")
                        .param("title", "创建测试")
                        .param("desc","创建描述")
                        .param("visible","1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("创建成功"));
    }
    @Test
    @WithMockUser
    @SneakyThrows
    void createFavoriteN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("createFavoriteN");
        responseResult.setMessage("创建失败");
        responseResult.setCode(404);
        when(favoriteController.createFavorite("创建失败测试","NULL",-3)).thenReturn(responseResult);
        mockMvc.perform(post("/favorite/create")
                        .param("title", "创建失败测试")
                        .param("desc","NULL")
                        .param("visible","-3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("404"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("创建失败"));
    }
}