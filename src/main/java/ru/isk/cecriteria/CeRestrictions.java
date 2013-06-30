package ru.isk.cecriteria;


import com.filenet.api.constants.PropertyNames;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author imustafin
 *
 */

/**
 * Содержит методы для создания ограничений результата SQL поиска. Служит в виде фабрики для объектов {@link HasSqlStringFragment}.
 */
public class CeRestrictions {

    private CeRestrictions() {
        throw new AssertionError();
    }

    /**
     * Устанавливает ограничение равенства по значению свойства
     * @param propertyName имя свойства
     * @param value значение
     * @return
     */
    public static HasSqlStringFragment eq(final String propertyName, final Object value) {
        return generateSimpleExpression(propertyName, SimpleOperand.EQ.stringValue(), value);
    }


    /**
     * Устанавливает ограничение по ключевому слову {@code OBJECT}
     * @param propertyName имя свойства
     * @param objectIdOrPath значение свойства которое нужно звернуть в  {@code OBJECT}
     * @return
     */
    public static HasSqlStringFragment isObject(final String propertyName, final String objectIdOrPath) {
        return new HasSqlStringFragment() {
            @Override
            public String toSqlString(String className) {
                return propertyName + " = OBJECT('" + objectIdOrPath + "')";
            }
        };
    }


    /**
     * Устанавливает ограничение "NOT EQUAL" (Не равно) на свойство.
     * @param propertyName
     * @param value
     * @return
     */
    public static HasSqlStringFragment notEq(String propertyName, Object value) {
        return generateSimpleExpression(propertyName, SimpleOperand.NOT_EQ.stringValue(), value);
    }


    /**
     * Устанавливает ограничение "Больше чем" (greater than)
     * @param propertyName
     * @param value
     * @return
     */
    public static HasSqlStringFragment gt(final String propertyName, final Object value) {
        return generateSimpleExpression(propertyName, SimpleOperand.GT.stringValue(), value);
    }

    /**
     * Устанавливает ограничение "Больше чем или равно" (greater than or equal)
     * @param propertyName
     * @param value
     * @return
     */
    public static HasSqlStringFragment ge(final String propertyName, final Object value) {
        return generateSimpleExpression(propertyName, SimpleOperand.GE.stringValue(), value);
    }

    /**
     * Устанавливает ограничение "Меньше чем" (less than)
     * @param propertyName
     * @param value
     * @return
     */
    public static HasSqlStringFragment lt(final String propertyName, final Object value) {
        return generateSimpleExpression(propertyName, SimpleOperand.LT.stringValue(), value);
    }


    /**
     * Устанавливает ограничение "Меньше чем или равно" (greater than or equal)
     * @param propertyName
     * @param value
     * @return
     */
    public static HasSqlStringFragment le(final String propertyName, final Object value) {
        return generateSimpleExpression(propertyName, SimpleOperand.LE.stringValue(), value);
    }


    /**
     * Устанавливает "LIKE" ограничение для свойства
     * @param propertyName
     * @param value
     * @return
     */
    public static HasSqlStringFragment like(final String propertyName, final String value) {
        return generateSimpleExpression(propertyName, "LIKE", value);
    }


    /**
     * Устанавоивает ограничение по папке. Объект должен находится в папке (не рекурсивно)
     * @param folderIdOrPath
     * @return
     */
    public static HasSqlStringFragment inFolder(final String folderIdOrPath) {
        return new HasSqlStringFragment() {
            @Override
            public String toSqlString(String className) {
                return new StringBuilder()
                        .append(className)
                        .append('.')
                        .append(PropertyNames.THIS)
                        .append(" INFOLDER")
                        .append(getSearchStringValue(folderIdOrPath))
                        .toString();
            }
        };
    }

    /**
     * Устанавоивает ограничение по папке. Объект должен находится в папке или в любой из подпапок (рекурсивно)
     * @param folderIdOrPath
     * @return
     */
    public static HasSqlStringFragment inSubfolder(final String folderIdOrPath) {
        return new HasSqlStringFragment() {
            @Override
            public String toSqlString(String className) {
                return new StringBuilder()
                        .append(className)
                        .append('.')
                        .append(PropertyNames.THIS)
                        .append(" INSUBFOLDER")
                        .append(getSearchStringValue(folderIdOrPath))
                        .toString();
            }
        };
    }


    /**
     * Устанавливает ограничение по голове связи
     * @param headId
     * @return
     */
    public static HasSqlStringFragment whereHeadIs(String headId) {
        return generateWhereObjectExpression(PropertyNames.HEAD, headId);
    }

    /**
     * Устанавливает ограничение по хвосту связи
     * @param tailId
     * @return
     */
    public static HasSqlStringFragment whereTailIs(String tailId) {
        return generateWhereObjectExpression(PropertyNames.TAIL, tailId);
    }

    /**
     * Устанавливает ограничение по голове или по хвосту связи
     * @param headOrTailId - идентификатор головы или хвоста связи
     * @return
     */
    public static HasSqlStringFragment whereHeadOrTailIs(String headOrTailId) {
        return new CeDisjunction()
                .add(generateWhereObjectExpression(PropertyNames.TAIL, headOrTailId))
                .add(generateWhereObjectExpression(PropertyNames.HEAD, headOrTailId));
    }

    /**
     * Устанавливает ограничение по предку объекта
     * @param parentIdorPath
     * @return
     */
    public static HasSqlStringFragment whereParentIs(String parentIdorPath) {
        return generateWhereObjectExpression(PropertyNames.PARENT, parentIdorPath);
    }

    /**
     * Уставналивает ограничение "BETWEEN" для значения свойства
     * @param propertyName
     * @param value1
     * @param value2
     * @return
     */
    public static HasSqlStringFragment between(final String propertyName, final Object value1, final Object value2) {
        return new HasSqlStringFragment() {
            @Override
            public String toSqlString(String className) {
                throw new UnsupportedOperationException("Метод не реализован");
            }
        };
    }

    /**
     * Из документации FileNet:
     *
     * The INTERSECTS operator can be used to test if a list property contains any member of a set of values,
     * or has values in common with another list property or with the return values of a subquery.
     *
     * The following format is used for the INTERSECTS operator: <br>
     * {@code <list 1> INTERSECTS <list 2> } <br>
     * Example:<br>
     * {@code SELECT … FROM Document WHERE ListPropertyString INTERSECTS ('value1', 'value2', 'value3')} <br>
     * {@code SELECT … FROM Document WHERE ListPropertyString1 INTERSECTS ListPropertyString2 }
     * @return
     */
    public static HasSqlStringFragment intersects() {
        return new HasSqlStringFragment() {
            @Override
            public String toSqlString(String className) {
                throw new UnsupportedOperationException("Метод не реализован");
            }
        };
    }

    /**
     * Устанавливает ограничение "in" для свойства (значение свойства должно входить в указанный диапазон).
     * @param propertyName
     * @param values
     * @return HasSqlStringFragment
     */
    public static HasSqlStringFragment in(String propertyName, Object[] values) {
        return new HasSqlStringFragment() {
            @Override
            public String toSqlString(String className) {
                throw new UnsupportedOperationException("Метод не реализован");
            }
        };
    }


    /**
     * Устанавливает "is null" ограничение на свойство. (т.е. значение свойства должно быть null).
     * @return Criterion
     */
    public static HasSqlStringFragment isNull(String propertyName) {
        return generateNullExpression(propertyName, true);
    }

    /**
     * Устанавливает "is null" ограничение на свойство. (т.е. значение свойства должно быть null).
     * @return Criterion
     */
    public static HasSqlStringFragment isNotNull(String propertyName) {
        return generateNullExpression(propertyName, false);
    }

    /**
     * Группирует выражения вместе в единую конъюнкцию (A and B and C...). <br>
     * Конъюнкция - AND.
     *
     * @return Conjunction
     */
    public static CeConjunction conjunction() {
        return new CeConjunction();
    }

    /**
     * Группирует выражения вместе в единую дизъюнкцию (A or B or C...). <br>
     * Дизъюнкция - OR.
     * @return Conjunction
     */
    public static CeDisjunction disjunction() {
        return new CeDisjunction();
    }

    /**
     * Возвращает строковое представление объекта для передачи в SQL строку. Строки обрамляются одинарными кавычками,
     * дата форматируется в {@code yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'}
     * @param value - объект, чье строковое представлние нужно вернуть
     * @return - строковое представление объекта
     */
    private static String getSearchStringValue(Object value) {
        String result;
        //
        if (value instanceof String) {
            result = quoteString(value.toString(), '\'');
        } else if (value instanceof Date) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            result = dateFormat.format((Date) value);
        } else
            result = String.valueOf(value);
        //
        return result;
    }

    /**
     * Обрамление строки.
     *
     * @param str Исходная строка
     * @param quoteChar Обрамляющий символ
     * @return Обрамленная строка
     */
    private static String quoteString(String str, Character quoteChar) {
        return quoteChar + str.replace(quoteChar.toString(), quoteChar.toString() + quoteChar.toString()) + quoteChar;
    }

    private static HasSqlStringFragment generateSimpleExpression(final String propertyName, final String op, final Object value) {
        return new HasSqlStringFragment() {
            @Override
            public String toSqlString(String className) {
                return new StringBuilder()
                        .append(propertyName)
                        .append(' ')
                        .append(op)
                        .append(' ')
                        .append(getSearchStringValue(value)).toString();
            }
        };
    }

    private static HasSqlStringFragment generateWhereObjectExpression(final String property, final String id) {
        return new HasSqlStringFragment() {
            @Override
            public String toSqlString(String className) {
                return new StringBuilder()
                        .append(property)
                        .append(" = OBJECT('")
                        .append(id)
                        .append("')")
                        .toString();
            }
        };
    }

    private static HasSqlStringFragment generateNullExpression(final String property, final boolean isNull) {
        return new HasSqlStringFragment() {
            @Override
            public String toSqlString(String className) {
                StringBuilder sqlFragment =  new StringBuilder();
                sqlFragment.append(property);

                if (isNull) {
                    sqlFragment.append(" IS NULL");
                } else {
                    sqlFragment.append(" IS NOT NULL");
                }

                return sqlFragment.toString();
            }
        };
    }

}
