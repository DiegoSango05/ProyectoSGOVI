package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceRequestDao;
import es.uji.ei1027.sps.dao.ContractDao;
import es.uji.ei1027.sps.dao.NegotiationDao;
import es.uji.ei1027.sps.dao.OVIUserDao;
import es.uji.ei1027.sps.dao.PAPAssistantDao;
import es.uji.ei1027.sps.model.AssistanceRequest;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/contract")
public class ContractController {

    private ContractDao contractDao;
    private NegotiationDao negotiationDao;
    private AssistanceRequestDao assistanceRequestDao;
    private OVIUserDao oviUserDao;
    private PAPAssistantDao papAssistantDao;
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
    public String myList(@RequestParam(value = "search", required = false, defaultValue = "") String search,
                         @RequestParam(value = "orderBy", required = false, defaultValue = "startDate") String orderBy,
                         @RequestParam(value = "dir", required = false, defaultValue = "desc") String dir,
                         @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                         HttpSession session, Model model) {
        OVIUser user = getLoggedOVIUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        List<Contract> contracts = contractDao.getContractsByOVIUser(user.getDni());
        if (contracts == null) {
            contracts = new ArrayList<Contract>();
        }

        String normalizedSearch = normalize(search);
        if (!normalizedSearch.isEmpty()) {
            contracts = contracts.stream()
                    .filter(contract -> containsIgnoreCase(contract.getStatus(), normalizedSearch)
                            || containsIgnoreCase(contract.getDocument(), normalizedSearch)
                            || containsIgnoreCase(String.valueOf(contract.getId()), normalizedSearch)
                            || containsIgnoreCase(String.valueOf(contract.getIdNegotiation()), normalizedSearch)
                            || containsIgnoreCase(contract.getStartDate() == null ? null : contract.getStartDate().toString(), normalizedSearch)
                            || containsIgnoreCase(contract.getEndDate() == null ? null : contract.getEndDate().toString(), normalizedSearch))
                    .collect(Collectors.toList());
        }

        Comparator<Contract> comparator = Comparator.comparing(Contract::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
        if ("endDate".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(Contract::getEndDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("status".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(contract -> safeLower(contract.getStatus()));
        } else if ("document".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(contract -> safeLower(contract.getDocument()));
        }
        if ("desc".equalsIgnoreCase(dir)) {
            comparator = comparator.reversed();
        }
        contracts.sort(comparator);

        PageResult<Contract> pageResult = paginate(contracts, page, 5);

        model.addAttribute("contracts", pageResult.items());
        model.addAttribute("search", search);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("dir", dir);
        model.addAttribute("currentPage", pageResult.currentPage());
        model.addAttribute("totalPages", pageResult.totalPages());
        model.addAttribute("totalRecords", pageResult.totalRecords());
        return "contract/ovi-list";
    }

    @RequestMapping("/assistant-list")
    public String assistantList(@RequestParam(value = "search", required = false, defaultValue = "") String search,
                                @RequestParam(value = "orderBy", required = false, defaultValue = "startDate") String orderBy,
                                @RequestParam(value = "dir", required = false, defaultValue = "desc") String dir,
                                @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                HttpSession session, Model model) {
        PAPAssistant assistant = getLoggedAssistant(session);
        if (assistant == null) {
            return "redirect:/login";
        }
        List<Contract> contracts = contractDao.getContractsByAssistant(assistant.getDni());
        if (contracts == null) {
            contracts = new ArrayList<Contract>();
        }

        String normalizedSearch = normalize(search);
        if (!normalizedSearch.isEmpty()) {
            contracts = contracts.stream()
                    .filter(contract -> containsIgnoreCase(contract.getStatus(), normalizedSearch)
                            || containsIgnoreCase(contract.getDocument(), normalizedSearch)
                            || containsIgnoreCase(String.valueOf(contract.getId()), normalizedSearch)
                            || containsIgnoreCase(String.valueOf(contract.getIdNegotiation()), normalizedSearch)
                            || containsIgnoreCase(contract.getStartDate() == null ? null : contract.getStartDate().toString(), normalizedSearch)
                            || containsIgnoreCase(contract.getEndDate() == null ? null : contract.getEndDate().toString(), normalizedSearch))
                    .collect(Collectors.toList());
        }

        Comparator<Contract> comparator = Comparator.comparing(Contract::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
        if ("endDate".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(Contract::getEndDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("status".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(contract -> safeLower(contract.getStatus()));
        } else if ("document".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(contract -> safeLower(contract.getDocument()));
        }
        if ("desc".equalsIgnoreCase(dir)) {
            comparator = comparator.reversed();
        }
        contracts.sort(comparator);

        PageResult<Contract> pageResult = paginate(contracts, page, 5);

        model.addAttribute("contracts", pageResult.items());
        model.addAttribute("search", search);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("dir", dir);
        model.addAttribute("currentPage", pageResult.currentPage());
        model.addAttribute("totalPages", pageResult.totalPages());
        model.addAttribute("totalRecords", pageResult.totalRecords());
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
                                 @RequestParam(value = "search", required = false, defaultValue = "") String search,
                                 @RequestParam(value = "orderBy", required = false, defaultValue = "ovi") String orderBy,
                                 @RequestParam(value = "dir", required = false, defaultValue = "asc") String dir,
                                 @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                 HttpSession session, Model model) {

        Object role = session.getAttribute("role");
        Object user = session.getAttribute("user");

        if (!"admin".equals(role) || user == null) {
            return "redirect:/login";
        }

        List<Negotiation> mutualAgreements = negotiationDao.getMutualAgreements();
        addParticipantDataToNegotiations(mutualAgreements);
        mutualAgreements = filterNegotiations(mutualAgreements, search);
        sortNegotiations(mutualAgreements, orderBy, dir);
        PageResult<Negotiation> agreementPageResult = paginate(mutualAgreements, page, 3);

        List<Contract> filteredContracts = new ArrayList<Contract>();

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
        }

        addParticipantDataToContracts(filteredContracts);
        filteredContracts = filterContracts(filteredContracts, search);
        sortContracts(filteredContracts, orderBy, dir);
        PageResult<Contract> contractPageResult = paginate(filteredContracts, page, 3);

        if ("Pending".equalsIgnoreCase(status)) {
            model.addAttribute("mutualAgreements", agreementPageResult.items());
            model.addAttribute("currentPage", agreementPageResult.currentPage());
            model.addAttribute("totalPages", agreementPageResult.totalPages());
            model.addAttribute("totalRecords", agreementPageResult.totalRecords());
        } else {
            model.addAttribute("mutualAgreements", new ArrayList<Negotiation>());
            model.addAttribute("currentPage", contractPageResult.currentPage());
            model.addAttribute("totalPages", contractPageResult.totalPages());
            model.addAttribute("totalRecords", contractPageResult.totalRecords());
        }
        model.addAttribute("contracts", contractPageResult.items());
        model.addAttribute("currentStatus", status);
        model.addAttribute("search", search);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("dir", dir);

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

    private void addParticipantDataToContracts(List<Contract> contracts) {
        for (Contract contract : contracts) {
            Negotiation negotiation = negotiationDao.getNegotiation(contract.getIdNegotiation());
            if (negotiation == null) {
                continue;
            }
            addParticipantDataToContract(contract, negotiation);
        }
    }

    private void addParticipantDataToContract(Contract contract, Negotiation negotiation) {
        AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(negotiation.getIdRequest());
        if (request != null) {
            contract.setDniOVIUser(request.getDniOVIuser());
            OVIUser oviUser = oviUserDao.getOVIUser(request.getDniOVIuser());
            contract.setNameOVIUser(oviUser == null ? request.getDniOVIuser() : oviUser.getName());
        }

        contract.setDniAssistant(negotiation.getDniAssistant());
        PAPAssistant assistant = papAssistantDao.getPAPAssistant(negotiation.getDniAssistant());
        contract.setNameAssistant(assistant == null ? negotiation.getDniAssistant() : assistant.getName());
    }

    private void addParticipantDataToNegotiations(List<Negotiation> negotiations) {
        for (Negotiation negotiation : negotiations) {
            AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(negotiation.getIdRequest());
            if (request != null) {
                negotiation.setDniOVIUser(request.getDniOVIuser());
                OVIUser oviUser = oviUserDao.getOVIUser(request.getDniOVIuser());
                negotiation.setNameOVIUser(oviUser == null ? request.getDniOVIuser() : oviUser.getName());
            }

            PAPAssistant assistant = papAssistantDao.getPAPAssistant(negotiation.getDniAssistant());
            negotiation.setNameAssistant(assistant == null ? negotiation.getDniAssistant() : assistant.getName());
        }
    }

    private List<Contract> filterContracts(List<Contract> contracts, String search) {
        String normalizedSearch = normalize(search);
        if (normalizedSearch.isEmpty()) {
            return contracts;
        }

        return contracts.stream()
                .filter(c -> containsIgnoreCase(c.getNameOVIUser(), normalizedSearch)
                        || containsIgnoreCase(c.getDniOVIUser(), normalizedSearch)
                        || containsIgnoreCase(c.getNameAssistant(), normalizedSearch)
                        || containsIgnoreCase(c.getDniAssistant(), normalizedSearch)
                        || containsIgnoreCase(c.getDocument(), normalizedSearch))
                .collect(Collectors.toList());
    }

    private List<Negotiation> filterNegotiations(List<Negotiation> negotiations, String search) {
        String normalizedSearch = normalize(search);
        if (normalizedSearch.isEmpty()) {
            return negotiations;
        }

        return negotiations.stream()
                .filter(n -> containsIgnoreCase(n.getNameOVIUser(), normalizedSearch)
                        || containsIgnoreCase(n.getDniOVIUser(), normalizedSearch)
                        || containsIgnoreCase(n.getNameAssistant(), normalizedSearch)
                        || containsIgnoreCase(n.getDniAssistant(), normalizedSearch))
                .collect(Collectors.toList());
    }

    private void sortContracts(List<Contract> contracts, String orderBy, String dir) {
        Comparator<Contract> comparator = Comparator.comparing(
                c -> safeLower(c.getNameOVIUser()));
        if ("oviDni".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(c -> safeLower(c.getDniOVIUser()));
        } else if ("assistant".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(c -> safeLower(c.getNameAssistant()));
        } else if ("assistantDni".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(c -> safeLower(c.getDniAssistant()));
        } else if ("date".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(Contract::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if ("desc".equalsIgnoreCase(dir)) {
            comparator = comparator.reversed();
        }
        contracts.sort(comparator);
    }

    private void sortNegotiations(List<Negotiation> negotiations, String orderBy, String dir) {
        Comparator<Negotiation> comparator = Comparator.comparing(
                n -> safeLower(n.getNameOVIUser()));
        if ("oviDni".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(n -> safeLower(n.getDniOVIUser()));
        } else if ("assistant".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(n -> safeLower(n.getNameAssistant()));
        } else if ("assistantDni".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(n -> safeLower(n.getDniAssistant()));
        } else if ("date".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(Negotiation::getNegotiationDate, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if ("desc".equalsIgnoreCase(dir)) {
            comparator = comparator.reversed();
        }
        negotiations.sort(comparator);
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
}
