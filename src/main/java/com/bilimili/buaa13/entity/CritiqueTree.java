package com.bilimili.buaa13.entity;




import com.bilimili.buaa13.entity.dto.UserDTO;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import java.util.List;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class CritiqueTree {
    private Integer criId;
    private Integer aid;
    private Integer rootCid;
    private Integer parentCid;//父节点id，给这个id的评论发送
    private String content;//评论内容
    private Integer postId;
    private Integer acceptId;
    private Integer upVote;
    private Integer downVote;
    private List<CommentTree> sonNode;//子节点，用于递归
    private Date createTime;
    private Long count;//回复的数量
}


