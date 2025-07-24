package org.easytech.pelatologio.dao;

import org.easytech.pelatologio.models.TaskCategory;
import org.easytech.pelatologio.models.Tasks;

import java.sql.SQLException;
import java.util.List;

public interface TaskDao {
    List<Tasks> getAllTasks();
    boolean completeTask(int taskId, boolean isCompleted);
    boolean saveTask(Tasks tasks);
    boolean updateTask(Tasks tasks);
    void updateTaskCalendar(Tasks tasks);
    void deleteTask(int taskId) throws SQLException;
    void saveTaskCategory(TaskCategory newTaskCategory);
    void deleteTaskCategory(int id);
    void updateTaskCategory(TaskCategory updatedCategory);
    List<TaskCategory> getAllTaskCategory();
    int getTasksCount();
    int getAppointmentsCount();
    List<Tasks> getAllCustomerTasks(int customerCode);
    List<Tasks> getUpcomingAppointments(java.time.LocalDateTime dateTime);
    void snoozeAppointment(int id);
    List<Tasks> getTodaysTasks();
}