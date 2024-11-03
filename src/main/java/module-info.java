module org.easytech.pelatologio {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.xml.ws;
    requires java.xml.bind;
    requires javax.jws;
    requires com.fasterxml.jackson.databind;
    requires java.sql;
    requires org.seleniumhq.selenium.api;
    requires org.seleniumhq.selenium.chrome_driver;

    opens org.easytech.pelatologio to javafx.fxml;
    exports org.easytech.pelatologio;
}