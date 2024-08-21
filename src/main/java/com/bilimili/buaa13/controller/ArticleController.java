package com.bilimili.buaa13.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bilimili.buaa13.entity.*;
import com.bilimili.buaa13.im.handler.NoticeHandler;
import com.bilimili.buaa13.mapper.*;
import com.bilimili.buaa13.service.article.ArticleService;
import com.bilimili.buaa13.service.category.CategoryService;
import com.bilimili.buaa13.service.user.UserService;
import com.bilimili.buaa13.service.video.FavoriteVideoService;
import com.bilimili.buaa13.service.video.VideoReviewService;
import com.bilimili.buaa13.service.video.VideoStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@RestController
public class ArticleController {
    @Autowired
    private OSS ossClient;


    @Autowired
    private VideoReviewService videoReviewService;



    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FavoriteVideoMapper favoriteVideoMapper;


    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CritiqueMapper critiqueMapper;


    @Autowired
    private ArticleService articleService;

    @Autowired
    private VideoStatusMapper videoStatusMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private FavoriteVideoService favoriteVideoService;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private VideoStatusService videoStatusService;

    @Autowired
    private CategoryService categoryService;


    @Value("${oss.bucket}")
    private String OSS_BUCKET;

    @Value("${oss.bucketUrl}")
    private String OSS_BUCKET_URL;





    /**
     * 更新专栏状态，包括过审、不通过、删除，其中审核相关需要管理员权限，删除可以是管理员或者投稿用户
     * @param aid 专栏ID
     * @param status 要修改的状态，1通过 2不通过 3删除
     * @return 无data返回 仅返回响应
     */
    @PostMapping("/article/change/status")
    public ResponseResult updateStatus(@RequestParam("aid") Integer aid,
                                       @RequestParam("status") Integer status) {
        try {
            boolean canGetArticle = false;

            System.out.println("controller层没问题");
            ResponseResult responseResult = articleService.updateArticleStatus(aid, status);
            System.out.println("updateArticleStatus没问题");
            if(status == 1){
                System.out.println("Status = 1");
                //将发布消息发送给粉丝
                // redis 查不到再查数据库
                QueryWrapper<Article> queryWrapper = new QueryWrapper<>();

                //修改于2024.08.16
                if(canGetArticle){
                    Video video = new Video();
                    Map<String,Object> map = new HashMap<>();
                    if (video.getStatus() == 3) {
                        // 视频已删除
                        Video video1 = new Video();
                        video1.setVid(video.getVid());
                        video1.setUid(video.getUid());
                        video1.setStatus(video.getStatus());
                        video1.setDeleteDate(video.getDeleteDate());
                        map.put("video", video1);
                    }
                    else{
                        try{
                            map.put("video", video);
                            map.put("user", userService.getUserByUId(video.getUid()));
                            map.put("stats", videoStatusService.getStatusByVideoId(video.getVid()));
                            map.put("category", categoryService.getCategoryById(video.getMainClassId(), video.getSubClassId()));
                        }
                        catch (Exception e){
                            //log.error(e.getMessage(),e);
                        }
                    }
                }
                queryWrapper.eq("aid", aid).ne("status", 3); //未被删除
                Article article = articleMapper.selectOne(queryWrapper);
                System.out.println("Article =" + article.getAid());
                Integer up_id = article.getUid();
                NoticeHandler.send(up_id,aid);
            }
            return responseResult;
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult(500, "操作失败", null);
        }
    }
    /**
     * 接收aid，返回文章的contentUrl,标题，coverUrl
     * 以下是传输参数
     * @param aid   对应视频ID
     * @return  文件
     *
     *
     * */
    @GetMapping("/article/get")

    public ResponseResult getArticleById(@RequestParam("aid") Integer aid
                                              ) {
        ResponseResult responseResult = new ResponseResult();
        Article article = null;
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("aid", aid).ne("status", 3);
        article = articleMapper.selectOne(queryWrapper);
        Map<String, Object> map = new HashMap<>();
            map.put("coverUrl", article.getCoverUrl());
            map.put("contentUrl",article.getContentUrl());
            map.put("title",article.getTitle());
        responseResult.setData(map);
        return responseResult;
    }

    /**
     * 接收aid，返回文章的contentUrl,标题，coverUrl
     * 以下是传输参数
     * @param aid   对应视频ID
     * @return  文件
     *
     *
     * */
    /**
     * 接收aid，返回文章的contentUrl,标题，coverUrl
     * 以下是传输参数
     * @param aid   对应视频ID
     * @return  文件
     *
     *
     * */
    @GetMapping("/column/markdown")
    public ResponseResult getArticleContentByVid(@RequestParam("aid") Integer aid) {
        ResponseResult responseResult = new ResponseResult();
        Article article = null;
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("aid", aid).ne("status", 3);
        article = articleMapper.selectOne(queryWrapper);
        System.out.println("getArticleContentByVid: " + article);
        if (article == null) {
            responseResult.setCode(404);
            responseResult.setMessage("Article not found");
            return responseResult;
        }
        String contentUrl = article.getContentUrl();
        String bucketName = OSS_BUCKET; // 请根据实际情况修改
        String key = contentUrl.replace(OSS_BUCKET_URL, ""); // 获取对象的key

        try {
            OSSObject ossObject = ossClient.getObject(bucketName, key);
            BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();

            Map<String, Object> map = new HashMap<>();
            map.put("coverUrl", article.getCoverUrl());
            map.put("content", content.toString());
            map.put("title", article.getTitle());
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("uid", article.getUid());
            User user = userMapper.selectOne(userQueryWrapper);
            map.put("user", user);
            responseResult.setData(map);
            responseResult.setCode(200);
            responseResult.setMessage("Success");
        } catch (Exception e) {
            responseResult.setCode(500);
            responseResult.setMessage("Failed to retrieve content from OSS");
        }

        return responseResult;
    }


    /**
     * 接收aid，收藏这个aid下的所有关联视频
     * 以下是传输参数
     * @param aid   对应视频ID
     * @return  文件
     * */

    @GetMapping("/column/favoriteVideo")
    public ResponseResult favoriteRelatedVideo(@RequestParam("aid") Integer aid,
                                               @RequestParam("uid") Integer uid
    ) {
        System.out.println(aid);
        ResponseResult responseResult = new ResponseResult();
        Article article = null;
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("aid", aid).ne("status", 3);
        article = articleMapper.selectOne(queryWrapper);
        Map<String, Object> map = new HashMap<>();
        if(article == null){
            return new ResponseResult(500,"未找到文章对应的aid",null);
        }
        List<Integer> videoList = new ArrayList<>();
        String[] videos = article.getVid().split(",");
        for (String s : videos) {
            try {
                videoList.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                // 处理可能的转换异常
                System.err.println("Invalid number format: " + s);
            }
        }
        //依次收藏视频
        QueryWrapper<Favorite> favoriteQueryWrapper = new QueryWrapper<>();
        favoriteQueryWrapper.eq("fid", uid).eq("type", 1);
        Favorite favorite = favoriteMapper.selectOne(favoriteQueryWrapper);
        if(favorite == null){
            responseResult.setCode(404);
            responseResult.setMessage("Favorite not found");
            return responseResult;
        }
        Set<Integer>addSet = new HashSet<>();
        Integer fid = favorite.getFid();
        List<Integer> collectedVid = favoriteVideoMapper.getVidByFid(fid);
        addSet.add(fid);
        int flag = 0;
        for(Integer vid:videoList){
            if(collectedVid.contains(vid)){
                flag++;
            }
            else favoriteVideoService.addToFav(uid,vid,addSet);
        }
        if(flag >= videoList.size()){
            responseResult.setData(true);
        }
        else {
            responseResult.setData(false);
        }
        return responseResult;
    }



    /**
     * 接收aid，收藏这个aid下的所有关联视频
     * 以下是传输参数
     * @param aid   对应视频ID
     * @return  文件
     * */

    /**
     * 分页查询对应状态专栏
     * @param uid 状态 0待审核 1通过 2未通过
     * @param page  当前页
     * @param quantity  每页的数量
     * @return
     */
    @GetMapping("/article/user-works")
    public ResponseResult getArticlesByUid(@RequestParam("uid") Integer uid,
                                           @RequestParam(value = "page", defaultValue = "1") Integer page,
                                           @RequestParam(value = "quantity", defaultValue = "10") Integer quantity) {
        return articleService.getArticlesByPage(uid, page, quantity);
    }


    /**
     * 获取专栏对应的视频
     * @param aid 专栏aid
     */
    @GetMapping("/column/video-data")
    public ResponseResult getAllVideoInfo(@RequestParam("aid") Integer aid){
        ResponseResult responseResult = new ResponseResult();
        Map<String, Object> map = articleService.getArticleWithDataById(aid);
        if (map == null) {
            responseResult.setCode(404);
            responseResult.setMessage("错误");
            return responseResult;
        }
        Article article = (Article) map.get("article");
        if (article.getStatus() != 1) {
            responseResult.setCode(405);
            responseResult.setMessage("错误");
            return responseResult;
        }
        String[] vidString = article.getVid().split(",");
        List<Integer> vidList = new ArrayList<>();
        for(String vid : vidString){
            vidList.add(Integer.parseInt(vid));
        }
        Map<String, Object> dataMap = new HashMap<>();
        HistoryController.setHistoryMap(vidList, videoMapper, videoStatusMapper,dataMap);
        responseResult.setData(dataMap);
        return responseResult;
    }

}
