package com.bilimili.buaa13.entity;

import com.bilimili.buaa13.entity.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CritiqueTree {
    private Integer criId;
    private Integer aid;
    private Integer rootCid;
    private Integer parentCid;//父节点id，给这个id的评论发送
    private String content;//评论内容
    private UserDTO post;//发送者
    private UserDTO accept;//接收者
    private Integer upVote;
    private Integer downVote;
    private List<CritiqueTree> sonNode;//子节点，用于递归
    private Date createTime;
    private Long count;//回复的数量
}


