package top.xlaoer.nowcodercommunity.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
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
import top.xlaoer.nowcodercommunity.util.RedisKeyUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    public User findUserById(int id) {
//        return userMapper.selectById(id);
        //redis缓存用户信息
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
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
                //用户信息被更改，删除redis缓存
                clearCache(id);
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
//        loginTicketMapper.insertLoginTicket(loginTicket);
        //将凭证存入redis中
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        // redis重构
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        //redis重构
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeaderUrl(int userId,String headerUrl){
        clearCache(userId);
        return userMapper.updateHeader(userId,headerUrl);
    }

    public int updatePassword(int userId,String password){
        return userMapper.updatePassword(userId,password);
    }

    public User selectByEmail(String email){
        return userMapper.selectByEmail(email);
    }

    public void sendForgetMail(String email,String code) {
        //验证有效性
        if (StringUtils.isBlank(email)) {
//            throw new IllegalArgumentException("email参数为空");
            return;
        }
        if (StringUtils.isBlank(code)) {
//            throw new IllegalArgumentException("code参数为空");
            return;
        }

        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("code", code);

        String process = templateEngine.process("/mail/forget", context);
        mailClient.sendEmail(email,"找回帐号", process, true);
    }

    public User findUserByName(String username){
        return userMapper.selectByUserName(username);
    }

    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    // 获得用户的权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();

        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });

        return list;
    }
}
