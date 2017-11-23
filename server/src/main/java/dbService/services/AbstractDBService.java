package dbService.services;

import dbService.dao.DAO;
import dbService.dataSets.AbstractDataSet;
import dbService.exceptions.DBException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.ServiceRegistry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;


/**
 * Created by Dmitrii Stoianov
 */


public abstract class AbstractDBService {
    private static final String hibernate_show_sql = "false";
    private static final String hibernate_hbm2ddl_auto = "update";


    private final SessionFactory sessionFactory;
    private final Class dataSetClass;

    AbstractDBService(Class dataSetClass) {
        Configuration configuration = getPostgresConfiguration(dataSetClass);
        sessionFactory = createSessionFactory(configuration);
        this.dataSetClass = dataSetClass;
    }


    private Configuration getPostgresConfiguration(Class clazz) {
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(clazz);

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost/users");
        configuration.setProperty("hibernate.connection.username", "postgres");
        configuration.setProperty("hibernate.connection.password", "password");
        configuration.setProperty("hibernate.show_sql", hibernate_show_sql);
        configuration.setProperty("hibernate.hbm2ddl.auto", hibernate_hbm2ddl_auto);
        return configuration;
    }


    private SessionFactory createSessionFactory(Configuration configuration) {
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = builder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }


    void printConnectInfo() {
        try {
            SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
            Connection connection = sessionFactoryImpl.getConnectionProvider().getConnection();
            System.out.println("DB name: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("DB version: " + connection.getMetaData().getDatabaseProductVersion());
            System.out.println("Driver: " + connection.getMetaData().getDriverName());
            System.out.println("Autocommit: " + connection.getAutoCommit());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    <T extends AbstractDataSet> T getNote(String key, String value) throws DBException {
        try {
            Session session = sessionFactory.openSession();
            T dataSet = new DAO(session).getNote(dataSetClass, key, value);
            session.close();
            return dataSet;
        } catch (HibernateException e) {
            throw new DBException(e);
        }
    }


    private void makeTransaction(Function<DAO, Void> function) throws DBException {
        try {
            Session session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();
            function.apply(new DAO(session));
            transaction.commit();
            session.close();
        } catch (HibernateException e) {
            throw new DBException(e);
        }
    }

    void addNote(AbstractDataSet dataSet) throws DBException {
        makeTransaction(dao -> {
            dao.addNote(dataSet);
            return null;
        });
    }

    public void deleteNote(AbstractDataSet dataSet) throws DBException {
        makeTransaction(dao -> {
            dao.deleteNote(dataSet);
            return null;
        });
    }

    public void updateNote(AbstractDataSet dataSet) throws DBException {
        makeTransaction(dao -> {
            dao.updateNote(dataSet);
            return null;
        });
    }

    public void addOrUpdateNote(AbstractDataSet dataSet) throws DBException {
        makeTransaction(dao -> {
            dao.addOrUpdateNote(dataSet);
            return null;
        });
    }


}
