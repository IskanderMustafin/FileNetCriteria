package ru.isk.cecriteria;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import ru.isk.cecriteria.exceptions.CeCriteriaException;
import ru.isk.cecriteria.exceptions.CeCriteriaObjectNotFoundException;
import ru.isk.cecriteria.exceptions.CeCriteriaUniqueViolationException;
import ru.isk.cecriteria.join.JoinOperator;
import ru.isk.cecriteria.utils.CeCriteriaUtils;
import ru.brbpm.common.filenet.server.ce.util.CeConvertUtil;
import ru.brbpm.common.filenet.shared.ce.core.CeEngineObject;
import ru.brbpm.common.utils.server.LoggerUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author imustafin
 */

/**
 * Простой поиск объектов по СЕ.
 * Не поддерживает подзапросы,  выборку из нескольких хранилищ и некоторые другие функции.
 */

// TODO Подумать над возможностью разбиения функционала получения результата и функционала задания параметров поиска
// TODO Разбить класс на 2 сущности: одна должна хранить состояние объекта объекта запроса, вторая должна отвечать за выполнение запросов
public class CeCriteria implements ICeCriteria {

    private static final LoggerUtils.MyLogger logger = LoggerUtils.getLogger(CeCriteria.class);

    /**
     * Объект хранилища в котором будет произведен поиск
     */
    private final ObjectStore objectStore;
    /**
     * Имя класса по которому будет производиться поиск
     */
    private final String className;
    /**
     * Список для хранения условий поиска
     */
    private final List<HasSqlStringFragment> criterions = new ArrayList<HasSqlStringFragment>();

    /**
     * Алиас для класса по которому происходит выборка
     */
    private final String classNameAlias;

    /**
     * PropertyFilter который будет применен
     */
    private PropertyFilter propertyFilter;
    /**
     * Размер страницы
     */
    private Integer pageSize;
    /**
     * Таймаут запроса в секундах
     */
    private Integer timeout;
    /**
     * Свойства, которые нужно получить. Пустое массив для получения всех свойств. Null - для получения только ID
     */
    private String[] properties;
    /**
     * Флаг, указывающий нужно ли включать подклассы в результат запроса
     */
    private boolean includeSubclasses = true;
    /**
     * Макс. количество объектов для поиска
     */
    private Integer maxResult;
    /**
     * Задает порядок ORDER BY
     */
    private CeOrder order;
    /**
     * Оператор Join
     */
    private JoinOperator joinOperator;


    private CeCriteria(ObjectStore objectStore, String className, String alias) {
        this.objectStore = objectStore;
        this.className = className;
        this.classNameAlias = alias;
    }

    // используем статик-фактори метод для того чтобы вернуть интерфейс а не реализацию // TODO может сделать билдер?
    public static ICeCriteria create(ObjectStore objectStore, String className) {
        return new CeCriteria(objectStore, className, null);
    }

    public static ICeCriteria create(ObjectStore objectStore, String className, String alias) {
        return new CeCriteria(objectStore, className, alias);
    }



    public ICeCriteria setJoinOperator(JoinOperator joinOperator) {
        this.joinOperator = joinOperator;
        return this;
    }

    @Override
    public ICeCriteria setSelectList(String... properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public ICeCriteria add(HasSqlStringFragment criterion) {
        criterions.add(criterion);
        return this;
    }

    @Override
    public ICeCriteria setPropFilter(PropertyFilter propertyFilter) {
        this.propertyFilter = propertyFilter;
        return this;
    }

    @Override
    public ICeCriteria setPropFilterAsArray(String... properties) {
        PropertyFilter filter = new PropertyFilter();
        String propsAsString = CeCriteriaUtils.join(properties, " ");
        filter.addIncludeProperty(0, null, null, propsAsString, null);
        this.propertyFilter = filter;
        return this;
    }

    @Override
    public ICeCriteria setPageSize(int pageSize) {
        if (pageSize <= 0) {
            throw new CeCriteriaException("Размер страницы не может быть меньше или равен нулю. ");
        }
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public ICeCriteria setMaxResult(int maxResult) {
        if (maxResult <= 0) {
            throw new CeCriteriaException("Количество объектов для поиска не может быть меньше или равно нулю. ");
        }
        this.maxResult = maxResult;
        return this;
    }

    @Override
    public ICeCriteria setIncludeSubclasses(boolean includeSubclasses) {
        this.includeSubclasses = includeSubclasses;
        return this;
    }

    @Override
    public ICeCriteria setTimeOut(int timeout) {
        if (timeout < 0) {
            throw new CeCriteriaException("Таймаут не может быть отрицательным. ");
        }
        this.timeout = timeout;
        return this;
    }

    @Override
    public ICeCriteria setOrder(CeOrder order) {
        this.order = order;
        return this;
    }
    /*=======*/

    private IndependentObjectSet fetchIndependentObjectSet() {
        SearchSQL searchSQL = new SearchSQL(generateSqlString());
        SearchScope scope = new SearchScope(objectStore);

        // Если пэйджинг задан, то нужно флаг `continuable` сделать = true
        boolean continuable = false;
        if (pageSize != null) {
            continuable = true;
        }

        return scope.fetchObjects(searchSQL, pageSize, propertyFilter, continuable);
    }

    private String generateSqlString() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        // если запрос не постраничный и максимальное количество задано
        if (pageSize == null && maxResult != null) {
            sql.append("TOP ")
                    .append(maxResult)
                    .append(' ');
        }

        // задаем набор свойств
        if (properties == null) { // если свойства не заданы, то по умолчанию пытаемся зафетчить только ID объектов
            sql.append(PropertyNames.ID);
        } else if (properties.length == 0) { // если массив свойств задан, но он пустой, то выбираем все т.е. *
            sql.append('*');
        } else { // если заданы свойства

            // если в запросе есть JOIN, то нужно добавить имя класса перед именем свойства
            boolean needToAddClassNamePrefix = joinOperator != null;
            sql.append(propertiesToSqlSubString(properties , needToAddClassNamePrefix));
        }
        //
        sql.append(" FROM ")
                .append(className);

        // алиас для класса выборки
        if (classNameAlias != null) {
            sql.append(" ")
                    .append(classNameAlias);
        }

        // если подклассы включать в результат поиска не нужно. По умолчанию они включены (INCLDESUBCLASSES)
        if (!includeSubclasses) {
            sql.append(" WITH EXCLUDESUBCLASSES");
        }

        // Формируем Join
        if(joinOperator != null) {
            sql.append(joinOperator.toSqlString(className));
        }

        // формируем WHERE clause
        if (!criterions.isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < criterions.size(); i++) {
                sql.append(criterions.get(i).toSqlString(className));
                if (i < criterions.size() - 1) {
                    sql.append(" AND ");
                }
            }

            //sql.append(')');
        }

        // Добавляем ORDER BY если он задан
        if (order != null) {
            sql.append(order.toSqlString());
        }

        // Задаем доп. параметры запроса
        if (timeout != null) {
            sql.append(" OPTIONS(TIMELIMIT ")
                    .append(timeout)
                    .append(')');
        }

        logger.trace("Generated SQL query: {0}", sql.toString());
        return sql.toString();
    }

    /**
     * Преобразует массив свойств в SQL строку
     *
     * @param props массив свойст
     * @param addClassNamePrefix указывает на то нужно ли добавлять имя таблицы и точку перед названием свойства.
     *                           Например {@code Person.Id} вместо просто {@code Id}.
     *                           Необходимо добавлять имя класса если используется JOIN.
     * @return строковое представление массива свойсв, в виде FileNet SQL строки
     */
    private String propertiesToSqlSubString(String[] props, boolean addClassNamePrefix) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < props.length; i++) {
            if (addClassNamePrefix) {

                // если есть алиас, то добавим его, если алиас не задан, то добавим имя класса
                if (classNameAlias != null) {
                    result.append(classNameAlias);
                } else {
                    result.append(className);
                }
                result.append('.');
            }

            result.append(props[i]);

            if (i < props.length - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /*============МЕТОДЫ ВОЗВРАЩАЮЩИЕ РЕЗУЛЬТАТ========*/

    @Override
    public IndependentObjectSet asIndependentObjectSet() {
        try {
            return fetchIndependentObjectSet();
        } catch (Exception e) {
            throw new CeCriteriaException("Произошла ошибка при пыпытке получить IndependentObjectSet. ", e);
        }
    }

    @Override
    public IndependentObject asSingleIndependentObject() {
        try {
            // игнорим пэйджинг если нужен только один результ
            pageSize = null;
            // устанавливаем макс. колво строк = 1
            setMaxResult(1);
            IndependentObjectSet independentObjectSet = fetchIndependentObjectSet();

            IndependentObject result = null;
            @SuppressWarnings("unchecked")
            Iterator<IndependentObject> iter = independentObjectSet.iterator();
            if (iter.hasNext()) {
                result = iter.next();
            }
            return result;
        } catch (Exception e) {
            throw new CeCriteriaException("Произошла ошибка при пыпытке получить IndependentObject. ", e);
        }
    }

    @Override
    public IndependentObject asUniqueIndependentObject() {
        try {
            // игнорим пэйджинг если нужен только один результ
            pageSize = null;
            // устанавливаем макс. колво строк = 1
            setMaxResult(1);
            IndependentObjectSet independentObjectSet = fetchIndependentObjectSet();

            if (independentObjectSet.isEmpty()) {
                throw new CeCriteriaObjectNotFoundException("Не удалось найти ни одного объекта. ");
            }

            IndependentObject result = null;
            @SuppressWarnings("unchecked")
            Iterator<IndependentObject> iter = independentObjectSet.iterator();
            if (iter.hasNext()) {
                result = iter.next();
                if (iter.hasNext()) {
                    throw new CeCriteriaUniqueViolationException("Найдено больше одного объекта!");
                }
            }
            return result;

        } catch (CeCriteriaException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CeCriteriaException("Произошла ошибка при пыпытке получить уникальный объект IndependentObject. ", e);
        }
    }

    @Override
    public <T extends CeEngineObject> T asSingleDto(Class<T> clazz) throws IllegalAccessException, InstantiationException {
        try {
            // игнорим пэйджинг если нужен только один результ
            pageSize = null;
            // устанавливаем макс. колво строк = 1
            setMaxResult(1);

            IndependentObject independentObject = asSingleIndependentObject();
            if (independentObject == null) {
                return null;
            }
            return CeConvertUtil.createDto(clazz.newInstance(), independentObject);
        } catch (Exception e) {
            throw new CeCriteriaException("Произошла ошибка при пыпытке получить DTO.", e);
        }
    }

    @Override
    public List<IndependentObject> asIndependentObjectList() {

        if (pageSize != null) {
            throw new CeCriteriaException("Нельзя использовать этот метод при постраничном получении объектов.");
        }

        List<IndependentObject> result = new ArrayList<IndependentObject>();

        IndependentObjectSet independentObjectSet = fetchIndependentObjectSet();


        for (Iterator<IndependentObject> iter = independentObjectSet.iterator(); iter.hasNext(); ) {
            result.add(iter.next());
        }
        return result;
    }


    @Override
    public <T extends CeEngineObject> T asUniqueDto(Class<T> clazz) {
        try {
            // игнорим пэйджинг если нужен только один результ
            pageSize = null;
            // устанавливаем макс. кол-во строк = 1
            setMaxResult(1);
            IndependentObject independentObject = asUniqueIndependentObject();
            return CeConvertUtil.createDto(clazz.newInstance(), independentObject);
        } catch (CeCriteriaException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CeCriteriaException("Произошла ошибка при пыпытке получить уникальный объект DTO. ", e);
        }
    }
}
