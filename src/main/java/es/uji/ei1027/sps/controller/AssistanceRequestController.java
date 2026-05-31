package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceRequestDao;
import es.uji.ei1027.sps.dao.NegotiationDao;
import es.uji.ei1027.sps.dao.SelectionDao;
import es.uji.ei1027.sps.model.AssistanceRequest;
import es.uji.ei1027.sps.model.Negotiation;
import es.uji.ei1027.sps.model.OVIUser;
import es.uji.ei1027.sps.model.PAPAssistant;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/assistancerequest")
public class AssistanceRequestController {

    private AssistanceRequestDao assistanceRequestDao;
    private NegotiationDao negotiationDao;

    @Autowired
    public void setAssistanceRequestDao(AssistanceRequestDao assistanceRequestDao) {
        this.assistanceRequestDao = assistanceRequestDao;
    }
    @Autowired
    private SelectionDao selectionDao;

    @Autowired
    public void setNegotiationDao(NegotiationDao negotiationDao) {
        this.negotiationDao = negotiationDao;
    }

    // LISTAR
    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("assistanceRequests", assistanceRequestDao.getAssistanceRequests());
        return "assistancerequest/list";
    }

    @RequestMapping("/my-list")
    public String myList(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        List<AssistanceRequest> assistanceRequests = assistanceRequestDao.getAssistanceRequestsByOVIUser(user.getDni());
        model.addAttribute("assistanceRequests", assistanceRequests);
        addOVIListAttributes(model, assistanceRequests);
        return "assistancerequest/ovi-list";
    }

    @RequestMapping("/assistant-list")
    public String assistantList(HttpSession session, Model model) {
        PAPAssistant assistant = getLoggedAssistant(session);
        if (assistant == null) {
            return "redirect:/login";
        }
        List<AssistanceRequest> assistanceRequests =
                assistanceRequestDao.getAcceptedAssistanceRequestsByAssistant(assistant.getDni());
        Map<Integer, Negotiation> negotiationsByRequestId = new HashMap<Integer, Negotiation>();
        for (AssistanceRequest request : assistanceRequests) {
            Negotiation negotiation = negotiationDao.getActiveNegotiation(request.getId(), assistant.getDni());
            if (negotiation != null) {
                negotiationsByRequestId.put(request.getId(), negotiation);
            }
        }
        model.addAttribute("assistanceRequests", assistanceRequests);
        model.addAttribute("negotiationsByRequestId", negotiationsByRequestId);
        return "assistancerequest/assistant-list";
    }

    // AÑADIR (Formulario)
    @RequestMapping("/add")
    public String addAssistanceRequest(Model model) {
        model.addAttribute("assistancerequest", new AssistanceRequest());
        return "assistancerequest/add";
    }

    @RequestMapping("/ovi-add")
    public String addOVIAssistanceRequest(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        AssistanceRequest request = new AssistanceRequest();
        // CORRECCIÓN: Usar "Pending" en lugar de "Pendiente" por la restricción de la BBDD
        request.setStatus("Pending");
        request.setDniOVIuser(user.getDni());
        model.addAttribute("assistancerequest", request);
        return "assistancerequest/ovi-add";
    }

    // AÑADIR (Procesar)
    @RequestMapping(value="/add", method= RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("assistancerequest") AssistanceRequest request,
                                   BindingResult bindingResult,
                                   HttpSession session) {
        if (bindingResult.hasErrors())
            return "assistancerequest/add";

        assistanceRequestDao.addAssistanceRequest(request);
        if (getLoggedOVIUser(session) != null) {
            return "redirect:my-list";
        }
        return "redirect:list";
    }

    @RequestMapping(value="/ovi-add", method= RequestMethod.POST)
    public String processOVIAddSubmit(@ModelAttribute("assistancerequest") AssistanceRequest request,
                                      BindingResult bindingResult,
                                      HttpSession session,
                                      Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        request.setStatus("Pending");
        request.setDniOVIuser(user.getDni());

        if (bindingResult.hasErrors()) {
            return "assistancerequest/ovi-add";
        }

        assistanceRequestDao.addAssistanceRequest(request);
        model.addAttribute("savedRequest", request);
        return "assistancerequest/ovi-add-confirm";
    }

    // ELIMINAR
    @RequestMapping(value="/delete/{id}")
    public String processDelete(@PathVariable int id) {
        assistanceRequestDao.deleteAssistanceRequest(id);
        return "redirect:../list";
    }

    private OVIUser getLoggedOVIUser(HttpSession session) {
        Object user = session.getAttribute("user");
        Object role = session.getAttribute("role");
        if (!"ovi".equals(role) || !(user instanceof OVIUser)) {
            return null;
        }
        return (OVIUser) user;
    }

    private PAPAssistant getLoggedAssistant(HttpSession session) {
        Object user = session.getAttribute("user");
        Object role = session.getAttribute("role");
        if (!"asistente".equals(role) || !(user instanceof PAPAssistant)) {
            return null;
        }
        return (PAPAssistant) user;
    }

    private void addOVIListAttributes(Model model, List<AssistanceRequest> assistanceRequests) {
        Map<Integer, String> statusCssByRequestId = new HashMap<Integer, String>();
        Map<Integer, Boolean> acceptedByRequestId = new HashMap<Integer, Boolean>();
        Map<Integer, Negotiation> negotiationsByRequestId = new HashMap<Integer, Negotiation>();

        for (AssistanceRequest request : assistanceRequests) {
            statusCssByRequestId.put(request.getId(), getStatusCssClass(request.getStatus()));
            acceptedByRequestId.put(request.getId(), false);
        }

        for (Negotiation negotiation : negotiationDao.getNegotiations()) {
            int requestId = negotiation.getIdRequest();
            if (acceptedByRequestId.containsKey(requestId) && isAccepted(negotiation.getStatus())) {
                acceptedByRequestId.put(requestId, true);
                negotiationsByRequestId.put(requestId, negotiation);
            }
        }

        model.addAttribute("statusCssByRequestId", statusCssByRequestId);
        model.addAttribute("acceptedByRequestId", acceptedByRequestId);
        model.addAttribute("negotiationsByRequestId", negotiationsByRequestId);
    }

    private String getStatusCssClass(String status) {
        if ("Accepted".equalsIgnoreCase(status)) {
            return "status-accepted";
        }
        if ("Rejected".equalsIgnoreCase(status)) {
            return "status-rejected";
        }
        return "status-pending";
    }

    private boolean isAccepted(String status) {
        return "Accepted".equalsIgnoreCase(status);
    }

    @GetMapping("/candidates/{id}")
    public String listCandidates(@PathVariable int id, HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) return "redirect:/login";

        AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(id);

        // Solo permitimos ver candidatos si la solicitud es del usuario y está aceptada
        if (request == null || !request.getDniOVIuser().equals(user.getDni())
                || !"Accepted".equalsIgnoreCase(request.getStatus())) {
            return "redirect:/assistancerequest/my-list";
        }

        List<PAPAssistant> candidates = selectionDao.getAssistantsForRequest(id);
        model.addAttribute("request", request);
        model.addAttribute("candidates", candidates);
        model.addAttribute("contactedAssistantDnis", negotiationDao.getActiveAssistantDnisByRequest(id));

        return "assistancerequest/ovi-candidates";
    }

}
