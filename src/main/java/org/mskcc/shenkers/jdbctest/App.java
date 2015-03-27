package org.mskcc.shenkers.jdbctest;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import test.Account;
import test.Account_;

/**
 * Hello world!
 *
 */
public class App {

    public static void main2(String[] args) throws NamingException {

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
        String dbName = "testDB";
        EmbeddedDataSource eds = new EmbeddedConnectionPoolDataSource();
        eds.setDatabaseName(dbName);
        eds.setCreateDatabase("create");

        Context ctx = new InitialContext();
//        ctx = (Context) ctx.lookup("java:comp/env");
        ctx.bind("jdbc/MyDB", eds);
        eds.setShutdownDatabase("shutdown");
    }

    public static void main3(String[] args) throws NamingException, SQLException {
        String connect = "jdbc:derby:testTB;create=true";
        String shutdwn = "jdbc:derby:testTB;shutdown=true";
        Connection connection = DriverManager.getConnection(connect);
//        connection.
        DriverManager.getConnection(shutdwn);
    }

    public static void addEntity(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("account");
        EntityManager em = emf.createEntityManager();
        EntityTransaction et = em.getTransaction();
        et.begin();
        Account a = new Account("Bob");
        em.persist(a);
        et.commit();
        System.out.println("a id " + a.getId());
    }

    public static void main(String[] args) throws UnknownHostException, Exception {
        String userHomeDir = System.getProperty("user.home", ".");
        String systemDir = userHomeDir + "/derbydatabase";
        NetworkServerControl nsc = new NetworkServerControl(InetAddress.getByName("localhost"), 1234);
        nsc.start(null);

        // Set the db system directory.
        System.setProperty("derby.system.home", systemDir);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("account");
        EntityManager em = emf.createEntityManager();
        {
            CriteriaQuery<Account> query = em.getCriteriaBuilder().createQuery(Account.class);
            Root<Account> root = query.from(Account.class);
            List<Account> resultList = em.createQuery(query).getResultList();
            for (Account act : resultList) {
                System.out.println("act: " + act.getName() + " id " + act.getId());
            }
        }
        {
//            javac -classpath path/to/openjpa-all.jar -Aopenjpa.metamodel=true mypackage/MyEntity.java
            CriteriaQuery<String> query = em.getCriteriaBuilder().createQuery(String.class);
            Root<Account> root = query.from(Account.class);
            query.select(root.<String>get("name"));
            List<String> resultList = em.createQuery(query).getResultList();
            for (String act : resultList) {
                System.out.println("name: " + act);
            }
        }
        {
//            javac -classpath path/to/openjpa-all.jar -Aopenjpa.metamodel=true mypackage/MyEntity.java
            CriteriaQuery<Long> query = em.getCriteriaBuilder().createQuery(Long.class);
            Root<Account> root = query.from(Account.class);
            query.select(root.<Long>get("id"));
            
            query.where(root.get(Account_.name).in("Dan"));
            
            List<Long> resultList = em.createQuery(query).getResultList();
            for (Long act : resultList) {
                System.out.println("id: " + act);
            }

        }
        {
            EntityTransaction et = em.getTransaction();
            et.begin();
            Account a = new Account("Dan");
            em.persist(a);
            et.commit();
            System.out.println("a id " + a.getId());
        }
        {
//            javac -classpath path/to/openjpa-all.jar -Aopenjpa.metamodel=true mypackage/MyEntity.java
            CriteriaQuery<Account> query = em.getCriteriaBuilder().createQuery(Account.class);
            Root<Account> root = query.from(Account.class);
            query.where(root.<String>get("name").in("Dan"));
            List<Account> resultList = em.createQuery(query).getResultList();

            for (Account act : resultList) {
                System.out.println("act: " + act.getName() + " id " + act.getId());
            }

            EntityTransaction et = em.getTransaction();
            et.begin();
            for (Account act : resultList) {
                System.out.println("REMOVING act: " + act.getName() + " id " + act.getId());
                em.remove(act);
            }
            et.commit();
        }
        new File(systemDir).delete();
        System.in.read();
    }
}
