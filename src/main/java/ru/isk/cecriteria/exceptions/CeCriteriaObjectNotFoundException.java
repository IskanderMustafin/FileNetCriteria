package ru.isk.cecriteria.exceptions;

/**
 * @author imustafin
 *         Date: 06.02.13
 */
public class CeCriteriaObjectNotFoundException extends CeCriteriaException {

    public CeCriteriaObjectNotFoundException(String message) {
        super(message);
    }

    public CeCriteriaObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
