package ru.isk.cecriteria.exceptions;

/**
 * @author imustafin
 */

/**
 * Базовый класс исключений
 */
public class CeCriteriaException extends RuntimeException {

    public CeCriteriaException(String message) {
        super(message);
    }

    public CeCriteriaException(String message, Throwable cause) {
        super(message, cause);
    }
}
