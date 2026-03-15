package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.CommunicationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/communication")
public class CommunicationControler {

    private CommunicationDao communicationDao;

    @Autowired
    public void setCommunicationDao(CommunicationDao communicationDao) {
        this.communicationDao = communicationDao;
    }

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("communications", communicationDao.getCommunications());
        return "communication/list";
    }
}
