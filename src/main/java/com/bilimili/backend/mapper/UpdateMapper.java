package com.bilimili.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bilimili.backend.pojo.Comment;
import com.bilimili.backend.pojo.Update;
import com.bilimili.backend.pojo.UpdateTree;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UpdateMapper extends BaseMapper<Update> {
//    @Select("SELECT * FROM comment WHERE root_id = #{rootId} AND vid = #{vid}")
//    List<Comment> getChildCommentsByRootId(@Param("rootId") Integer rootId, @Param("vid") Integer vid);

    @Select("SELECT * FROM update WHERE vid = #{vid} AND root_id = 0")
    List<Update> getRootUpdatesByUid(@Param("uid") Integer uid);

}
