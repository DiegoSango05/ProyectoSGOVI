package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.ActivityDao;
import es.uji.ei1027.sps.dao.AssistanceListDao;
import es.uji.ei1027.sps.model.AssistanceList;
import es.uji.ei1027.sps.model.Activity;
import es.uji.ei1027.sps.model.OVIUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/activity")
public class ActivityController {

    private ActivityDao activityDao;
    private AssistanceListDao assistanceListDao;

    @Autowired
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Autowired
    public void setAssistanceListDao(AssistanceListDao assistanceListDao) {
        this.assistanceListDao = assistanceListDao;
    }

    // LISTAR
    @RequestMapping("/list")
    public String listActivities(Model model) {
        model.addAttribute("activities", activityDao.getActivities());
        return "activity/list";
    }

    @RequestMapping("/my-list")
    public String myActivities(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("activities", activityDao.getActivitiesByOVIUser(user.getDni()));
        return "activity/ovi-list";
    }

    @RequestMapping("/join-list")
    public String joinList(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        List<Activity> activities = activityDao.getActivities();
        model.addAttribute("activities", activities);
        model.addAttribute("registeredByActivityId", getRegisteredByActivityId(user.getDni(), activities));
        return "activity/join-list";
    }

    @RequestMapping(value="/join/{id}", method= RequestMethod.POST)
    public String joinActivity(@PathVariable int id, HttpSession session) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        if (!assistanceListDao.isOVIUserRegisteredInActivity(user.getDni(), id)) {
            AssistanceList assistanceList = new AssistanceList();
            assistanceList.setId_list(assistanceListDao.getNextId());
            assistanceList.setAssistanceDate(LocalDate.now());
            assistanceList.setAssistanceTime(LocalTime.now());
            assistanceList.setParticipation(true);
            assistanceList.setIdActivity(id);
            assistanceList.setDniAssistant(null);
            assistanceList.setDniOVIUser(user.getDni());
            assistanceListDao.addAssistanceList(assistanceList);
        }
        return "redirect:/activity/my-list";
    }

    // BORRARSE DE UNA ACTIVIDAD
    @RequestMapping(value="/leave/{id}", method= RequestMethod.POST)
    public String leaveActivity(@PathVariable int id, HttpSession session) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        assistanceListDao.deleteOVIUserFromActivity(user.getDni(), id);
        return "redirect:/activity/my-list";
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

    // ELIMINAR
    @RequestMapping(value="/delete/{id}")
    public String processDelete(@PathVariable int id) {
        activityDao.deleteActivity(id);
        return "redirect:../list";
    }

    // ACTUALIZAR (Formulario)
    @RequestMapping(value="/update/{id}", method = RequestMethod.GET)
    public String editActivity(Model model, @PathVariable int id) {
        model.addAttribute("activity", activityDao.getActivity(id));
        return "activity/update";
    }

    // ACTUALIZAR (Procesar)
    @RequestMapping(value="/update", method = RequestMethod.POST)
    public String processUpdateSubmit(@ModelAttribute("activity") Activity activity,
                                      BindingResult bindingResult) {
        ActivityValidator activityValidator = new ActivityValidator();
        activityValidator.validate(activity, bindingResult);
        if (bindingResult.hasErrors())
            return "activity/update";
        activityDao.updateActivity(activity);
        return "redirect:list";
    }

    private OVIUser getLoggedOVIUser(HttpSession session) {
        Object user = session.getAttribute("user");
        Object role = session.getAttribute("role");
        if (!"ovi".equals(role) || !(user instanceof OVIUser)) {
            return null;
        }
        return (OVIUser) user;
    }

    private Map<Integer, Boolean> getRegisteredByActivityId(String dniOVIUser, List<Activity> activities) {
        Map<Integer, Boolean> registeredByActivityId = new HashMap<Integer, Boolean>();
        for (Activity activity : activities) {
            registeredByActivityId.put(
                    activity.getId(),
                    assistanceListDao.isOVIUserRegisteredInActivity(dniOVIUser, activity.getId()));
        }
        return registeredByActivityId;
    }
}
