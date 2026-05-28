package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceListDao;
import es.uji.ei1027.sps.model.AssistanceList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/assistancelist")
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
        AssistanceListValidator validator = new AssistanceListValidator();
        validator.validate(assistanceList, bindingResult);

        if (bindingResult.hasErrors())
            return "assistancelist/add";
        assistanceListDao.addAssistanceList(assistanceList);
        return "redirect:list";
    }

    // ELIMINAR
    @RequestMapping(value="/delete/{id_list}")
    public String processDelete(@PathVariable int id_list) {
        AssistanceList assistance = assistanceListDao.getAssistanceList(id_list);

        if (assistance != null) {
            int idActivity = assistance.getIdActivity();
            assistanceListDao.deleteAssistanceList(id_list);
            return "redirect:/activity/manage-participants/" + idActivity;
        }

        return "redirect:/activity/list";
    }

    // ACTUALIZAR (Formulario)
    @RequestMapping(value="/update/{id_list}", method = RequestMethod.GET)
    public String editAssistanceList(Model model, @PathVariable int id_list) {
        model.addAttribute("assistancelist", assistanceListDao.getAssistanceList(id_list));
        return "assistancelist/update";
    }

    // ACTUALIZAR (Procesar)
    @RequestMapping(value="/update", method = RequestMethod.POST)
    public String processUpdateSubmit(@ModelAttribute("assistancelist") AssistanceList assistanceList,
                                      BindingResult bindingResult) {
        AssistanceListValidator validator = new AssistanceListValidator();
        validator.validate(assistanceList, bindingResult);
        if (bindingResult.hasErrors())
            return "assistancelist/update";
        assistanceListDao.updateAssistanceList(assistanceList);
        return "redirect:list";
    }


}