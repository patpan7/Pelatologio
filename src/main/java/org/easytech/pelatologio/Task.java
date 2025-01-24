package org.easytech.pelatologio;
import javafx.beans.property.*;

import java.time.LocalDate;

public class Task {
    private final IntegerProperty id;
    private final StringProperty title;
    private final StringProperty description;
    private final ObjectProperty<LocalDate> dueDate;
    private final BooleanProperty isCompleted;
    private final ObjectProperty<Integer> customerId;

    public Task(int id, String title, String description, LocalDate dueDate, boolean isCompleted, Integer customerId) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.dueDate = new SimpleObjectProperty<>(dueDate);
        this.isCompleted = new SimpleBooleanProperty(isCompleted);
        this.customerId = new SimpleObjectProperty<>(customerId);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate.get();
    }

    public ObjectProperty<LocalDate> dueDateProperty() {
        return dueDate;
    }

    public boolean isCompleted() {
        return isCompleted.get();
    }

    public BooleanProperty isCompletedProperty() {
        return isCompleted;
    }

    public Integer getCustomerId() {
        return customerId.get();
    }

    public ObjectProperty<Integer> customerIdProperty() {
        return customerId;
    }
}


