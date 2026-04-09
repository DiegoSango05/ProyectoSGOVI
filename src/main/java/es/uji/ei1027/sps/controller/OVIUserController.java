package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.OVIUserDao;
import es.uji.ei1027.sps.model.OVIUser;
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
        if (bindingResult.hasErrors())
            return "oviuser/add";

        oviUserDao.addOVIUser(oviUser);
        return "redirect:list";
    }
}
