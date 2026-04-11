package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.model.OVIUser;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class OVIUserValidator implements Validator {
    @Override
    public boolean supports(Class<?> cls) {
        return OVIUser.class.equals(cls);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        OVIUser oviUser = (OVIUser) obj;

        // Validación DNI
        if (oviUser.getDni().trim().isEmpty()) {
            errors.rejectValue("dni", "obligatorio", "Es necesario introducir un DNI");
        }

        // Validación Nombre
        if (oviUser.getName().trim().isEmpty()) {
            errors.rejectValue("name", "obligatorio", "El nombre es obligatorio");

        }

        // Validación Email (que no esté vacío y tenga formato básico)
        if (oviUser.getEmail().trim().isEmpty()) {
            errors.rejectValue("email", "obligatorio", "El correo electrónico es obligatorio");
        } else if (!oviUser.getEmail().contains("@") || !oviUser.getEmail().contains(".")) {
            errors.rejectValue("email", "formato", "El formato del correo no es válido");
        }

        // Validación Teléfono (mínimo 9 dígitos)
        if (oviUser.getPhoneNumber().trim().length() < 9) {
            errors.rejectValue("phoneNumber", "corto", "El teléfono debe tener al menos 9 dígitos");
        }

        // Validación Contacto de Emergencia
        if (oviUser.getEmergencyContact().trim().isEmpty()) {
            errors.rejectValue("emergencyContact", "obligatorio", "Debe indicar un número de emergencia");
        }
    }
}
