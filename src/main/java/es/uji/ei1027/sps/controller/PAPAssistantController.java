package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.PAPAssistantDao;
import es.uji.ei1027.sps.model.PAPAssistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pap_assistant")
public class PAPAssistantController {

    private PAPAssistantDao papAssistantDao;

    @Autowired
    public void setPapAssistantDao(PAPAssistantDao papAssistantDao) {
        this.papAssistantDao = papAssistantDao;
    }

    // LISTAR
    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("papassistants", papAssistantDao.getPAPAssistants());
        return "pap_assistant/list";
    }

    // AÑADIR (Formulario)
    @RequestMapping("/add")
    public String addPAPAssistant(Model model) {
        model.addAttribute("papassistant", new PAPAssistant());
        return "pap_assistant/add";
    }

    // AÑADIR (Procesar)
    @RequestMapping(value="/add", method=RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("papassistant") PAPAssistant assistant,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "pap_assistant/add";

        papAssistantDao.addPAPAssistant(assistant);
        return "redirect:list";
    }

    // ELIMINAR
    @RequestMapping(value="/delete/{dni}")
    public String processDelete(@PathVariable String dni) {
        papAssistantDao.deletePAPAssistant(dni);
        return "redirect:../list";
    }
}
