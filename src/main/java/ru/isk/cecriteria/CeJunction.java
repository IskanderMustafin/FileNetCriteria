package ru.isk.cecriteria;

import java.util.ArrayList;
import java.util.List;

/**
 * @author imustafin
 */

/**
 * Последовательность логических операций связанных между собой оператором (OR, AND)
 */
public class CeJunction implements HasSqlStringFragment {

    private final List<HasSqlStringFragment> criterions = new ArrayList<HasSqlStringFragment>();

    /**
     * Оператор (OR, AND)
     */
    private final String op;

    CeJunction(String op) {
        this.op = op;
    }

    public CeJunction add(HasSqlStringFragment criterion) {
        criterions.add(criterion);
        return this;
    }

    public String getOp() {
        return op;
    }

    @Override
    public String toSqlString(String className) {
        // если пустой объект дизъюнкции/конъюнкции, то нужно что нибудь вернуть. Возвращаем самую "легкую" операцию
        if (criterions.isEmpty()) {
            return "1=1";
        }

        StringBuilder sql = new StringBuilder();
        sql.append('(');

        for(int i=0; i < criterions.size(); i++) {
           sql.append(criterions.get(i).toSqlString(className));

            if (i < criterions.size() - 1) {
                sql.append(' ')
                        .append(op)
                        .append(' ');
            }
        }
        sql.append(')');

        return sql.toString();
    }
}
