package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.HBM2DDL_AUTO, "update");
        properties.put(Environment.SHOW_SQL, "true");
        properties.put(Environment.FORMAT_SQL, "true");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");

         sessionFactory = new Configuration()
                 .addAnnotatedClass(Player.class)
                 .addProperties(properties)
                 .buildSessionFactory()
                 ;
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {

        int rowsSkip = pageNumber * pageSize;
        String getAllQuery = "select * from rpg.player";

        try (Session session = sessionFactory.openSession()){
            return session.createNativeQuery(getAllQuery, Player.class)
                    .setFirstResult(rowsSkip)
                    .setMaxResults(pageSize)
                    .list();
        }
    }

    @Override
    public int getAllCount() {
        try(Session session = sessionFactory.openSession()){
            Query<Long> playerGetAllCount = session.createNamedQuery("Player_getAllCount", Long.class);
            return Math.toIntExact(playerGetAllCount.uniqueResult());
        }
    }

    @Override
    public Player save(Player player) {
        try(Session session = sessionFactory.openSession()){
            try{
                session.beginTransaction();
                session.save(player);
                session.getTransaction().commit();
            } catch (RuntimeException e){
                session.getTransaction().rollback();
                throw e;
            }
        }
        return player;
    }

    @Override
    public Player update(Player player) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            try{
                session.update(player);
                session.getTransaction().commit();
            } catch (RuntimeException e){
                session.getTransaction().rollback();
                throw e;
            }
        }
        return player;
    }

    @Override
    public Optional<Player> findById(long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Player.class, id));
        }
    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            try{
                session.remove(player);
                session.getTransaction().commit();
            } catch (RuntimeException e){
                session.getTransaction().rollback();
                throw e;
            }
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}