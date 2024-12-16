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
    //requires org.seleniumhq.selenium.devtools.v128;
    requires dev.failsafe.core;
    requires java.desktop;
    requires java.mail;
    requires jfxtras.controls;
    requires jfxtras.agenda;
    requires com.calendarfx.view;

    opens org.easytech.pelatologio to javafx.fxml;
    exports org.easytech.pelatologio;
}