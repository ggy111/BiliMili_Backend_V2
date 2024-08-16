package com.bilimili.buaa13.service.video;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.UserVideo;
import com.bilimili.buaa13.entity.Video;
import org.springframework.transaction.annotation.Transactional;

public interface VideoDenounceService {
    @Transactional
    ResponseResult deleteCritique(Integer criId, Integer postId, boolean isAdmin);
}
