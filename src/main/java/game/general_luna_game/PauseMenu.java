package game.general_luna_game;

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

public class PauseMenu extends StackPane {

    private final Game game;
    private Label titleLabel;
    private Label missionInfoLabel;
    private Slider bgmSlider;
    private Slider sfxSlider;
    private Label bgmValueLabel;
    private Label sfxValueLabel;
    private Button resumeBtn;
    private Button restartBtn;
    private Button quitBtn;

    public PauseMenu(Game game) {
        this.game = game;
        initializeUI();
    }

    private void initializeUI() {
        setPrefSize(1000, 800);
        setMaxSize(1000, 800);
        setStyle("-fx-background-color: rgba(10, 14, 20, 0.85);");

        VBox modalBox = new VBox(16);
        modalBox.setAlignment(Pos.CENTER);
        modalBox.setPrefWidth(550);
        modalBox.setMaxWidth(550);
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

        // Header
        titleLabel = new Label("⏸ GAME PAUSED");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.web("#0cc0df"));

        Label subtitle = new Label("Tactical Operations Suspended");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        subtitle.setTextFill(Color.web("#A8DADC"));

        // Mission Info Box
        VBox infoBox = new VBox(6);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(10, 16, 10, 16));
        infoBox.setStyle(
            "-fx-background-color: #24303C; " +
            "-fx-border-color: #3E5165; " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px;"
        );

        missionInfoLabel = new Label("Stage: 1 | Score: 0 | Time: 00:00");
        missionInfoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        missionInfoLabel.setTextFill(Color.web("#FFE082"));
        infoBox.getChildren().add(missionInfoLabel);

        // Volume Controls Section
        VBox audioBox = new VBox(10);
        audioBox.setAlignment(Pos.CENTER);
        audioBox.setPadding(new Insets(12, 16, 12, 16));
        audioBox.setStyle(
            "-fx-background-color: #162029; " +
            "-fx-border-color: #2D3E4F; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px;"
        );

        Label audioTitle = new Label("AUDIO SETTINGS");
        audioTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        audioTitle.setTextFill(Color.web("#81C784"));

        // BGM Slider row
        HBox bgmRow = new HBox(12);
        bgmRow.setAlignment(Pos.CENTER_LEFT);
        Label bgmLabel = new Label("BGM Volume:");
        bgmLabel.setPrefWidth(90);
        bgmLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        bgmLabel.setTextFill(Color.web("#CFD8DC"));

        bgmSlider = new Slider(0, 100, SoundManager.getInstance().getBgmVolume() * 100);
        bgmSlider.setPrefWidth(240);
        bgmValueLabel = new Label((int)(SoundManager.getInstance().getBgmVolume() * 100) + "%");
        bgmValueLabel.setPrefWidth(45);
        bgmValueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        bgmValueLabel.setTextFill(Color.web("#0cc0df"));

        bgmSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue() / 100.0;
            SoundManager.getInstance().setBgmVolume(vol);
            bgmValueLabel.setText(newVal.intValue() + "%");
        });
        bgmRow.getChildren().addAll(bgmLabel, bgmSlider, bgmValueLabel);

        // SFX Slider row
        HBox sfxRow = new HBox(12);
        sfxRow.setAlignment(Pos.CENTER_LEFT);
        Label sfxLabel = new Label("SFX Volume:");
        sfxLabel.setPrefWidth(90);
        sfxLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        sfxLabel.setTextFill(Color.web("#CFD8DC"));

        sfxSlider = new Slider(0, 100, SoundManager.getInstance().getSfxVolume() * 100);
        sfxSlider.setPrefWidth(240);
        sfxValueLabel = new Label((int)(SoundManager.getInstance().getSfxVolume() * 100) + "%");
        sfxValueLabel.setPrefWidth(45);
        sfxValueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        sfxValueLabel.setTextFill(Color.web("#0cc0df"));

        sfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue() / 100.0;
            SoundManager.getInstance().setSfxVolume(vol);
            sfxValueLabel.setText(newVal.intValue() + "%");
        });
        sfxRow.getChildren().addAll(sfxLabel, sfxSlider, sfxValueLabel);

        audioBox.getChildren().addAll(audioTitle, bgmRow, sfxRow);

        // Action Buttons
        VBox buttonsBox = new VBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        resumeBtn = createStyledButton("RESUME (ESC)", "#2E7D32", "#81C784", "#388E3C");
        resumeBtn.setOnAction(e -> game.resumeGame());

        restartBtn = createStyledButton("RESTART MISSION", "#00838F", "#4DD0E1", "#0097A7");
        restartBtn.setOnAction(e -> {
            hide();
            game.resetGame();
        });

        quitBtn = createStyledButton("QUIT TO MAIN MENU", "#C62828", "#EF5350", "#D32F2F");
        quitBtn.setOnAction(e -> game.quitToMainMenu());

        buttonsBox.getChildren().addAll(resumeBtn, restartBtn, quitBtn);

        modalBox.getChildren().addAll(
            titleLabel,
            subtitle,
            infoBox,
            audioBox,
            buttonsBox
        );

        getChildren().add(modalBox);
        setAlignment(Pos.CENTER);
        setVisible(false);
    }

    private Button createStyledButton(String text, String bgColor, String borderColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setPrefWidth(340);
        btn.setPrefHeight(40);
        String normalStyle = String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-border-color: %s; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;",
            bgColor, borderColor
        );
        String hoverStyle = String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-border-color: %s; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;",
            hoverColor, borderColor
        );
        btn.setStyle(normalStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        return btn;
    }

    public void show() {
        if (game != null) {
            long elapsedTime = game.getElapsedTime();
            String timeStr = String.format("%02d:%02d", elapsedTime / 60, elapsedTime % 60);
            int hp = game.getPlayer() != null ? (int)game.getPlayer().getHealth() : 0;
            int maxHp = game.getPlayer() != null ? (int)game.getPlayer().getMaxHealth() : 0;
            missionInfoLabel.setText(String.format("Stage: %d | Score: %d | Time: %s | HP: %d/%d",
                game.getCurrentStage(), game.getScore(), timeStr, hp, maxHp));
        }

        // Sync volume sliders
        double bgmVol = SoundManager.getInstance().getBgmVolume() * 100;
        double sfxVol = SoundManager.getInstance().getSfxVolume() * 100;
        bgmSlider.setValue(bgmVol);
        bgmValueLabel.setText((int)bgmVol + "%");
        sfxSlider.setValue(sfxVol);
        sfxValueLabel.setText((int)sfxVol + "%");

        setVisible(true);
        toFront();
    }

    public void hide() {
        setVisible(false);
    }

    public Slider getBgmSlider() {
        return bgmSlider;
    }

    public Slider getSfxSlider() {
        return sfxSlider;
    }

    public Button getResumeBtn() {
        return resumeBtn;
    }

    public Button getRestartBtn() {
        return restartBtn;
    }

    public Button getQuitBtn() {
        return quitBtn;
    }

    public Label getMissionInfoLabel() {
        return missionInfoLabel;
    }
}
