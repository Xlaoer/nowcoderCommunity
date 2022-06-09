package top.xlaoer.nowcodercommunity.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import top.xlaoer.nowcodercommunity.dao.DiscussPostMapper;
import top.xlaoer.nowcodercommunity.entity.DiscussPost;
import top.xlaoer.nowcodercommunity.entity.ReplyPostResult;
import top.xlaoer.nowcodercommunity.util.RedisKeyUtil;
import top.xlaoer.nowcodercommunity.util.SensitiveFilter;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Xlaoer
 * @date 2022/5/3 20:25
 */
@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeien核心接口：Cache，常用子接口：LoadingCache,AsyncLoadingCache

    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数的缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        Caffeine<Object, Object> objectObjectCaffeine = Caffeine.newBuilder();
        objectObjectCaffeine.maximumSize(maxSize);
        // 多长时间自动过期
        objectObjectCaffeine.expireAfterWrite(expireSeconds, TimeUnit.SECONDS);
        postListCache = objectObjectCaffeine.build(new CacheLoader<String, List<DiscussPost>>() {
            // 缓存的数据来源，从数据库中查询数据
            @Nullable
            @Override
            public List<DiscussPost> load(@NonNull String key) throws Exception {
                if (key == null || key.length() == 0) {
                    throw new IllegalArgumentException("参数错误！");
                }

                String[] params = key.split(":");
                if (params == null || params.length != 2) {
                    throw new IllegalArgumentException("参数错误！");
                }

                int offset = Integer.valueOf(params[0]);
                int limit = Integer.valueOf(params[1]);


                // 可以在这里加上二级缓存：Redis，先查询Redis中是否有数据，如果没有再到数据库中查询
                String hotPostKey = RedisKeyUtil.getHotPostKey();
                List<DiscussPost> list = redisTemplate.opsForList().range(hotPostKey, offset, offset + limit - 1);
                if (list == null || list.size() == 0) {
                    logger.debug("load post list from DB.");
                    list = discussPostMapper.selectDiscussPosts(0, offset, limit, 1);

                    // 将数据存入Redis
                    redisTemplate.opsForList().rightPushAll(hotPostKey, list);
                    // 设置过期时间
                    redisTemplate.expire(hotPostKey, expireSeconds, TimeUnit.SECONDS);
                }

                return list;
            }
        });

        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        // 仅仅只在查询首页热门帖子的时候查询缓存
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }

        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    public int findDiscussPostRows(int userId) {
        // 仅仅只有在首页查询行数的时候查询缓存
        if (userId == 0) {
            return postRowsCache.get(userId);
        }

        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int insertDiscussPost(DiscussPost discussPost){
        if(discussPost == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        // 转义HTML标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost findDiscussPostById(int discussPostId) {
        return discussPostMapper.selectDiscussPostById(discussPostId);
    }

    public int updateCommentCount(int entityId, int commentCount) {
        return discussPostMapper.updateCommentCount(entityId,commentCount);
    }

    public List<ReplyPostResult> findReplyDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectReplyDiscussPosts(userId,offset,limit);
    }

    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double score){
        return discussPostMapper.updateScore(id,score);
    }

}
