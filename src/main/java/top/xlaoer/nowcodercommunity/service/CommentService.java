package top.xlaoer.nowcodercommunity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.xlaoer.nowcodercommunity.dao.CommentMapper;
import top.xlaoer.nowcodercommunity.entity.Comment;

import java.util.List;

/**
 * @author Xlaoer
 * @date 2022/5/17 14:22
 */
@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCommentByEntity(entityType, entityId);
    }
}
