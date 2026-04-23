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

    // ELIMINAR
    @RequestMapping(value="/delete/{id}")
    public String processDelete(@PathVariable int id) {
        negotiationDao.deleteNegotiation(id);
        return "redirect:../list";
    }

    // ACTUALIZAR (Formulario)
    @RequestMapping(value="/update/{id}", method = RequestMethod.GET)
    public String editNegotiation(Model model, @PathVariable int id) {
        model.addAttribute("negotiation", negotiationDao.getNegotiation(id));
        return "negotiation/update";
    }

    // ACTUALIZAR (Procesar)
    @RequestMapping(value="/update", method = RequestMethod.POST)
    public String processUpdateSubmit(@ModelAttribute("negotiation") Negotiation negotiation,
                                      BindingResult bindingResult) {
        // Aquí llamarías a su respectivo validador si lo tienes creado
        if (bindingResult.hasErrors())
            return "negotiation/update";
        negotiationDao.updateNegotiation(negotiation);
        return "redirect:list";
    }
}
