package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.NegotiationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/negotiation")
public class NegotiationController {

    private NegotiationDao negotiationDao;

    @Autowired
    public void setNegotiationDao(NegotiationDao negotiationDao) {
        this.negotiationDao = negotiationDao;
    }

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("negotiations", negotiationDao.getNegotiations());
        return "negotiation/list";
    }
}
