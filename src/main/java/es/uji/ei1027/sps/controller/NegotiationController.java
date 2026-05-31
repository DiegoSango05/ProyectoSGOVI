package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceRequestDao;
import es.uji.ei1027.sps.dao.CommunicationDao;
import es.uji.ei1027.sps.dao.NegotiationDao;
import es.uji.ei1027.sps.dao.SelectionDao;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/negotiation")
public class NegotiationController {

    @Autowired
    private NegotiationDao negotiationDao;

    @Autowired
    private AssistanceRequestDao assistanceRequestDao;

    @Autowired
    private CommunicationDao communicationDao;

    @Autowired
    private SelectionDao selectionDao;

    @Autowired
    public void setNegotiationDao(NegotiationDao negotiationDao) {
        this.negotiationDao = negotiationDao;
    }

    // LISTAR
    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("negotiations", negotiationDao.getNegotiations());
        return "negotiation/list";
    }

    // AÑADIR (Formulario)
    @RequestMapping("/add")
    public String addNegotiation(Model model) {
        model.addAttribute("negotiation", new Negotiation());
        return "negotiation/add";
    }

    // AÑADIR (Procesar)
    @RequestMapping(value="/add", method= RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("negotiation") Negotiation negotiation,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "negotiation/add";

        negotiationDao.addNegotiation(negotiation);
        return "redirect:list";
    }

    // ELIMINAR
    @RequestMapping(value="/delete/{id}")
    public String processDelete(@PathVariable int id) {
        negotiationDao.deleteNegotiation(id);
        return "redirect:../list";
    }

    // ACTUALIZAR (Formulario)
    @RequestMapping(value="/update/{id}", method = RequestMethod.GET)
    public String editNegotiation(Model model, @PathVariable int id) {
        model.addAttribute("negotiation", negotiationDao.getNegotiation(id));
        return "negotiation/update";
    }

    // ACTUALIZAR (Procesar)
    @RequestMapping(value="/update", method = RequestMethod.POST)
    public String processUpdateSubmit(@ModelAttribute("negotiation") Negotiation negotiation,
                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "negotiation/update";
        negotiationDao.updateNegotiation(negotiation);
        return "redirect:list";
    }

    @PostMapping("/start")
    public String startNegotiation(@RequestParam("idRequest") int idRequest,
                                   @RequestParam("dniAssistant") String dniAssistant,
                                   HttpSession session) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(idRequest);
        if (request == null || !user.getDni().equals(request.getDniOVIuser())
                || !"Accepted".equalsIgnoreCase(request.getStatus())) {
            return "redirect:/assistancerequest/my-list";
        }
        if (!selectionDao.isAssistantSelectedForRequest(idRequest, dniAssistant)) {
            return "redirect:/assistancerequest/candidates/" + idRequest;
        }

        Negotiation existingNegotiation = negotiationDao.getActiveNegotiation(idRequest, dniAssistant);
        if (existingNegotiation != null) {
            return "redirect:/communication/list?idNegotiation=" + existingNegotiation.getIdNegotiation();
        }

        Negotiation negotiation = new Negotiation();
        negotiation.setStatus("Pending");
        negotiation.setNegotiationDate(LocalDate.now());
        negotiation.setIdRequest(idRequest);
        negotiation.setDniAssistant(dniAssistant);

        int idNegotiation = negotiationDao.addNegotiationAndReturnId(negotiation);

        Communication firstMessage = new Communication();
        firstMessage.setIdNegotiation(idNegotiation);
        firstMessage.setSender(user.getDni());
        firstMessage.setMessage("Chat iniciado");
        firstMessage.setMessageDate(LocalDateTime.now());
        communicationDao.addChatMessage(firstMessage);

        return "redirect:/communication/list?idNegotiation=" + idNegotiation;
    }

    @PostMapping("/start-by-assistant")
    public String startNegotiationByAssistant(@RequestParam("idRequest") int idRequest,
                                              HttpSession session) {
        PAPAssistant assistant = getLoggedAssistant(session);
        if (assistant == null) {
            return "redirect:/login";
        }

        AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(idRequest);
        if (request == null || !"Accepted".equalsIgnoreCase(request.getStatus())
                || !selectionDao.isAssistantSelectedForRequest(idRequest, assistant.getDni())) {
            return "redirect:/assistancerequest/assistant-list";
        }

        Negotiation existingNegotiation = negotiationDao.getActiveNegotiation(idRequest, assistant.getDni());
        if (existingNegotiation != null) {
            return "redirect:/communication/list?idNegotiation=" + existingNegotiation.getIdNegotiation();
        }

        Negotiation negotiation = new Negotiation();
        negotiation.setStatus("Pending");
        negotiation.setNegotiationDate(LocalDate.now());
        negotiation.setIdRequest(idRequest);
        negotiation.setDniAssistant(assistant.getDni());

        int idNegotiation = negotiationDao.addNegotiationAndReturnId(negotiation);

        Communication firstMessage = new Communication();
        firstMessage.setIdNegotiation(idNegotiation);
        firstMessage.setSender(assistant.getDni());
        firstMessage.setMessage("Chat iniciado");
        firstMessage.setMessageDate(LocalDateTime.now());
        communicationDao.addChatMessage(firstMessage);

        return "redirect:/communication/list?idNegotiation=" + idNegotiation;
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
}
