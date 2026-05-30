package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.*;
import es.uji.ei1027.sps.model.*;
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

    // --- MÉTODOS ACCESIBLES POR ADMIN (Pepe López) ---

    @RequestMapping("/list")
    public String listActivities(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("activities", activityDao.getActivities());
        return "activity/list";
    }

    @RequestMapping(value="/add", method=RequestMethod.GET)
    public String addActivity(HttpSession session, Model model, @RequestParam(required=false) String type) {
        if (!isAdmin(session)) return "redirect:/login";

        Activity activity = new Activity();
        activity.setMaxParticipants(1);
        if (type != null) activity.setType(type);

        model.addAttribute("activity", activity);

        if (type != null && !type.isEmpty()) {
            model.addAttribute("instructors", instructorDao.getInstructorsBySpecialty(type));
        } else {
            model.addAttribute("instructors", instructorDao.getInstructors());
        }
        return "activity/add";
    }

    @RequestMapping(value="/add", method= RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("activity") Activity activity,
                                   BindingResult bindingResult, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

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

    @RequestMapping(value="/delete/{id}")
    public String processDelete(@PathVariable int id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        activityDao.deleteActivity(id);
        return "redirect:../list";
    }

    @RequestMapping(value="/update/{id}", method = RequestMethod.GET)
    public String editActivity(HttpSession session, Model model, @PathVariable int id) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("activity", activityDao.getActivity(id));
        return "activity/update";
    }

    @RequestMapping(value="/update", method = RequestMethod.POST)
    public String processUpdateSubmit(@ModelAttribute("activity") Activity activity,
                                      HttpSession session, BindingResult bindingResult) {
        if (!isAdmin(session)) return "redirect:/login";

        ActivityValidator activityValidator = new ActivityValidator();
        activityValidator.validate(activity, bindingResult);
        if (bindingResult.hasErrors()) return "activity/update";

        activityDao.updateActivity(activity);
        return "redirect:list";
    }

    @GetMapping("/manage-participants/{id}")
    public String manageParticipants(@PathVariable int id, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";

        Activity activity = activityDao.getActivity(id);
        List<AssistanceList> inscritos = assistanceListDao.getAssistanceListsByActivity(id);

        List<OVIUser> allUsers = oviUserDao.getOVIUsers();
        List<PAPAssistant> allAssistants = papAssistantDao.getPAPAssistants();

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

        return "activity/manage-participants";
    }

    @PostMapping("/add-participant-manual")
    public String addParticipantManual(@RequestParam int idActivity,
                                       @RequestParam(required = false) String dniUser,
                                       @RequestParam(required = false) String dniAssistant,
                                       HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        boolean yaInscrito = false;
        if (dniUser != null && !dniUser.isEmpty()) {
            yaInscrito = assistanceListDao.isOVIUserRegisteredInActivity(dniUser, idActivity);
        } else if (dniAssistant != null && !dniAssistant.isEmpty()) {
            yaInscrito = assistanceListDao.isAssistantRegisteredInActivity(dniAssistant, idActivity);
        }

        if (!yaInscrito) {
            boolean tieneUser = (dniUser != null && !dniUser.isEmpty());
            boolean tieneAsis = (dniAssistant != null && !dniAssistant.isEmpty());

            if (tieneUser || tieneAsis) {
                AssistanceList participation = new AssistanceList();
                participation.setId_list(assistanceListDao.getNextId());
                participation.setIdActivity(idActivity);
                participation.setDniOVIUser(tieneUser ? dniUser : null);
                participation.setDniAssistant(tieneAsis ? dniAssistant : null);
                participation.setAssistanceDate(LocalDate.now());
                participation.setAssistanceTime(LocalTime.now());
                participation.setParticipation(true);

                assistanceListDao.addAssistanceList(participation);
            }
        }
        return "redirect:/activity/manage-participants/" + idActivity;
    }

    // --- MÉTODOS PARA USUARIOS OVI ---

    @RequestMapping("/my-list")
    public String myActivities(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        PAPAssistant assistant = getLoggedAssistant(session);
        if (user == null && assistant == null) return "redirect:/login";

        if (user != null) {
            model.addAttribute("activities", activityDao.getActivitiesByOVIUser(user.getDni()));
            model.addAttribute("homeUrl", "/oviuser");
        } else {
            model.addAttribute("activities", activityDao.getActivitiesByAssistant(assistant.getDni()));
            model.addAttribute("homeUrl", "/pap_assistant/index");
        }
        return "activity/ovi-list";
    }

    @RequestMapping("/join-list")
    public String joinList(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        PAPAssistant assistant = getLoggedAssistant(session);
        if (user == null && assistant == null) return "redirect:/login";

        List<Activity> activities = activityDao.getActivities();
        model.addAttribute("activities", activities);
        if (user != null) {
            model.addAttribute("registeredByActivityId", getRegisteredByActivityId(user.getDni(), activities, false));
            model.addAttribute("homeUrl", "/oviuser");
        } else {
            model.addAttribute("registeredByActivityId", getRegisteredByActivityId(assistant.getDni(), activities, true));
            model.addAttribute("homeUrl", "/pap_assistant/index");
        }
        return "activity/join-list";
    }

    @RequestMapping(value="/join/{id}", method= RequestMethod.POST)
    public String joinActivity(@PathVariable int id, HttpSession session) {
        OVIUser user = getLoggedOVIUser(session);
        PAPAssistant assistant = getLoggedAssistant(session);
        if (user == null && assistant == null) return "redirect:/login";

        String dni = user != null ? user.getDni() : assistant.getDni();
        boolean registered = user != null
                ? assistanceListDao.isOVIUserRegisteredInActivity(dni, id)
                : assistanceListDao.isAssistantRegisteredInActivity(dni, id);

        if (!registered) {
            AssistanceList assistanceList = new AssistanceList();
            assistanceList.setId_list(assistanceListDao.getNextId());
            assistanceList.setAssistanceDate(LocalDate.now());
            assistanceList.setAssistanceTime(LocalTime.now());
            assistanceList.setParticipation(true);
            assistanceList.setIdActivity(id);
            if (user != null) {
                assistanceList.setDniOVIUser(dni);
            } else {
                assistanceList.setDniAssistant(dni);
            }
            assistanceListDao.addAssistanceList(assistanceList);
        }
        return "redirect:/activity/my-list";
    }

    @RequestMapping(value="/leave/{id}", method= RequestMethod.POST)
    public String leaveActivpity(@PathVariable int id, HttpSession session) {
        OVIUser user = getLoggedOVIUser(session);
        PAPAssistant assistant = getLoggedAssistant(session);
        if (user == null && assistant == null) return "redirect:/login";

        if (user != null) {
            assistanceListDao.deleteOVIUserFromActivity(user.getDni(), id);
        } else {
            assistanceListDao.deleteAssistantFromActivity(assistant.getDni(), id);
        }
        return "redirect:/activity/my-list";
    }

    // --- MÉTODOS AUXILIARES DE SEGURIDAD ---

    private boolean isAdmin(HttpSession session) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        String role = (String) session.getAttribute("role");
        return user != null && "admin".equals(role);
    }

    private OVIUser getLoggedOVIUser(HttpSession session) {
        Object user = session.getAttribute("user");
        Object role = session.getAttribute("role");
        if (!"ovi".equals(role) || !(user instanceof OVIUser)) return null;
        return (OVIUser) user;
    }

    private PAPAssistant getLoggedAssistant(HttpSession session) {
        Object user = session.getAttribute("user");
        Object role = session.getAttribute("role");
        if (!"asistente".equals(role) || !(user instanceof PAPAssistant)) return null;
        return (PAPAssistant) user;
    }

    private Map<Integer, Boolean> getRegisteredByActivityId(String dni, List<Activity> activities, boolean assistant) {
        Map<Integer, Boolean> registeredByActivityId = new HashMap<>();
        for (Activity activity : activities) {
            boolean registered = assistant
                    ? assistanceListDao.isAssistantRegisteredInActivity(dni, activity.getId())
                    : assistanceListDao.isOVIUserRegisteredInActivity(dni, activity.getId());
            registeredByActivityId.put(activity.getId(), registered);
        }
        return registeredByActivityId;
    }
}
