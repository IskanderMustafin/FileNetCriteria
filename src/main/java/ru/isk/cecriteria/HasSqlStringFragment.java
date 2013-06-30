package ru.isk.cecriteria;

/**
* @author imustafin
*/
public interface HasSqlStringFragment {
    /**
     * Формирует фрагмент SQL строки
     * @param className имя класса, по которому производится операция поиска
     * @return Сформированный фоагмент SQL строки
     */
    String toSqlString(String className);
}
