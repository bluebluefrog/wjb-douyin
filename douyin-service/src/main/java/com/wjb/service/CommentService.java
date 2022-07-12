package com.wjb.service;

import com.wjb.bo.CommentBO;
import com.wjb.pojo.Comment;
import com.wjb.utils.PagedGridResult;
import com.wjb.vo.CommentVO;

public interface CommentService {

    public CommentVO createComment (CommentBO commentBO);

    public PagedGridResult queryVlogComments(String vlogId, String userId,Integer page, Integer pageSize);

    public void deleteComments(String commentUserId, String commentId, String vlogId);

    Comment getComment(String id);
}
