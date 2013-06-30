package ru.isk.cecriteria;

/**
 * @author imustafin
 */

import java.io.Serializable;


public class CeOrder implements Serializable {

    private final boolean ascending;
    private final String propertyName;

    private static final String DESC__ODER = "DESC";

    /**
     * Приватный конструктор
     */
    private CeOrder(String propertyName, boolean ascending) {
        this.propertyName = propertyName;
        this.ascending = ascending;
    }

    /**
     * ORDER BY по возрастанию
     * @param propertyName
     * @return Order
     */
    public static CeOrder asc(String propertyName) {
        return new CeOrder(propertyName, true);
    }

    /**
     * ORDER BY по убыванию
     * @param propertyName
     * @return Order
     */
    public static CeOrder desc(String propertyName) {
        return new CeOrder(propertyName, false);
    }

    /**
     * Генерирует SQL фрагмент
     *
     */
    public String toSqlString() {
        StringBuilder sqlFragment = new StringBuilder();
        sqlFragment.append("ORDER BY ")
                .append(propertyName);

        if (!ascending) {
            sqlFragment.append(' ')
                    .append(DESC__ODER);
        }

        return sqlFragment.toString();
    }

    public String toString() {
        return propertyName + ' ' + (ascending?"asc":"desc");
    }

}

