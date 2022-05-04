package top.xlaoer.nowcodercommunity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.xlaoer.nowcodercommunity.dao.DiscussPostMapper;
import top.xlaoer.nowcodercommunity.entity.DiscussPost;

import java.util.List;

/**
 * @author Xlaoer
 * @date 2022/5/3 20:25
 */
@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

}
