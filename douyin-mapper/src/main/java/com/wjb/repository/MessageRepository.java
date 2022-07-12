package com.wjb.repository;

import com.wjb.mo.MessageMO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<MessageMO, String> {//需要跟泛型草丛的类型和主键类型

    //通过实现Repository自定义条件进行查询
    //第二个参数Pageable用于构建分页查询
    List<MessageMO> findAllByToUserIdOrderByCreateTimeDesc(String toUserId, Pageable pageable);
}
