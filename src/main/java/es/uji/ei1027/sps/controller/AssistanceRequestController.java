package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceRequestDao;
import es.uji.ei1027.sps.model.AssistanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/assistancerequest")
public class AssistanceRequestController {

    private AssistanceRequestDao assistanceRequestDao;

    @Autowired
    public void setAssistanceRequestDao(AssistanceRequestDao assistanceRequestDao) {
        this.assistanceRequestDao = assistanceRequestDao;
    }

    // LISTAR
    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("assistanceRequests", assistanceRequestDao.getAssistanceRequests());
        return "assistancerequest/list";
    }

    // AÑADIR (Formulario)
    @RequestMapping("/add")
    public String addAssistanceRequest(Model model) {
        model.addAttribute("assistancerequest", new AssistanceRequest());
        return "assistancerequest/add";
    }

    // AÑADIR (Procesar)
    @RequestMapping(value="/add", method= RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("assistancerequest") AssistanceRequest request,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "assistancerequest/add";

        assistanceRequestDao.addAssistanceRequest(request);
        return "redirect:list";
    }
}
