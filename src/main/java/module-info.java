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
    requires net.sf.jasperreports.core;
    requires sikulixapi;
    requires com.sun.jna.platform;
    requires com.sun.jna;
    requires org.seleniumhq.selenium.support;
    requires atlantafx.base;
    requires com.jfoenix;
    requires org.apache.commons.lang3;
    //requires org.mnode.ical4j.core;
    requires log4j;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires jdk.httpserver;
    requires asterisk.java;
    requires playwright;

    opens org.easytech.pelatologio to javafx.fxml;
    exports org.easytech.pelatologio;
}