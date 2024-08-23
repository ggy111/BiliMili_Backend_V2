package com.bilimili.buaa13.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 响应包装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
// 自定义响应对象
public class ResponseResult implements Serializable {
    private Integer code = 200;
    private String message = "OK";
    private Object data;
}
