package top.xlaoer.nowcodercommunity.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import top.xlaoer.nowcodercommunity.dao.LoginTicketMapper;
import top.xlaoer.nowcodercommunity.dao.UserMapper;
import top.xlaoer.nowcodercommunity.entity.LoginTicket;
import top.xlaoer.nowcodercommunity.entity.User;
import top.xlaoer.nowcodercommunity.util.CommunityConstant;
import top.xlaoer.nowcodercommunity.util.CommunityUtil;
import top.xlaoer.nowcodercommunity.util.MailClient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Xlaoer
 * @date 2022/5/3 20:25
 */
@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Value("${activation.domain}")
    private String domain;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        HashMap<String, Object> map = new HashMap<>();
        //验证有效性
        if (user == null) {
            throw new IllegalArgumentException("user参数为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }
        if (userMapper.selectByUserName(user.getUsername()) != null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }
        if (userMapper.selectByEmail(user.getEmail()) != null) {
            map.put("emailMsg", "该邮箱已注册");
            return map;
        }


        //注册
        user.setCreateTime(new Date());
        user.setSalt(CommunityUtil.generateUUID().substring(0, 6));
        user.setType(0);
        user.setStatus(0);
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setActivationCode(CommunityUtil.generateUUID());
        userMapper.insertUser(user);


        Context context = new Context();
        context.setVariable("email", user.getEmail());
        context.setVariable("url", domain + "/" + user.getId() + "/" + user.getActivationCode());

        String process = templateEngine.process("/mail/activation", context);
        mailClient.sendEmail(user.getEmail(), "激活帐号", process, true);

        return map;
    }

    public int activation(int id, String code) {
        if(id<0||StringUtils.isBlank(code))return ACTIVATION_FAILURE;
        User user = userMapper.selectById(id);
        if (code.equals(user.getActivationCode())) {
            if (user.getStatus() == 0) {
                userMapper.updateStatus(id, 1);
                return ACTIVATION_SUCCESS;
            } else {
                return ACTIVATION_REPEAT;
            }
        }
        return CommunityConstant.ACTIVATION_FAILURE;
    }

    public Map<String, Object> login(String username, String password, long expiredSeconds) {

        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByUserName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }

        // 判断账号是否激活
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        // 将登录凭证保存到mysql
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }
}
