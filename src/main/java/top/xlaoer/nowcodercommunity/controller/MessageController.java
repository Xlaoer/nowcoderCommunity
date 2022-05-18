package top.xlaoer.nowcodercommunity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import top.xlaoer.nowcodercommunity.entity.Message;
import top.xlaoer.nowcodercommunity.entity.Page;
import top.xlaoer.nowcodercommunity.entity.User;
import top.xlaoer.nowcodercommunity.service.MessageService;
import top.xlaoer.nowcodercommunity.service.UserService;
import top.xlaoer.nowcodercommunity.util.CommunityUtil;
import top.xlaoer.nowcodercommunity.util.HostHolder;

import java.util.*;

/**
 * @author Xlaoer
 * @date 2022/5/18 16:36
 */
@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // 私信列表
    @RequestMapping(value = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {

        User user = hostHolder.getUser();

        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        int totalCount = 0;

        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                // 查询对应会话的消息总数量
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                // 查询对应会话的未读消息数量
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                // 和当前用户对话的用户id
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);
        // 查询当前用户的未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        return "/site/letter";
    }

    @RequestMapping(value = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 设置分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));
        // 设置未读消息已读
        List<Integer> ids = getUnreadLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    private User getLetterTarget(String conversationId) {
        // 拆分会话id，得到fromId和toId
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    private List<Integer> getUnreadLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                // 如果当前用户是接收者 并且 这条消息未读
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    @RequestMapping(value = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {

        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());

        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(value = "/letter/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteLetter(String id) {
        int mid = Integer.parseInt(id);
        Message tobeDeletedLetter = messageService.findLetterById(mid);
        if(tobeDeletedLetter==null){
            return CommunityUtil.getJSONString(1, "消息不存在");
        }
        if(tobeDeletedLetter.getFromId()!=hostHolder.getUser().getId()){
            return CommunityUtil.getJSONString(1, "不能删除别人发送的消息哦");
        }
        messageService.deleteMessage(mid);
        return CommunityUtil.getJSONString(0);
    }
}
