package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.InstructorDao;
import es.uji.ei1027.sps.model.Instructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/instructor")
public class InstructorController {

    private InstructorDao instructorDao;

    @Autowired
    public void setInstructorDao(InstructorDao instructorDao) {
        this.instructorDao = instructorDao;
    }

    // LISTAR
    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("instructors", instructorDao.getInstructors());
        return "instructor/list";
    }

    // AÑADIR (Formulario)
    @RequestMapping("/add")
    public String addInstructor(Model model) {
        model.addAttribute("instructor", new Instructor());
        return "instructor/add";
    }

    // AÑADIR (Procesar)
    @RequestMapping(value="/add", method= RequestMethod.POST)
    public String processAddSubmit(@ModelAttribute("instructor") Instructor instructor,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return "instructor/add";

        instructorDao.addInstructor(instructor);
        return "redirect:list";
    }
}
