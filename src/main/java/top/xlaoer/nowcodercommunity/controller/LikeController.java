package top.xlaoer.nowcodercommunity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import top.xlaoer.nowcodercommunity.entity.User;
import top.xlaoer.nowcodercommunity.service.LikeService;
import top.xlaoer.nowcodercommunity.util.CommunityUtil;
import top.xlaoer.nowcodercommunity.util.HostHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Xlaoer
 * @date 2022/5/20 16:04
 */
@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(value = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId) {
        //这里不做权限判断，课程后面会统一的进行权限控制
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId);

        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        return CommunityUtil.getJSONString(0, null, map);
    }
}
