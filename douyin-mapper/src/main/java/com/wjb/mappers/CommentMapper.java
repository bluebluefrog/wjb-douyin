package com.wjb.mappers;


import com.wjb.my.mapper.MyMapper;
import com.wjb.pojo.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMapper extends MyMapper<Comment> {
}