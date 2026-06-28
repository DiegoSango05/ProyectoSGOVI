package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceRequestDao;
import es.uji.ei1027.sps.dao.CommunicationDao;
import es.uji.ei1027.sps.dao.NegotiationDao;
import es.uji.ei1027.sps.dao.OVIUserDao;
import es.uji.ei1027.sps.dao.PAPAssistantDao;
import es.uji.ei1027.sps.model.AssistanceRequest;
import es.uji.ei1027.sps.model.Communication;
import es.uji.ei1027.sps.model.Negotiation;
import es.uji.ei1027.sps.model.OVIUser;
import es.uji.ei1027.sps.model.PAPAssistant;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/communication")
public class CommunicationControler {

    private CommunicationDao communicationDao;
    private NegotiationDao negotiationDao;
    private AssistanceRequestDao assistanceRequestDao;
    private OVIUserDao oviUserDao;
    private PAPAssistantDao papAssistantDao;

    @Autowired
    public void setCommunicationDao(CommunicationDao communicationDao) {
        this.communicationDao = communicationDao;
    }

    @Autowired
    public void setNegotiationDao(NegotiationDao negotiationDao) {
        this.negotiationDao = negotiationDao;
    }

    @Autowired
    public void setAssistanceRequestDao(AssistanceRequestDao assistanceRequestDao) {
        this.assistanceRequestDao = assistanceRequestDao;
    }

    @Autowired
    public void setOviUserDao(OVIUserDao oviUserDao) {
        this.oviUserDao = oviUserDao;
    }

    @Autowired
    public void setPapAssistantDao(PAPAssistantDao papAssistantDao) {
        this.papAssistantDao = papAssistantDao;
    }

    // LISTAR
    @RequestMapping("/list")
    public String list(@RequestParam(required = false) Integer idNegotiation,
                       @RequestParam(required = false) String q,
                       HttpSession session,
                       Model model) {
        OVIUser user = getLoggedOVIUser(session);
        PAPAssistant assistant = getLoggedAssistant(session);
        List<Negotiation> negotiations;
        List<String> ownSenders;

        if (user != null) {
            ownSenders = Arrays.asList(user.getDni(), user.getName(), "OVIUser", "oviuser", "Usuario OVI", "ovi");
            negotiations = negotiationDao.getActiveNegotiationsByOVIUser(user.getDni());
            negotiations = filterNegotiationsByParticipant(negotiations, q, true);
            idNegotiation = getSelectedNegotiationId(idNegotiation, negotiations);
            model.addAttribute("oviuser", user);
            model.addAttribute("backUrl", "/oviuser/chats");
        } else if (assistant != null) {
            ownSenders = Arrays.asList(assistant.getDni(), assistant.getName(), "PAPAssistant", "pap_assistant", "asistente", "Asistente PAP");
            negotiations = negotiationDao.getActiveNegotiationsByAssistant(assistant.getDni());
            negotiations = filterNegotiationsByParticipant(negotiations, q, false);
            idNegotiation = getSelectedNegotiationId(idNegotiation, negotiations);
            model.addAttribute("papassistant", assistant);
            model.addAttribute("backUrl", "/pap_assistant/chats");
        } else {
            ownSenders = Arrays.asList("OVIUser", "oviuser", "Usuario OVI", "ovi");
            negotiations = negotiationDao.getNegotiations();
            negotiations = filterNegotiationsByParticipant(negotiations, q, null);
            idNegotiation = getSelectedNegotiationId(idNegotiation, negotiations);
            model.addAttribute("backUrl", "/oviuser/chats");
        }

        for (Negotiation neg : negotiations) {
            List<Communication> comms = communicationDao.getCommunicationsByNegotiation(neg.getIdNegotiation());
            int count = 0;
            for (int i = comms.size() - 1; i >= 0; i--) {
                Communication comm = comms.get(i);
                if (!ownSenders.contains(comm.getSender())) {
                    count++;
                } else {
                    break;
                }
            }
            neg.setPendingMessagesCount(count);
        }

        model.addAttribute("ownSenders", ownSenders);
        model.addAttribute("negotiations", negotiations);
        model.addAttribute("oviUserDnisByNegotiationId", getOVIUserDnisByNegotiationId(negotiations));
        model.addAttribute("oviUserLabelsByNegotiationId", getOVIUserLabelsByNegotiationId(negotiations));
        model.addAttribute("assistantLabelsByNegotiationId", getAssistantLabelsByNegotiationId(negotiations));
        model.addAttribute("searchQuery", q == null ? "" : q.trim());
        model.addAttribute("selectedIdNegotiation", idNegotiation);
        model.addAttribute("communications", idNegotiation == null
                ? communicationDao.getCommunications()
                : communicationDao.getCommunicationsByNegotiation(idNegotiation));
        addAcceptanceAttributes(model, idNegotiation, user, assistant);
        return "communication/list";
    }

    @RequestMapping("/my-list")
    public String myList(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        PAPAssistant assistant = getLoggedAssistant(session);
        if (user == null && assistant == null) {
            return "redirect:/login";
        }
        return "redirect:/communication/list";
    }

    @RequestMapping("/start/{idNegotiation}")
    public String startChat(@PathVariable int idNegotiation, HttpSession session) {
        OVIUser user = getLoggedOVIUser(session);
        PAPAssistant assistant = getLoggedAssistant(session);
        if (user == null && assistant == null) {
            return "redirect:/login";
        }
        return "redirect:/communication/list?idNegotiation=" + idNegotiation;
    }

    @PostMapping("/accept")
    public String acceptNegotiation(@RequestParam("idNegotiation") int idNegotiation,
                                    @RequestParam(required = false) String q,
                                    HttpSession session) {
        OVIUser user = getLoggedOVIUser(session);
        PAPAssistant assistant = getLoggedAssistant(session);
        if (user == null && assistant == null) {
            return "redirect:/login";
        }

        List<Negotiation> negotiations = user != null
                ? negotiationDao.getActiveNegotiationsByOVIUser(user.getDni())
                : negotiationDao.getActiveNegotiationsByAssistant(assistant.getDni());
        if (!hasNegotiationId(idNegotiation, negotiations)) {
            return "redirect:/communication/list" + buildInitialSearchRedirect(q);
        }

        Negotiation negotiation = negotiationDao.getNegotiation(idNegotiation);
        if (negotiation != null && "Pending".equalsIgnoreCase(negotiation.getStatus())) {
            if (user != null) {
                negotiationDao.acceptByCustomer(idNegotiation);
            } else {
                negotiationDao.acceptByAssistant(idNegotiation);
            }
        }

        return "redirect:/communication/list?idNegotiation=" + idNegotiation + buildSearchRedirect(q);
    }

    @RequestMapping(value="/send", method=RequestMethod.POST)
    public String sendMessage(@RequestParam("idNegotiation") int idNegotiation,
                              @RequestParam("message") String message,
                              @RequestParam(required = false) String q,
                              HttpSession session) {
        OVIUser user = getLoggedOVIUser(session);
        PAPAssistant assistant = getLoggedAssistant(session);
        if (user == null && assistant == null) {
            return "redirect:/login";
        }

        List<Negotiation> negotiations = user != null
                ? negotiationDao.getActiveNegotiationsByOVIUser(user.getDni())
                : negotiationDao.getActiveNegotiationsByAssistant(assistant.getDni());
        if (!hasNegotiationId(idNegotiation, negotiations)) {
            return "redirect:/communication/list" + buildInitialSearchRedirect(q);
        }

        if (message == null || message.trim().isEmpty()) {
            return "redirect:/communication/list?idNegotiation=" + idNegotiation + buildSearchRedirect(q);
        }

        Communication communication = new Communication();
        communication.setIdNegotiation(idNegotiation);
        communication.setSender(user != null ? user.getDni() : assistant.getDni());
        communication.setMessage(message.trim());
        communication.setMessageDate(LocalDateTime.now());
        communicationDao.addChatMessage(communication);

        return "redirect:/communication/list?idNegotiation=" + idNegotiation + buildSearchRedirect(q);
    }

    // NUEVO CHAT (Formulario simplificado)
    @RequestMapping("/new-chat")
    public String newChat(Model model) {
        model.addAttribute("communication", new Communication());
        return "communication/new-chat";
    }

    // NUEVO CHAT (Procesar)
    @RequestMapping(value="/new-chat", method=RequestMethod.POST)
    public String processNewChat(@ModelAttribute("communication") Communication communication,
                                 HttpSession session, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "communication/new-chat";

        communicationDao.addCommunication(communication);
        return "redirect:list";
    }

    // AÑADIR (Formulario)
    @RequestMapping("/add")
    public String addCommunication(Model model) {
        model.addAttribute("communication", new Communication());
        return "communication/add";
    }

    // AÑADIR (Procesar)
    @RequestMapping(value="/add", method= RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("communication") Communication communication,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "communication/add";

        communicationDao.addCommunication(communication);
        return "redirect:list";
    }

    // ELIMINAR
    @RequestMapping(value="/delete/{id}")
    public String processDelete(@PathVariable int id) {
        communicationDao.deleteCommunication(id);
        return "redirect:../list";
    }

    // ACTUALIZAR (Formulario)
    @RequestMapping(value="/update/{id}", method = RequestMethod.GET)
    public String editCommunication(Model model, @PathVariable int id) {
        model.addAttribute("communication", communicationDao.getCommunication(id));
        return "communication/update";
    }

    // ACTUALIZAR (Formulario)
    @RequestMapping(value="/update", method = RequestMethod.POST)
    public String processUpdateSubmit(@ModelAttribute("communication") Communication communication,
                                      BindingResult bindingResult) {
        // Si creaste un CommunicationValidator, llámalo aquí
        if (bindingResult.hasErrors())
            return "communication/update";

        communicationDao.updateCommunication(communication);
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

    private PAPAssistant getLoggedAssistant(HttpSession session) {
        Object user = session.getAttribute("user");
        Object role = session.getAttribute("role");
        if (!"asistente".equals(role) || !(user instanceof PAPAssistant)) {
            return null;
        }
        return (PAPAssistant) user;
    }

    private Integer getSelectedNegotiationId(Integer requestedId, List<Negotiation> negotiations) {
        if (negotiations == null || negotiations.isEmpty()) {
            return null;
        }
        if (requestedId != null) {
            for (Negotiation negotiation : negotiations) {
                if (negotiation.getIdNegotiation() == requestedId) {
                    return requestedId;
                }
            }
        }
        return negotiations.get(0).getIdNegotiation();
    }

    private boolean hasNegotiationId(int idNegotiation, List<Negotiation> negotiations) {
        if (negotiations == null) {
            return false;
        }
        for (Negotiation negotiation : negotiations) {
            if (negotiation.getIdNegotiation() == idNegotiation) {
                return true;
            }
        }
        return false;
    }

    private Map<Integer, String> getOVIUserDnisByNegotiationId(List<Negotiation> negotiations) {
        Map<Integer, String> oviUserDnis = new HashMap<Integer, String>();
        for (Negotiation negotiation : negotiations) {
            AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(negotiation.getIdRequest());
            if (request != null) {
                oviUserDnis.put(negotiation.getIdNegotiation(), request.getDniOVIuser());
            }
        }
        return oviUserDnis;
    }

    private List<Negotiation> filterNegotiationsByParticipant(List<Negotiation> negotiations,
                                                              String searchText,
                                                              Boolean searchAssistant) {
        if (searchText == null || searchText.isBlank()) {
            return negotiations;
        }

        String normalizedSearch = searchText.trim().toLowerCase(Locale.ROOT);
        return negotiations.stream()
                .filter(negotiation -> {
                    if (Boolean.TRUE.equals(searchAssistant)) {
                        return containsIgnoreCase(getAssistantLabel(negotiation), normalizedSearch);
                    }
                    if (Boolean.FALSE.equals(searchAssistant)) {
                        return containsIgnoreCase(getOVIUserLabel(negotiation), normalizedSearch);
                    }
                    return containsIgnoreCase(getAssistantLabel(negotiation), normalizedSearch)
                            || containsIgnoreCase(getOVIUserLabel(negotiation), normalizedSearch);
                })
                .toList();
    }

    private Map<Integer, String> getOVIUserLabelsByNegotiationId(List<Negotiation> negotiations) {
        Map<Integer, String> oviUserLabels = new HashMap<Integer, String>();
        for (Negotiation negotiation : negotiations) {
            oviUserLabels.put(negotiation.getIdNegotiation(), getOVIUserLabel(negotiation));
        }
        return oviUserLabels;
    }

    private Map<Integer, String> getAssistantLabelsByNegotiationId(List<Negotiation> negotiations) {
        Map<Integer, String> assistantLabels = new HashMap<Integer, String>();
        for (Negotiation negotiation : negotiations) {
            assistantLabels.put(negotiation.getIdNegotiation(), getAssistantLabel(negotiation));
        }
        return assistantLabels;
    }

    private String getOVIUserLabel(Negotiation negotiation) {
        AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(negotiation.getIdRequest());
        if (request == null) {
            return "Sin dato";
        }
        OVIUser oviUser = oviUserDao.getOVIUser(request.getDniOVIuser());
        return oviUser == null
                ? nullSafe(request.getDniOVIuser())
                : oviUser.getName() + " (" + oviUser.getDni() + ")";
    }

    private String getAssistantLabel(Negotiation negotiation) {
        PAPAssistant assistant = papAssistantDao.getPAPAssistant(negotiation.getDniAssistant());
        return assistant == null
                ? nullSafe(negotiation.getDniAssistant())
                : assistant.getName() + " (" + assistant.getDni() + ")";
    }

    private boolean containsIgnoreCase(String value, String normalizedSearch) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedSearch);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "Sin dato" : value;
    }

    private String buildInitialSearchRedirect(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return "";
        }
        return "?q=" + URLEncoder.encode(searchText.trim(), StandardCharsets.UTF_8);
    }

    private String buildSearchRedirect(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return "";
        }
        return "&q=" + URLEncoder.encode(searchText.trim(), StandardCharsets.UTF_8);
    }

    private void addAcceptanceAttributes(Model model,
                                         Integer idNegotiation,
                                         OVIUser user,
                                         PAPAssistant assistant) {
        Negotiation negotiation = idNegotiation == null
                ? null
                : negotiationDao.getNegotiation(idNegotiation);
        boolean currentUserAccepted = false;
        boolean otherPartyAccepted = false;

        if (negotiation != null && user != null) {
            currentUserAccepted = negotiation.isAcceptedCustomer();
            otherPartyAccepted = negotiation.isAcceptedAssistant();
        } else if (negotiation != null && assistant != null) {
            currentUserAccepted = negotiation.isAcceptedAssistant();
            otherPartyAccepted = negotiation.isAcceptedCustomer();
        }

        model.addAttribute("selectedNegotiation", negotiation);
        model.addAttribute("currentUserAccepted", currentUserAccepted);
        model.addAttribute("otherPartyAccepted", otherPartyAccepted);
        model.addAttribute("canAcceptNegotiation",
                negotiation != null
                        && (user != null || assistant != null)
                        && "Pending".equalsIgnoreCase(negotiation.getStatus())
                        && !currentUserAccepted);
    }
}
