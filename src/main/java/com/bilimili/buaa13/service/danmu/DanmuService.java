package com.bilimili.buaa13.service.danmu;

import com.bilimili.buaa13.entity.Danmu;
import com.bilimili.buaa13.entity.ResponseResult;

import java.util.List;
import java.util.Set;

public interface DanmuService {
    /**
     * 根据弹幕ID集合查询弹幕列表
     *
     * @param bidSet 弹幕ID集合
     * @param vid 视频id
     * @return 弹幕列表
     */
    List<Danmu> getBarrageListByIdSetOrVid(Set<Object> bidSet, Integer vid);

    /**
     * 删除弹幕
     * @param bid    弹幕id
     * @param uid   操作用户
     * @param isAdmin   是否管理员
     * @return  响应对象
     */
    ResponseResult deleteBarrage(Integer bid, Integer uid, boolean isAdmin);
}
