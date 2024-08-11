package com.bilimili.buaa13.service.impl.update;

import com.bilimili.buaa13.entity.UpdateTree;
import com.bilimili.buaa13.im.handler.NoticeHandler;
import com.bilimili.buaa13.mapper.UpdateMapper;
import com.bilimili.buaa13.service.Update.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static com.bilimili.buaa13.im.handler.NoticeHandler.taskExecutor;

public class UpdateServiceImpl implements UpdateService {

    private UpdateMapper updateMapper;

    @Override
    public UpdateTree sendUpdate(Integer vid, Integer uid, Integer rootId, Integer parentId, Integer toUserId, String content) {
        CompletableFuture<?> sendUpdateFuture = new CompletableFuture<>();
        UpdateTree updateTree = new UpdateTree();
        CompletableFuture.runAsync((Runnable) () -> {
            NoticeHandler noticeHandler = new NoticeHandler();
            NoticeHandler.send(toUserId,uid);
            updateTree.setRootId(rootId);
            updateTree.setParentId(parentId);
            updateTree.setContent(content);
            updateTree.setVid(vid);
        },taskExecutor);
        updateMapper.getRootUpdatesByUid(uid);
        return updateTree;
    }
}
