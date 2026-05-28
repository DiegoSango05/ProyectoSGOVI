package es.uji.ei1027.sps.controller;

import jakarta.servlet.http.HttpSession;
import es.uji.ei1027.sps.dao.OVIUserDao;
import es.uji.ei1027.sps.dao.PAPAssistantDao;
import es.uji.ei1027.sps.dao.SystemUserDao; // 1. Importamos el nuevo DAO
import es.uji.ei1027.sps.model.OVIUser;
import es.uji.ei1027.sps.model.PAPAssistant;
import es.uji.ei1027.sps.model.SystemUser; // 2. Importamos el modelo
import es.uji.ei1027.sps.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LoginController {

    @Autowired
    private SystemUserDao systemUserDao; // 3. Inyectamos el DAO de gestión

    @Autowired
    private OVIUserDao oviUserDao;

    @Autowired
    private PAPAssistantDao papAssistantDao;

    @RequestMapping("/login")
    public String login(Model model) {
        model.addAttribute("user", new UserDetails());
        return "login";
    }

    @RequestMapping(value="/login", method=RequestMethod.POST)
    public String checkLogin(@ModelAttribute("user") UserDetails userDetails,
                             BindingResult bindingResult, HttpSession session) {

        String username = userDetails.getUsername().trim();
        String password = userDetails.getPassword().trim();

        if (username.isEmpty() || password.isEmpty()) {
            bindingResult.rejectValue("username", "empty", "Debes introducir usuario y contraseña");
            return "login";
        }

        // 1. COMPROBAR ADMINISTRADORES
        SystemUser admin = systemUserDao.loadUserByUsername(username, password);
        if (admin != null) {
            session.setAttribute("user", admin);
            session.setAttribute("role", "admin");
            return "redirect:/admin/index";
        }

        // 2. COMPROBAR OVIUSER
        OVIUser ovi = oviUserDao.loadUserByUsername(username, password);
        if (ovi != null) {
            session.setAttribute("user", ovi);
            session.setAttribute("role", "ovi");

            // BLINDAJE: Si el status es null, lo tratamos como "Pending" por defecto
            String status = (ovi.getStatus() != null) ? ovi.getStatus() : "Pending";

            switch (status) {
                case "Pending":
                    return "oviuser/status-pending";
                case "Rejected":
                    return "oviuser/status-rejected";
                case "Accepted":
                    return "redirect:/oviuser/index";
                default:
                    return "oviuser/status-pending";
            }
        }

        // 3. COMPROBAR PAPASSISTANT
        PAPAssistant pap = papAssistantDao.loadUserByUsername(username, password);
        if (pap != null) {
            session.setAttribute("user", pap);
            session.setAttribute("role", "asistente");

            // BLINDAJE: Si el status es null, lo tratamos como "Pending" por defecto
            String statusPap = (pap.getStatus() != null) ? pap.getStatus() : "Pending";

            switch (statusPap) {
                case "Pending": return "pap_assistant/status_pending";
                case "Rejected": return "pap_assistant/status_rejected";
                case "Accepted": return "redirect:/pap_assistant/index";
                default: return "pap_assistant/status_pending";
            }
        }

        bindingResult.rejectValue("password", "badpw", "Usuario o contraseña no válidos");
        return "login";
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/"; // Mejor redirigir a la Landing Page al cerrar sesión
    }
}
