package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.PAPAssistantDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/papassistant")
public class PAPAssistantController {

    private PAPAssistantDao papAssistantDao;

    @Autowired
    public void setPapAssistantDao(PAPAssistantDao papAssistantDao) {
        this.papAssistantDao = papAssistantDao;
    }

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("papassistants", papAssistantDao.getPAPAssistants());
        return "papassistant/list";
    }
}
