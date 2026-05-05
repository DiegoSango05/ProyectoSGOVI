package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.model.PAPAssistant;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PAPAssistantValidator implements Validator {
    @Override
    public boolean supports(Class<?> cls) {
        return PAPAssistant.class.equals(cls);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        PAPAssistant assistant = (PAPAssistant) obj;

        if (assistant.getDni().trim().isEmpty()) {
            errors.rejectValue("dni", "obligatorio", "El DNI es obligatorio");
        }

        if (assistant.getName().trim().isEmpty()) {
            errors.rejectValue("name", "obligatorio", "El nombre es obligatorio");
        }

        if (assistant.getLocation().trim().isEmpty()) {
            errors.rejectValue("location", "obligatorio", "La ubicación es obligatoria");
        }

        // Validación de disponibilidad (importante para los contratos)
        if (assistant.getAvailability().trim().isEmpty()) {
            errors.rejectValue("availability", "obligatorio", "Debes indicar tu horario de disponibilidad");
        }

        // Validación de Password (solo si es nuevo o se está cambiando)
        if (assistant.getPassword() != null && assistant.getPassword().length() < 6) {
            errors.rejectValue("password", "corto", "La contraseña debe tener al menos 6 caracteres");
        }
    }
}
