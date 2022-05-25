package top.xlaoer.nowcodercommunity.dao.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import top.xlaoer.nowcodercommunity.entity.DiscussPost;

/**
 * @author Xlaoer
 * @date 2022/5/25 15:18
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {
}