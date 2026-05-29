package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.AssistanceRequestDao;
import es.uji.ei1027.sps.dao.OVIUserDao;
import es.uji.ei1027.sps.dao.PAPAssistantDao;
import es.uji.ei1027.sps.dao.SelectionDao;
import es.uji.ei1027.sps.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @RequestMapping("/index")
    public String indexAdmin(HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }
        model.addAttribute("adminName", user.getName());
        return "admin/index";
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
                               HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }

        List<AssistanceRequest> solicitudesCompleta = assistanceRequestDao.getAssistanceRequests();

        List<AssistanceRequest> solicitudesFiltradas = solicitudesCompleta.stream()
                .filter(s -> s.getStatus() != null && s.getStatus().equalsIgnoreCase(status))
                .toList();

        model.addAttribute("solicitudes", solicitudesFiltradas);
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
        return "redirect:/admin/requests";
    }

    // Listar todos los asistentes para que el admin los gestione (Añadida ordenación alfabética)
    @GetMapping("/assistants")
    public String listAssistants(@RequestParam(required = false, defaultValue = "Pending") String status,
                                 HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }

        List<PAPAssistant> todosLosAsistentes = papAssistantDao.getPAPAssistants();

        // Filtramos y ordenamos la lista alfabéticamente por nombre
        List<PAPAssistant> asistentesFiltrados = todosLosAsistentes.stream()
                .filter(a -> a.getStatus() != null && a.getStatus().equalsIgnoreCase(status))
                .sorted((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()))
                .toList();

        model.addAttribute("assistants", asistentesFiltrados);
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
        }
        return "redirect:/admin/assistants";
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
        return "redirect:/admin/assistants";
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
                               HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) return "redirect:/login";

        List<OVIUser> todosLosUsuarios = oviUserDao.getOVIUsers();

        // Filtramos y ordenamos la lista alfabéticamente por nombre
        List<OVIUser> usuariosFiltrados = todosLosUsuarios.stream()
                .filter(u -> u.getStatus() != null && u.getStatus().equalsIgnoreCase(status))
                .sorted((u1, u2) -> u1.getName().compareToIgnoreCase(u2.getName()))
                .toList();

        model.addAttribute("users", usuariosFiltrados);
        model.addAttribute("currentStatus", status);
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
        }
        return "redirect:/admin/ovi-users";
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
        return "redirect:/admin/ovi-users";
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

        return "redirect:/admin/requests";
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
                                 HttpSession session, Model model) {
        SystemUser user = (SystemUser) session.getAttribute("user");
        if (user == null || !session.getAttribute("role").equals("admin")) {
            return "redirect:/login";
        }

        if (type.equalsIgnoreCase("usuario")) {
            List<OVIUser> usuarios = oviUserDao.getOVIUsers();

            List<OVIUser> usuariosOrdenados = usuarios.stream()
                    .sorted((u1, u2) -> u1.getName().compareToIgnoreCase(u2.getName()))
                    .toList();

            model.addAttribute("perfiles", usuariosOrdenados);
        } else {
            List<PAPAssistant> asistentes = papAssistantDao.getPAPAssistants();

            List<PAPAssistant> asistentesOrdenados = asistentes.stream()
                    .sorted((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()))
                    .toList();

            model.addAttribute("perfiles", asistentesOrdenados);
        }

        model.addAttribute("currentType", type); // Guarda si estamos en "usuario" o "asistente"
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
            return "admin/create-profile"; // Si hay fallos de DNI, teléfono, etc., recarga la vista con errores
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
            return "admin/create-profile"; // Recarga la vista inyectando las alertas de error
        }

        // Si pasa tu validador, lo insertamos en la BD usando tu DAO
        papAssistantDao.addPAPAssistant(papForm);
        return "redirect:/admin/manage-profiles?type=asistente";
    }
}