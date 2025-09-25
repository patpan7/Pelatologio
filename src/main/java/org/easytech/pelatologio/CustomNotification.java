package org.easytech.pelatologio;

import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

public class CustomNotification {

    private String title = "";
    private String message = "";
    private Pos position = Pos.TOP_RIGHT;
    private Duration hideAfter = Duration.seconds(5);

    private CustomNotification() {}

    public static CustomNotification create() {
        return new CustomNotification();
    }

    public CustomNotification title(String title) {
        this.title = title;
        return this;
    }

    public CustomNotification text(String message) {
        this.message = message;
        return this;
    }

    public CustomNotification position(Pos position) {
        this.position = position;
        return this;
    }

    public CustomNotification hideAfter(Duration hideAfter) {
        this.hideAfter = hideAfter;
        return this;
    }

    public void showInfo() {
        show("info");
    }

    public void showError() {
        show("error");
    }

    public void showConfirmation() {
        show("confirmation");
    }

    public void showWarning() {
        show("warning");
    }

    private void show(String type) {
        try {
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("notification.fxml"));
            Parent root = loader.load();

            ImageView icon = (ImageView) root.lookup("#icon");
            Label titleLabel = (Label) root.lookup("#titleLabel");
            Label messageLabel = (Label) root.lookup("#messageLabel");

            titleLabel.setText(title);
            messageLabel.setText(message);

            String imagePath = "icons/" + type + ".png";
            try {
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                icon.setImage(image);
            } catch (Exception e) {
                System.err.println("Could not load image: " + imagePath);
            }

            root.getStyleClass().add(type);

            Scene scene = new Scene(root);
            scene.setFill(null);
            stage.setScene(scene);

            stage.show(); // Show before getting dimensions

            double stageWidth = stage.getWidth();
            double stageHeight = stage.getHeight();

            double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

            double x = switch (position.getHpos()) {
                case LEFT -> 10;
                case CENTER -> (screenWidth - stageWidth) / 2;
                case RIGHT -> screenWidth - stageWidth - 10;
                default -> screenWidth - stageWidth - 10; // Default to TOP_RIGHT
            };

            double y = switch (position.getVpos()) {
                case TOP -> 10;
                case CENTER -> (screenHeight - stageHeight) / 2;
                case BOTTOM -> screenHeight - stageHeight - 10;
                default -> 10; // Default to TOP_RIGHT
            };

            stage.setX(x);
            stage.setY(y);

            PauseTransition delay = new PauseTransition(hideAfter);
            delay.setOnFinished(event -> stage.close());
            delay.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}