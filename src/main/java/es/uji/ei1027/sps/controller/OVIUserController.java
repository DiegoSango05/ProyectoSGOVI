package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.OVIUserDao;
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
    public void setOviUserDao(OVIUserDao oviUserDao) {
        this.oviUserDao = oviUserDao;
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

    // AÑADIR (Procesar)
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
            return "pap_assistant/add";
        }
        return "redirect:list";
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

    // VISUALIZAR PERFIL
    @RequestMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        OVIUser user = (OVIUser) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("oviuser", oviUserDao.getOVIUser(user.getDni()));
        return "oviuser/update";
    }
}
