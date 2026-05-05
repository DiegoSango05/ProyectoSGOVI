package es.uji.ei1027.sps.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "index-admin";
    }

    @RequestMapping("/profiles-management")
    public String profilesManagement() {
        return "profiles-management";
    }
}
