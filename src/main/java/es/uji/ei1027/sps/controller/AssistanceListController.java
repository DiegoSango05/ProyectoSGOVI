package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceListDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/assistancelist") // Todo minúsculas
public class AssistanceListController {

    private AssistanceListDao assistanceListDao;

    @Autowired
    public void setAssistanceListDao(AssistanceListDao assistanceListDao) {
        this.assistanceListDao = assistanceListDao;
    }

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("assistancesList", assistanceListDao.getAssistanceLists());
        return "assistancelist/list";
    }
}