package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceRequestDao;
import es.uji.ei1027.sps.dao.PAPAssistantDao;
import es.uji.ei1027.sps.model.AssistanceRequest;
import es.uji.ei1027.sps.model.PAPAssistant;
import jakarta.servlet.http.HttpSession;
import es.uji.ei1027.sps.model.SystemUser;
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

    @PostMapping("/propose-candidates")
    public String processPropose(@RequestParam int idRequest,
                                 @RequestParam(required = false) List<String> selectedAssistants) {

        if (selectedAssistants == null || selectedAssistants.isEmpty()) {
            return "redirect:/admin/request-details/" + idRequest + "?error=nocandidates";
        }

        AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(idRequest);
        if (request != null) {
            request.setStatus("Accepted");
            assistanceRequestDao.updateAssistanceRequest(request);
        }
        return "redirect:/admin/requests";
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
}