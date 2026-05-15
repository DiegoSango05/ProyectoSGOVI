package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceRequestDao;
import es.uji.ei1027.sps.dao.OVIUserDao;
import es.uji.ei1027.sps.dao.PAPAssistantDao;
import es.uji.ei1027.sps.dao.SelectionDao;
import es.uji.ei1027.sps.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AssistanceRequestDao assistanceRequestDao;

    @Autowired
    private PAPAssistantDao papAssistantDao;

    @Autowired
    private OVIUserDao oviUserDao;

    @Autowired
    private SelectionDao selectionDao;

    @RequestMapping("/index")
    public String indexAdmin(HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }
        model.addAttribute("adminName", user.getName());
        return "admin/index";
    }

    @GetMapping("/request-details/{id}")
    public String showCandidates(@PathVariable int id, HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }

        AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(id);
        if (request != null) {
            List<PAPAssistant> candidatos = papAssistantDao.getCandidatesForRequest(request);
            model.addAttribute("request", request);
            model.addAttribute("candidatos", candidatos);
        }
        return "admin/candidates-list";
    }

    @GetMapping("/requests")
    public String listRequests(HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }
        List<AssistanceRequest> solicitudes = assistanceRequestDao.getAssistanceRequests();
        model.addAttribute("solicitudes", solicitudes);
        return "admin/requests-list";
    }

    @PostMapping("/reject/{id}")
    public String rejectRequest(@PathVariable int id) {
        AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(id);
        if (request != null) {
            request.setStatus("Rejected");
            assistanceRequestDao.updateAssistanceRequest(request);
        }
        return "redirect:/admin/requests";
    }

    // Listar todos los asistentes para que el admin los gestione
    @GetMapping("/assistants")
    public String listAssistants(HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }
        model.addAttribute("assistants", papAssistantDao.getPAPAssistants());
        return "admin/assistants-list";
    }

    // Aprobar a un asistente
    @PostMapping("/assistants/approve/{dni}")
    public String approveAssistant(@PathVariable String dni) {
        PAPAssistant assistant = papAssistantDao.getPAPAssistant(dni);
        if (assistant != null) {
            assistant.setStatus("Accepted");
            papAssistantDao.updatePAPAssistant(assistant);
        }
        return "redirect:/admin/assistants";
    }

    // Rechazar a un asistente
    @PostMapping("/assistants/reject/{dni}")
    public String rejectAssistant(@PathVariable String dni) {
        PAPAssistant assistant = papAssistantDao.getPAPAssistant(dni);
        if (assistant != null) {
            assistant.setStatus("Rejected");
            papAssistantDao.updatePAPAssistant(assistant);
        }
        return "redirect:/admin/assistants";
    }

    // Ver perfil completo del asistente antes de aprobar/rechazar
    @GetMapping("/assistants/details/{dni}")
    public String assistantDetails(@PathVariable String dni, HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }

        PAPAssistant assistant = papAssistantDao.getPAPAssistant(dni);
        if (assistant != null) {
            model.addAttribute("asistente", assistant);
        }
        return "admin/assistant-details"; // Nueva vista
    }

    // Listado de Usuarios OVI
    @GetMapping("/ovi-users")
    public String listOVIUsers(HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) return "redirect:/login";

        model.addAttribute("users", oviUserDao.getOVIUsers());
        return "admin/ovi-users-list";
    }

    // Ficha de detalle del Usuario OVI
    @GetMapping("/ovi-users/details/{dni}")
    public String oviUserDetails(@PathVariable String dni, HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) return "redirect:/login";

        OVIUser oviUser = oviUserDao.getOVIUser(dni);
        if (oviUser != null) {
            model.addAttribute("usuario", oviUser);
        }
        return "admin/ovi-user-details";
    }

    // Aprobar Usuario OVI
    @PostMapping("/ovi-users/approve/{dni}")
    public String approveOVIUser(@PathVariable String dni) {
        OVIUser oviUser = oviUserDao.getOVIUser(dni);
        if (oviUser != null) {
            oviUser.setStatus("Accepted");
            oviUserDao.updateOVIUser(oviUser);
        }
        return "redirect:/admin/ovi-users";
    }

    // Rechazar/Eliminar Usuario OVI
    @PostMapping("/ovi-users/reject/{dni}")
    public String rejectOVIUser(@PathVariable String dni) {
        OVIUser oviUser = oviUserDao.getOVIUser(dni);
        if (oviUser != null) {
            oviUser.setStatus("Rejected");
            oviUserDao.updateOVIUser(oviUser);
        }
        return "redirect:/admin/ovi-users";
    }

    @PostMapping("/requests/approve/{id}")
    public String approveRequest(@PathVariable int id,
                                 @RequestParam(value = "selectedAssistants", required = false) List<String> selectedAssistants) {
        
        if (selectedAssistants == null || selectedAssistants.isEmpty()) {
            return "redirect:/admin/request-details/" + id + "?error=nocandidates";
        }

        assistanceRequestDao.updateStatus(id, "Accepted");

        selectionDao.deleteSelectionsByRequest(id);

        for (String dni : selectedAssistants) {
            Selection selection = new Selection();
            selection.setIdRequest(id);
            selection.setDniAssistant(dni);
            selectionDao.addSelection(selection);
        }

        return "redirect:/admin/requests";
    }

    @GetMapping("/activities")
    public String listActivitiesAdmin(HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }
        return "redirect:/activity/list";
    }

    @GetMapping("/request-user-details/{dni}")
    public String requestUserDetails(@PathVariable String dni,
                                     @RequestParam Integer requestId,
                                     HttpSession session, Model model) {


        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) return "redirect:/login";

        OVIUser oviUser = oviUserDao.getOVIUser(dni);
        if (oviUser != null) {
            model.addAttribute("usuario", oviUser);
            model.addAttribute("requestId", requestId); // Pasamos el ID para el botón de volver
        }

        return "admin/request-user-details";
    }
}