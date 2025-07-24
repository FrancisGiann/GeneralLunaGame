module game.general_luna_game {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;


    opens game.general_luna_game to javafx.fxml;
    exports game.general_luna_game;
}