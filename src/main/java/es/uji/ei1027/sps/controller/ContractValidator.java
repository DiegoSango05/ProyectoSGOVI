package es.uji.ei1027.sps.controller;

import es.uji.ei1027.sps.model.Contract;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ContractValidator implements Validator {

    @Override
    public boolean supports(Class<?> cls) {
        return Contract.class.equals(cls);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        Contract contract = (Contract) obj;

        // 1. Validación de la Fecha de Inicio (Obligatoria)
        if (contract.getStartDate() == null) {
            errors.rejectValue("startDate", "obligatorio", "La fecha de inicio de contrato es obligatoria");
        }

        // 2. Validación de la Fecha Límite (Obligatoria)
        if (contract.getEndDate() == null) {
            errors.rejectValue("endDate", "obligatorio", "La fecha límite o de finalización es obligatoria");
        }

        // 3. Validación cruzada de rangos temporales
        if (contract.getStartDate() != null && contract.getEndDate() != null) {
            if (contract.getEndDate().isBefore(contract.getStartDate())) {
                errors.rejectValue("endDate", "fechaAnterior", "La fecha de finalización no puede ser anterior a la de inicio");
            }
        }
    }
}