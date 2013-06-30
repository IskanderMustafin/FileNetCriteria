package ru.isk.cecriteria.join;

import ru.isk.cecriteria.HasSqlStringFragment;
import ru.isk.cecriteria.SimpleOperand;

/**
 * @author imustafin
 */

/**
 * Описание условия ON в операторе JOIN.
 */
public class JoinOnClause implements HasSqlStringFragment {

    private final String value1;
    private final SimpleOperand operand;
    private final String value2;

    /**
     * Создает объект описания условия ON в операторе JOIN.
     * @param value1  первое значение
     * @param operand операнд для сравнения
     * @param value2 второе значение
     */
    public JoinOnClause(String value1, SimpleOperand operand, String value2) {
        this.value1 = value1;
        this.operand = operand;
        this.value2 = value2;
    }

    @Override
    public String toSqlString(String className) {
        return new StringBuilder()
                .append(" ON ")
                .append(value1)
                .append(" ")
                .append(operand.stringValue())
                .append(" ")
                .append(value2)
                .toString();
    }
}
