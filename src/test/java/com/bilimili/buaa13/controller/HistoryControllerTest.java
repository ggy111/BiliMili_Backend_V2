package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.mapper.FavoriteVideoMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class HistoryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private HistoryController historyController;
    @MockBean
    private FavoriteVideoMapper favoriteVideoMapper;

    @Test
    @SneakyThrows
    @WithMockUser
    void getRecordVideoByUid() {
        List<Integer> vids = new ArrayList<>();
        vids.add(6);
        when(favoriteVideoMapper.getVidByFid(5000+5)).thenReturn(vids);
        mockMvc.perform(get("/Record/Video?uid=5"))
                .andExpect(status().isOk());
        Mockito.verify(favoriteVideoMapper,times(0)).getVidByFid(5000+5);
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void getRecordVideoByUidN() {
        List<Integer> vids = new ArrayList<>();
        vids.add(6);
        when(favoriteVideoMapper.getVidByFid(5000-12)).thenReturn(vids);
        mockMvc.perform(get("/Record/Video?uif=-12"))
                .andExpect(status().isBadRequest());
        Mockito.verify(favoriteVideoMapper,times(0)).getVidByFid(5000-12);
    }
}