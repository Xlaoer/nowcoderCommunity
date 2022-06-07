package top.xlaoer.nowcodercommunity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import top.xlaoer.nowcodercommunity.dao.DiscussPostMapper;
import top.xlaoer.nowcodercommunity.dao.LoginTicketMapper;
import top.xlaoer.nowcodercommunity.dao.UserMapper;
import top.xlaoer.nowcodercommunity.entity.DiscussPost;
import top.xlaoer.nowcodercommunity.entity.LoginTicket;
import top.xlaoer.nowcodercommunity.entity.User;

import java.util.Date;
import java.util.List;

/**
 * @author Xlaoer
 * @date 2022/5/3 20:09
 */
@SpringBootTest
@ContextConfiguration(classes = NowcoderCommunityApplication.class)
class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void selectById() {
        User user = userMapper.selectById(1);
        System.out.println(user);

    }

    @Test
    public void insertUser() {
        User user = new User();
        user.setUsername("xlaoer");
        user.setCreateTime(new Date());
        user.setEmail("makjys@gmail.com");
        user.setPassword("test");
        user.setType(1);
        userMapper.insertUser(user);

    }

    @Test
    public void selectDiscussPost() {
        int i = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(i);
        int i1 = discussPostMapper.selectDiscussPostRows(1);
        System.out.println(i1);
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(1, 1, 10,0);
//        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(101, 1, 10);
//        if(discussPosts.isEmpty())return;
        for(DiscussPost discussPost:discussPosts){
            System.out.println(discussPost);
        }
    }

    @Test
    public void insertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket("test");
        loginTicket.setUserId(1);
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date());
        loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println(loginTicket.getId());
    }
}