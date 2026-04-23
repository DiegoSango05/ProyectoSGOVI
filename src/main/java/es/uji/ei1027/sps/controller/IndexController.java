package es.uji.ei1027.sps.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/")
    public String index() {
        return "index-admin";
    }

    @RequestMapping("/profiles-management")
    public String profilesManagement() {
        return "profiles-management";
    }
}
