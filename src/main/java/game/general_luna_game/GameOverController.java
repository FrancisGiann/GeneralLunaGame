package game.general_luna_game;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class GameOverController {
    @FXML
    private Label timeLabel;
    @FXML
    private Button retryButton;
    @FXML
    private Button mainMenuButton;
    private GeneralLunaGame app;
    private Game currentGame;

    public void setGameApp(GeneralLunaGame app) {
        this.app = app;
    }

    public void setCurrentGame(Game game) {
        this.currentGame = game;
    }

    public void setFinalTime(long finalTime) {
        timeLabel.setText("Time Survived: " + finalTime + " seconds");
    }

    @FXML
    public void initialize() {
        retryButton.setOnAction(event -> {
            try {
                if (currentGame != null) {
                    currentGame.resetGame();
                    app.showGameScreen(app.getCurrentHero());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        mainMenuButton.setOnAction(event -> {
            try {
                app.showHomeScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}