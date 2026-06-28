package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.OVIUserDao;
import es.uji.ei1027.sps.dao.SelectionDao;
import es.uji.ei1027.sps.dao.SupportChatDao;
import es.uji.ei1027.sps.dao.NegotiationDao;
import es.uji.ei1027.sps.dao.CommunicationDao;
import es.uji.ei1027.sps.model.*;
import jakarta.servlet.http.HttpSession;
// import org.jasypt.util.password.BasicPasswordEncryptor; // Encriptación de la contraseña para nuevos usuarios
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/oviuser")
public class OVIUserController {

    private OVIUserDao oviUserDao;

    @Autowired
    private SelectionDao selectionDao;

    @Autowired
    private SupportChatDao supportChatDao;

    @Autowired
    private NegotiationDao negotiationDao;

    @Autowired
    private CommunicationDao communicationDao;

    @Autowired
    public void setOviUserDao(OVIUserDao oviUserDao) {
        this.oviUserDao = oviUserDao;
    }

    @RequestMapping({"", "/"})
    public String home() {
        return "redirect:/oviuser/index";
    }

    @RequestMapping("/index")
    public String index(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("oviuser", oviUserDao.getOVIUser(user.getDni()));

        // Calcular contadores de tareas pendientes para el usuario OVI
        int pendingChats = 0;
        // 1. Chats de soporte
        for (SupportChat chat : supportChatDao.getSupportChatsByParticipant(user.getDni(), "OVI")) {
            List<SupportMessage> messages = supportChatDao.getMessagesByChat(chat.getId());
            if (!messages.isEmpty()) {
                SupportMessage lastMsg = messages.get(messages.size() - 1);
                if ("Administrador".equals(lastMsg.getSender())) {
                    pendingChats++;
                }
            }
        }
        // 2. Negociaciones
        for (Negotiation neg : negotiationDao.getActiveNegotiationsByOVIUser(user.getDni())) {
            List<Communication> messages = communicationDao.getCommunicationsByNegotiation(neg.getIdNegotiation());
            if (!messages.isEmpty()) {
                Communication lastMsg = messages.get(messages.size() - 1);
                if (neg.getDniAssistant().equals(lastMsg.getSender())) {
                    pendingChats++;
                }
            }
        }

        // 3. Negociaciones pendientes de aceptar por el cliente
        long pendingNegotiations = negotiationDao.getActiveNegotiationsByOVIUser(user.getDni()).stream()
                .filter(n -> !n.isAcceptedCustomer() && "Pending".equalsIgnoreCase(n.getStatus()))
                .count();

        model.addAttribute("pendingChats", pendingChats);
        model.addAttribute("pendingNegotiations", pendingNegotiations);

        return "oviuser/index";
    }

    @RequestMapping("/requests-contracts")
    public String requestsContractsIndex(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("oviuser", oviUserDao.getOVIUser(user.getDni()));
        return "oviuser/requests-contracts";
    }

    @RequestMapping("/chats")
    public String chatsIndex(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("oviuser", oviUserDao.getOVIUser(user.getDni()));
        return "oviuser/chats";
    }

    // LISTAR
    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("oviusers", oviUserDao.getOVIUsers());
        return "oviuser/list";
    }

    // AÑADIR (Formulario)
    @RequestMapping("/add")
    public String addOVIUser(Model model) {
        model.addAttribute("oviuser", new OVIUser());
        return "oviuser/add";
    }

    // AÑADIR (Procesar Registro)
    @RequestMapping(value="/add", method=RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("oviuser") OVIUser oviUser,
                                   BindingResult bindingResult) {

        OVIUserValidator oviUserValidator = new OVIUserValidator();
        oviUserValidator.validate(oviUser, bindingResult);

        if (bindingResult.hasErrors())
            return "oviuser/add";

        /*
        // =========================================================================
        // Encriptacion de la contraseña para nuevos usuarios
        // =========================================================================
        BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword(oviUser.getPassword());
        oviUser.setPassword(encryptedPassword);
        */

        try {
            oviUserDao.addOVIUser(oviUser);
        } catch (Exception e) {
            bindingResult.rejectValue("dni", "duplicado", "Este DNI ya está registrado");
            return "oviuser/add";
        }

        return "redirect:/oviuser/registration-success";
    }

    @GetMapping("/registration-success")
    public String registrationSuccess() {
        return "oviuser/registration-success";
    }

    // ELIMINAR
    @RequestMapping(value="/delete/{dni}")
    public String processDelete(@PathVariable String dni) {
        oviUserDao.deleteOVIUser(dni);
        return "redirect:../list";
    }

    // ACTUALIZAR (Formulario)
    @RequestMapping(value="/update/{dni}", method = RequestMethod.GET)
    public String editOVIUser(Model model, @PathVariable String dni) {
        model.addAttribute("oviuser", oviUserDao.getOVIUser(dni));
        return "oviuser/update";
    }

    // ACTUALIZAR (Procesar)
    @RequestMapping(value="/update", method = RequestMethod.POST)
    public String processUpdateSubmit(@ModelAttribute("oviuser") OVIUser oviUser,
                                      BindingResult bindingResult) {
        OVIUserValidator validator = new OVIUserValidator();
        validator.validate(oviUser, bindingResult);
        if (bindingResult.hasErrors())
            return "oviuser/update";

        /*
        // =========================================================================
        // Encriptacion de la contraseña para nuevos usuarios
        // =========================================================================
        BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword(oviUser.getPassword());
        oviUser.setPassword(encryptedPassword);
        */

        oviUserDao.updateOVIUser(oviUser);
        return "redirect:list";
    }

    @RequestMapping("/profile")
    public String profile(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("oviuser", oviUserDao.getOVIUser(user.getDni()));
        return "oviuser/profile";
    }

    @RequestMapping("/profile/view")
    public String viewProfile(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("oviuser", oviUserDao.getOVIUser(user.getDni()));
        return "oviuser/profile-view";
    }

    @RequestMapping(value="/profile/config", method = RequestMethod.GET)
    public String configureProfile(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("oviuser", oviUserDao.getOVIUser(user.getDni()));
        return "oviuser/profile-config";
    }

    @RequestMapping(value="/profile/config", method = RequestMethod.POST)
    public String processConfigureProfile(@ModelAttribute("oviuser") OVIUser oviUser,
                                          BindingResult bindingResult,
                                          HttpSession session) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        OVIUser currentUser = oviUserDao.getOVIUser(user.getDni());
        oviUser.setDni(user.getDni());
        oviUser.setStatus(currentUser.getStatus());
        oviUser.setRejectionReason(currentUser.getRejectionReason());

        OVIUserValidator validator = new OVIUserValidator();
        validator.validateProfile(oviUser, bindingResult);
        if (bindingResult.hasErrors()) {
            return "oviuser/profile-config";
        }

        /*
        // =========================================================================
        // Encriptacion de la contraseña para nuevos usuarios
        // =========================================================================
        BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword(oviUser.getPassword());
        oviUser.setPassword(encryptedPassword);
        */

        oviUserDao.updateOVIUser(oviUser);

        // Comentamos la limpieza para mantener el texto plano en sesión si no encriptamos todavía
        // oviUser.setPassword(null);
        session.setAttribute("user", oviUser);

        return "redirect:/oviuser/profile/view";
    }

    private OVIUser getLoggedOVIUser(HttpSession session) {
        Object user = session.getAttribute("user");
        Object role = session.getAttribute("role");
        if (!"ovi".equals(role) || !(user instanceof OVIUser)) {
            return null;
        }
        return (OVIUser) user;
    }
}