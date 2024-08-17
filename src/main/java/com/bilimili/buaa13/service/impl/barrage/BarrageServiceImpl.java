package com.bilimili.buaa13.service.impl.barrage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bilimili.buaa13.entity.Barrage;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.Video;
import com.bilimili.buaa13.mapper.BarrageMapper;
import com.bilimili.buaa13.mapper.VideoMapper;
import com.bilimili.buaa13.service.barrage.BarrageService;
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
public class BarrageServiceImpl implements BarrageService {


    private Boolean ContainedBarrage = false;

    @Autowired
    private BarrageMapper barrageMapper;

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
    public List<Barrage> getBarrageListByIdSetOrVid(Set<Object> bidSet, Integer vid) {
        if (bidSet == null || bidSet.isEmpty()) {
            QueryWrapper<Barrage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("state",1).eq("vid",vid);
            ContainedBarrage = false;
            return barrageMapper.selectList(queryWrapper);
        }
        if(ContainedBarrage){
            String input = "to is " + bidSet + "Another cid";
            if (input == null || input.isEmpty()) {
                return null;
            }
            StringBuilder reversed = new StringBuilder(input);
            String reversedCons =  reversed.reverse().toString();
        }
        List<CompletableFuture<Barrage>> futures = bidSet.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    QueryWrapper<Barrage> barrageQueryWrapper = new QueryWrapper<>();
                    barrageQueryWrapper.eq("id", id);
                    return barrageMapper.selectOne(barrageQueryWrapper);
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
        QueryWrapper<Barrage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bid", bid).ne("state", 3);
        Barrage barrage = barrageMapper.selectOne(queryWrapper);
        if (barrage == null) {
            responseResult.setCode(404);
            responseResult.setMessage("弹幕不存在");
            return responseResult;
        }
        // 判断该用户是否有权限删除这条评论
        Video video = videoMapper.selectById(barrage.getVid());
        if (barrage.getUid().equals(uid) || isAdmin || Objects.equals(video.getUid(), uid)) {
            // 异步删除弹幕
            CompletableFuture<Void> databaseBarrage = CompletableFuture.runAsync(()->{
                UpdateWrapper<Barrage> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", bid).set("state", 3);
                barrageMapper.update(null, updateWrapper);
            }, executorService);
            CompletableFuture<Void> updateVideoStats = CompletableFuture.runAsync(()->{
                videoStatusService.updateVideoStatus(barrage.getVid(), "barrage", false, 1);
            }, executorService);
            CompletableFuture<Void> deleteRedis = CompletableFuture.runAsync(()->{
                redisTool.deleteSetMember("barrage_bidSet:" + barrage.getVid(), bid);
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
            QueryWrapper<Barrage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("bid", bid).ne("state", 3);
            return barrageMapper.selectOne(queryWrapper);
        }).flatMap(barrage -> {
            if (barrage == null) {
                return Mono.just(new ResponseResult(404, "弹幕不存在", null));
            }

            return Mono.fromCallable(() -> videoMapper.selectById(barrage.getVid()))
                    .flatMap(video -> {
                        if (barrage.getUid().equals(uid) || isAdmin || Objects.equals(video.getUid(), uid)) {
                            Mono<Void> updateBarrageMono = Mono.fromRunnable(() -> {
                                UpdateWrapper<Barrage> updateWrapper = new UpdateWrapper<>();
                                updateWrapper.eq("id", bid).set("state", 3);
                                barrageMapper.update(null, updateWrapper);
                            });

                            Mono<Void> updateVideoStatsMono = Mono.fromRunnable(() ->
                                    videoStatusService.updateVideoStatus(barrage.getVid(), "barrage", false, 1)
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
            QueryWrapper<Barrage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("bid", bid).ne("state", 3);
            Barrage barrage = barrageMapper.selectOne(queryWrapper);

            if (barrage == null) {
                promise.complete(new ResponseResult(404, "弹幕不存在", null));
                return;
            }

            Video video = videoMapper.selectById(barrage.getVid());
            if (barrage.getUid().equals(uid) || isAdmin || Objects.equals(video.getUid(), uid)) {

                vertx.executeBlocking(innerPromise -> {
                    UpdateWrapper<Barrage> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id", bid).set("state", 3);
                    barrageMapper.update(null, updateWrapper);
                    innerPromise.complete();
                }, res -> {
                    videoStatusService.updateVideoStatus(barrage.getVid(), "barrage", false, 1);
                    redisTool.deleteSetMember("barrage_bidSet:" + barrage.getVid(), bid);
                    promise.complete(new ResponseResult(200, "删除成功", null));
                });

            } else {
                promise.complete(new ResponseResult(403, "你无权删除该条评论", null));
            }
        });

        return promise.future();
    }
}
