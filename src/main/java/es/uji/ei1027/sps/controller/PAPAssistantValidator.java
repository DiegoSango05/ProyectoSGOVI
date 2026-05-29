package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.model.PAPAssistant;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import java.time.LocalDate;
import java.time.Period;

public class PAPAssistantValidator implements Validator {
    @Override
    public boolean supports(Class<?> cls) {
        return PAPAssistant.class.equals(cls);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        PAPAssistant assistant = (PAPAssistant) obj;

        // 1. Validación DNI/NIE (Formato oficial)
        String dni = assistant.getDni().trim();
        if (dni.isEmpty()) {
            errors.rejectValue("dni", "obligatorio", "El DNI/NIE es obligatorio");
        } else if (!dni.matches("^[0-9]{8}[TRWAGMYFPDXBNJZSQVHLCKE]$|^[XYZ][0-9]{7}[TRWAGMYFPDXBNJZSQVHLCKE]$")) {
            errors.rejectValue("dni", "formato", "El formato del DNI/NIE no es válido");
        }

        // 2. Validación Nombre
        if (assistant.getName().trim().isEmpty()) {
            errors.rejectValue("name", "obligatorio", "El nombre completo es obligatorio");
        }

        // 3. Validación Fecha de Nacimiento
        if (assistant.getBirthDate() == null) {
            errors.rejectValue("birthDate", "obligatorio", "La fecha de nacimiento es obligatoria");
        } else {
            LocalDate today = LocalDate.now();
            if (assistant.getBirthDate().isAfter(today)) {
                errors.rejectValue("birthDate", "futura", "La fecha no puede ser futura");
            } else {
                int edad = Period.between(assistant.getBirthDate(), today).getYears();
                if (edad < 18) {
                    errors.rejectValue("birthDate", "menor", "El asistente debe ser mayor de edad (18 años)");
                }
            }
        }

        // 4. Validación Ubicación
        if (assistant.getLocation().trim().isEmpty()) {
            errors.rejectValue("location", "obligatorio", "La ubicación es obligatoria");
        }

        // 5. Validación Disponibilidad
        if (assistant.getAvailability().trim().isEmpty()) {
            errors.rejectValue("availability", "obligatorio", "Debes indicar tu horario de disponibilidad");
        }

        // 6. Validación Teléfono
        String phone = assistant.getPhoneNumber() != null ? assistant.getPhoneNumber().trim() : "";
        if (phone.isEmpty()) {
            errors.rejectValue("phoneNumber", "obligatorio", "El teléfono de contacto es obligatorio");
        } else if (!phone.matches("\\d{9}")) {
            errors.rejectValue("phoneNumber", "formato", "El teléfono debe tener exactamente 9 números");
        }

        // 7. Validación de Password
        if (assistant.getPassword() == null || assistant.getPassword().isEmpty()) {
            errors.rejectValue("password", "obligatorio", "La contraseña es obligatoria");
        } else if (assistant.getPassword().length() < 6) {
            errors.rejectValue("password", "corto", "La contraseña debe tener al menos 6 caracteres");
        }
    }
}