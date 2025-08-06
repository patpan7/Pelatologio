package org.easytech.pelatologio.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.easytech.pelatologio.dao.TaskDao;
import org.easytech.pelatologio.models.TaskCategory;
import org.easytech.pelatologio.models.Tasks;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TaskDaoImpl implements TaskDao {

    private final HikariDataSource dataSource;

    public TaskDaoImpl(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public List<Tasks> getAllTasks() {
        List<Tasks> tasks = new ArrayList<>();
        String query = "SELECT t.id, t.title, t.description, t.dueDate, t.is_Completed, t.customerId, t.category, t.is_ergent, t.is_wait, t.is_calendar, t.start_time, t.end_time, c.name " +
                "FROM Tasks t " +
                "LEFT JOIN Customers c ON t.customerId = c.code";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                LocalDate dueDate = resultSet.getDate("dueDate").toLocalDate();
                boolean isCompleted = resultSet.getBoolean("is_Completed");
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                String category = resultSet.getString("category");
                String customerName = resultSet.getString("name");
                Boolean isErgent = resultSet.getBoolean("is_ergent");
                Boolean isWait = resultSet.getBoolean("is_wait");
                Boolean isCalendar = resultSet.getBoolean("is_calendar");
                LocalDateTime startTime = resultSet.getTimestamp("start_time") != null ? resultSet.getTimestamp("start_time").toLocalDateTime() : null;
                LocalDateTime endTime = resultSet.getTimestamp("end_time") != null ? resultSet.getTimestamp("end_time").toLocalDateTime() : null;


                Tasks task = new Tasks(id, title, description, dueDate, isCompleted, category, customerId, customerName, isErgent, isWait, isCalendar, startTime, endTime);
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    @Override
    public boolean completeTask(int taskId, boolean isCompleted) {
        String query = "UPDATE tasks SET is_completed = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, isCompleted);
            stmt.setInt(2, taskId);
            if (stmt.executeUpdate() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean saveTask(Tasks tasks) {
        String query = "INSERT INTO Tasks (title, description, dueDate, is_completed, customerId, category, is_ergent, is_wait, is_calendar, start_time, end_time, snooze) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tasks.getTitle());
            stmt.setString(2, tasks.getDescription());
            stmt.setDate(3, Date.valueOf(tasks.getDueDate()));
            stmt.setBoolean(4, false);
            if (tasks.getCustomerId() != null) {
                stmt.setInt(5, tasks.getCustomerId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setString(6, tasks.getCategory());
            stmt.setBoolean(7, tasks.getErgent());
            stmt.setBoolean(8, tasks.getWait());
            stmt.setBoolean(9, tasks.getIsCalendar());
            stmt.setTimestamp(10, Timestamp.valueOf(tasks.getStartTime()));
            stmt.setTimestamp(11, Timestamp.valueOf(tasks.getEndTime()));
            stmt.setBoolean(12, false);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return true; // Ενημερώθηκε επιτυχώς
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
                return saveTask(tasks);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateTask(Tasks tasks) {
        String query = "UPDATE Tasks SET title = ?, description = ?, dueDate = ?, is_Completed = ?, category = ?, customerId = ?, is_ergent = ?, is_wait = ?, is_calendar = ?, start_time = ?, end_time = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tasks.getTitle());
            stmt.setString(2, tasks.getDescription());
            stmt.setDate(3, Date.valueOf(tasks.getDueDate()));
            stmt.setBoolean(4, tasks.getCompleted());
            stmt.setString(5, tasks.getCategory());
            if (tasks.getCustomerId() != null) {
                stmt.setInt(6, tasks.getCustomerId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            stmt.setBoolean(7, tasks.getErgent());
            stmt.setBoolean(8, tasks.getWait());
            stmt.setBoolean(9, tasks.getIsCalendar());
            stmt.setTimestamp(10, Timestamp.valueOf(tasks.getStartTime()));
            stmt.setTimestamp(11, Timestamp.valueOf(tasks.getEndTime()));
            stmt.setInt(12, tasks.getId());


            if (stmt.executeUpdate() > 0) {
                return true;
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void updateTaskCalendar(Tasks tasks) {
        String query = "UPDATE Tasks SET is_calendar = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, tasks.getIsCalendar());
            stmt.setInt(2, tasks.getId());


            if (stmt.executeUpdate() > 0) {
            } else {
                // Αν δεν υπάρχει το ραντεβού, το προσθέτουμε
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTask(int taskId) throws SQLException {
        String query = "DELETE FROM Tasks WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, taskId);
            statement.executeUpdate();
        }
    }

    @Override
    public void saveTaskCategory(TaskCategory newTaskCategory) {
        String query = "INSERT INTO TaskCategories (name) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, newTaskCategory.getName());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                newTaskCategory.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTaskCategory(int id) {
        String query = "DELETE FROM TaskCategories WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTaskCategory(TaskCategory updatedCategory) {
        String query = "UPDATE TaskCategories SET name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, updatedCategory.getName());
            pstmt.setInt(2, updatedCategory.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<TaskCategory> getAllTaskCategory() {
        List<TaskCategory> taskCategories = new ArrayList<>();
        String query = "SELECT * FROM TaskCategories";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                taskCategories.add(new TaskCategory(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return taskCategories;
    }

    @Override
    public int getTasksCount() {
        String query = "SELECT COUNT(*) FROM Tasks where is_Completed = 0";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getAppointmentsCount() {
        String query = "SELECT COUNT(*) AS total_appointments " +
                "FROM appointments " +
                "WHERE CAST(start_time AS DATE) = CAST(GETDATE() AS DATE);";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<Tasks> getAllCustomerTasks(int customerCode) {
        List<Tasks> tasks = new ArrayList<>();
        String query = "SELECT t.id, t.title, t.description, t.dueDate, t.is_Completed, t.customerId, t.category, t.is_ergent, t.is_wait, t.is_calendar, t.start_time, t.end_time, c.name " +
                "FROM Tasks t " +
                "LEFT JOIN Customers c ON t.customerId = c.code " +
                "WHERE t.customerId = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerCode);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                LocalDate dueDate = resultSet.getDate("dueDate").toLocalDate();
                boolean isCompleted = resultSet.getBoolean("is_Completed");
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                String category = resultSet.getString("category");
                boolean isErgent = resultSet.getBoolean("is_ergent");
                boolean isWait = resultSet.getBoolean("is_wait");
                String customerName = resultSet.getString("name");
                boolean isCalendar = resultSet.getBoolean("is_calendar");
                LocalDateTime startTime = resultSet.getObject("start_time", LocalDateTime.class);
                LocalDateTime endTime = resultSet.getObject("end_time", LocalDateTime.class);

                Tasks task = new Tasks(id, title, description, dueDate, isCompleted, category, customerId, customerName, isErgent, isWait, isCalendar, startTime, endTime);
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    @Override
    public List<Tasks> getUpcomingAppointments(LocalDateTime checkTime) {
        List<Tasks> tasks = new ArrayList<>();
        // Στρογγυλοποιούμε την checkTime για να αφαιρέσουμε τη νανοδευτερόλεπτη ακρίβεια
        checkTime = checkTime.truncatedTo(ChronoUnit.SECONDS);
        // Ερώτημα SQL για να βρούμε τα ραντεβού που ξεκινούν σε απόσταση 15 λεπτών από την τρέχουσα ώρα
        String query = "SELECT id, customerId, title, description, start_time, end_time FROM Tasks " +
                "WHERE start_time BETWEEN ? AND ? AND is_completed = 0 AND snooze = 0";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            // Βάζουμε το χρονικό παράθυρο για τα ραντεβού (από τώρα μέχρι 15 λεπτά μετά)
            LocalDateTime startRange = checkTime;
            LocalDateTime endRange = checkTime.plusMinutes(30);

            stmt.setTimestamp(1, Timestamp.valueOf(startRange));
            stmt.setTimestamp(2, Timestamp.valueOf(endRange));
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    LocalDateTime startTime = resultSet.getTimestamp("start_time").toLocalDateTime();
                    int customerId = resultSet.getInt("customerId");

                    // Δημιουργούμε το αντικείμενο Appointment και το προσθέτουμε στη λίστα
                    Tasks appointment = new Tasks();
                    appointment.setId(id);
                    appointment.setTitle(title);
                    appointment.setStartTime(startTime);
                    appointment.setCustomerId(customerId);
                    tasks.add(appointment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    @Override
    public void snoozeAppointment(int id) {
        String query = "UPDATE Tasks SET snooze = 1 WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Tasks> getTodaysTasks() {
        List<Tasks> tasks = new ArrayList<>();
        String query = "SELECT t.id, t.title, t.description, t.dueDate, t.is_Completed, t.customerId, t.category, t.is_ergent, t.is_wait, t.is_calendar, t.start_time, t.end_time, c.name " +
                "FROM Tasks t " +
                "LEFT JOIN Customers c ON t.customerId = c.code " +
                "WHERE t.is_Completed = 0 AND CAST(t.dueDate AS DATE) = CAST(GETDATE() AS DATE)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                LocalDate dueDate = resultSet.getDate("dueDate").toLocalDate();
                boolean isCompleted = resultSet.getBoolean("is_Completed");
                Integer customerId = resultSet.getObject("customerId", Integer.class);
                String category = resultSet.getString("category");
                String customerName = resultSet.getString("name");
                Boolean isErgent = resultSet.getBoolean("is_ergent");
                Boolean isWait = resultSet.getBoolean("is_wait");
                Boolean isCalendar = resultSet.getBoolean("is_calendar");
                LocalDateTime startTime = resultSet.getTimestamp("start_time") != null ? resultSet.getTimestamp("start_time").toLocalDateTime() : null;
                LocalDateTime endTime = resultSet.getTimestamp("end_time") != null ? resultSet.getTimestamp("end_time").toLocalDateTime() : null;

                Tasks task = new Tasks(id, title, description, dueDate, isCompleted, category, customerId, customerName, isErgent, isWait, isCalendar, startTime, endTime);
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }
}