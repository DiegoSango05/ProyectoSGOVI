package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.ContractDao;
import es.uji.ei1027.sps.dao.NegotiationDao;
import es.uji.ei1027.sps.model.Contract;
import es.uji.ei1027.sps.model.Negotiation;
import es.uji.ei1027.sps.model.OVIUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/contract")
public class ContractController {

    private ContractDao contractDao;
    private NegotiationDao negotiationDao;

    @Autowired
    public void setContractDao(ContractDao contractDao) {
        this.contractDao = contractDao;
    }

    @Autowired
    public void setNegotiationDao(NegotiationDao negotiationDao) {
        this.negotiationDao = negotiationDao;
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

    @GetMapping("/admin-dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("mutualAgreements", negotiationDao.getMutualAgreements());
        model.addAttribute("contracts", contractDao.getContracts());
        return "admin/contracts-dashboard";
    }

    @GetMapping("/admin-create")
    public String showAdminCreateForm(@RequestParam("idNegotiation") int idNegotiation, Model model) {
        Contract contract = new Contract();
        contract.setIdNegotiation(idNegotiation);

        // Configuración inicial por defecto. El ID se inicializa automáticamente en 0
        // y será gestionado como autoincremental por PostgreSQL al insertar.
        contract.setStartDate(LocalDate.now());
        contract.setStatus("InVigor");
        contract.setDocument("Contrato_OVI_" + idNegotiation + ".pdf");

        model.addAttribute("contract", contract);
        return "admin/contract-create";
    }

    @PostMapping("/admin-create")
    public String processAdminCreateSubmit(@ModelAttribute("contract") Contract contract, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/contract-create";
        }

        // 1. Persistencia del contrato delegando el ID incremental a la base de datos
        contractDao.addContract(contract);

        // 2. Transición de estado de la negociación origen para archivarla como exitosa
        Negotiation negotiation = negotiationDao.getNegotiation(contract.getIdNegotiation());
        if (negotiation != null) {
            negotiation.setStatus("Accepted");
            negotiationDao.updateNegotiation(negotiation);
        }

        return "redirect:/contract/admin-dashboard";
    }

    @GetMapping("/admin-edit/{id}")
    public String showAdminEditForm(@PathVariable int id, Model model) {
        Contract contract = contractDao.getContract(id);
        if (contract == null) {
            return "redirect:/contract/admin-dashboard";
        }
        model.addAttribute("contract", contract);
        return "admin/contract-edit";
    }

    @PostMapping("/admin-edit")
    public String processAdminEditSubmit(@ModelAttribute("contract") Contract contract, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/contract-edit";
        }

        contractDao.updateContract(contract);
        return "redirect:/contract/admin-dashboard";
    }
}