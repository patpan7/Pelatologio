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
    requires dev.failsafe.core;
    requires org.seleniumhq.selenium.edge_driver;
    requires java.desktop;
    requires java.mail;
    requires org.seleniumhq.selenium.devtools_v128;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires jacob;
    requires javax.websocket.api;


    opens org.easytech.pelatologio to javafx.fxml;
    exports org.easytech.pelatologio;
}