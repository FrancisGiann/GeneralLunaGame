package game.general_luna_game;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class HomeScreenController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button startGameButton;
    @FXML
    private Button leaderboardButton;
    @FXML
    private Button settingsButton;

    private StackPane settingsOverlay;
    private Slider bgmSlider;
    private Slider sfxSlider;
    private Label bgmValLabel;
    private Label sfxValLabel;

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

        if (settingsButton != null) {
            settingsButton.setOnAction(event -> showSettingsModal());
        }
    }

    private void showSettingsModal() {
        if (settingsOverlay == null) {
            createSettingsModal();
        }
        updateSettingsValues();
        settingsOverlay.setVisible(true);
        settingsOverlay.toFront();
    }

    private void hideSettingsModal() {
        if (settingsOverlay != null) {
            settingsOverlay.setVisible(false);
        }
    }

    private void createSettingsModal() {
        settingsOverlay = new StackPane();
        settingsOverlay.setPrefSize(1000, 800);
        settingsOverlay.setStyle("-fx-background-color: rgba(10, 14, 20, 0.85);");

        VBox modalBox = new VBox(16);
        modalBox.setAlignment(Pos.CENTER);
        modalBox.setPrefWidth(520);
        modalBox.setMaxWidth(520);
        modalBox.setPadding(new Insets(26, 32, 26, 32));
        modalBox.setStyle(
            "-fx-background-color: #1A232A; " +
            "-fx-border-color: #0cc0df; " +
            "-fx-border-width: 3px; " +
            "-fx-border-radius: 12px; " +
            "-fx-background-radius: 12px;"
        );

        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.85));
        dropShadow.setRadius(30);
        dropShadow.setOffsetY(10);
        modalBox.setEffect(dropShadow);

        Label title = new Label("⚙ SETTINGS & AUDIO");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#0cc0df"));

        Label subtitle = new Label("Configure BGM and SFX volume levels");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        subtitle.setTextFill(Color.web("#A8DADC"));

        // Controls container
        VBox controlsBox = new VBox(14);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(14, 18, 14, 18));
        controlsBox.setStyle(
            "-fx-background-color: #162029; " +
            "-fx-border-color: #2D3E4F; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px;"
        );

        // BGM Slider Row
        HBox bgmRow = new HBox(12);
        bgmRow.setAlignment(Pos.CENTER_LEFT);
        Label bgmLabel = new Label("Music (BGM):");
        bgmLabel.setPrefWidth(100);
        bgmLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        bgmLabel.setTextFill(Color.web("#CFD8DC"));

        bgmSlider = new Slider(0, 100, SoundManager.getInstance().getBgmVolume() * 100);
        bgmSlider.setPrefWidth(220);
        bgmValLabel = new Label((int)(SoundManager.getInstance().getBgmVolume() * 100) + "%");
        bgmValLabel.setPrefWidth(45);
        bgmValLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        bgmValLabel.setTextFill(Color.web("#0cc0df"));

        bgmSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue() / 100.0;
            SoundManager.getInstance().setBgmVolume(vol);
            bgmValLabel.setText(newVal.intValue() + "%");
        });
        bgmRow.getChildren().addAll(bgmLabel, bgmSlider, bgmValLabel);

        // SFX Slider Row
        HBox sfxRow = new HBox(12);
        sfxRow.setAlignment(Pos.CENTER_LEFT);
        Label sfxLabel = new Label("Sound Effects:");
        sfxLabel.setPrefWidth(100);
        sfxLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        sfxLabel.setTextFill(Color.web("#CFD8DC"));

        sfxSlider = new Slider(0, 100, SoundManager.getInstance().getSfxVolume() * 100);
        sfxSlider.setPrefWidth(220);
        sfxValLabel = new Label((int)(SoundManager.getInstance().getSfxVolume() * 100) + "%");
        sfxValLabel.setPrefWidth(45);
        sfxValLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        sfxValLabel.setTextFill(Color.web("#0cc0df"));

        sfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue() / 100.0;
            SoundManager.getInstance().setSfxVolume(vol);
            sfxValLabel.setText(newVal.intValue() + "%");
        });
        sfxRow.getChildren().addAll(sfxLabel, sfxSlider, sfxValLabel);

        // Test Sound Button
        Button testSfxBtn = new Button("🔊 TEST SOUND EFFECT");
        testSfxBtn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        testSfxBtn.setPrefWidth(220);
        testSfxBtn.setPrefHeight(30);
        testSfxBtn.setStyle(
            "-fx-background-color: #00838F; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: #4DD0E1; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 6px; " +
            "-fx-background-radius: 6px; " +
            "-fx-cursor: hand;"
        );
        testSfxBtn.setOnAction(e -> SoundManager.getInstance().playSfx("Shoot_Pistol"));

        controlsBox.getChildren().addAll(bgmRow, sfxRow, testSfxBtn);

        // Close Button
        Button closeBtn = new Button("CLOSE SETTINGS");
        closeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        closeBtn.setPrefWidth(260);
        closeBtn.setPrefHeight(38);
        closeBtn.setStyle(
            "-fx-background-color: #2E7D32; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: #81C784; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> hideSettingsModal());

        modalBox.getChildren().addAll(
            title,
            subtitle,
            controlsBox,
            closeBtn
        );

        settingsOverlay.getChildren().add(modalBox);
        settingsOverlay.setAlignment(Pos.CENTER);

        if (rootPane != null) {
            rootPane.getChildren().add(settingsOverlay);
        }
    }

    private void updateSettingsValues() {
        if (bgmSlider != null && sfxSlider != null) {
            double bgmVol = SoundManager.getInstance().getBgmVolume() * 100;
            double sfxVol = SoundManager.getInstance().getSfxVolume() * 100;
            bgmSlider.setValue(bgmVol);
            bgmValLabel.setText((int)bgmVol + "%");
            sfxSlider.setValue(sfxVol);
            sfxValLabel.setText((int)sfxVol + "%");
        }
    }

    private void playBackgroundMusic() {
        SoundManager.getInstance().playHomeScreenBgm();
    }

    public void stopBackgroundMusic() {
        SoundManager.getInstance().stopBgm();
    }

    public Button getStartGameButton() {
        return startGameButton;
    }

    public Button getLeaderboardButton() {
        return leaderboardButton;
    }

    public Button getSettingsButton() {
        return settingsButton;
    }

    public StackPane getSettingsOverlay() {
        return settingsOverlay;
    }

    public Slider getBgmSlider() {
        return bgmSlider;
    }

    public Slider getSfxSlider() {
        return sfxSlider;
    }
}