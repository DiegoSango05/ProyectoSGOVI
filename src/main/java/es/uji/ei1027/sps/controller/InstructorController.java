package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.dao.InstructorDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/instructor")
public class InstructorController {

    private InstructorDao instructorDao;

    @Autowired
    public void setInstructorDao(InstructorDao instructorDao) {
        this.instructorDao = instructorDao;
    }

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("instructors", instructorDao.getInstructors());
        return "instructor/list";
    }
}
