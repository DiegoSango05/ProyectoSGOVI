package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceRequestDao;
import es.uji.ei1027.sps.dao.CommunicationDao;
import es.uji.ei1027.sps.dao.NegotiationDao;
import es.uji.ei1027.sps.dao.OVIUserDao;
import es.uji.ei1027.sps.dao.PAPAssistantDao;
import es.uji.ei1027.sps.dao.SelectionDao;
import es.uji.ei1027.sps.dao.SupportChatDao;
import es.uji.ei1027.sps.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AssistanceRequestDao assistanceRequestDao;

    @Autowired
    private PAPAssistantDao papAssistantDao;

    @Autowired
    private OVIUserDao oviUserDao;

    @Autowired
    private SelectionDao selectionDao;

    @Autowired
    private SupportChatDao supportChatDao;

    @Autowired
    private CommunicationDao communicationDao;

    @Autowired
    private NegotiationDao negotiationDao;

    @RequestMapping("/index")
    public String indexAdmin(HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }
        model.addAttribute("adminName", user.getName());
        return "admin/index";
    }

    @GetMapping("/chats")
    public String chats(HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        return "admin/chats";
    }

    @GetMapping("/chats/all")
    public String allChats(@RequestParam(required = false) String chat,
                           @RequestParam(required = false) String q,
                           HttpSession session,
                           Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<AdminChatSummary> chats = filterChatsByParticipant(getAllChatSummaries(), q);
        AdminChatSummary selectedChat = getSelectedChat(chat, chats);

        model.addAttribute("chats", chats);
        model.addAttribute("searchQuery", q == null ? "" : q.trim());
        model.addAttribute("selectedChat", selectedChat);
        model.addAttribute("messages", selectedChat == null
                ? List.of()
                : getMessagesForAdminChat(selectedChat.getKey()));
        return "admin/all-chats";
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
    public String listRequests(@RequestParam(required = false, defaultValue = "Pending") String status,
                               @RequestParam(value = "search", required = false, defaultValue = "") String search,
                               @RequestParam(value = "orderBy", required = false, defaultValue = "name") String orderBy,
                               @RequestParam(value = "dir", required = false, defaultValue = "asc") String dir,
                               @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                               HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }

        List<AssistanceRequest> solicitudesCompleta = assistanceRequestDao.getAssistanceRequests();
        if (solicitudesCompleta == null) {
            solicitudesCompleta = new ArrayList<AssistanceRequest>();
        }
        addOVIUserNamesToRequests(solicitudesCompleta);

        List<AssistanceRequest> solicitudesFiltradas = solicitudesCompleta.stream()
                .filter(s -> s.getStatus() != null && s.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());

        String searchLower = search.toLowerCase(Locale.ROOT).trim();
        if (!searchLower.isEmpty()) {
            solicitudesFiltradas = solicitudesFiltradas.stream()
                    .filter(s -> containsIgnoreCase(s.getDniOVIuser(), searchLower)
                            || containsIgnoreCase(s.getType(), searchLower)
                            || containsIgnoreCase(s.getLocation(), searchLower)
                            || containsIgnoreCase(s.getNameOVIuser(), searchLower))
                    .collect(Collectors.toList());
        }

        Comparator<AssistanceRequest> comparator = Comparator.comparing(
                s -> s.getNameOVIuser() != null ? s.getNameOVIuser().toLowerCase(Locale.ROOT) : "");
        if ("dni".equalsIgnoreCase(orderBy)) {
            comparator = Comparator.comparing(
                    s -> s.getDniOVIuser() != null ? s.getDniOVIuser().toLowerCase(Locale.ROOT) : "");
        }
        if ("desc".equalsIgnoreCase(dir)) {
            comparator = comparator.reversed();
        }
        solicitudesFiltradas.sort(comparator);

        PageResult<AssistanceRequest> pageResult = paginate(solicitudesFiltradas, page, 3);

        model.addAttribute("solicitudes", pageResult.items());
        model.addAttribute("search", search);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("dir", dir);
        model.addAttribute("currentPage", pageResult.currentPage());
        model.addAttribute("totalPages", pageResult.totalPages());
        model.addAttribute("totalRecords", pageResult.totalRecords());
        model.addAttribute("nextDir", "asc".equalsIgnoreCase(dir) ? "desc" : "asc");
        model.addAttribute("currentStatus", status); // Guardamos cuál está activa para pintarla en el HTML

        return "admin/requests-list";
    }

    @PostMapping("/reject/{id}")
    public String rejectRequest(@PathVariable int id) {
        AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(id);
        if (request != null) {
            request.setStatus("Rejected");
            assistanceRequestDao.updateAssistanceRequest(request);
        }
        return "redirect:/admin/requests?status=Rejected";
    }

    // Listar todos los asistentes para que el admin los gestione (Añadida ordenación alfabética)
    @GetMapping("/assistants")
    public String listAssistants(@RequestParam(required = false, defaultValue = "Pending") String status,
                                 @RequestParam(value = "search", required = false, defaultValue = "") String search,
                                 @RequestParam(value = "orderBy", required = false, defaultValue = "name") String orderBy,
                                 @RequestParam(value = "dir", required = false, defaultValue = "asc") String dir,
                                 @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                 HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }

        List<PAPAssistant> todosLosAsistentes = papAssistantDao.getPAPAssistants();
        if (todosLosAsistentes == null) {
            todosLosAsistentes = new ArrayList<PAPAssistant>();
        }

        // Filtramos y ordenamos la lista alfabéticamente por nombre
        List<PAPAssistant> asistentesFiltrados = todosLosAsistentes.stream()
                .filter(a -> a.getStatus() != null && a.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());

        String assistantSearchLower = search.toLowerCase(Locale.ROOT).trim();
        if (!assistantSearchLower.isEmpty()) {
            asistentesFiltrados = asistentesFiltrados.stream()
                    .filter(a -> containsIgnoreCase(a.getDni(), assistantSearchLower)
                            || containsIgnoreCase(a.getName(), assistantSearchLower)
                            || containsIgnoreCase(a.getAssistanceType(), assistantSearchLower)
                            || containsIgnoreCase(a.getLocation(), assistantSearchLower))
                    .collect(Collectors.toList());
        }

        Comparator<PAPAssistant> assistantComparator = Comparator.comparing(
                a -> a.getName() != null ? a.getName().toLowerCase(Locale.ROOT) : "");
        if ("dni".equalsIgnoreCase(orderBy)) {
            assistantComparator = Comparator.comparing(
                    a -> a.getDni() != null ? a.getDni().toLowerCase(Locale.ROOT) : "");
        }
        if ("desc".equalsIgnoreCase(dir)) {
            assistantComparator = assistantComparator.reversed();
        }
        asistentesFiltrados.sort(assistantComparator);

        PageResult<PAPAssistant> assistantPageResult = paginate(asistentesFiltrados, page, 3);

        model.addAttribute("assistants", assistantPageResult.items());
        model.addAttribute("search", search);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("dir", dir);
        model.addAttribute("currentPage", assistantPageResult.currentPage());
        model.addAttribute("totalPages", assistantPageResult.totalPages());
        model.addAttribute("totalRecords", assistantPageResult.totalRecords());
        model.addAttribute("nextDir", "asc".equalsIgnoreCase(dir) ? "desc" : "asc");
        model.addAttribute("currentStatus", status); // Para marcar la pestaña activa
        return "admin/assistants-list";
    }

    // Aprobar a un asistente
    @PostMapping("/assistants/approve/{dni}")
    public String approveAssistant(@PathVariable String dni) {
        PAPAssistant assistant = papAssistantDao.getPAPAssistant(dni);
        if (assistant != null) {
            assistant.setStatus("Accepted");
            papAssistantDao.updatePAPAssistant(assistant);
            supportChatDao.createWelcomeChatIfAbsent(assistant.getDni(), "PAP", assistant.getName());
        }
        return "redirect:/admin/assistants?status=Accepted";
    }

    // Rechazar a un asistente
    @PostMapping("/assistants/reject/{dni}")
    public String rejectAssistant(@PathVariable String dni, @RequestParam("rejectionReason") String rejectionReason) {
        PAPAssistant assistant = papAssistantDao.getPAPAssistant(dni);
        if (assistant != null) {
            assistant.setStatus("Rejected");
            assistant.setRejectionReason(rejectionReason);
            papAssistantDao.updatePAPAssistant(assistant);
        }
        return "redirect:/admin/assistants?status=Rejected";
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

    // Listado de Usuarios OVI (Añadida ordenación alfabética)
    @GetMapping("/ovi-users")
    public String listOVIUsers(@RequestParam(required = false, defaultValue = "Pending") String status,
                               @RequestParam(value = "search", required = false, defaultValue = "") String search,
                               @RequestParam(value = "orderBy", required = false, defaultValue = "name") String orderBy,
                               @RequestParam(value = "dir", required = false, defaultValue = "asc") String dir,
                               @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                               HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) return "redirect:/login";

        List<OVIUser> todosLosUsuarios = oviUserDao.getOVIUsers();
        if (todosLosUsuarios == null) {
            todosLosUsuarios = new ArrayList<OVIUser>();
        }

        // Filtramos y ordenamos la lista alfabéticamente por nombre
        List<OVIUser> usuariosFiltrados = todosLosUsuarios.stream()
                .filter(u -> u.getStatus() != null && u.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());

        String userSearchLower = search.toLowerCase(Locale.ROOT).trim();
        if (!userSearchLower.isEmpty()) {
            usuariosFiltrados = usuariosFiltrados.stream()
                    .filter(u -> containsIgnoreCase(u.getDni(), userSearchLower)
                            || containsIgnoreCase(u.getName(), userSearchLower)
                            || containsIgnoreCase(u.getAddress(), userSearchLower))
                    .collect(Collectors.toList());
        }

        Comparator<OVIUser> userComparator = Comparator.comparing(
                u -> u.getName() != null ? u.getName().toLowerCase(Locale.ROOT) : "");
        if ("dni".equalsIgnoreCase(orderBy)) {
            userComparator = Comparator.comparing(
                    u -> u.getDni() != null ? u.getDni().toLowerCase(Locale.ROOT) : "");
        }
        if ("desc".equalsIgnoreCase(dir)) {
            userComparator = userComparator.reversed();
        }
        usuariosFiltrados.sort(userComparator);

        PageResult<OVIUser> userPageResult = paginate(usuariosFiltrados, page, 3);

        model.addAttribute("users", userPageResult.items());
        model.addAttribute("currentStatus", status);
        model.addAttribute("search", search);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("dir", dir);
        model.addAttribute("currentPage", userPageResult.currentPage());
        model.addAttribute("totalPages", userPageResult.totalPages());
        model.addAttribute("totalRecords", userPageResult.totalRecords());
        model.addAttribute("nextDir", "asc".equalsIgnoreCase(dir) ? "desc" : "asc");
        return "admin/ovi-users-list";
    }

    // Ficha de detalle del Usuario OVI
    @GetMapping("/ovi-users/details/{dni}")
    public String oviUserDetails(@PathVariable String dni, HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) return "redirect:/login";

        OVIUser oviUser = oviUserDao.getOVIUser(dni);
        if (oviUser != null) {
            model.addAttribute("usuario", oviUser);
        }
        return "admin/ovi-user-details";
    }

    // Aprobar Usuario OVI
    @PostMapping("/ovi-users/approve/{dni}")
    public String approveOVIUser(@PathVariable String dni) {
        OVIUser oviUser = oviUserDao.getOVIUser(dni);
        if (oviUser != null) {
            oviUser.setStatus("Accepted");
            oviUserDao.updateOVIUser(oviUser);
            supportChatDao.createWelcomeChatIfAbsent(oviUser.getDni(), "OVI", oviUser.getName());
        }
        return "redirect:/admin/ovi-users?status=Accepted";
    }

    @PostMapping("/ovi-users/reject/{dni}")
    public String rejectOVIUser(@PathVariable String dni,
                                @RequestParam("rejectionReason") String rejectionReason) {

        OVIUser oviUser = oviUserDao.getOVIUser(dni);
        if (oviUser != null) {
            oviUser.setStatus("Rejected");
            oviUser.setRejectionReason(rejectionReason);

            oviUserDao.updateOVIUser(oviUser);
        }
        return "redirect:/admin/ovi-users?status=Rejected";
    }

    @PostMapping("/requests/approve/{id}")
    public String approveRequest(@PathVariable int id,
                                 @RequestParam(value = "selectedAssistants", required = false) List<String> selectedAssistants) {

        if (selectedAssistants == null || selectedAssistants.isEmpty()) {
            return "redirect:/admin/request-details/" + id + "?error=nocandidates";
        }

        assistanceRequestDao.updateStatus(id, "Accepted");

        selectionDao.deleteSelectionsByRequest(id);

        for (String dni : selectedAssistants) {
            Selection selection = new Selection();
            selection.setIdRequest(id);
            selection.setDniAssistant(dni);
            selectionDao.addSelection(selection);
        }

        return "redirect:/admin/requests?status=Accepted";
    }

    @GetMapping("/activities")
    public String listActivitiesAdmin(HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }
        return "redirect:/activity/list";
    }

    @GetMapping("/request-user-details/{dni}")
    public String requestUserDetails(@PathVariable String dni,
                                     @RequestParam Integer requestId,
                                     HttpSession session, Model model) {


        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) return "redirect:/login";

        OVIUser oviUser = oviUserDao.getOVIUser(dni);
        if (oviUser != null) {
            model.addAttribute("usuario", oviUser);
            model.addAttribute("requestId", requestId); // Pasamos el ID para el botón de volver
        }

        return "admin/request-user-details";
    }

    @GetMapping("/manage-profiles")
    public String manageProfiles(@RequestParam(required = false, defaultValue = "usuario") String type,
                                 @RequestParam(value = "search", required = false, defaultValue = "") String search,
                                 @RequestParam(value = "orderBy", required = false, defaultValue = "name") String orderBy,
                                 @RequestParam(value = "dir", required = false, defaultValue = "asc") String dir,
                                 @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                 HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }

        if (type.equalsIgnoreCase("usuario")) {
            List<OVIUser> usuarios = oviUserDao.getOVIUsers();
            if (usuarios == null) {
                usuarios = new ArrayList<OVIUser>();
            }

            String userSearchLower = search.toLowerCase(Locale.ROOT).trim();
            if (!userSearchLower.isEmpty()) {
                usuarios = usuarios.stream()
                        .filter(u -> containsIgnoreCase(u.getDni(), userSearchLower)
                                || containsIgnoreCase(u.getName(), userSearchLower)
                                || containsIgnoreCase(u.getPhoneNumber(), userSearchLower)
                                || containsIgnoreCase(u.getEmail(), userSearchLower)
                                || containsIgnoreCase(u.getAddress(), userSearchLower)
                                || containsIgnoreCase(u.getEmergencyContact(), userSearchLower)
                                || containsIgnoreCase(u.getStatus(), userSearchLower))
                        .collect(Collectors.toList());
            }

            Comparator<OVIUser> userComparator = Comparator.comparing(
                    u -> u.getName() != null ? u.getName().toLowerCase(Locale.ROOT) : "");
            if ("dni".equalsIgnoreCase(orderBy)) {
                userComparator = Comparator.comparing(
                        u -> u.getDni() != null ? u.getDni().toLowerCase(Locale.ROOT) : "");
            } else if ("phone".equalsIgnoreCase(orderBy)) {
                userComparator = Comparator.comparing(
                        u -> u.getPhoneNumber() != null ? u.getPhoneNumber().toLowerCase(Locale.ROOT) : "");
            } else if ("extra".equalsIgnoreCase(orderBy)) {
                userComparator = Comparator.comparing(
                        u -> u.getEmail() != null ? u.getEmail().toLowerCase(Locale.ROOT) : "");
            }
            if ("desc".equalsIgnoreCase(dir)) {
                userComparator = userComparator.reversed();
            }
            usuarios.sort(userComparator);

            PageResult<OVIUser> userPageResult = paginate(usuarios, page, 3);
            model.addAttribute("perfiles", userPageResult.items());
            model.addAttribute("currentPage", userPageResult.currentPage());
            model.addAttribute("totalPages", userPageResult.totalPages());
            model.addAttribute("totalRecords", userPageResult.totalRecords());
        } else {
            List<PAPAssistant> asistentes = papAssistantDao.getPAPAssistants();
            if (asistentes == null) {
                asistentes = new ArrayList<PAPAssistant>();
            }

            String assistantSearchLower = search.toLowerCase(Locale.ROOT).trim();
            if (!assistantSearchLower.isEmpty()) {
                asistentes = asistentes.stream()
                        .filter(a -> containsIgnoreCase(a.getDni(), assistantSearchLower)
                                || containsIgnoreCase(a.getName(), assistantSearchLower)
                                || containsIgnoreCase(a.getPhoneNumber(), assistantSearchLower)
                                || containsIgnoreCase(a.getAssistanceType(), assistantSearchLower)
                                || containsIgnoreCase(a.getLocation(), assistantSearchLower)
                                || containsIgnoreCase(a.getProfessionalTraining(), assistantSearchLower)
                                || containsIgnoreCase(a.getAvailability(), assistantSearchLower)
                                || containsIgnoreCase(a.getStatus(), assistantSearchLower))
                        .collect(Collectors.toList());
            }

            Comparator<PAPAssistant> assistantComparator = Comparator.comparing(
                    a -> a.getName() != null ? a.getName().toLowerCase(Locale.ROOT) : "");
            if ("dni".equalsIgnoreCase(orderBy)) {
                assistantComparator = Comparator.comparing(
                        a -> a.getDni() != null ? a.getDni().toLowerCase(Locale.ROOT) : "");
            } else if ("phone".equalsIgnoreCase(orderBy)) {
                assistantComparator = Comparator.comparing(
                        a -> a.getPhoneNumber() != null ? a.getPhoneNumber().toLowerCase(Locale.ROOT) : "");
            } else if ("extra".equalsIgnoreCase(orderBy)) {
                assistantComparator = Comparator.comparing(
                        a -> a.getAssistanceType() != null ? a.getAssistanceType().toLowerCase(Locale.ROOT) : "");
            } else if ("location".equalsIgnoreCase(orderBy)) {
                assistantComparator = Comparator.comparing(
                        a -> a.getLocation() != null ? a.getLocation().toLowerCase(Locale.ROOT) : "");
            }
            if ("desc".equalsIgnoreCase(dir)) {
                assistantComparator = assistantComparator.reversed();
            }
            asistentes.sort(assistantComparator);

            PageResult<PAPAssistant> assistantPageResult = paginate(asistentes, page, 3);
            model.addAttribute("perfiles", assistantPageResult.items());
            model.addAttribute("currentPage", assistantPageResult.currentPage());
            model.addAttribute("totalPages", assistantPageResult.totalPages());
            model.addAttribute("totalRecords", assistantPageResult.totalRecords());
        }

        model.addAttribute("currentType", type); // Guarda si estamos en "usuario" o "asistente"
        model.addAttribute("search", search);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("dir", dir);
        return "admin/manage-profiles";
    }

    @GetMapping("/profiles/view/{type}/{dni}")
    public String viewProfile(@PathVariable String type, @PathVariable String dni,
                              HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }

        String dniUpper = dni.toUpperCase();

        if (type.equalsIgnoreCase("usuario")) {
            OVIUser ovi = oviUserDao.getOVIUser(dniUpper);
            model.addAttribute("perfil", ovi);
        } else {
            PAPAssistant pap = papAssistantDao.getPAPAssistant(dniUpper);
            model.addAttribute("perfil", pap);
        }

        model.addAttribute("type", type);
        return "admin/view-profile";
    }

    // Formulario de edición (GET)
    @GetMapping("/profiles/edit/{type}/{dni}")
    public String editProfile(@PathVariable String type, @PathVariable String dni,
                              HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }

        String dniUpper = dni.toUpperCase();

        if (type.equalsIgnoreCase("usuario")) {
            OVIUser ovi = oviUserDao.getOVIUser(dniUpper);
            model.addAttribute("perfil", ovi);
        } else {
            PAPAssistant pap = papAssistantDao.getPAPAssistant(dniUpper);
            model.addAttribute("perfil", pap);
        }

        model.addAttribute("type", type);
        return "admin/edit-profile";
    }

    @PostMapping("/profiles/edit/usuario")
    public String processEditUserSubmit(@ModelAttribute("perfil") OVIUser oviForm,
                                        BindingResult bindingResult,
                                        HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) return "redirect:/login";

        OVIUser oviDb = oviUserDao.getOVIUser(oviForm.getDni());

        if (oviDb != null) {
            oviDb.setName(oviForm.getName());
            oviDb.setPhoneNumber(oviForm.getPhoneNumber());
            oviDb.setEmail(oviForm.getEmail());
            oviDb.setAddress(oviForm.getAddress());
            oviDb.setEmergencyContact(oviForm.getEmergencyContact());

            OVIUserValidator oviValidator = new OVIUserValidator();
            oviValidator.validate(oviDb, bindingResult);

            if (bindingResult.hasErrors()) {
                model.addAttribute("type", "usuario");
                return "admin/edit-profile";
            }

            oviUserDao.updateOVIUser(oviDb);
        }

        return "redirect:/admin/manage-profiles?type=usuario";
    }

    @PostMapping("/profiles/edit/asistente")
    public String processEditAssistantSubmit(@ModelAttribute("perfil") PAPAssistant papForm,
                                             BindingResult bindingResult,
                                             HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) return "redirect:/login";

        PAPAssistant papDb = papAssistantDao.getPAPAssistant(papForm.getDni());

        if (papDb != null) {
            papDb.setName(papForm.getName());
            papDb.setPhoneNumber(papForm.getPhoneNumber());
            papDb.setLocation(papForm.getLocation());
            papDb.setAssistanceType(papForm.getAssistanceType());
            papDb.setProfessionalTraining(papForm.getProfessionalTraining());

            PAPAssistantValidator papValidator = new PAPAssistantValidator();
            papValidator.validate(papDb, bindingResult);

            if (bindingResult.hasErrors()) {
                model.addAttribute("type", "asistente");
                return "admin/edit-profile";
            }

            papAssistantDao.updatePAPAssistant(papDb);
        }

        return "redirect:/admin/manage-profiles?type=asistente";
    }

    // Formulario de creación (GET)
    @GetMapping("/profiles/create/{type}")
    public String createProfile(@PathVariable String type, HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }

        if (type.equalsIgnoreCase("usuario")) {
            OVIUser nuevoUsuario = new OVIUser();
            nuevoUsuario.setStatus("Accepted");
            model.addAttribute("perfil", nuevoUsuario);
            model.addAttribute("type", type);
            return "admin/create-user-profile"; // Devuelve la plantilla del usuario
        } else {
            PAPAssistant nuevoAsistente = new PAPAssistant();
            nuevoAsistente.setStatus("Accepted");
            model.addAttribute("perfil", nuevoAsistente);
            model.addAttribute("type", type);
            return "admin/create-assistant-profile"; // Devuelve la plantilla del asistente
        }
    }

    // Procesar la creación de un USUARIO OVI (POST)
    @PostMapping("/profiles/create/usuario")
    public String processCreateUserSubmit(@ModelAttribute("perfil") OVIUser oviForm,
                                          BindingResult bindingResult,
                                          HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) return "redirect:/login";

        // Ejecutamos tu validador estricto de usuario
        OVIUserValidator oviValidator = new OVIUserValidator();
        oviValidator.validate(oviForm, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("type", "usuario");
            return "admin/create-user-profile"; // Si hay fallos de DNI, teléfono, etc., recarga la vista con errores
        }

        // Si pasa tu validador, lo insertamos en la BD usando tu DAO
        oviUserDao.addOVIUser(oviForm);
        return "redirect:/admin/manage-profiles?type=usuario";
    }

    // Procesar la creación de un ASISTENTE PAP (POST)
    @PostMapping("/profiles/create/asistente")
    public String processCreateAssistantSubmit(@ModelAttribute("perfil") PAPAssistant papForm,
                                               BindingResult bindingResult,
                                               HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) return "redirect:/login";

        // Ejecutamos tu validador estricto de asistente
        PAPAssistantValidator papValidator = new PAPAssistantValidator();
        papValidator.validate(papForm, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("type", "asistente");
            return "admin/create-assistant-profile"; // Recarga la vista inyectando las alertas de error
        }

        // Si pasa tu validador, lo insertamos en la BD usando tu DAO
        papAssistantDao.addPAPAssistant(papForm);
        return "redirect:/admin/manage-profiles?type=asistente";
    }

    private void addOVIUserNamesToRequests(List<AssistanceRequest> requests) {
        for (AssistanceRequest request : requests) {
            if (request.getDniOVIuser() != null) {
                OVIUser oviUser = oviUserDao.getOVIUser(request.getDniOVIuser());
                request.setNameOVIuser(oviUser == null ? request.getDniOVIuser() : oviUser.getName());
            }
        }
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

    private record PageResult<T>(List<T> items, int currentPage, int totalPages, int totalRecords) {
    }

    private boolean isAdmin(HttpSession session) {
        Object user = session.getAttribute("user");
        Object role = session.getAttribute("role");
        return user instanceof SystemUser && "admin".equals(role);
    }

    private List<AdminChatSummary> getAllChatSummaries() {
        List<AdminChatSummary> summaries = new ArrayList<AdminChatSummary>();

        for (SupportChat chat : supportChatDao.getSupportChats()) {
            List<SupportMessage> messages = supportChatDao.getMessagesByChat(chat.getId());
            SupportMessage lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);

            AdminChatSummary summary = new AdminChatSummary();
            summary.setKey("support-" + chat.getId());
            summary.setType("Soporte");
            summary.setTitle("Administrador - " + chat.getParticipantName());
            summary.setSubtitle(chat.getParticipantType() + " - " + chat.getParticipantDni());
            summary.setMessageCount(messages.size());
            summary.setLastMessage(lastMessage == null ? "Sin mensajes todavia" : lastMessage.getMessage());
            summary.setLastMessageDate(lastMessage == null ? null : lastMessage.getMessageDate());
            summaries.add(summary);
        }

        for (Negotiation negotiation : negotiationDao.getNegotiations()) {
            List<Communication> messages = communicationDao.getCommunicationsByNegotiation(negotiation.getIdNegotiation());
            Communication lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);
            AssistanceRequest request = assistanceRequestDao.getAssistanceRequest(negotiation.getIdRequest());
            OVIUser oviUser = request == null ? null : oviUserDao.getOVIUser(request.getDniOVIuser());
            PAPAssistant assistant = papAssistantDao.getPAPAssistant(negotiation.getDniAssistant());

            AdminChatSummary summary = new AdminChatSummary();
            summary.setKey("negotiation-" + negotiation.getIdNegotiation());
            summary.setType("Negociacion");
            summary.setTitle(displayName(oviUser, request == null ? null : request.getDniOVIuser())
                    + " - " + displayName(assistant, negotiation.getDniAssistant()));
            summary.setSubtitle("Solicitud #" + negotiation.getIdRequest() + " - " + nullSafe(negotiation.getStatus()));
            summary.setMessageCount(messages.size());
            summary.setLastMessage(lastMessage == null ? "Sin mensajes todavia" : lastMessage.getMessage());
            summary.setLastMessageDate(lastMessage == null ? null : lastMessage.getMessageDate());
            summaries.add(summary);
        }

        summaries.sort(Comparator
                .comparing(AdminChatSummary::getLastMessageDate,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(AdminChatSummary::getTitle, String.CASE_INSENSITIVE_ORDER));
        return summaries;
    }

    private List<AdminChatSummary> filterChatsByParticipant(List<AdminChatSummary> chats, String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return chats;
        }

        String normalizedSearch = searchText.trim().toLowerCase(Locale.ROOT);
        return chats.stream()
                .filter(chat -> containsIgnoreCase(chat.getTitle(), normalizedSearch)
                        || containsIgnoreCase(chat.getSubtitle(), normalizedSearch))
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String value, String normalizedSearch) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedSearch);
    }

    private AdminChatSummary getSelectedChat(String requestedKey, List<AdminChatSummary> chats) {
        if (chats.isEmpty()) {
            return null;
        }
        if (requestedKey != null) {
            for (AdminChatSummary chat : chats) {
                if (requestedKey.equals(chat.getKey())) {
                    return chat;
                }
            }
        }
        return chats.get(0);
    }

    private List<AdminChatMessage> getMessagesForAdminChat(String key) {
        if (key == null) {
            return List.of();
        }

        List<AdminChatMessage> messages = new ArrayList<AdminChatMessage>();
        if (key.startsWith("support-")) {
            int idChat = Integer.parseInt(key.substring("support-".length()));
            for (SupportMessage message : supportChatDao.getMessagesByChat(idChat)) {
                messages.add(toAdminMessage(message.getSender(), message.getMessage(), message.getMessageDate()));
            }
        } else if (key.startsWith("negotiation-")) {
            int idNegotiation = Integer.parseInt(key.substring("negotiation-".length()));
            for (Communication communication : communicationDao.getCommunicationsByNegotiation(idNegotiation)) {
                messages.add(toAdminMessage(communication.getSender(), communication.getMessage(), communication.getMessageDate()));
            }
        }
        return messages;
    }

    private AdminChatMessage toAdminMessage(String sender, String message, java.time.LocalDateTime messageDate) {
        AdminChatMessage adminMessage = new AdminChatMessage();
        adminMessage.setSender(sender);
        adminMessage.setMessage(message);
        adminMessage.setMessageDate(messageDate);
        return adminMessage;
    }

    private String displayName(OVIUser user, String fallback) {
        return user == null ? nullSafe(fallback) : user.getName() + " (" + user.getDni() + ")";
    }

    private String displayName(PAPAssistant assistant, String fallback) {
        return assistant == null ? nullSafe(fallback) : assistant.getName() + " (" + assistant.getDni() + ")";
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "Sin dato" : value;
    }
}
