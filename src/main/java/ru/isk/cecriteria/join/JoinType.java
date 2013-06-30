package ru.isk.cecriteria.join;

/**
 * @author imustafin
 */

/**
 * Тип оператора JOIN
 */
public enum JoinType {

    /**
     * Указывает на  full outer join.
     */
    FULL_OUTER ("FULL OUTER JOIN"),
    /**
     * Указывает на inner join
     */
    INNER ("INNER JOIN"),

    /**
     * Указывает на left outer join
     */
    LEFT_OUTER ("LEFT OUTER JOIN"),

    /**
     * Указывает на right outer join
     */
    RIGHT_OUTER ("RIGHT OUTER JOIN");


    private String sqlStringValue;

    JoinType(String sqlStringValue) {
        this.sqlStringValue = sqlStringValue;
    }

    public String getSqlStringValue() {
        return sqlStringValue;
    }
}