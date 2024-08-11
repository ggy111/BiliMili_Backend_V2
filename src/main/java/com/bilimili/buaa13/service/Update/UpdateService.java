package com.bilimili.buaa13.service.Update;

import com.bilimili.buaa13.entity.UpdateTree;

public interface UpdateService {

    UpdateTree sendUpdate(Integer vid, Integer uid, Integer rootId, Integer parentId, Integer toUserId, String content);

}
