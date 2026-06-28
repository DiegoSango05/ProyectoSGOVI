package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.PAPAssistantDao;
import es.uji.ei1027.sps.dao.SupportChatDao;
import es.uji.ei1027.sps.dao.NegotiationDao;
import es.uji.ei1027.sps.dao.CommunicationDao;
import es.uji.ei1027.sps.dao.AssistanceRequestDao;
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
@RequestMapping("/pap_assistant")
public class PAPAssistantController {

    private PAPAssistantDao papAssistantDao;

    @Autowired
    private SupportChatDao supportChatDao;

    @Autowired
    private NegotiationDao negotiationDao;

    @Autowired
    private CommunicationDao communicationDao;

    @Autowired
    private AssistanceRequestDao assistanceRequestDao;

    @Autowired
    public void setPapAssistantDao(PAPAssistantDao papAssistantDao) {
        this.papAssistantDao = papAssistantDao;
    }

    @RequestMapping("/index")
    public String index(HttpSession session, Model model) {
        PAPAssistant assistant = (PAPAssistant) session.getAttribute("user");
        if (assistant == null || !"asistente".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        model.addAttribute("papassistant", papAssistantDao.getPAPAssistant(assistant.getDni()));

        // Calcular contadores de tareas pendientes para el asistente PAP
        int pendingChats = 0;
        // 1. Chats de soporte
        for (SupportChat chat : supportChatDao.getSupportChatsByParticipant(assistant.getDni(), "PAP")) {
            List<SupportMessage> messages = supportChatDao.getMessagesByChat(chat.getId());
            if (!messages.isEmpty()) {
                SupportMessage lastMsg = messages.get(messages.size() - 1);
                if ("Administrador".equals(lastMsg.getSender())) {
                    pendingChats++;
                }
            }
        }
        // 2. Negociaciones
        for (Negotiation neg : negotiationDao.getActiveNegotiationsByAssistant(assistant.getDni())) {
            List<Communication> messages = communicationDao.getCommunicationsByNegotiation(neg.getIdNegotiation());
            if (!messages.isEmpty()) {
                Communication lastMsg = messages.get(messages.size() - 1);
                AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(neg.getIdRequest());
                if (request != null && request.getDniOVIuser().equals(lastMsg.getSender())) {
                    pendingChats++;
                }
            }
        }

        // 3. Negociaciones pendientes de aceptar por el asistente
        long pendingNegotiations = negotiationDao.getActiveNegotiationsByAssistant(assistant.getDni()).stream()
                .filter(n -> !n.isAcceptedAssistant() && "Pending".equalsIgnoreCase(n.getStatus()))
                .count();

        model.addAttribute("pendingChats", pendingChats);
        model.addAttribute("pendingNegotiations", pendingNegotiations);

        return "pap_assistant/index";
    }

    // LISTAR
    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("papassistants", papAssistantDao.getPAPAssistants());
        return "pap_assistant/list";
    }

    // AÑADIR (Formulario)
    @RequestMapping("/add")
    public String addPAPAssistant(Model model) {
        model.addAttribute("papassistant", new PAPAssistant());
        return "pap_assistant/add";
    }

    // AÑADIR (Procesar)
    @RequestMapping(value="/add", method=RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("papassistant") PAPAssistant assistant,
                                   BindingResult bindingResult) {
        PAPAssistantValidator validator = new PAPAssistantValidator();
        validator.validate(assistant, bindingResult);

        if (bindingResult.hasErrors()) {
            return "pap_assistant/add";
        }

        // Forzamos el estado a Pendiente siempre
        assistant.setStatus("Pending");

        /*
        // =========================================================================
        // Encriptacion de la contraseña para nuevos usuarios
        // =========================================================================
        BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword(assistant.getPassword());
        assistant.setPassword(encryptedPassword);
        */

        try {
            papAssistantDao.addPAPAssistant(assistant);
        } catch (Exception e) {
            bindingResult.rejectValue("dni", "duplicado", "Este DNI ya está registrado");
            return "pap_assistant/add";
        }

        return "pap_assistant/registration_success";
    }

    // ELIMINAR
    @RequestMapping(value="/delete/{dni}")
    public String processDelete(@PathVariable String dni) {
        papAssistantDao.deletePAPAssistant(dni);
        return "redirect:../list";
    }

    // ACTUALIZAR (Formulario)
    @RequestMapping(value="/update/{dni}", method = RequestMethod.GET)
    public String editOVIUser(Model model, @PathVariable String dni) {
        model.addAttribute("papassistant", papAssistantDao.getPAPAssistant(dni));
        return "pap_assistant/update";
    }

    // ACTUALIZAR (Procesar)
    @RequestMapping(value="/update", method = RequestMethod.POST)
    public String processUpdateSubmit(@ModelAttribute("papassistant") PAPAssistant pap_assistant,
                                      BindingResult bindingResult) {
        PAPAssistantValidator validator = new PAPAssistantValidator();
        validator.validate(pap_assistant, bindingResult);
        if (bindingResult.hasErrors())
            return "pap_assistant/update";

        /*
        // =========================================================================
        // Encriptacion de la contraseña para nuevos usuarios
        // =========================================================================
        BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword(pap_assistant.getPassword());
        pap_assistant.setPassword(encryptedPassword);
        */

        papAssistantDao.updatePAPAssistant(pap_assistant);
        return "redirect:list";
    }

    @RequestMapping("/profile")
    public String profile(HttpSession session, Model model) {
        PAPAssistant assistant = (PAPAssistant) session.getAttribute("user");
        if (assistant == null || !"asistente".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        model.addAttribute("papassistant", papAssistantDao.getPAPAssistant(assistant.getDni()));
        return "pap_assistant/profile";
    }

    @RequestMapping("/profile/view")
    public String viewProfile(HttpSession session, Model model) {
        PAPAssistant assistant = (PAPAssistant) session.getAttribute("user");
        if (assistant == null || !"asistente".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        model.addAttribute("papassistant", papAssistantDao.getPAPAssistant(assistant.getDni()));
        return "pap_assistant/profile-view";
    }

    @RequestMapping(value="/profile/config", method = RequestMethod.GET)
    public String configureProfile(HttpSession session, Model model) {
        PAPAssistant assistant = (PAPAssistant) session.getAttribute("user");
        if (assistant == null || !"asistente".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        model.addAttribute("papassistant", papAssistantDao.getPAPAssistant(assistant.getDni()));
        return "pap_assistant/profile-config";
    }

    @RequestMapping(value="/profile/config", method = RequestMethod.POST)
    public String processConfigureProfile(@ModelAttribute("papassistant") PAPAssistant papAssistant,
                                          BindingResult bindingResult,
                                          HttpSession session) {
        PAPAssistant assistant = (PAPAssistant) session.getAttribute("user");
        if (assistant == null || !"asistente".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }

        PAPAssistant currentAssistant = papAssistantDao.getPAPAssistant(assistant.getDni());
        papAssistant.setDni(assistant.getDni());
        papAssistant.setStatus(currentAssistant.getStatus());
        papAssistant.setRejectionReason(currentAssistant.getRejectionReason());

        PAPAssistantValidator validator = new PAPAssistantValidator();
        validator.validateProfile(papAssistant, bindingResult);
        if (bindingResult.hasErrors()) {
            return "pap_assistant/profile-config";
        }

        /*
        // =========================================================================
        // Encriptacion de la contraseña para nuevos usuarios
        // =========================================================================
        BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword(papAssistant.getPassword());
        papAssistant.setPassword(encryptedPassword);
        */

        papAssistantDao.updatePAPAssistant(papAssistant);

        // Comentamos la limpieza para mantener el texto plano en sesión si no encriptamos todavía
        // papAssistant.setPassword(null);
        session.setAttribute("user", papAssistant);

        return "redirect:/pap_assistant/profile/view";
    }

    @RequestMapping("/chats")
    public String chatsIndex(HttpSession session, Model model) {
        PAPAssistant assistant = (PAPAssistant) session.getAttribute("user");
        if (assistant == null || !"asistente".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        model.addAttribute("papassistant", papAssistantDao.getPAPAssistant(assistant.getDni()));
        return "pap_assistant/chats";
    }

    @RequestMapping("/requests-contracts")
    public String requestsContractsIndex(HttpSession session, Model model) {
        Object user = session.getAttribute("user");
        if (!"asistente".equals(session.getAttribute("role")) || !(user instanceof PAPAssistant)) {
            return "redirect:/login";
        }
        PAPAssistant assistant = (PAPAssistant) user;
        model.addAttribute("papassistant", papAssistantDao.getPAPAssistant(assistant.getDni()));
        return "pap_assistant/requests-contracts";
    }
}