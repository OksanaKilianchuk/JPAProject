package classes;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;


public class App {

    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPATest");
    static EntityManager em = emf.createEntityManager();


    public static void main( String[] args ) {

        try {
            Group group1 = new Group("Course-1");
            Group group2 = new Group("Course-2");
            Client client;
            long gid1, gid2;

            // #1
            System.out.println("------------------ #1 ------------------");

            for (int i = 0; i < 10; i++) {
                client = new Client("Name" + i, i);
                group1.addClient(client);
            }
            for (int i = 0; i < 5; i++) {
                client = new Client("Name" + i, i);
                group2.addClient(client);
            }

            em.getTransaction().begin();
            try {
                em.persist(group1); // save groups with clients
                em.persist(group2);
                em.getTransaction().commit();

                System.out.println("New group id #1: " + (gid1 = group1.getId()));
                System.out.println("New group id #2: " + (gid2 = group2.getId()));
            } catch (Exception ex) {
                em.getTransaction().rollback();
                return;
            }

            // #2
            System.out.println("------------------ #2 ------------------");
            em.clear();

            group1 = em.find(Group.class, gid1);
            if (group1 == null) {
                System.out.println("Course #1 not found error!");
                return;
            }
            for (Client c : group1.getClients())
                System.out.println("Client :" + c + " from " + c.getGroup().getName());

            group2 = em.find(Group.class, gid2);
            if (group2 == null) {
                System.out.println("Course #2 not found error!");
                return;
            }
            for (Client c : group2.getClients())
                System.out.println("Client :" + c + " from " + c.getGroup().getName());

            // #3
            System.out.println("------------------ #3 ------------------");
            em.clear();

            printGroups();

        } finally {
            em.close();
            emf.close();
        }


    }

    public static void printGroups(){

        System.out.println("The first way (using criteria query and group.getClients().size):");
        CriteriaQuery<Group> criteriaQuery = em.getCriteriaBuilder().createQuery(Group.class);
        Root<Group> c = criteriaQuery.from(Group.class);
        criteriaQuery.select(c);
        TypedQuery<Group> typedQuery = em.createQuery(criteriaQuery);
        List<Group> groupList = typedQuery.getResultList();
        for(Group group: groupList){
            System.out.println("Group "+group.getName()+" Number of clients = "+group.getClients().size());
        }

        System.out.println("The second way (using simple query):");
        Query query = em.createQuery("SELECT COUNT(client) FROM Client client WHERE client.group.name = :name");
        for (Group group:groupList) {
            query.setParameter("name",group.getName());
            System.out.println("Group "+group.getName()+" has "+query.getSingleResult()+" clients");
        }
    }

}
