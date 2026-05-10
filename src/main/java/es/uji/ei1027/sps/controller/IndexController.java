package es.uji.ei1027.sps.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/")
    public String index(HttpSession session) {
        // Si no hay nadie logueado, enseñamos la Landing Page (templates/index.html)
        if (session.getAttribute("user") == null) {
            return "index";
        }

        // Si ya hay una sesión, redirigimos a la RUTA del controlador, no al archivo
        String role = (String) session.getAttribute("role");

        if ("admin".equals(role)) {
            // REDIRECCIÓN a la URL que gestiona el AdminController
            return "redirect:/admin/index";
        } else if ("asistente".equals(role)) {
            // REDIRECCIÓN a la URL que gestiona el PAPAssistantController
            return "redirect:/pap_assistant/index";
        } else if ("ovi".equals(role)) {
            // REDIRECCIÓN a la URL que gestiona el OVIUserController
            return "redirect:/oviuser/index";
        }

        return "index";
    }

    @RequestMapping("/profiles-management")
    public String profilesManagement() {
        return "profiles-management";
    }
}