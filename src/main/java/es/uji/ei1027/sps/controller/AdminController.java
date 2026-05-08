package es.uji.ei1027.sps.controller;

import jakarta.servlet.http.HttpSession;
import es.uji.ei1027.sps.model.SystemUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin") // Añadimos esto para que todas sus rutas empiecen por /admin
public class AdminController {

    @RequestMapping("/index")
    public String indexAdmin(HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }
        model.addAttribute("adminName", user.getName());
        return "admin/index";
    }
}