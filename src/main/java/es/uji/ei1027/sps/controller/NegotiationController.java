package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.NegotiationDao;
import es.uji.ei1027.sps.model.Negotiation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/negotiation")
public class NegotiationController {

    private NegotiationDao negotiationDao;

    @Autowired
    public void setNegotiationDao(NegotiationDao negotiationDao) {
        this.negotiationDao = negotiationDao;
    }

    // LISTAR
    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("negotiations", negotiationDao.getNegotiations());
        return "negotiation/list";
    }
    // AÑADIR (Formulario)
    @RequestMapping("/add")
    public String addNegotiation(Model model) {
        model.addAttribute("negotiation", new Negotiation());
        return "negotiation/add";
    }

    // AÑADIR (Procesar)
    @RequestMapping(value="/add", method= RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("negotiation") Negotiation negotiation,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "negotiation/add";

        negotiationDao.addNegotiation(negotiation);
        return "redirect:list";
    }
}
