package top.xlaoer.nowcodercommunity.dao;

import org.apache.ibatis.annotations.Mapper;
import top.xlaoer.nowcodercommunity.entity.Comment;

import java.util.List;

/**
 * @author Xlaoer
 * @date 2022/5/17 14:18
 */
@Mapper
public interface CommentMapper {
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCommentByEntity(int entityType, int entityId);

    int insertComment(Comment comment);
}
