package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.CommunicationDao;
import es.uji.ei1027.sps.dao.NegotiationDao;
import es.uji.ei1027.sps.model.Communication;
import es.uji.ei1027.sps.model.Negotiation;
import es.uji.ei1027.sps.model.OVIUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/communication")
public class CommunicationControler {

    private CommunicationDao communicationDao;
    private NegotiationDao negotiationDao;

    @Autowired
    public void setCommunicationDao(CommunicationDao communicationDao) {
        this.communicationDao = communicationDao;
    }

    @Autowired
    public void setNegotiationDao(NegotiationDao negotiationDao) {
        this.negotiationDao = negotiationDao;
    }

    // LISTAR
    @RequestMapping("/list")
    public String list(@RequestParam(required = false) Integer idNegotiation,
                       HttpSession session,
                       Model model) {
        OVIUser user = getLoggedOVIUser(session);
        List<Negotiation> negotiations;
        List<String> ownSenders;

        if (user != null) {
            ownSenders = Arrays.asList(user.getDni(), user.getName(), "OVIUser", "oviuser", "Usuario OVI", "ovi");
            negotiations = negotiationDao.getActiveNegotiationsByOVIUser(user.getDni());
            negotiations = filterNegotiationsWithUserMessages(negotiations, ownSenders);
            idNegotiation = getSelectedNegotiationId(idNegotiation, negotiations);
            model.addAttribute("oviuser", user);
        } else {
            ownSenders = Arrays.asList("OVIUser", "oviuser", "Usuario OVI", "ovi");
            negotiations = negotiationDao.getNegotiations();
            idNegotiation = getSelectedNegotiationId(idNegotiation, negotiations);
        }

        model.addAttribute("ownSenders", ownSenders);
        model.addAttribute("negotiations", negotiations);
        model.addAttribute("selectedIdNegotiation", idNegotiation);
        model.addAttribute("communications", idNegotiation == null
                ? communicationDao.getCommunications()
                : communicationDao.getCommunicationsByNegotiation(idNegotiation));
        return "communication/list";
    }

    @RequestMapping("/my-list")
    public String myList(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        return "redirect:/communication/list";
    }

    @RequestMapping("/start/{idNegotiation}")
    public String startChat(@PathVariable int idNegotiation, HttpSession session) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        return "redirect:/communication/list?idNegotiation=" + idNegotiation;
    }

    @RequestMapping(value="/send", method=RequestMethod.POST)
    public String sendMessage(@RequestParam("idNegotiation") int idNegotiation,
                              @RequestParam("message") String message,
                              HttpSession session) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        List<Negotiation> negotiations = negotiationDao.getActiveNegotiationsByOVIUser(user.getDni());
        if (!hasNegotiationId(idNegotiation, negotiations)) {
            return "redirect:/communication/list";
        }

        if (message == null || message.trim().isEmpty()) {
            return "redirect:/communication/list?idNegotiation=" + idNegotiation;
        }

        Communication communication = new Communication();
        communication.setIdNegotiation(idNegotiation);
        communication.setSender(user.getDni());
        communication.setMessage(message.trim());
        communication.setMessageDate(LocalDateTime.now());
        communicationDao.addChatMessage(communication);

        return "redirect:/communication/list?idNegotiation=" + idNegotiation;
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

    private List<Negotiation> filterNegotiationsWithUserMessages(List<Negotiation> negotiations, List<String> ownSenders) {
        List<Negotiation> visibleNegotiations = new ArrayList<Negotiation>();
        for (Negotiation negotiation : negotiations) {
            List<Communication> communications = communicationDao.getCommunicationsByNegotiation(negotiation.getIdNegotiation());
            for (Communication communication : communications) {
                if (isOwnSender(communication.getSender(), ownSenders)) {
                    visibleNegotiations.add(negotiation);
                    break;
                }
            }
        }
        return visibleNegotiations;
    }

    private boolean isOwnSender(String sender, List<String> ownSenders) {
        if (sender == null) {
            return false;
        }
        for (String ownSender : ownSenders) {
            if (ownSender != null && ownSender.equalsIgnoreCase(sender.trim())) {
                return true;
            }
        }
        return false;
    }
}
