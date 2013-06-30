package ru.isk.cecriteria.utils;

/**
 * @author imustafin
 */
public class CeCriteriaUtils {

    private CeCriteriaUtils() {}

/*
    */
/**
     * Получение строкового представления имени объекта для поиска, чтобы не было совпадения с ключевыми словами SQL.
     *
     * @param name Имя объекта
     * @return Строковое представление имени объекта
     *//*

    public static String getSearchStringToken(String name) {
        return '[' + name + ']';
    }
*/


    /**
     * Объединяет элементы массива в одну строку
     * @param input - входной массив
     * @param delimiter разделитель между элементами массива
     * @return объединенная строка
     */
    public static String join(String[] input, String delimiter)
    {
        StringBuilder sb = new StringBuilder();
        for(String value : input)
        {
            sb.append(value);
            sb.append(delimiter);
        }
        int length = sb.length();
        if(length > 0)
        {
            // Remove the extra delimiter
            sb.setLength(length - delimiter.length());
        }
        return sb.toString();
    }
}
