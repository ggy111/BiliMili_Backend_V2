package com.bilimili.buaa13.service.impl.article;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bilimili.buaa13.entity.ArticleStatus;
import com.bilimili.buaa13.mapper.ArticleStatusMapper;
import com.bilimili.buaa13.service.article.ArticleStatusService;
import com.bilimili.buaa13.tools.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class ArticleStatusServiceImpl implements ArticleStatusService {
    @Autowired
    private ArticleStatusMapper articleStatusMapper;
    @Autowired
    private RedisTool redisTool;
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 根据专栏ID查询数据
     *
     * @param aid 专栏ID
     * @return 数据统计
     */
    @Override
    public ArticleStatus getStatusByArticleId(Integer aid) {
        //1注释Redis
        ArticleStatus articleStatus = redisTool.getObject("articleStatus:" + aid, ArticleStatus.class);
        if (articleStatus == null) {
            articleStatus = articleStatusMapper.selectById(aid);
            if (articleStatus != null) {
                ArticleStatus finalArticleStatus = articleStatus;
                CompletableFuture.runAsync(() -> {
                    redisTool.setExObjectValue("articleStatus:" + aid, finalArticleStatus);    // 异步更新到redis
                }, taskExecutor);
            } else {
                return null;
            }
        }
        return articleStatusMapper.selectById(aid);
    }

    /**
     * 更新指定字段
     *
     * @param aid      专栏ID
     * @param column   对应数据库的列名
     * @param increase 是否增加，true则增加 false则减少
     * @param count    增减数量 一般是1，只有投币可以加2
     */
    @Override
    public void updateArticleStatus(Integer aid, String column, boolean increase, Integer count) {
        UpdateWrapper<ArticleStatus> updateWrapper = new UpdateWrapper<>();
        if (increase) {
            updateWrapper.eq("aid", aid);
            updateWrapper.setSql(column + " = " + column + " + " + count);
        } else {
            // 更新后的字段不能小于0
            updateWrapper.eq("aid", aid);
            updateWrapper.setSql(column + " = CASE WHEN " + column + " - " + count + " < 0 THEN 0 ELSE " + column + " - " + count + " END");
        }
        articleStatusMapper.update(null, updateWrapper);
        //1注释Redis
        redisTool.deleteValue("articleStats:" + aid);
    }

    /**
     * 同时更新点赞和点踩
     *
     * @param aid     视频ID
     * @param addGood 是否点赞，true则good+1&bad-1，false则good-1&bad+1
     */
    @Override
    public void updateGoodAndBad(Integer aid, boolean addGood) {
        UpdateWrapper<ArticleStatus> updateWrapper = new UpdateWrapper<>();
        if (addGood) {
            updateWrapper.eq("aid", aid);
            updateWrapper.setSql("good = good + 1");
            updateWrapper.setSql("bad = CASE WHEN bad - 1 < 0 THEN 0 ELSE bad - 1 END");
        } else {
            updateWrapper.eq("aid", aid);
            updateWrapper.setSql("bad = bad + 1");
            updateWrapper.setSql("good = CASE WHEN good - 1 < 0 THEN 0 ELSE good - 1 END");
        }
        articleStatusMapper.update(null, updateWrapper);
        //1注释Redis
        redisTool.deleteValue("articleStats:" + aid);
    }
}
