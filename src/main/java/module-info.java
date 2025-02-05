module org.easytech.pelatologio {
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.xml.ws;
    requires java.xml.bind;
    requires javax.jws;
    requires com.fasterxml.jackson.databind;
    requires java.sql;
    requires org.seleniumhq.selenium.chrome_driver;
    requires org.seleniumhq.selenium.firefox_driver;
    requires org.seleniumhq.selenium.edge_driver;
    requires dev.failsafe.core;
    requires java.mail;
    requires com.calendarfx.view;
    requires javafx.swing;

    opens org.easytech.pelatologio to javafx.fxml;
    exports org.easytech.pelatologio;
}