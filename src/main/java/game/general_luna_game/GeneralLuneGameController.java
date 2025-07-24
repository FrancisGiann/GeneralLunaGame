package game.general_luna_game;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class GameController {

    @FXML
    private Pane gamePane;

    private Character selectedCharacter;
    private GeneralLunaGame app;

    public void setGameApp(GeneralLunaGame app) {
        this.app = app;
    }
    public void setCharacter(Character character) {
        this.selectedCharacter = character;
        // Initialize game with this character
    }

    public void endGame(long finalTime) {
        try {
            app.showGameOver(finalTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
