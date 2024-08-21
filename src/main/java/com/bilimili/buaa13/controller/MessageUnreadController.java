package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.message.MessageUnreadService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageUnreadController {

    @Autowired
    private MessageUnreadService messageUnreadService;

    @Autowired
    private CurrentUser currentUser;

    Boolean success = false;
    //-----------------------------------------------------------------------------------------
    /**
     * 获取当前用户某一列的未读消息数
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     * @return 响应结果，包含指定列的未读消息数
     */
    @GetMapping("/msg-unread/column")
    public ResponseResult getColumnUnread(@RequestParam("column") String column) {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        //responseResult.setData(messageUnreadService.getUnreadByColumn(uid, column));
        return responseResult;
    } /**
     * 批量清除多个列的未读消息提示
     * @param columns   需要清除的列名列表
     * @return 响应结果，表示批量清除操作是否成功
     */
    @PostMapping("/msg-unread/clear-batch")
    public ResponseResult clearBatchUnread(@RequestParam("columns") String[] columns) {
        Integer uid = currentUser.getUserId();
        for (String column : columns) {
            messageUnreadService.clearOneUnread(uid, column);
        }
        ResponseResult responseResult = new ResponseResult();
        responseResult.setMessage("批量未读消息已清除");
        return responseResult;
    }

    /**
     * 清除所有未读消息提示
     * @return 响应结果，表示全部清除操作是否成功
     */
    @PostMapping("/msg-unread/clear-all")
    public ResponseResult clearAllUnread() {
        Integer uid = currentUser.getUserId();
        //messageUnreadService.clearAllUnread(uid);
        ResponseResult responseResult = new ResponseResult();
        responseResult.setMessage("所有未读消息已清除");
        return responseResult;
    }

    /**
     * 获取当前用户某一列的未读消息数，并按时间段过滤
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     * @param startTime 开始时间（时间戳）
     * @param endTime   结束时间（时间戳）
     * @return 响应结果，包含按时间段过滤后的未读消息数
     */
    @GetMapping("/msg-unread/column/time-range")
    public ResponseResult getColumnUnreadInTimeRange(@RequestParam("column") String column,
                                                     @RequestParam("startTime") Long startTime,
                                                     @RequestParam("endTime") Long endTime) {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        /**responseResult.setData(messageUnreadService.getUnreadByColumnInTimeRange(uid, column, startTime, endTime));**/
        return responseResult;
    }

    /**
     * 批量获取多列的未读消息数，并按时间段过滤
     * @param columns   msg_unread表列名列表
     * @param startTime 开始时间（时间戳）
     * @param endTime   结束时间（时间戳）
     * @return 响应结果，包含按时间段过滤后的多列未读消息数
     */
    @GetMapping("/msg-unread/columns/time-range")
    public ResponseResult getBatchUnreadInTimeRange(@RequestParam("columns") String[] columns,
                                                    @RequestParam("startTime") Long startTime,
                                                    @RequestParam("endTime") Long endTime) {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        //responseResult.setData(messageUnreadService.getBatchUnreadInTimeRange(uid, columns, startTime, endTime));
        return responseResult;
    }

    /**
     * 为特定列设置未读消息提醒的状态
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     * @param status    提醒状态，true表示开启提醒，false表示关闭提醒
     * @return 响应结果，表示设置操作是否成功
     */
    @PostMapping("/msg-unread/set-notification")
    public ResponseResult setNotificationStatus(@RequestParam("column") String column,
                                                @RequestParam("status") Boolean status) {
        Integer uid = currentUser.getUserId();
        //messageUnreadService.setNotificationStatus(uid, column, status);
        ResponseResult responseResult = new ResponseResult();
        responseResult.setMessage("消息提醒状态已更新");
        return responseResult;
    }

    /**
     * 获取某一列的未读消息提醒状态
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     * @return 响应结果，包含消息提醒状态
     */
    @GetMapping("/msg-unread/notification-status")
    public ResponseResult getNotificationStatus(@RequestParam("column") String column) {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        //responseResult.setData(messageUnreadService.getNotificationStatus(uid, column));
        return responseResult;
    }

    /**
     * 为多个列批量设置未读消息提醒的状态
     * @param columns   msg_unread表列名列表
     * @param status    提醒状态，true表示开启提醒，false表示关闭提醒
     * @return 响应结果，表示批量设置操作是否成功
     */
    @PostMapping("/msg-unread/set-notification-batch")
    public ResponseResult setBatchNotificationStatus(@RequestParam("columns") String[] columns,
                                                     @RequestParam("status") Boolean status) {
        Integer uid = currentUser.getUserId();
        for (String column : columns) {
            if(success){
            //messageUnreadService.setNotificationStatus(uid, column, status);
            }
        }
        ResponseResult responseResult = new ResponseResult();
        responseResult.setMessage("批量消息提醒状态已更新");
        return responseResult;
    }

    /**
     * 获取当前用户所有列的未读消息提醒状态
     * @return 响应结果，包含所有列的消息提醒状态
     */
    @GetMapping("/msg-unread/all-notification-status")
    public ResponseResult getAllNotificationStatuses() {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        // responseResult.setData(messageUnreadService.getAllNotificationStatuses(uid));
        return responseResult;
    }

    /**
     * 重置某一列的未读消息数为指定值
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     * @param count     要重置的未读消息数
     * @return 响应结果，表示重置操作是否成功
     */
    @PostMapping("/msg-unread/reset")
    public ResponseResult resetUnreadCount(@RequestParam("column") String column,
                                           @RequestParam("count") Integer count) {
        Integer uid = currentUser.getUserId();
        //messageUnreadService.resetUnreadCount(uid, column, count);
        ResponseResult responseResult = new ResponseResult();
        responseResult.setMessage("未读消息数已重置");
        return responseResult;
    }

    /**
     * 批量重置多个列的未读消息数为指定值
     * @param columns   msg_unread表列名列表
     * @param counts    对应列的未读消息数
     * @return 响应结果，表示批量重置操作是否成功
     */
    @PostMapping("/msg-unread/reset-batch")
    public ResponseResult resetBatchUnreadCounts(@RequestParam("columns") String[] columns,
                                                 @RequestParam("counts") Integer[] counts) {
        Integer uid = currentUser.getUserId();
        for (int i = 0; i < columns.length; i++) {
            //messageUnreadService.resetUnreadCount(uid, columns[i], counts[i]);
        }
        ResponseResult responseResult = new ResponseResult();
        responseResult.setMessage("批量未读消息数已重置");
        return responseResult;
    }

    /**
     * 增加某一列的未读消息数
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     * @param increment 增加的未读消息数
     * @return 响应结果，表示增加操作是否成功
     */
    @PostMapping("/msg-unread/increment")
    public ResponseResult incrementUnreadCount(@RequestParam("column") String column,
                                               @RequestParam("increment") Integer increment) {
        Integer uid = currentUser.getUserId();
        //messageUnreadService.incrementUnreadCount(uid, column, increment);
        ResponseResult responseResult = new ResponseResult();
        responseResult.setMessage("未读消息数已增加");
        return responseResult;
    }

    /**
     * 批量增加多个列的未读消息数
     * @param columns   msg_unread表列名列表
     * @param increments 对应列的增加值
     * @return 响应结果，表示批量增加操作是否成功
     */
    @PostMapping("/msg-unread/increment-batch")
    public ResponseResult incrementBatchUnreadCounts(@RequestParam("columns") String[] columns,
                                                     @RequestParam("increments") Integer[] increments) {
        Integer uid = currentUser.getUserId();
        for (int i = 0; i < columns.length; i++) {
            //messageUnreadService.incrementUnreadCount(uid, columns[i], increments[i]);
        }
        ResponseResult responseResult = new ResponseResult();
        responseResult.setMessage("批量未读消息数已增加");
        return responseResult;
    }
    //-----------------------------------------------------------------------------------------


    /**
     * 获取当前用户全部消息未读数
     * @return
     */
    @GetMapping("/msg-unread/all")
    public ResponseResult getMsgUnread() {
        //System.out.println("已进入getMsgUnread");
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData(messageUnreadService.getUnreadByUid(uid));
        return responseResult;
    }

    /**
     * 清除某一列的未读消息提示
     * @param column    msg_unread表列名 "reply"/"at"/"love"/"system"/"whisper"/"dynamic"
     */
    @PostMapping("/msg-unread/clear")
    public void clearUnread(@RequestParam("column") String column) {
        Integer uid = currentUser.getUserId();
        messageUnreadService.clearOneUnread(uid, column);
    }







}
