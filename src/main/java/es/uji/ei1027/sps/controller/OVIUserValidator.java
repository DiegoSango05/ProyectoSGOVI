package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.model.OVIUser;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import java.time.LocalDate;

public class
OVIUserValidator implements Validator {
    @Override
    public boolean supports(Class<?> cls) {
        return OVIUser.class.equals(cls);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        OVIUser oviUser = (OVIUser) obj;

        // 1. Validación DNI (Más estricta)
        String dni = oviUser.getDni().trim();
        if (dni.isEmpty()) {
            errors.rejectValue("dni", "obligatorio", "Es necesario introducir un DNI/NIE");
        } else if (!dni.matches("^[0-9]{8}[TRWAGMYFPDXBNJZSQVHLCKE]$|^[XYZ][0-9]{7}[TRWAGMYFPDXBNJZSQVHLCKE]$")) {
            // Esta Regex valida tanto DNI español como NIE extranjero
            errors.rejectValue("dni", "formato", "El formato del DNI/NIE no es válido");
        }

        // 2. Validación Nombre
        if (oviUser.getName().trim().isEmpty()) {
            errors.rejectValue("name", "obligatorio", "El nombre completo es obligatorio");
        } else if (oviUser.getName().length() > 100) {
            errors.rejectValue("name", "largo", "El nombre es demasiado largo (máximo 100 caracteres)");
        }

        // 3. Validación Fecha de Nacimiento
        if (oviUser.getBirthDate() == null) {
            errors.rejectValue("birthDate", "obligatorio", "La fecha de nacimiento es obligatoria");
        } else if (oviUser.getBirthDate().isAfter(LocalDate.now())) {
            errors.rejectValue("birthDate", "futura", "La fecha de nacimiento no puede ser una fecha futura");
        } else if (oviUser.getBirthDate().plusYears(18).isAfter(LocalDate.now())) {
            errors.rejectValue("birthDate", "menorEdad", "El usuario debe ser mayor de edad (18 años o más) para formar parte de la OVI");
        }

        // 4. Validación Email (Regex oficial)
        String email = oviUser.getEmail().trim();
        if (email.isEmpty()) {
            errors.rejectValue("email", "obligatorio", "El correo electrónico es obligatorio");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.rejectValue("email", "formato", "El formato del correo electrónico no es válido");
        }

        // 5. Validación Teléfono (9 dígitos exactos, solo números)
        // Quitamos espacios por si el usuario escribe "600 000 000"
        String phone = oviUser.getPhoneNumber().replace(" ", "");
        if (phone.isEmpty()) {
            errors.rejectValue("phoneNumber", "obligatorio", "Debe indicar un número de teléfono");
        } else if (phone.startsWith("+")) {
            errors.rejectValue("phoneNumber", "formato", "No incluya el prefijo internacional (+34), solo los 9 dígitos");
        } else if (!phone.matches("^[0-9]{9}$")) {
            errors.rejectValue("phoneNumber", "formato", "El teléfono debe tener exactamente 9 números (ej: 600123123)");
        }

        // 6. Validación Contacto de Emergencia
        String emergency = oviUser.getEmergencyContact().replace(" ", "");
        if (emergency.isEmpty()) {
            errors.rejectValue("emergencyContact", "obligatorio", "Debe indicar un número de emergencia");
        } else if (!emergency.matches("^[0-9]{9}$")) {
            errors.rejectValue("emergencyContact", "formato", "El número de emergencia debe tener 9 dígitos numéricos");
        } else if (emergency.equals(phone)) {
            errors.rejectValue("emergencyContact", "igual", "El contacto de emergencia no puede ser el mismo que tu teléfono personal");
        }

        // 7. Validación de Contraseña para OVIUser
        String password = oviUser.getPassword();
        if (password == null || password.trim().isEmpty()) {
            errors.rejectValue(
                    "password",
                    "obligatorio",
                    "La contraseña es obligatoria para el registro");
        }
        else if (password.length() < 6) {
            errors.rejectValue(
                    "password",
                    "corto",
                    "La contraseña debe tener al menos 6 caracteres");
        }

        String confirmPassword = oviUser.getConfirmPassword();
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            errors.rejectValue(
                    "confirmPassword",
                    "obligatorio",
                    "Debe confirmar la contraseña");
        }
        else if (password != null && !password.equals(confirmPassword)) {
            errors.rejectValue(
                    "confirmPassword",
                    "distinta",
                    "Las contraseñas no coinciden");
        }

        // 8. Consentimiento RGPD/LOPDGDD
        if (oviUser.getAcceptedPrivacyPolicy() == null ||
                !oviUser.getAcceptedPrivacyPolicy()) {

            errors.rejectValue(
                    "acceptedPrivacyPolicy",
                    "obligatorio",
                    "Debe aceptar la política de privacidad para continuar"
            );
        }
    }
}