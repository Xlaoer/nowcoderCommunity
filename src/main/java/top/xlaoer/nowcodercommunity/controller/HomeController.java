package top.xlaoer.nowcodercommunity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import top.xlaoer.nowcodercommunity.entity.DiscussPost;
import top.xlaoer.nowcodercommunity.entity.Page;
import top.xlaoer.nowcodercommunity.entity.User;
import top.xlaoer.nowcodercommunity.service.DiscussPostService;
import top.xlaoer.nowcodercommunity.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Xlaoer
 * @date 2022/5/3 19:17
 */
@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        // url中的current参数自动封装到page中
        // 方法调用之前，SpringMVC会自动实例化Model和Page，并将Page注入Model（自动将方法中的参数注入到Model）
        // 所以，在thymeleaf中可以直接访问Page对象中的数据（而不需要手动将Page添加到Model里面）
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null){
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }
}
