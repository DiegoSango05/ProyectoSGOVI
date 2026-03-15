package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.OVIUserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/oviuser")
public class OVIUserController {

    private OVIUserDao oviUserDao;

    @Autowired
    public void setOviUserDao(OVIUserDao oviUserDao) {
        this.oviUserDao = oviUserDao;
    }

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("oviusers", oviUserDao.getOVIUsers());
        return "oviuser/list";
    }
}
