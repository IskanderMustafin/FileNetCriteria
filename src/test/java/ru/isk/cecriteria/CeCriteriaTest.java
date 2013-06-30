package ru.isk.cecriteria;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.PageIterator;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.core.*;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import org.junit.*;
import org.xml.sax.SAXException;
import ru.isk.cecriteria.join.JoinClass;
import ru.isk.cecriteria.join.JoinOnClause;
import ru.isk.cecriteria.join.JoinOperator;
import ru.isk.cecriteria.join.JoinType;
import ru.brbpm.common.filenet.server.CeConnection;
import ru.brbpm.common.filenet.server.CeConnectionImpl;
import ru.brbpm.common.filenet.server.ConnectionConstants;
import ru.brbpm.common.utils.server.LoggerUtils;
import ru.brbpm.common.utils.shared.HasGetProperty;
import ru.brbpm.common.utils.shared.Preconditions;
import ru.brbpm.config_service.client.ConfigServiceClient;
import ru.brbpm.config_service.client.ConfigServiceClientException;
import ru.brbpm.config_service.client.ConfigServiceClientFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author imustafin
 */

public class CeCriteriaTest {

    private ObjectStore objectStore;

    private static HasGetProperty hasGetProperty;

    private static final LoggerUtils.MyLogger logger = LoggerUtils.getLogger(CeCriteriaTest.class);

    private static ConfigServiceClient configService;

    private static CeConnection conn;

    @BeforeClass
    public static void setUp() throws ConfigServiceClientException, IOException, SAXException, ParserConfigurationException {

        configService = ConfigServiceClientFactory.getSingleton("ce-criteria", true);
        hasGetProperty = ConfigServiceClientFactory.getProperties(configService);

        //зачитываем конфигурацию логгера с помощью конфиг-сервиса
        LoggerUtils.readConfiguration(Preconditions.checkNotNull(configService.getResource("log4j.xml"),
                "log4j.xml не найден. "));
    }

    @Before
    public void beforeMethod() throws ConfigServiceClientException {
        conn = new CeConnectionImpl(hasGetProperty);
        logger.info("Connecting to OS. ");
        conn.logInAdmin();
        Domain domain = Factory.Domain.getInstance(conn.getConnection(),
                Preconditions.checkNotNull(configService.getConfigProperty(ConnectionConstants.CE_DOMAIN, null)));
        objectStore = Factory.ObjectStore.getInstance(domain, Preconditions.checkNotNull(configService.getConfigProperty("ce.os", null)));
    }

    @After
    public void afterMethod() {
        logger.info("Disconnecting from OS. ");
        conn.logOffAdmin();
    }

    @Ignore
    @Test
    public void testAsIndependentObjectSet() throws Exception {
        IndependentObjectSet personSet = CeCriteria.create(objectStore, "Person")
                .setSelectList(new String[]{}) // получаем все свойства
                .setTimeOut(5)
                .setPropFilterAsArray(PropertyNames.ID) // метод принимает vararg
                .setOrder(CeOrder.asc("Surname"))
                .add(CeRestrictions.inSubfolder("\\Сотрудники"))
                .add(CeRestrictions.disjunction()
                        .add(CeRestrictions.eq("Patronymic", "Николаевич"))
                        .add(CeRestrictions.conjunction()
                                .add(CeRestrictions.like("PersonName", "В%"))
                                .add(CeRestrictions.isNotNull("Surname"))
                        )
                )
                .asIndependentObjectSet();
        logger.debug("Is set empty? " + personSet.isEmpty());
        for (Iterator<?> iter = personSet.iterator(); iter.hasNext(); ) {
            IndependentObject obj = (IndependentObject) iter.next();
            logger.debug("Person names are : " + obj.getProperties().getStringValue("PersonName"));
        }
    }
    @Ignore
    @Test
    public void simpleTest() throws Exception {
        String sql = "SELECT Id FROM Link WHERE ((Tail = OBJECT('{6529A230-4DF0-4710-BB5F-8C43B93B2F2E}') OR Head = OBJECT('{6529A230-4DF0-4710-BB5F-8C43B93B2F2E}')))";
        SearchSQL searchsql = new SearchSQL(sql);
        SearchScope scope = new SearchScope(objectStore);
        PropertyFilter idFilter = new PropertyFilter();
        idFilter.addIncludeProperty(0, null, null, PropertyNames.ID, null);
        //idFilter.addIncludeProperty(0, null, null, PropertyNames.TAIL, null);
        IndependentObjectSet independentObjectSet = scope.fetchObjects(searchsql, null, idFilter, null);

        logger.debug("Is empty : " + independentObjectSet.isEmpty());

        for (Iterator<?> iter = independentObjectSet.iterator(); iter.hasNext(); ) {
            Link link = (Link)iter.next();
            //logger.debug("Id of curObject" + link.get_Id().toString());

            logger.debug(link.get_Tail().getClassName());
        }
    }


    @Ignore
    @Test
    public void testJoinClause() throws Exception {
        IndependentObjectSet positions = CeCriteria.create(objectStore, "Position", "p")
                .setSelectList("This", "Id")
                .setPageSize(25)
                .setJoinOperator(new JoinOperator(
                        JoinType.INNER,
                        new JoinClass("PersonOnPosition", "pp"),
                        new JoinOnClause("pp.Tail", SimpleOperand.EQ, "p.This"))

                )
                .add(CeRestrictions.isObject("pp.Tail", "//root"))
                .asIndependentObjectSet();

        PageIterator pageIterator = positions.pageIterator();
        Assert.assertEquals(pageIterator.getPageSize(), 25);
        if (pageIterator.nextPage()) {
            Object[] currentPage = pageIterator.getCurrentPage();
            logger.debug(((Containable) currentPage[0]).get_Id().toString());
            logger.debug(Arrays.toString(pageIterator.getCurrentPageCheckpoint()));
        }
    }

    @Ignore
    @Test
    public void testPersonsInOrganization() throws Exception {
        IndependentObjectSet independentObjectSet = CeCriteria.create(objectStore, "Person", "p")
                .setSelectList("*")
                .setPageSize(25)
                .setJoinOperator(new JoinOperator(JoinType.INNER,
                        new JoinClass("PersonInOrganization", "PO"),
                        new JoinOnClause("PO.Head", SimpleOperand.EQ, "P.This")))
                .add(CeRestrictions.isNotNull("PO.Head"))
                .add(CeRestrictions.isObject("PO.Tail", "{C964FFA6-7F9D-4021-83E9-E7EC338ABD5F}"))
                .asIndependentObjectSet();

        logger.debug(" IS EMPTY :  " + independentObjectSet.isEmpty());

    }

    public void testAsUniqueDto() throws Exception {

    }
}
