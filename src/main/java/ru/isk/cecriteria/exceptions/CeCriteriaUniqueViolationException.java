package ru.isk.cecriteria.exceptions;

/**
 * @author imustafin
 *
 */

/**
 * Ошибка, пробрасываемая при нарушении условия уникальности.
 */
public class CeCriteriaUniqueViolationException extends CeCriteriaException {

    public CeCriteriaUniqueViolationException(String message) {
        super(message);
    }

    public CeCriteriaUniqueViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
