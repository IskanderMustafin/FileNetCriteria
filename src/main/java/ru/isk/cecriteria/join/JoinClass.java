package ru.isk.cecriteria.join;

import ru.isk.cecriteria.HasSqlStringFragment;

/**
 * @author imustafin
 */

/**
 * Имя класса который будет использован в опеаторе JOIN
 */
public class JoinClass implements HasSqlStringFragment {

    private final String joinClassName;
    private final String alias;


    /**
     * Создает объект описания класса который будет использован в операторе JOIN
     * @param joinClassName имя класса
     * @param alias алиас
     */
    public JoinClass(String joinClassName, String alias) {
        this.joinClassName = joinClassName;
        this.alias = alias;
    }

    /**
     * Создает объект описания класса который будет использован в операторе JOIN
     * @param joinClassName имя класса
     */
    public JoinClass(String joinClassName) {
        this.joinClassName = joinClassName;
        this.alias = null;
    }

    @Override
    public String toSqlString(String className) {
        return new StringBuilder()
                .append(joinClassName)
                .append(" ")
                .append(alias)
                .toString();
    }
}
