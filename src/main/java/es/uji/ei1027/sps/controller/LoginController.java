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

        // 1. COMPROBAR ADMINISTRADORES (Múltiples administradores desde DB)
        SystemUser admin = systemUserDao.loadUserByUsername(username, password);
        if (admin != null) {
            session.setAttribute("user", admin); // Guardamos el objeto completo
            session.setAttribute("role", "admin");
            return "redirect:/admin/index";
        }

        // 2. COMPROBAR OVIUSER (DNI + Password)
        OVIUser ovi = oviUserDao.loadUserByUsername(username, password);
        if (ovi != null) {
            session.setAttribute("user", ovi);
            session.setAttribute("role", "ovi");
            return "redirect:/oviuser/index";
        }

        // 3. COMPROBAR PAPASSISTANT (DNI + Password + Estados)
        PAPAssistant pap = papAssistantDao.loadUserByUsername(username, password);
        if (pap != null) {
            session.setAttribute("user", pap);
            session.setAttribute("role", "asistente");

            switch (pap.getStatus()) {
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
