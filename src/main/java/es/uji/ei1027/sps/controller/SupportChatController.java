package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.SupportChatDao;
import es.uji.ei1027.sps.model.OVIUser;
import es.uji.ei1027.sps.model.PAPAssistant;
import es.uji.ei1027.sps.model.SupportChat;
import es.uji.ei1027.sps.model.SystemUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/support-chats")
public class SupportChatController {
    private static final String ADMIN_SENDER = "Administrador";

    @Autowired
    private SupportChatDao supportChatDao;

    @RequestMapping("/list")
    public String list(@RequestParam(required = false) Integer idChat, HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        Object user = session.getAttribute("user");
        List<SupportChat> chats;
        String ownSender;

        if ("admin".equals(role) && user instanceof SystemUser) {
            chats = supportChatDao.getSupportChats();
            ownSender = ADMIN_SENDER;
            model.addAttribute("adminView", true);
            model.addAttribute("backUrl", "/admin/index");
        } else if ("ovi".equals(role) && user instanceof OVIUser oviUser) {
            chats = supportChatDao.getSupportChatsByParticipant(oviUser.getDni(), "OVI");
            ownSender = oviUser.getDni();
            model.addAttribute("adminView", false);
            model.addAttribute("backUrl", "/oviuser/chats");
        } else if ("asistente".equals(role) && user instanceof PAPAssistant assistant) {
            chats = supportChatDao.getSupportChatsByParticipant(assistant.getDni(), "PAP");
            ownSender = assistant.getDni();
            model.addAttribute("adminView", false);
            model.addAttribute("backUrl", "/pap_assistant/chats");
        } else {
            return "redirect:/login";
        }

        SupportChat selectedChat = getSelectedChat(idChat, chats);
        model.addAttribute("chats", chats);
        model.addAttribute("selectedChat", selectedChat);
        model.addAttribute("ownSender", ownSender);
        model.addAttribute("messages", selectedChat == null
                ? List.of()
                : supportChatDao.getMessagesByChat(selectedChat.getId()));
        return "support-chat/list";
    }

    @PostMapping("/send")
    public String send(@RequestParam int idChat, @RequestParam String message, HttpSession session) {
        String role = (String) session.getAttribute("role");
        Object user = session.getAttribute("user");
        List<SupportChat> chats;
        String sender;

        if ("admin".equals(role) && user instanceof SystemUser) {
            chats = supportChatDao.getSupportChats();
            sender = ADMIN_SENDER;
        } else if ("ovi".equals(role) && user instanceof OVIUser oviUser) {
            chats = supportChatDao.getSupportChatsByParticipant(oviUser.getDni(), "OVI");
            sender = oviUser.getDni();
        } else if ("asistente".equals(role) && user instanceof PAPAssistant assistant) {
            chats = supportChatDao.getSupportChatsByParticipant(assistant.getDni(), "PAP");
            sender = assistant.getDni();
        } else {
            return "redirect:/login";
        }

        if (message != null && !message.trim().isEmpty() && getSelectedChat(idChat, chats) != null) {
            supportChatDao.addMessage(idChat, sender, message.trim());
        }
        return "redirect:/support-chats/list?idChat=" + idChat;
    }

    private SupportChat getSelectedChat(Integer requestedId, List<SupportChat> chats) {
        if (chats.isEmpty()) {
            return null;
        }
        if (requestedId != null) {
            for (SupportChat chat : chats) {
                if (chat.getId() == requestedId) {
                    return chat;
                }
            }
        }
        return chats.get(0);
    }
}
