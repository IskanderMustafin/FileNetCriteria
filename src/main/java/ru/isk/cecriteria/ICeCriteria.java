package ru.isk.cecriteria;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.property.PropertyFilter;
import ru.isk.cecriteria.join.JoinOperator;
import ru.brbpm.common.filenet.shared.ce.core.CeEngineObject;

import java.util.List;

/**
 * @author imustafin
 */
public interface ICeCriteria {

    /**
     * Добавляет ограничение поиска
     * @param criterion - объект, полученный в результате вызова одного из метода класса {@link CeRestrictions}
     * @return this
     */
    ICeCriteria add(HasSqlStringFragment criterion);

    /**
     * Устанавливает PropertyFilter для запроса
     * @param propertyFilter объект {@link PropertyFilter}
     * @return this
     */
    ICeCriteria setPropFilter(PropertyFilter propertyFilter);

    /**
     * Устанавливает PropertyFilter для запроса. Свойства которые нужно добавить в фильтр передаются как массив
     * @param props массив свойств которые будут конвертированы в PropertyFilter
     * @return this
     */
    ICeCriteria setPropFilterAsArray(String... props);

    /**
     * Устанавливает размер страницы при получении объектов.
     * Метод вызывается при постраничном обращении получении объектов.
     * Если значение установлено, то параметр {@code maxResult} игнорируется
     *
     * @param pageSize размер страницы при постраничном выводе
     * @return this
     */
    ICeCriteria setPageSize(int pageSize);

    /**
     * Задает свойства, которые необходиом заполучить.
     * Передать пустой массив для получения ВСЕХ свойст. т.е. SELECT *. Если null,
     * то по умолчанию будут получены только ID объектов
     * @param properties массив свойств для получения
     * @return this
     */
    ICeCriteria setSelectList(String... properties);


    /**
     * Задает занчение {@code WITH (INCLUDESUBCLASSES | EXCLUDESUBCLASSES )} в SQL строку. По умолчанию используется
     *   {@code WITH INCLUDESUBCLASSES}
     * @param - булево значение - включать или исключать подклассы из поиска
     * @return this
     */
    ICeCriteria setIncludeSubclasses(boolean includeSubclasses);

    /**
     * Задает таймаут запроса в секундах. Если не задано, то таймаут берется из общих настроек FN.
     * @param timeout - значение таймаута
     * @return this
     */
    ICeCriteria setTimeOut(int timeout);

    /**
     * Устанавливает максимальное количество возвращаемых строк.
     * Если задан параметр pageSize то этот параметр не учитывается.
     * @param maxResult - макс. количество строк
     * @return this
     */
    ICeCriteria setMaxResult(int maxResult);


    /**
     * Задает оператор JOIN который необходимо использовать в запросе
     * @param joinOperator объект описывающий характериситики JOIN'a
     * @return this
     */
    ICeCriteria setJoinOperator(JoinOperator joinOperator);

    /*==================================================*/


    /**
     * Возвращает результат запроса в виде IndependentObjectSet'a
     * @return IndependentObjectSet
     */
    IndependentObjectSet asIndependentObjectSet();

    /**
     * Возвращает первый результат запроса. Либо null если искомая коллекция объектов пуста
     * @return IndependentObject
     */
    IndependentObject asSingleIndependentObject();

    /**
     * Пытается вернуть уникальный объект в виде IndependentObject,
     * если объект = null или количество полученных объектов в результате запроса
     * больше еденицы, то пробрасывается исклчюение.
     * @throws ru.isk.cecriteria.exceptions.CeCriteriaUniqueViolationException -
     * если найдено больше одного объекта
     * @throws ru.isk.cecriteria.exceptions.CeCriteriaObjectNotFoundException - если объект не найден
     * @return IndependentObject
     */
    IndependentObject asUniqueIndependentObject();

    /**
     * Возвращает результат в виде DTO объекта, либо null если объект не найден
     * @param clazz класс объекта DTO. Обязательно должен иметь конструктор без аргументов
     * @return DTO (доменный) объект
     */
    <T extends CeEngineObject> T asSingleDto(Class<T> clazz) throws IllegalAccessException, InstantiationException;

    /**
     * Пытается вернуть уникальный объект в виде DTO,
     * если объект = null или количество полученных объектов в результате запроса
     * больше еденицы, то пробрасывается исклчюение.
     * @throws ru.isk.cecriteria.exceptions.CeCriteriaUniqueViolationException
     * если найдено больше одного объекта
     * @throws ru.isk.cecriteria.exceptions.CeCriteriaObjectNotFoundException если объект не найден
     * @return DTO (доменный) объект
     */
    <T extends CeEngineObject> T asUniqueDto(Class<T> clazz) throws IllegalAccessException, InstantiationException;

    /**
     * Задает ORDER BY
     * @param order объект с информацией для генерации ORDER BY
     * @return this
     */
    ICeCriteria setOrder(CeOrder order);

    /**
     * Вернуть резульатт  в виде списка (List). Не используйте этот метод если нужен постраничный результат.
     * @return
     */
    List<IndependentObject> asIndependentObjectList();
}
