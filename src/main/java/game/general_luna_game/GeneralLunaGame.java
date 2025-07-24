package game.general_luna_game;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GeneralLunaGame extends Application {
    private Stage primaryStage;
    private Hero currentHero;
    private Game currentGame;
    private Leaderboard leaderboard = new Leaderboard();

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("General Lune Game");
        primaryStage.setResizable(false);
        showHomeScreen();
        primaryStage.show();
    }

    public void showHomeScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("HomeScreen.fxml"));
        Parent root = loader.load();
        HomeScreenController controller = loader.getController();
        controller.setGameApp(this);
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);

        if (currentGame != null) {
            currentGame.stopGame();
            Game.backgroundMusic.stop();
            currentGame = null;
        }
    }

    public void showCharacterSelection() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CharacterSelection.fxml"));
        Parent root = loader.load();
        CharacterSelectionController controller = loader.getController();
        controller.setGameApp(this);
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
    }

    public void showGameScreen(Hero hero) throws Exception {
        this.currentHero = hero;

        if (currentGame != null) {
            currentGame.resetGame();
        } else {
            currentGame = new Game(hero, this);
        }
        currentGame = new Game(hero, this);
        Scene scene = new Scene(currentGame, 1000, 800);
        primaryStage.setScene(scene);
        currentGame.initializeKeyHandling(scene);
        currentGame.requestFocus();

    }

    public void showLeaderboard() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Leaderboard.fxml"));
        Parent root = loader.load();
        LeaderboardController controller = loader.getController();
        controller.setGameApp(this);
        controller.setLeaderboard(leaderboard);
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        Game.backgroundMusic.stop();
    }

    public void addScoreToLeaderboard(String name, long time) {
        leaderboard.addEntry(name, time);
    }

    public void showGameOver(long finalTime) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameOverScreen.fxml"));
        Parent root = loader.load();
        GameOverController controller = loader.getController();
        controller.setGameApp(this);
        controller.setCurrentGame(currentGame);
        controller.setFinalTime(finalTime);
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
    }
    public void showVictoryScreen(long finalTime) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("VictoryScreen.fxml"));
        Parent root = loader.load();
        VictoryScreenController controller = loader.getController();
        controller.setGameApp(this);
        controller.setFinalTime(finalTime);
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
    }


    public Hero getCurrentHero() {
        return currentHero;
    }
    public Game getCurrentGame() {
        return currentGame;
    }


    public static void main(String[] args) {
        launch(args);
    }

}
