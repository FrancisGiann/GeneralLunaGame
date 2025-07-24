package game.general_luna_game;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public class LeaderboardController {
    @FXML
    private ListView<String> leaderboardList;
    @FXML
    private Button mainMenuButton;

    private GeneralLunaGame app;
    private Leaderboard leaderboard;

    public void setGameApp(GeneralLunaGame app) {
        this.app = app;
    }

    public void setLeaderboard(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
        updateLeaderboardDisplay();
    }

    private void updateLeaderboardDisplay() {
        leaderboardList.getItems().clear();
        for (Leaderboard.ScoreEntry entry : leaderboard.getEntries()) {
            leaderboardList.getItems().add(entry.getName() + " - " + entry.getTime() + " seconds");
        }
    }

    @FXML
    public void initialize() {
        mainMenuButton.setOnAction(event -> {
            try {
                app.showHomeScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

