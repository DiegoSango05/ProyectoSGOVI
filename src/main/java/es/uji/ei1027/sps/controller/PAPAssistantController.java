package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.PAPAssistantDao;
import es.uji.ei1027.sps.model.OVIUser;
import es.uji.ei1027.sps.model.PAPAssistant;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pap_assistant")
public class PAPAssistantController {

    private PAPAssistantDao papAssistantDao;

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

        try {
            papAssistantDao.addPAPAssistant(assistant);
        } catch (Exception e) {
            bindingResult.rejectValue("dni", "duplicado", "Este DNI ya está registrado");
            return "pap_assistant/add";
        }

        // Redirigimos a una página de agradecimiento en lugar de a la lista
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

        PAPAssistantValidator validator = new PAPAssistantValidator();
        validator.validate(papAssistant, bindingResult);
        if (bindingResult.hasErrors()) {
            return "pap_assistant/profile-config";
        }

        papAssistantDao.updatePAPAssistant(papAssistant);
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
}
