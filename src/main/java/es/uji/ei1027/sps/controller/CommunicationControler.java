package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.CommunicationDao;
import es.uji.ei1027.sps.model.Communication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/communication")
public class CommunicationControler {

    private CommunicationDao communicationDao;

    @Autowired
    public void setCommunicationDao(CommunicationDao communicationDao) {
        this.communicationDao = communicationDao;
    }

    // LISTAR
    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("communications", communicationDao.getCommunications());
        return "communication/list";
    }

    // AÑADIR (Formulario)
    @RequestMapping("/add")
    public String addCommunication(Model model) {
        model.addAttribute("communication", new Communication());
        return "communication/add";
    }

    // AÑADIR (Procesar)
    @RequestMapping(value="/add", method= RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("communication") Communication communication,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "communication/add";

        communicationDao.addCommunication(communication);
        return "redirect:list";
    }
}
