package org.habitTracker.dao;

import org.habitTracker.entity.HabitLog;
import org.habitTracker.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;

public class HabitLogDAO {

    public void saveLog(HabitLog log){
        Session session = HibernateUtil.getSessionFactoy().openSession();
        Transaction tx = session.beginTransaction();

        session.persist(log);

        tx.commit();
        session.close();
    }

    public void updateCompletionCount(long habitId, LocalDate date, int completionCount){
        Session session = HibernateUtil.getSessionFactoy().openSession();
        Transaction tx = session.beginTransaction();

         int updatedEntities = session.createMutationQuery("UPDATE HabitLog SET completionCount= :newCompletionCount WHERE habit.id = :id AND date = :date")
                .setParameter("id", habitId)
                .setParameter("newCompletionCount", completionCount)
                .setParameter("date", date)
                .executeUpdate();

        System.out.println(updatedEntities + " entity updated successfully");
        session.clear();
        tx.commit();

        session.close();
    }

    public List<HabitLog> getLogsByHabit(long habitId){
        Session session = HibernateUtil.getSessionFactoy().openSession();

        List<HabitLog> logs = session.createQuery(
                "from HabitLog where habit.id =: id order by date desc",
                HabitLog.class
        ).setParameter("id", habitId)
                .list();

        session.close();
        return logs;
    }


    public int getLogsCompletionCount(long habitId, LocalDate date){
        Session session = HibernateUtil.getSessionFactoy().openSession();

        List<HabitLog> logs = session.createQuery(
                        "from HabitLog where habit.id =:id and date = :date order by date desc",
                        HabitLog.class
                ).setParameter("id", habitId)
                .setParameter("date", date)
                .list();
        if (logs == null) return -99999;
        int completionCount = logs.get(0).getCompletionCount();
        session.close();
        return completionCount;
    }
}
