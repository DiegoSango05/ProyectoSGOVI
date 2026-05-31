package es.uji.ei1027.sps.controller;

import jakarta.servlet.http.HttpSession;
import es.uji.ei1027.sps.dao.OVIUserDao;
import es.uji.ei1027.sps.dao.PAPAssistantDao;
import es.uji.ei1027.sps.dao.SystemUserDao;
import es.uji.ei1027.sps.model.OVIUser;
import es.uji.ei1027.sps.model.PAPAssistant;
import es.uji.ei1027.sps.model.SystemUser;
import es.uji.ei1027.sps.model.UserDetails;
// import org.jasypt.util.password.BasicPasswordEncryptor; // Encriptación de la contraseña para nuevos usuarios
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
    private SystemUserDao systemUserDao;

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
        // Mantenemos la validación clásica en texto plano activa para tus pruebas actuales
        OVIUser ovi = oviUserDao.loadUserByUsername(username, password);

        /*
        // =========================================================================
        // Encriptacion de la contraseña para nuevos usuarios
        // =========================================================================
        OVIUser oviVerif = oviUserDao.getOVIUser(username);
        if (oviVerif != null) {
            BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
            if (passwordEncryptor.checkPassword(password, oviVerif.getPassword())) {
                ovi = oviVerif;
                ovi.setPassword(null);
            }
        }
        */

        if (ovi != null) {
            session.setAttribute("user", ovi);
            session.setAttribute("role", "ovi");

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
        // Mantenemos la validación clásica en texto plano activa para tus pruebas actuales
        PAPAssistant pap = papAssistantDao.loadUserByUsername(username, password);

        /*
        // =========================================================================
        // Encriptacion de la contraseña para nuevos usuarios
        // =========================================================================
        PAPAssistant papVerif = papAssistantDao.getPAPAssistant(username);
        if (papVerif != null) {
            BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
            if (passwordEncryptor.checkPassword(password, papVerif.getPassword())) {
                pap = papVerif;
                pap.setPassword(null);
            }
        }
        */

        if (pap != null) {
            session.setAttribute("user", pap);
            session.setAttribute("role", "asistente");

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
        return "redirect:/";
    }
}