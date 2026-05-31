package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.ContractDao;
import es.uji.ei1027.sps.dao.NegotiationDao;
import es.uji.ei1027.sps.model.Contract;
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
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/contract")
public class ContractController {

    private ContractDao contractDao;
    private NegotiationDao negotiationDao;
    private ContractValidator contractValidator;

    @Autowired
    public void setContractDao(ContractDao contractDao) {
        this.contractDao = contractDao;
    }

    @Autowired
    public void setNegotiationDao(NegotiationDao negotiationDao) {
        this.negotiationDao = negotiationDao;
    }

    @Autowired
    public void setContractValidator(ContractValidator contractValidator) {
        this.contractValidator = contractValidator;
    }

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("contracts", contractDao.getContracts());
        return "contract/list";
    }

    @RequestMapping("/my-list")
    public String myList(HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("contracts", contractDao.getContractsByOVIUser(user.getDni()));
        return "contract/ovi-list";
    }

    @RequestMapping("/assistant-list")
    public String assistantList(HttpSession session, Model model) {
        PAPAssistant assistant = getLoggedAssistant(session);
        if (assistant == null) {
            return "redirect:/login";
        }
        model.addAttribute("contracts", contractDao.getContractsByAssistant(assistant.getDni()));
        return "contract/assistant-list";
    }

    @RequestMapping("/delete/{id}")
    public String processDelete(@PathVariable int id) {
        contractDao.deleteContract(id);
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

    @GetMapping("/admin-dashboard")
    public String adminDashboard(@RequestParam(value = "status", defaultValue = "Pending") String status,
                                 HttpSession session, Model model) {

        Object role = session.getAttribute("role");
        Object user = session.getAttribute("user");

        if (!"admin".equals(role) || user == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentStatus", status);
        model.addAttribute("mutualAgreements", negotiationDao.getMutualAgreements());

        List<Contract> filteredContracts = new ArrayList<>();

        if ("Accepted".equalsIgnoreCase(status)) {
            List<Contract> allContracts = contractDao.getContracts();
            for (Contract c : allContracts) {
                if ("Accepted".equalsIgnoreCase(c.getStatus())) {
                    filteredContracts.add(c);
                }
            }
            model.addAttribute("contracts", filteredContracts);
        } else if ("Rejected".equalsIgnoreCase(status)) {
            List<Contract> allContracts = contractDao.getContracts();
            for (Contract c : allContracts) {
                if ("Rejected".equalsIgnoreCase(c.getStatus())) {
                    filteredContracts.add(c);
                }
            }
            model.addAttribute("contracts", filteredContracts);
        } else {
            model.addAttribute("contracts", filteredContracts);
        }

        return "admin/contracts-dashboard";
    }

    @GetMapping("/admin-create")
    public String showAdminCreateForm(@RequestParam("idNegotiation") int idNegotiation,
                                      HttpSession session, Model model) {
        Object role = session.getAttribute("role");
        Object user = session.getAttribute("user");
        if (!"admin".equals(role) || user == null) {
            return "redirect:/login";
        }

        Contract contract = new Contract();
        contract.setIdNegotiation(idNegotiation);
        contract.setStartDate(LocalDate.now());
        contract.setStatus("Accepted");

        try {
            Negotiation negotiation = negotiationDao.getNegotiation(idNegotiation);
            if (negotiation != null) {
                model.addAttribute("idRequest", negotiation.getIdRequest());
                contract.setDocument("Contrato_Peticion_" + negotiation.getIdRequest() + ".pdf");
            }
        } catch (Exception e) {
            model.addAttribute("idRequest", 0);
            contract.setDocument("Contrato_OVI_" + idNegotiation + ".pdf");
        }

        model.addAttribute("contract", contract);
        return "admin/contract-create";
    }

    @PostMapping("/admin-create")
    public String processAdminCreateSubmit(@ModelAttribute("contract") Contract contract,
                                           BindingResult bindingResult,
                                           @RequestParam(value = "confirmado", defaultValue = "false") boolean confirmado,
                                           HttpSession session, Model model) {
        Object role = session.getAttribute("role");
        Object user = session.getAttribute("user");

        if (!"admin".equals(role) || user == null) {
            return "redirect:/login";
        }

        contractValidator.validate(contract, bindingResult);

        if (bindingResult.hasErrors()) {
            int idRequest = 0;
            try {
                Negotiation negotiation = negotiationDao.getNegotiation(contract.getIdNegotiation());
                if (negotiation != null) idRequest = negotiation.getIdRequest();
            } catch (Exception e) {}

            model.addAttribute("idRequest", idRequest);
            return "admin/contract-create";
        }

        if (!confirmado) {
            int idRequest = 0;
            try {
                Negotiation negotiation = negotiationDao.getNegotiation(contract.getIdNegotiation());
                if (negotiation != null) idRequest = negotiation.getIdRequest();
            } catch (Exception e) {}

            model.addAttribute("idRequest", idRequest);
            model.addAttribute("verModalConfirmacion", true);
            return "admin/contract-create";
        }

        contractDao.addContract(contract);
        return "redirect:/contract/admin-dashboard";
    }

    @GetMapping("/admin-edit/{id}")
    public String showAdminEditForm(@PathVariable int id, HttpSession session, Model model) {
        Object role = session.getAttribute("role");
        Object user = session.getAttribute("user");

        if (!"admin".equals(role) || user == null) {
            return "redirect:/login";
        }

        Contract contract = contractDao.getContract(id);
        if (contract == null) {
            return "redirect:/contract/admin-dashboard";
        }
        model.addAttribute("contract", contract);
        return "admin/contract-edit";
    }

    @PostMapping("/admin-edit")
    public String processAdminEditSubmit(@ModelAttribute("contract") Contract contract,
                                         BindingResult bindingResult,
                                         HttpSession session, Model model) {
        Object role = session.getAttribute("role");
        Object user = session.getAttribute("user");

        if (!"admin".equals(role) || user == null) {
            return "redirect:/login";
        }

        // Validamos el objeto editado temporalmente
        contractValidator.validate(contract, bindingResult);

        if (bindingResult.hasErrors()) {
            return "admin/contract-edit";
        }

        // 🌟 PASO DE PREVENCIÓN PERFECTO:
        // Si los datos son correctos, cargamos el modal y le inyectamos los nuevos valores al modelo
        model.addAttribute("contract", contract);
        model.addAttribute("verModalConfirmacion", true);
        return "admin/contract-edit";
    }

    // 🌟 NUEVO MÉTODO COMPLETAMENTE BLINDADO PARA HACER EL UPDATE SIN ERRORES DE BINDING
    @GetMapping("/admin-edit-confirm")
    public String processAdminEditConfirm(@RequestParam("id") int id,
                                          @RequestParam("idNegotiation") int idNegotiation,
                                          @RequestParam("startDate") String startDateStr,
                                          @RequestParam("endDate") String endDateStr,
                                          @RequestParam("status") String status,
                                          @RequestParam("document") String document,
                                          HttpSession session) {
        Object role = session.getAttribute("role");
        Object user = session.getAttribute("user");

        if (!"admin".equals(role) || user == null) {
            return "redirect:/login";
        }

        // Reconstruimos el objeto limpio a mano para que la base de datos lo actualice sin quejas
        Contract contract = new Contract();
        contract.setId(id);
        contract.setIdNegotiation(idNegotiation);
        contract.setStartDate(LocalDate.parse(startDateStr));
        if (endDateStr != null && !endDateStr.trim().isEmpty() && !"null".equals(endDateStr)) {
            contract.setEndDate(LocalDate.parse(endDateStr));
        } else {
            contract.setEndDate(null);
        }
        contract.setStatus(status);
        contract.setDocument(document);

        // Guardamos de forma limpia en la BD y redirigimos al listado
        contractDao.updateContract(contract);
        return "redirect:/contract/admin-dashboard";
    }
}