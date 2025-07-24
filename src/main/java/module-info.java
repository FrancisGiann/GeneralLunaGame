module game.general_luna_game {
    requires javafx.controls;
    requires javafx.fxml;


    opens game.general_luna_game to javafx.fxml;
    exports game.general_luna_game;
}