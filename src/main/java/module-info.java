module com.cryptosystem.prj_cryptosystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.logging;
    requires lombok;
    requires org.bouncycastle.provider;

    opens com.cryptosystem.prj_cryptosystem to javafx.fxml;
    exports com.cryptosystem.prj_cryptosystem;
}