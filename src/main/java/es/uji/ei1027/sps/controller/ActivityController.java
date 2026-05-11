package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.*;
import es.uji.ei1027.sps.model.AssistanceList;
import es.uji.ei1027.sps.model.Activity;
import es.uji.ei1027.sps.model.OVIUser;
import es.uji.ei1027.sps.model.PAPAssistant;
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
    private InstructorDao instructorDao;

    @Autowired
    private PAPAssistantDao papAssistantDao;

    @Autowired
    private OVIUserDao oviUserDao;

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
    @RequestMapping(value="/add", method=RequestMethod.GET)
    public String addActivity(Model model, @RequestParam(required=false) String type) {
        Activity activity = new Activity();
        activity.setMaxParticipants(1);
        if (type != null) activity.setType(type);

        model.addAttribute("activity", activity);

        // Si ya tenemos el tipo, buscamos instructores con el "Match"
        if (type != null && !type.isEmpty()) {
            model.addAttribute("instructors", instructorDao.getInstructorsBySpecialty(type));
        } else {
            model.addAttribute("instructors", instructorDao.getInstructors());
        }
        return "activity/add";
    }

    // AÑADIR (Procesar)
    @RequestMapping(value="/add", method= RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("activity") Activity activity,
                                   BindingResult bindingResult, Model model) {
        ActivityValidator activityValidator = new ActivityValidator();
        activityValidator.validate(activity, bindingResult);

        if (bindingResult.hasErrors()) {
            if (activity.getType() != null && !activity.getType().trim().isEmpty()) {
                model.addAttribute("instructors", instructorDao.getInstructorsBySpecialty(activity.getType()));
            } else {
                model.addAttribute("instructors", instructorDao.getInstructors());
            }

            return "activity/add";
        }

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

    @GetMapping("/manage-participants/{id}")
    public String manageParticipants(@PathVariable int id, Model model) {
        Activity activity = activityDao.getActivity(id);
        List<AssistanceList> inscritos = assistanceListDao.getAssistanceListsByActivity(id);

        // 1. Conseguimos todos los usuarios y asistentes
        List<OVIUser> allUsers = oviUserDao.getOVIUsers();
        List<PAPAssistant> allAssistants = papAssistantDao.getPAPAssistants();

        // 2. Filtramos: Solo dejamos los que NO están en la lista de 'inscritos'
        List<OVIUser> availableUsers = allUsers.stream()
                .filter(u -> inscritos.stream().noneMatch(p -> u.getDni().equals(p.getDniOVIUser())))
                .toList();

        List<PAPAssistant> availableAssistants = allAssistants.stream()
                .filter(a -> inscritos.stream().noneMatch(p -> a.getDni().equals(p.getDniAssistant())))
                .toList();

        model.addAttribute("activity", activity);
        model.addAttribute("currentParticipants", inscritos);
        model.addAttribute("allUsers", availableUsers);
        model.addAttribute("allAssistants", availableAssistants);

        return "admin/manage-participants";
    }

    @PostMapping("/add-participant-manual")
    public String addParticipantManual(@RequestParam int idActivity,
                                       @RequestParam(required = false) String dniUser,
                                       @RequestParam(required = false) String dniAssistant) {

        boolean yaInscrito = false;

        // Comprobamos según lo que nos haya llegado
        if (dniUser != null && !dniUser.isEmpty()) {
            yaInscrito = assistanceListDao.isOVIUserRegisteredInActivity(dniUser, idActivity);
        } else if (dniAssistant != null && !dniAssistant.isEmpty()) {
            yaInscrito = assistanceListDao.isAssistantRegisteredInActivity(dniAssistant, idActivity);
        }

        // Si NO está inscrito, lo añadimos
        if (!yaInscrito && ((dniUser != null && !dniUser.isEmpty()) || (dniAssistant != null && !dniAssistant.isEmpty()))) {
            AssistanceList participation = new AssistanceList();

            // Generamos el ID automáticamente con tu método getNextId()
            participation.setId_list(assistanceListDao.getNextId());
            participation.setIdActivity(idActivity);
            participation.setDniOVIUser(dniUser);
            participation.setDniAssistant(dniAssistant);

            // Valores por defecto para fecha/hora o participación si tu modelo los requiere
            participation.setParticipation(true);

            assistanceListDao.addAssistanceList(participation);
        }

        return "redirect:/activity/manage-participants/" + idActivity;
    }
}
