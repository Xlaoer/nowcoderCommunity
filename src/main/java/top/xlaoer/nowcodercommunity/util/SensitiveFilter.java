package top.xlaoer.nowcodercommunity.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Xlaoer
 * @date 2022/5/12 14:08
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 敏感词替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                // 类加载器会从类路径（classes目录）下加载资源，getResourceAsStream("文件名")可以加载到文件的字节流
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }

    }

    private void addKeyword(String keyword) {
        TrieNode curNode = rootNode;
        for(int i=0;i<keyword.length();i++){
            if(curNode.getChildren((keyword.charAt(i)))==null){
                TrieNode node = new TrieNode();
                if(i==keyword.length()-1)node.setKeyword(true);
                curNode.setChildren(keyword.charAt(i),node);
                curNode = node;
            }else{
                curNode=curNode.getChildren(keyword.charAt(i));
            }
        }
    }

    public String filter(String origin){
        if(StringUtils.isBlank(origin)){
            return null;
        }
        int begin = 0;
        int end = 0;
        TrieNode tmp = rootNode;
        StringBuilder sb = new StringBuilder();
        while(begin<origin.length()){
            if(end<origin.length()) {
                char c = origin.charAt(end);
                if (isSymbol(c)) {
                    if (begin == end) {
                        ++begin;
                        sb.append(c);
                    }
                    ++end;
                    continue;
                }

                tmp = tmp.getChildren(c);
                if (tmp == null) {
                    // 说明以begin开头的字符串不是敏感词
                    sb.append(origin.charAt(begin));
                    // 进入下一个位置
                    end = ++begin;
                    // 指针1重新指向根节点
                    tmp = rootNode;
                } else if (tmp.isKeyword()) {
                    sb.append(REPLACEMENT);
                    begin = ++end;
                    tmp = rootNode;
                } else {
                    // 处于匹配过程中，则position继续向下走
                    end++;
                }
            }else{
                // position已经走到结尾，但是begin还未到结尾，说明这段字符串未成功匹配到敏感词
                // 则将bengin位置上的字符加入结果中，然后指针继续向下走
                sb.append(origin.charAt(begin));
                end = ++begin;
                tmp = rootNode;
            }
        }
        return sb.toString();
    }

    private boolean isSymbol(Character c) {
        // isAsciiAlphanumeric方法判断字符是否是一个普通（合法）字符，0x2E80~0x9FFF 是东亚文字范围，包括中文，日文，韩文等等
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    private class TrieNode{
        //标记是否是结束节点
        private boolean isKeyword = false;

        //孩子节点
        private Map<Character,TrieNode> children = new HashMap<>();

        public boolean isKeyword() {
            return isKeyword;
        }

        public void setKeyword(boolean keyword) {
            isKeyword = keyword;
        }

        public TrieNode getChildren(Character c) {
            return this.children.get(c);
        }

        public void setChildren(Character c,TrieNode node) {
            this.children.put(c,node);
        }
    }



}
