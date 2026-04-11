package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.model.Activity;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ActivityValidator implements Validator {
    @Override
    public boolean supports(Class<?> cls) {
        return Activity.class.equals(cls);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        Activity activity = (Activity) obj;

        // Validación del Título (usando getTitle)
        if (activity.getTitle().trim().isEmpty()) {
            errors.rejectValue("title", "obligatorio", "El título de la actividad es obligatorio");
        }

        // Validación de Participantes Máximos (Número positivo)
        if (activity.getMaxParticipants() <= 0) {
            errors.rejectValue("maxParticipants", "valorIncorrecto", "El número máximo de participantes debe ser mayor que 0");
        }

        // Validación de la Ubicación
        if (activity.getLocation().trim().isEmpty()) {
            errors.rejectValue("location", "obligatorio", "Es necesario indicar un lugar para la actividad");
        }

        // Validación del DNI del Instructor
        if (activity.getDniInstructor().trim().isEmpty()) {
            errors.rejectValue("dniInstructor", "obligatorio", "El DNI del instructor es obligatorio");
        }
    }
}
