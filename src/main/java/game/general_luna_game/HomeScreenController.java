package game.general_luna_game;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class HomeScreenController {
    private MediaPlayer bgm;

    @FXML
    private Button startGameButton;
    @FXML
    private Button leaderboardButton;

    private Stage primaryStage;
    private GeneralLunaGame app;

    public void setGameApp(GeneralLunaGame app) {
        this.app = app;
    }

    @FXML
    public void initialize() {
        playBackgroundMusic();
        startGameButton.setOnAction(event -> {
            stopBackgroundMusic();
            try {
                app.showCharacterSelection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        leaderboardButton.setOnAction(event -> {
            stopBackgroundMusic();
            try {
                app.showLeaderboard();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void playBackgroundMusic() {
        try {
            Media media = new Media(getClass().getResource("/sounds/homescreen_bgm.mp3").toExternalForm());
            bgm = new MediaPlayer(media);
            bgm.setCycleCount(MediaPlayer.INDEFINITE);
            bgm.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void stopBackgroundMusic() {
        if (bgm != null) {
            bgm.stop();
        }
    }
}