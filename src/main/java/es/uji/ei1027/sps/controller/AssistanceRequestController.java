package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/assistanceRequest")
public class AssistanceRequestController {

    private AssistanceRequestDao assistanceRequestDao;

    @Autowired
    public void setAssistanceRequestDao(AssistanceRequestDao assistanceRequestDao) {
        this.assistanceRequestDao = assistanceRequestDao;
    }

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("assistanceRequests", assistanceRequestDao.getAssistanceRequests());
        return "assistanceRequest/list";
    }
}
