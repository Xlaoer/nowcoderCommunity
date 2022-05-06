package top.xlaoer.nowcodercommunity.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @author Xlaoer
 * @date 2022/5/6 11:28
 */
public class CommunityUtil {
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    // MD5加密
    public static String md5(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(s.getBytes());
    }
}
