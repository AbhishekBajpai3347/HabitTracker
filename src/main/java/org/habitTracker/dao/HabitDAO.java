package org.habitTracker.dao;

import org.habitTracker.entity.Habit;
import org.habitTracker.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HabitDAO {

    private static final Logger log = LoggerFactory.getLogger(HabitDAO.class);

    public void saveHabit(Habit habit){
        Session session = HibernateUtil.getSessionFactoy().openSession();
        Transaction tx = session.beginTransaction();

        session.persist(habit);

        tx.commit();
        session.close();
    }

    public void deleteHabit(long id){
        Session session = HibernateUtil.getSessionFactoy().openSession();
        Transaction tx = session.beginTransaction();
        session.remove(session.find(Habit.class, id));

        tx.commit();
        session.clear();
        session.close();
    }

    public Habit getHabitById(long id){
        Session session = HibernateUtil.getSessionFactoy().openSession();
        Habit habit = session.find(Habit.class, id);
        session.close();
        return habit;
    }

    public List<Habit> getAllHabits(){
        Session session = HibernateUtil.getSessionFactoy().openSession();
        List<Habit> habits = session.createQuery("from Habit", Habit.class).list();
        session.close();
        return habits;
    }

}
