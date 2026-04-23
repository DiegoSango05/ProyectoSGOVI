package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.model.AssistanceList;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class AssistanceListValidator implements Validator {
    @Override
    public boolean supports(Class<?> cls) {
        return AssistanceList.class.equals(cls);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        AssistanceList list = (AssistanceList) obj;

        // 1. Validación de Fecha (No puede ser nula)
        if (list.getAssistanceDate() == null) {
            errors.rejectValue("assistanceDate", "obligatorio", "La fecha de asistencia es obligatoria");
        }

        // 2. Validación de Hora
        if (list.getAssistanceTime() == null) {
            errors.rejectValue("assistanceTime", "obligatorio", "La hora de asistencia es obligatoria");
        }

        // 3. ID de Actividad (Debe ser > 0)
        if (list.getIdActivity() <= 0) {
            errors.rejectValue("idActivity", "valorIncorrecte", "Debes introducir un ID de actividad válido");
        }

        // 4. DNI Usuario OVI
        if (list.getDniOVIUser() == null || list.getDniOVIUser().trim().isEmpty()) {
            errors.rejectValue("dniOVIUser", "obligatorio", "El DNI del usuario OVI es obligatorio");
        }

        // 5. DNI Asistente
        if (list.getDniAssistant() == null || list.getDniAssistant().trim().isEmpty()) {
            errors.rejectValue("dniAssistant", "obligatorio", "El DNI del asistente es obligatorio");
        }
    }
}
