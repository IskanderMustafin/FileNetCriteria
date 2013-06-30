package ru.isk.cecriteria.join;

import ru.isk.cecriteria.HasSqlStringFragment;

/**
 * @author imustafin
 */


/**
 * Описание оператора JOIN
 */
public class JoinOperator implements HasSqlStringFragment {


    private final JoinType joinType;
    private final JoinClass joinClass;
    private final JoinOnClause onClause;

    public JoinOperator(JoinType joinType, JoinClass joinClassName, JoinOnClause onClause) {
        this.joinType = joinType;
        this.joinClass = joinClassName;
        this.onClause = onClause;
    }

    @Override
    public String toSqlString(String className) {
        return new StringBuilder()
                .append(" ")
                .append(joinType.getSqlStringValue())
                .append(" ")
                .append(joinClass.toSqlString(className))
                .append(onClause.toSqlString(className))
                .toString();
    }

}
