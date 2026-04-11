package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.model.Instructor;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class InstructorValidator implements Validator {
    @Override
    public boolean supports(Class<?> cls) {
        return Instructor.class.equals(cls);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        Instructor instructor = (Instructor) obj;

        // Validación del DNI
        if (instructor.getDni().trim().isEmpty()) {
            errors.rejectValue("dni", "obligatorio", "Es necesario introducir un DNI");
        }

        // Validación del Nombre
        if (instructor.getName().trim().isEmpty()) {
            errors.rejectValue("name", "obligatorio", "El nombre no puede estar vacío");
        } else if (instructor.getName().trim().length() < 3) {
            errors.rejectValue("name", "corto", "El nombre debe tener al menos 3 caracteres");
        }

        // Validación de la Especialización
        if (instructor.getSpecialization().trim().isEmpty()) {
            errors.rejectValue("specialization", "obligatorio", "Debes indicar una especialización");
        }
    }
}
