package es.uji.ei1027.sps.controller;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DataAccessResourceFailureException.class, SQLException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleDatabaseCrash(Exception ex, Model model) {
        model.addAttribute("status", "500");
        model.addAttribute("message", "Error crítico: No se ha podido conectar con la base de datos PostgreSQL. Verifica que el servidor de la universidad esté activo.");
        return "error/500";
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        ex.printStackTrace();

        model.addAttribute("status", "500");
        model.addAttribute("message", "Se ha producido un error interno inesperado en el servidor de SgOVI. Nuestro equipo técnico revisará el incidente.");
        return "error/error";
    }
}
