package es.uji.ei1027.sps.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RegistrationController {
    // Ruta para mostrar la pantalla de elegir tipo de usuario
    @RequestMapping("/choose-registration")
    public String chooseRegistration() {
        return "choose-registration";
    }
}
