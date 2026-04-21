package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceListDao;
import es.uji.ei1027.sps.model.AssistanceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/assistancelist") // Todo minúsculas
public class AssistanceListController {

    private AssistanceListDao assistanceListDao;

    @Autowired
    public void setAssistanceListDao(AssistanceListDao assistanceListDao) {
        this.assistanceListDao = assistanceListDao;
    }

    // LISTAR
    @RequestMapping("/list")
    public String list(@RequestParam(value="idActivity", required=false) Integer idActivity, Model model) {
        if (idActivity != null) {
            model.addAttribute("assistancesList", assistanceListDao.getAssistanceListsByActivity(idActivity));
            model.addAttribute("idActivity", idActivity);
        } else {
            model.addAttribute("assistancesList", assistanceListDao.getAssistanceLists());
        }
        return "assistancelist/list";
    }

    // AÑADIR (Formulario)
    @RequestMapping("/add")
    public String addAssistanceList(Model model) {
        model.addAttribute("assistancelist", new AssistanceList());
        return "assistancelist/add";
    }

    // AÑADIR (Procesar)
    @RequestMapping(value="/add", method= RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("assistancelist") AssistanceList assistanceList,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "assistancelist/add";
        assistanceListDao.addAssistanceList(assistanceList);
        return "redirect:list";
    }
}