package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.OVIUserDao;
import es.uji.ei1027.sps.dao.SelectionDao;
import es.uji.ei1027.sps.model.OVIUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/oviuser")
public class OVIUserController {

    private OVIUserDao oviUserDao;

    @Autowired
    private SelectionDao selectionDao;

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

        try {
            oviUserDao.addOVIUser(oviUser);
        } catch (Exception e) {
            bindingResult.rejectValue("dni", "duplicado", "Este DNI ya está registrado");
            return "oviuser/add";
        }

        // Cambiamos la redirección a la pantalla de éxito de registro
        return "oviuser/register-success";
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

        oviUser.setDni(user.getDni());
        OVIUserValidator validator = new OVIUserValidator();
        validator.validate(oviUser, bindingResult);
        if (bindingResult.hasErrors()) {
            return "oviuser/profile-config";
        }

        oviUserDao.updateOVIUser(oviUser);
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
