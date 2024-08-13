package com.bilimili.buaa13.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.bilimili.buaa13.entity.Critique;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;


@Mapper
public interface CritiqueMapper extends BaseMapper<Critique> {

    //------------------------------------------------------------------------
    //更新于2024.08.10

    //未修改完毕


    //查找根评论
    /**
     * 获取aid文章对应的评论，要求是根评论
     * @param aid 文章id
     * @return 根评论数组
     */
    @Select("SELECT * FROM critique WHERE aid = #{aid} AND root_id = 0")
    List<Critique> getRootCritiquesByAid(@Param("aid") Integer aid);

    
    //获取子评论
    /**
     * 根据开始的位置和偏移量获取子评论
     * @param rootCid 根级节点的criId
     * @param start 开始位置
     * @param limit 限制的个数
     * @return
     */
    @Select("select * from critique where parent_id = #{root_id} and critique.is_deleted = 0 limit #{limit} offset #{start}")
    List<Critique> getRootCritiquesByStartAndLimit(@Param("root_id") Integer rootCid,
                                                 @Param("start") Long start,
                                                 @Param("limit") Long limit
    );
    @Select("select * from critique where parent_id = #{root_id} and critique.is_deleted = 0 LIMIT 18446744073709551615 offset #{start}")
    List<Critique> getRootCritiqueByStartNoLimit(@Param("root_id") Integer rootCid,
                                               @Param("limit") Long limit
    );

    /**
     * 根据aid和开始位置，限制个数查询，按照热度排序
     */
    @Select("select * from critique where aid = #{aid} order by " +
            "(select up_vote - down_vote from critique where critique.aid = #{aid})" +
            "limit #{limit} offset #{start}")
    List<Critique> getAidRootCritiquesByHeat(@Param("aid") Integer aid,
                                           @Param("start") Long start,
                                           @Param("limit") Long limit);

    /**
     * 根据aid和时间排序查询
     */
    @Select("select * from critique where aid = #{aid} order by " +
            "(select critique.create_time from critique where critique.aid = #{aid})" +
            "limit #{limit} offset #{start}")
    List<Critique> getAidRootCritiquesByTime(@Param("aid") Integer aid,
                                           @Param("start") Long start,
                                           @Param("limit") Long limit);

}
