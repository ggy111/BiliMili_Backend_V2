package com.bilimili.buaa13.service.article;

import com.bilimili.buaa13.entity.ArticleStatus;
import com.bilimili.buaa13.entity.VideoStatus;

public interface ArticleStatusService {
    /**
     * 根据专栏ID查询数据
     * @param aid 专栏ID
     * @return 数据统计
     */
    ArticleStatus getStatusByArticleId(Integer aid);

    /**
     * 更新指定字段
     * @param aid   专栏ID
     * @param column    对应数据库的列名
     * @param increase  是否增加，true则增加 false则减少
     * @param count 增减数量 一般是1，只有投币可以加2
     */
    void updateArticleStatus(Integer aid, String column, boolean increase, Integer count);

    /**
     * 同时更新点赞和点踩
     * @param aid   视频ID
     * @param addGood   是否点赞，true则good+1&bad-1，false则good-1&bad+1
     */
    void updateGoodAndBad(Integer aid, boolean addGood);
}
