package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceRequestDao;
import es.uji.ei1027.sps.dao.NegotiationDao;
import es.uji.ei1027.sps.dao.PAPAssistantDao;
import es.uji.ei1027.sps.model.AssistanceRequest;
import es.uji.ei1027.sps.model.PAPAssistant;
import jakarta.servlet.http.HttpSession;
import es.uji.ei1027.sps.model.SystemUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin") // Añadimos esto para que todas sus rutas empiecen por /admin
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

    // Método para mostrar los candidatos recomendados para una solicitud
    @GetMapping("/request-details/{id}")
    public String showCandidates(@PathVariable int id, HttpSession session, Model model) {
        // Validación de sesión
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }

        // 1. Obtener la solicitud
        AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(id);

        if (request != null) {
            // 2. Obtener los candidatos ordenados por idoneidad
            List<PAPAssistant> candidatos = papAssistantDao.getCandidatesForRequest(request);

            // 3. Pasar los datos a la vista
            model.addAttribute("request", request);
            model.addAttribute("candidatos", candidatos);
        }

        // 4. Redirigir a la vista Thymeleaf
        return "admin/candidates-list";
    }

    // Dentro de AdminController.java

    @GetMapping("/requests")
    public String listRequests(HttpSession session, Model model) {
        // 1. Comprobar seguridad (que sea admin)
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }

        // 2. Pedirle los datos al DAO
        List<AssistanceRequest> solicitudes = assistanceRequestDao.getAssistanceRequests();
        model.addAttribute("solicitudes", solicitudes);

        // 3. Devolver el nombre del archivo HTML (¡sin el .html!)
        return "admin/requests-list";
    }

    @PostMapping("/approve/{id}")
    public String approveRequest(@PathVariable int id) {
        AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(id);
        if (request != null) {
            request.setStatus("Accepted");
            assistanceRequestDao.updateAssistanceRequest(request);
        }
        // Tras aprobar, redirige a la selección de candidatos
        return "redirect:/admin/request-details/" + id;
    }

    @PostMapping("/reject/{id}")
    public String rejectRequest(@PathVariable int id) {
        AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(id);
        if (request != null) {
            // Y para rechazar, usamos la otra palabra permitida
            request.setStatus("Rejected");
            assistanceRequestDao.updateAssistanceRequest(request);
        }
        return "redirect:/admin/requests";
    }
}
