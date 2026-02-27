package org.habitTracker.util;

import org.habitTracker.entity.Habit;
import org.habitTracker.entity.HabitLog;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(Habit.class)
                    .addAnnotatedClass(HabitLog.class)
                    .buildSessionFactory();
        } catch (Throwable ex){
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactoy(){
        return sessionFactory;
    }

}
