package top.xlaoer.nowcodercommunity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import top.xlaoer.nowcodercommunity.entity.DiscussPost;
import top.xlaoer.nowcodercommunity.entity.Page;
import top.xlaoer.nowcodercommunity.service.ElasticsearchService;
import top.xlaoer.nowcodercommunity.service.LikeService;
import top.xlaoer.nowcodercommunity.service.UserService;
import top.xlaoer.nowcodercommunity.util.CommunityConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Xlaoer
 * @date 2022/5/26 10:41
 */
@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // /search?keyword=
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page , Model model){
        // 查找帖子
        List<Object> list = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() -1, page.getLimit());

        // 聚合数据
        List<DiscussPost> tempPosts = (List<DiscussPost>) list.get(1);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(tempPosts.size() > 0){
            for(DiscussPost post : tempPosts){
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // 设置分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(list.get(0) == null ? 0 : (int)list.get(0));
        return "/site/search";
    }
}