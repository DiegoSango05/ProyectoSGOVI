package es.uji.ei1027.sps.controller;

import jakarta.servlet.http.HttpSession;
import es.uji.ei1027.sps.dao.OVIUserDao;
import es.uji.ei1027.sps.dao.PAPAssistantDao;
import es.uji.ei1027.sps.model.OVIUser;
import es.uji.ei1027.sps.model.PAPAssistant;
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

        // 1. COMPROBAR ADMIN
        if (username.equals("admin") && password.equals("admin")) {
            session.setAttribute("user", "Administrador");
            session.setAttribute("role", "admin");
            return "redirect:/";
        }

        // 2. COMPROBAR OVIUSER
        OVIUser ovi = oviUserDao.loadUserByUsername(username, password);
        if (ovi != null) {
            session.setAttribute("user", ovi);
            session.setAttribute("role", "ovi");
            return "redirect:/ovi/dashboard";
        }

        // 3. COMPROBAR PAPASSISTANT (Modificado para estados)
        PAPAssistant pap = papAssistantDao.loadUserByUsername(username, password);
        if (pap != null) {
            session.setAttribute("user", pap);
            session.setAttribute("role", "asistente");

            // Lógica de filtrado por estado de candidatura
            switch (pap.getStatus()) {
                case "Pending":
                    return "pap_assistant/status_pending";
                case "Rejected":
                    return "pap_assistant/status_rejected";
                case "Accepted":
                    return "redirect:/pap_assistant/dashboard";
                default:
                    // Por seguridad, si el estado no es ninguno de los anteriores
                    return "pap_assistant/status_pending";
            }
        }

        // 4. SI LLEGA AQUÍ: CREDENCIALES INVÁLIDAS
        bindingResult.rejectValue("password", "badpw", "Usuario o contraseña no válidos");
        return "login";
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}