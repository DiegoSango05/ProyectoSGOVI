package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.ActivityDao;
import es.uji.ei1027.sps.model.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/activity")
public class ActivityController {

    private ActivityDao activityDao;

    @Autowired
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    // LISTAR
    @RequestMapping("/list")
    public String listActivities(Model model) {
        model.addAttribute("activities", activityDao.getActivities());
        return "activity/list";
    }

    // AÑADIR (Formulario)
    @RequestMapping("/add")
    public String addActivity(Model model) {
        model.addAttribute("activity", new Activity());
        return "activity/add";
    }

    // AÑADIR (Procesar)
    @RequestMapping(value="/add", method= RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("activity") Activity activity,
                                   BindingResult bindingResult) {
        ActivityValidator activityValidator = new ActivityValidator();
        activityValidator.validate(activity, bindingResult);

        if (bindingResult.hasErrors())
            return "activity/add";
        activityDao.addActivity(activity);
        return "redirect:list";
    }
}
