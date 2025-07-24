package game.general_luna_game;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;

public class VictoryScreenController {
    @FXML
    private Button leaderboardButton;
    @FXML
    private Button mainMenuButton;

    private GeneralLunaGame app;
    private long finalTime;

    public void setGameApp(GeneralLunaGame app) {
        this.app = app;
    }

    public void setFinalTime(long finalTime) {
        this.finalTime = finalTime;
    }

    @FXML
    public void initialize() {
        leaderboardButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add to Leaderboard");
            dialog.setHeaderText("Victory!");
            dialog.setContentText("Enter your name:");

            dialog.showAndWait().ifPresent(name -> {
                if (name.trim().isEmpty()) {
                    System.out.println("Name cannot be empty!");
                    return;
                }
                app.addScoreToLeaderboard(name, finalTime);
                try {
                    app.showLeaderboard();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
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
