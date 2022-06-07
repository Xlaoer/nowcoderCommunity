package top.xlaoer.nowcodercommunity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import top.xlaoer.nowcodercommunity.entity.Event;
import top.xlaoer.nowcodercommunity.entity.User;
import top.xlaoer.nowcodercommunity.event.EventProducer;
import top.xlaoer.nowcodercommunity.service.LikeService;
import top.xlaoer.nowcodercommunity.util.CommunityConstant;
import top.xlaoer.nowcodercommunity.util.CommunityUtil;
import top.xlaoer.nowcodercommunity.util.HostHolder;
import top.xlaoer.nowcodercommunity.util.RedisKeyUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Xlaoer
 * @date 2022/5/20 16:04
 */
@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping(value = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId,int entityUserId, int postId) {
        //这里不做权限判断，课程后面会统一的进行权限控制
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId,entityUserId);

        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        // 触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 如果是对帖子点赞时，需重新计算帖子分数
        if (entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数，将其加入缓存队列
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}
