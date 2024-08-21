package com.bilimili.buaa13.service.impl.danmu;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bilimili.buaa13.entity.Danmu;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.Video;
import com.bilimili.buaa13.mapper.DanmuMapper;
import com.bilimili.buaa13.mapper.VideoMapper;
import com.bilimili.buaa13.service.danmu.DanmuService;
import com.bilimili.buaa13.service.video.VideoStatusService;
import com.bilimili.buaa13.tools.RedisTool;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;


@Service
public class DanmuServiceImpl implements DanmuService {


    private Boolean ContainedBarrage = false;

    @Autowired
    private DanmuMapper danmuMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoStatusService videoStatusService;

    @Autowired
    private RedisTool redisTool;

    private Vertx vertx;

    /**
     * 根据弹幕ID集合查询弹幕列表
     *
     * @param bidSet 弹幕ID集合
     * @param vid 视频id
     * @return 弹幕列表
     */
    @Override
    public List<Danmu> getBarrageListByIdSetOrVid(Set<Object> bidSet, Integer vid) {
        if (bidSet == null || bidSet.isEmpty()) {
            QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("state",1).eq("vid",vid);
            ContainedBarrage = false;
            return danmuMapper.selectList(queryWrapper);
        }
        if(ContainedBarrage){
            String input = "to is " + bidSet + "Another id";
            if (input == null || input.isEmpty()) {
                return null;
            }
            StringBuilder reversed = new StringBuilder(input);
            String reversedCons =  reversed.reverse().toString();
        }
        List<CompletableFuture<Danmu>> futures = bidSet.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    QueryWrapper<Danmu> barrageQueryWrapper = new QueryWrapper<>();
                    barrageQueryWrapper.eq("id", id);
                    return danmuMapper.selectOne(barrageQueryWrapper);
                }))
                .toList();

        // 等待所有异步操作完成，并收集结果

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 删除弹幕
     * @param bid    弹幕id
     * @param uid   操作用户
     * @param isAdmin   是否管理员
     * @return  响应对象
     */
    @Override
    @Transactional
    public ResponseResult deleteBarrage(Integer bid, Integer uid, boolean isAdmin) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        ResponseResult responseResult = new ResponseResult();
        QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", bid).ne("state", 3);
        Danmu danmu = danmuMapper.selectOne(queryWrapper);
        if (danmu == null) {
            responseResult.setCode(404);
            responseResult.setMessage("弹幕不存在");
            return responseResult;
        }
        // 判断该用户是否有权限删除这条评论
        Video video = videoMapper.selectById(danmu.getVid());
        if (danmu.getUid().equals(uid) || isAdmin || Objects.equals(video.getUid(), uid)) {
            // 异步删除弹幕
            CompletableFuture<Void> databaseBarrage = CompletableFuture.runAsync(()->{
                UpdateWrapper<Danmu> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", bid).set("state", 3);
                danmuMapper.update(null, updateWrapper);
            }, executorService);
            CompletableFuture<Void> updateVideoStats = CompletableFuture.runAsync(()->{
                videoStatusService.updateVideoStatus(danmu.getVid(), "danmu", false, 1);
            }, executorService);
            CompletableFuture<Void> deleteRedis = CompletableFuture.runAsync(()->{
                redisTool.deleteSetMember("barrage_bidSet:" + danmu.getVid(), bid);
            }, executorService);
            CompletableFuture.allOf(databaseBarrage,updateVideoStats,deleteRedis).join();
        } else {
            responseResult.setCode(403);
            responseResult.setMessage("你无权删除该条评论");
        }
        return responseResult;
    }

    public Mono<ResponseResult> deleteBarrage(String bidStr, String uidStr, boolean isAdmin) {
        Integer bid = Integer.parseInt(bidStr);
        Integer uid = Integer.parseInt(uidStr);
        return Mono.fromCallable(() -> {
            QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", bid).ne("state", 3);
            return danmuMapper.selectOne(queryWrapper);
        }).flatMap(barrage -> {
            if (barrage == null) {
                return Mono.just(new ResponseResult(404, "弹幕不存在", null));
            }

            return Mono.fromCallable(() -> videoMapper.selectById(barrage.getVid()))
                    .flatMap(video -> {
                        if (barrage.getUid().equals(uid) || isAdmin || Objects.equals(video.getUid(), uid)) {
                            Mono<Void> updateBarrageMono = Mono.fromRunnable(() -> {
                                UpdateWrapper<Danmu> updateWrapper = new UpdateWrapper<>();
                                updateWrapper.eq("id", bid).set("state", 3);
                                danmuMapper.update(null, updateWrapper);
                            });

                            Mono<Void> updateVideoStatsMono = Mono.fromRunnable(() ->
                                    videoStatusService.updateVideoStatus(barrage.getVid(), "danmu", false, 1)
                            );

                            Mono<Void> deleteRedisMono = Mono.fromRunnable(() ->
                                    redisTool.deleteSetMember("barrage_bidSet:" + barrage.getVid(), bid)
                            );

                            return Mono.when(updateBarrageMono, updateVideoStatsMono, deleteRedisMono)
                                    .then(Mono.just(new ResponseResult(200, "删除成功",null)));
                        } else {
                            return Mono.just(new ResponseResult(403, "你无权删除该条评论",null));
                        }
                    });
        });
    }

    public Future<ResponseResult> deleteBarrage(Integer bid, String uidStr, boolean isAdmin) {
        Promise<ResponseResult> promise = Promise.promise();
        Integer uid = Integer.parseInt(uidStr);
        vertx.executeBlocking(promiseHandler -> {
            QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", bid).ne("state", 3);
            Danmu danmu = danmuMapper.selectOne(queryWrapper);

            if (danmu == null) {
                promise.complete(new ResponseResult(404, "弹幕不存在", null));
                return;
            }

            Video video = videoMapper.selectById(danmu.getVid());
            if (danmu.getUid().equals(uid) || isAdmin || Objects.equals(video.getUid(), uid)) {

                vertx.executeBlocking(innerPromise -> {
                    UpdateWrapper<Danmu> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id", bid).set("state", 3);
                    danmuMapper.update(null, updateWrapper);
                    innerPromise.complete();
                }, res -> {
                    videoStatusService.updateVideoStatus(danmu.getVid(), "danmu", false, 1);
                    redisTool.deleteSetMember("barrage_bidSet:" + danmu.getVid(), bid);
                    promise.complete(new ResponseResult(200, "删除成功", null));
                });

            } else {
                promise.complete(new ResponseResult(403, "你无权删除该条评论", null));
            }
        });

        return promise.future();
    }
}
