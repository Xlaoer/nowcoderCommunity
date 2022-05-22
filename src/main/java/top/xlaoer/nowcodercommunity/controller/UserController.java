package top.xlaoer.nowcodercommunity.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import top.xlaoer.nowcodercommunity.annotaion.LoginRequired;
import top.xlaoer.nowcodercommunity.entity.User;
import top.xlaoer.nowcodercommunity.service.FollowService;
import top.xlaoer.nowcodercommunity.service.LikeService;
import top.xlaoer.nowcodercommunity.service.UserService;
import top.xlaoer.nowcodercommunity.util.CommunityConstant;
import top.xlaoer.nowcodercommunity.util.CommunityUtil;
import top.xlaoer.nowcodercommunity.util.HostHolder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;


/**
 * @author Xlaoer
 * @date 2022/5/11 13:54
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.uploadPath}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSetting(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String upload(MultipartFile headerImage, Model model){
        if(headerImage==null||headerImage.isEmpty()){
            model.addAttribute("error","图片不能为空");
            return "/site/setting";
        }
        String originalFilename = headerImage.getOriginalFilename();
        String format = originalFilename.substring(originalFilename.lastIndexOf('.'));
        if(StringUtils.isBlank(format)){
            model.addAttribute("error","文件格式出错");
            return "/site/setting";
        }
        String filename = CommunityUtil.generateUUID()+format;
        File dir = new File(uploadPath);
        if(!dir.exists()){
            dir.mkdir();
        }
        File file = new File(uploadPath+"/"+filename);
        try {
            headerImage.transferTo(file);
            String headUrl = "http://"+domain+contextPath+"/user/header/"+filename;
            //修改用户的
            userService.updateHeaderUrl(hostHolder.getUser().getId(),headUrl);
        } catch (IOException e) {
            logger.error("存储文件失败"+e.getMessage());
            model.addAttribute("error","存储文件失败");
            return "/site/setting";
        }
        return "redirect:/index";
    }

    @RequestMapping(path="/header/{filename}",method = RequestMethod.GET)
    public void getHeader(HttpServletResponse response, @PathVariable("filename")String filename){
        String suffix = filename.substring(filename.lastIndexOf('.')+1);
        response.setContentType("image/"+suffix);
        filename = uploadPath+"/"+filename;
        try(
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(filename);
                ){
            byte[] buffer = new byte[2048];
            int len;
            while((len = fis.read(buffer))!=-1){
                os.write(buffer,0,len);
            }


        }catch (Exception e){
            logger.error("获取头像失败"+e.getMessage());
        }

    }

    @LoginRequired
    @RequestMapping(path = "/update",method = RequestMethod.POST)
    public String updatePassword(String oldpassword,String newpassword,Model model){
        if(StringUtils.isBlank(oldpassword)){
            model.addAttribute("oldpasswordMsg","旧密码为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newpassword)){
            model.addAttribute("newpasswordMsg","新密码为空");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        if(!user.getPassword().equals(CommunityUtil.md5(oldpassword+user.getSalt()))){
            model.addAttribute("oldpasswordMsg","密码错误");
            return "/site/setting";
        }
        userService.updatePassword(user.getId(),CommunityUtil.md5(newpassword+user.getSalt()));
        return "redirect:/index";
    }

    // 个人主页
    @RequestMapping(value = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);


        return "/site/profile";
    }


}
