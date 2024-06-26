package top.xlaoer.nowcodercommunity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import top.xlaoer.nowcodercommunity.dao.CommentMapper;
import top.xlaoer.nowcodercommunity.entity.Comment;
import top.xlaoer.nowcodercommunity.util.CommunityConstant;
import top.xlaoer.nowcodercommunity.util.SensitiveFilter;

import java.util.List;

/**
 * @author Xlaoer
 * @date 2022/5/17 14:22
 */
@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCommentByEntity(entityType, entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 添加评论
        // 转义HTML标记
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        // 过滤敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // 更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCommentByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }

    public int findPostCommentCountByUserId(int userId, int entityType) {
        return commentMapper.selectPostCommentCountByUserId(userId, entityType);
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
