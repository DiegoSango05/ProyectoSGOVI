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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
    public String myList(@RequestParam(value = "search", required = false, defaultValue = "") String search,
                         @RequestParam(value = "orderBy", required = false, defaultValue = "type") String orderBy,
                         @RequestParam(value = "dir", required = false, defaultValue = "asc") String dir,
                         @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                         HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        List<AssistanceRequest> assistanceRequests = assistanceRequestDao.getAssistanceRequestsByOVIUser(user.getDni());
        if (assistanceRequests == null) {
            assistanceRequests = new ArrayList<AssistanceRequest>();
        }

        String searchLower = normalize(search);
        if (!searchLower.isEmpty()) {
            assistanceRequests = assistanceRequests.stream()
                    .filter(request -> containsIgnoreCase(request.getType(), searchLower)
                            || containsIgnoreCase(request.getDescription(), searchLower)
                            || containsIgnoreCase(request.getSchedule(), searchLower)
                            || containsIgnoreCase(request.getLocation(), searchLower)
                            || containsIgnoreCase(request.getRequirements(), searchLower)
                            || containsIgnoreCase(request.getStatus(), searchLower))
                    .collect(Collectors.toList());
        }

        Comparator<AssistanceRequest> comparator = Comparator.comparing(request -> safeLower(request.getType()));
        if ("location".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(request -> safeLower(request.getLocation()));
        } else if ("status".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(request -> safeLower(request.getStatus()));
        } else if ("schedule".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(request -> safeLower(request.getSchedule()));
        }
        if ("desc".equalsIgnoreCase(dir)) {
            comparator = comparator.reversed();
        }
        assistanceRequests.sort(comparator);

        PageResult<AssistanceRequest> pageResult = paginate(assistanceRequests, page, 4);

        model.addAttribute("assistanceRequests", pageResult.items());
        model.addAttribute("search", search);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("dir", dir);
        model.addAttribute("currentPage", pageResult.currentPage());
        model.addAttribute("totalPages", pageResult.totalPages());
        model.addAttribute("totalRecords", pageResult.totalRecords());
        addOVIListAttributes(model, pageResult.items());
        return "assistancerequest/ovi-list";
    }

    @RequestMapping("/assistant-list")
    public String assistantList(@RequestParam(value = "search", required = false, defaultValue = "") String search,
                                @RequestParam(value = "orderBy", required = false, defaultValue = "type") String orderBy,
                                @RequestParam(value = "dir", required = false, defaultValue = "asc") String dir,
                                @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                HttpSession session, Model model) {
        PAPAssistant assistant = getLoggedAssistant(session);
        if (assistant == null) {
            return "redirect:/login";
        }
        List<AssistanceRequest> assistanceRequests =
                assistanceRequestDao.getAcceptedAssistanceRequestsByAssistant(assistant.getDni());
        if (assistanceRequests == null) {
            assistanceRequests = new ArrayList<AssistanceRequest>();
        }

        String searchLower = normalize(search);
        if (!searchLower.isEmpty()) {
            assistanceRequests = assistanceRequests.stream()
                    .filter(request -> containsIgnoreCase(request.getType(), searchLower)
                            || containsIgnoreCase(request.getDescription(), searchLower)
                            || containsIgnoreCase(request.getSchedule(), searchLower)
                            || containsIgnoreCase(request.getLocation(), searchLower)
                            || containsIgnoreCase(request.getRequirements(), searchLower)
                            || containsIgnoreCase(request.getStatus(), searchLower)
                            || containsIgnoreCase(request.getDniOVIuser(), searchLower))
                    .collect(Collectors.toList());
        }

        Comparator<AssistanceRequest> comparator = Comparator.comparing(request -> safeLower(request.getType()));
        if ("location".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(request -> safeLower(request.getLocation()));
        } else if ("status".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(request -> safeLower(request.getStatus()));
        } else if ("schedule".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(request -> safeLower(request.getSchedule()));
        } else if ("dniOVIuser".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(request -> safeLower(request.getDniOVIuser()));
        }
        if ("desc".equalsIgnoreCase(dir)) {
            comparator = comparator.reversed();
        }
        assistanceRequests.sort(comparator);

        PageResult<AssistanceRequest> pageResult = paginate(assistanceRequests, page, 4);

        Map<Integer, Negotiation> negotiationsByRequestId = new HashMap<Integer, Negotiation>();
        for (AssistanceRequest request : pageResult.items()) {
            Negotiation negotiation = negotiationDao.getActiveNegotiation(request.getId(), assistant.getDni());
            if (negotiation != null) {
                negotiationsByRequestId.put(request.getId(), negotiation);
            }
        }
        model.addAttribute("assistanceRequests", pageResult.items());
        model.addAttribute("negotiationsByRequestId", negotiationsByRequestId);
        model.addAttribute("search", search);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("dir", dir);
        model.addAttribute("currentPage", pageResult.currentPage());
        model.addAttribute("totalPages", pageResult.totalPages());
        model.addAttribute("totalRecords", pageResult.totalRecords());
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

    private <T> PageResult<T> paginate(List<T> items, int requestedPage, int pageSize) {
        int totalRecords = items.size();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPages == 0) {
            totalPages = 1;
        }

        int currentPage = requestedPage;
        if (currentPage < 1) {
            currentPage = 1;
        }
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalRecords);
        List<T> pageItems = new ArrayList<T>();
        if (fromIndex < totalRecords) {
            pageItems = items.subList(fromIndex, toIndex);
        }

        return new PageResult<T>(pageItems, currentPage, totalPages, totalRecords);
    }

    private boolean containsIgnoreCase(String value, String normalizedSearch) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedSearch);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private record PageResult<T>(List<T> items, int currentPage, int totalPages, int totalRecords) {
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
