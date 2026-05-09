package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.ContractDao;
import es.uji.ei1027.sps.model.Contract;
import es.uji.ei1027.sps.model.OVIUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/contract")
public class ContractController {

    private ContractDao contractDao;

    @Autowired
    public void setContractDao(ContractDao contractDao) {
        this.contractDao = contractDao;
    }

    // LISTAR
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

    // AÑADIR (Formulario)
    @RequestMapping("/add")
    public String addContract(Model model) {
        model.addAttribute("contract", new Contract());
        return "contract/add";
    }

    // AÑADIR (Procesar)
    @RequestMapping(value="/add", method= RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("contract") Contract contract,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "contract/add";

        contractDao.addContract(contract);
        return "redirect:list";
    }

    // ELIMINAR
    @RequestMapping(value="/delete/{id}")
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
}
