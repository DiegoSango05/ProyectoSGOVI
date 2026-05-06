package es.uji.ei1027.sps.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/")
    public String index(HttpSession session) {
        // Si no hay nadie logueado, enseñamos la Landing Page (index.html)
        if (session.getAttribute("user") == null) {
            return "index";
        }

        // Si ya hay una sesión, redirigimos según el rol para que no tengan que pasar por la landing
        String role = (String) session.getAttribute("role");

        if ("admin".equals(role)) {
            return "index-admin";
        } else if ("asistente".equals(role)) {
            return "redirect:/pap_assistant/dashboard";
        } else if ("ovi".equals(role)) {
            return "redirect:/ovi/dashboard";
        }

        return "index";
    }

    @RequestMapping("/profiles-management")
    public String profilesManagement() {
        return "profiles-management";
    }
}