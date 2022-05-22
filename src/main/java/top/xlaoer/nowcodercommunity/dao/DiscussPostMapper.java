package top.xlaoer.nowcodercommunity.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.xlaoer.nowcodercommunity.entity.DiscussPost;
import top.xlaoer.nowcodercommunity.entity.ReplyPostResult;

import java.util.List;

/**
 * @author Xlaoer
 * @date 2022/5/3 19:54
 */
@Mapper
public interface DiscussPostMapper {

    //当方法只有一个参数时，并且该参数应用在<if>上时，就必须用@Param注解为该参数取别名
    int selectDiscussPostRows(@Param("userId") int userId);

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

    List<ReplyPostResult> selectReplyDiscussPosts(int userId, int offset, int limit);
}
