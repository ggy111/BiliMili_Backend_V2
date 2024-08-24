package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.Category;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.service.category.CategoryService;
import com.bilimili.buaa13.mapper.CategoryMapper;
import com.bilimili.buaa13.service.video.VideoStatusService;
import com.bilimili.buaa13.service.user.UserService;


import com.bilimili.buaa13.tools.JsonWebTokenTool;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserService userService;

    @MockBean
    private RedisTool redisTool;

    @MockBean
    private CategoryMapper categoryMapper;

    @MockBean
    private VideoStatusService videoStatusService;

    @MockBean
    private JsonWebTokenTool jsonWebTokenTool;

    @Test
    @SneakyThrows
    @WithMockUser
    public void testGetAll_Success() throws Exception {
        // Mocking the service method
        ResponseResult mockResponse = new ResponseResult(200, "成功获取全部分区", "someCategoryList");
        Mockito.when(categoryService.getAll()).thenReturn(mockResponse);

        // Performing the GET request and verifying the result
        mockMvc.perform(get("/category/getall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功获取全部分区"))
                .andExpect(jsonPath("$.data").exists());

        // Verify that the service method was called
        Mockito.verify(categoryService).getAll();
    }

    /*@Test
    @SneakyThrows
    @WithMockUser
    public void testGetAPage_FoundInRedis() throws Exception {
        // Mocking Redis response
        Category mockCategory = new Category();
        Mockito.when(redisTool.getObject("category:1:1", Category.class))
                .thenReturn(mockCategory);

        // Performing the request and verifying the result
        mockMvc.perform(get("/category/getAPage"))
                .andExpect(status().isOk());

        // Verify that the redisTool was called with the correct key
        Mockito.verify(redisTool).getObject("category:1:1", Category.class);
        //Mockito.verify(categoryMapper, Mockito.never()).findByMainAndSubClassId(Mockito.anyString(), Mockito.anyString());
    }*/

    @Test
    @SneakyThrows
    @WithMockUser
    public void testGetOne_Success() throws Exception {
        // Mocking the service method
        ResponseResult mockResponse = new ResponseResult(200, "成功获取分类数据", "someCategoryData");
        Mockito.when(categoryService.getAll()).thenReturn(mockResponse);

        // Performing the GET request and verifying the result
        mockMvc.perform(get("/category/getone")
                        .param("mcId", "1")
                        .param("scId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功获取分类数据"))
                .andExpect(jsonPath("$.data").exists());

        // Verify that the service method was called
        Mockito.verify(categoryService).getAll();
    }

    @Test
    @SneakyThrows
    @WithMockUser
    public void testGetOne_NoDataFound() throws Exception {
        // Mocking the service method to return an empty result
        ResponseResult mockResponse = new ResponseResult(200, "没有找到分类数据", null);
        Mockito.when(categoryService.getAll()).thenReturn(mockResponse);

        // Performing the GET request and verifying the result
        mockMvc.perform(get("/category/getone")
                        .param("mcId", "1")
                        .param("scId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("没有找到分类数据"))
                .andExpect(jsonPath("$.data").doesNotExist());

        // Verify that the service method was called
        Mockito.verify(categoryService).getAll();
    }
}
