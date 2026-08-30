package game.general_luna_game;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ShopMenu extends StackPane {

    private final Game game;
    private Label titleLabel;
    private Label scoreLabel;
    private Label statusLabel;
    private Label stageClearLabel;

    // Health Card Labels & Button
    private Label healthLevelLabel;
    private Label healthStatLabel;
    private Label healthCostLabel;
    private Button healthUpgradeBtn;

    // Speed Card Labels & Button
    private Label speedLevelLabel;
    private Label speedStatLabel;
    private Label speedCostLabel;
    private Button speedUpgradeBtn;

    // Damage Card Labels & Button
    private Label damageLevelLabel;
    private Label damageStatLabel;
    private Label damageCostLabel;
    private Button damageUpgradeBtn;

    private Button continueBtn;

    public ShopMenu(Game game) {
        this.game = game;
        initializeUI();
    }

    private void initializeUI() {
        setPrefSize(1000, 800);
        setMaxSize(1000, 800);
        setStyle("-fx-background-color: rgba(10, 14, 20, 0.85);");

        VBox modalBox = new VBox(14);
        modalBox.setAlignment(Pos.CENTER);
        modalBox.setPrefWidth(780);
        modalBox.setMaxWidth(780);
        modalBox.setPadding(new Insets(22, 28, 22, 28));
        modalBox.setStyle(
            "-fx-background-color: #1A232A; " +
            "-fx-border-color: #D4AF37; " +
            "-fx-border-width: 3px; " +
            "-fx-border-radius: 12px; " +
            "-fx-background-radius: 12px;"
        );

        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.8));
        dropShadow.setRadius(25);
        dropShadow.setOffsetY(10);
        modalBox.setEffect(dropShadow);

        // Header
        stageClearLabel = new Label("★ STAGE 1 CLEARED! ★");
        stageClearLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        stageClearLabel.setTextFill(Color.web("#FFD700"));

        titleLabel = new Label("MILITARY SUPPLY & TACTICAL ARMORY");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.web("#A8DADC"));

        Label subtitle = new Label("Requisition reinforcements and upgrades for General Luna using your battle score!");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        subtitle.setTextFill(Color.web("#CFD8DC"));

        // Score Badge
        HBox scoreBox = new HBox(10);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setPadding(new Insets(6, 20, 6, 20));
        scoreBox.setStyle(
            "-fx-background-color: #2E2611; " +
            "-fx-border-color: #FFD700; " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-radius: 20px; " +
            "-fx-background-radius: 20px;"
        );
        scoreLabel = new Label("💰 AVAILABLE SCORE: 0 PTS");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        scoreLabel.setTextFill(Color.web("#FFE082"));
        scoreBox.getChildren().add(scoreLabel);

        // Upgrade Cards Container
        HBox cardsBox = new HBox(15);
        cardsBox.setAlignment(Pos.CENTER);

        VBox healthCard = createHealthCard();
        VBox speedCard = createSpeedCard();
        VBox damageCard = createDamageCard();

        cardsBox.getChildren().addAll(healthCard, speedCard, damageCard);

        // Status / Feedback message
        statusLabel = new Label("Select an upgrade to strengthen your forces.");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        statusLabel.setTextFill(Color.web("#81C784"));

        // Continue Button
        continueBtn = new Button("DEPLOY TO NEXT STAGE ▶");
        continueBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        continueBtn.setPrefWidth(320);
        continueBtn.setPrefHeight(44);
        continueBtn.setStyle(
            "-fx-background-color: #2E7D32; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: #81C784; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-cursor: hand;"
        );
        continueBtn.setOnMouseEntered(e -> continueBtn.setStyle(
            "-fx-background-color: #388E3C; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: #A5D6A7; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-cursor: hand;"
        ));
        continueBtn.setOnMouseExited(e -> continueBtn.setStyle(
            "-fx-background-color: #2E7D32; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: #81C784; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-cursor: hand;"
        ));
        continueBtn.setOnAction(e -> game.closeShopAndNextStage());

        modalBox.getChildren().addAll(
            stageClearLabel,
            titleLabel,
            subtitle,
            scoreBox,
            cardsBox,
            statusLabel,
            continueBtn
        );

        getChildren().add(modalBox);
        setAlignment(Pos.CENTER);
        setVisible(false);
    }

    private VBox createCardBase(String title, String titleColor) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(225);
        card.setMinWidth(225);
        card.setPadding(new Insets(14, 12, 14, 12));
        card.setStyle(
            "-fx-background-color: #24303C; " +
            "-fx-border-color: #3E5165; " +
            "-fx-border-width: 2px; " +
            "-fx-border-radius: 10px; " +
            "-fx-background-radius: 10px;"
        );

        Label cardHeader = new Label(title);
        cardHeader.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        cardHeader.setTextFill(Color.web(titleColor));

        card.getChildren().add(cardHeader);
        return card;
    }

    private Button createUpgradeButton() {
        Button btn = new Button("UPGRADE");
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setPrefWidth(190);
        btn.setPrefHeight(34);
        btn.setStyle(
            "-fx-background-color: #00838F; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: #4DD0E1; " +
            "-fx-border-width: 1.5px; " +
            "-fx-border-radius: 6px; " +
            "-fx-background-radius: 6px; " +
            "-fx-cursor: hand;"
        );
        return btn;
    }

    private VBox createHealthCard() {
        VBox card = createCardBase("❤️ MAX HEALTH", "#FF6B6B");

        Label desc = new Label("+25 Max HP & instant +25 HP recovery.");
        desc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        desc.setTextFill(Color.web("#B0BEC5"));
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);

        healthLevelLabel = new Label("Level: Lvl 0");
        healthLevelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        healthLevelLabel.setTextFill(Color.web("#E0E0E0"));

        healthStatLabel = new Label("Current: 100 Max HP");
        healthStatLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        healthStatLabel.setTextFill(Color.web("#81C784"));

        healthCostLabel = new Label("Cost: 150 PTS");
        healthCostLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        healthCostLabel.setTextFill(Color.web("#FFD54F"));

        healthUpgradeBtn = createUpgradeButton();
        healthUpgradeBtn.setOnAction(e -> {
            boolean success = game.buyHealthUpgrade();
            if (success) {
                statusLabel.setTextFill(Color.web("#81C784"));
                statusLabel.setText("✔ Upgraded Max Health (+25 Max HP & +25 HP Healed)!");
                refresh();
            } else {
                statusLabel.setTextFill(Color.web("#EF5350"));
                statusLabel.setText("✖ Insufficient Score to upgrade Max Health!");
            }
        });

        card.getChildren().addAll(desc, healthLevelLabel, healthStatLabel, healthCostLabel, healthUpgradeBtn);
        return card;
    }

    private VBox createSpeedCard() {
        VBox card = createCardBase("⚡ MOVEMENT SPEED", "#4DD0E1");

        Label desc = new Label("+1 Tactical movement and dodging speed.");
        desc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        desc.setTextFill(Color.web("#B0BEC5"));
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);

        speedLevelLabel = new Label("Level: Lvl 0");
        speedLevelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        speedLevelLabel.setTextFill(Color.web("#E0E0E0"));

        speedStatLabel = new Label("Current: 4 Speed");
        speedStatLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        speedStatLabel.setTextFill(Color.web("#81C784"));

        speedCostLabel = new Label("Cost: 200 PTS");
        speedCostLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        speedCostLabel.setTextFill(Color.web("#FFD54F"));

        speedUpgradeBtn = createUpgradeButton();
        speedUpgradeBtn.setOnAction(e -> {
            boolean success = game.buySpeedUpgrade();
            if (success) {
                statusLabel.setTextFill(Color.web("#81C784"));
                statusLabel.setText("✔ Upgraded Movement Speed (+1 SPD)!");
                refresh();
            } else {
                statusLabel.setTextFill(Color.web("#EF5350"));
                statusLabel.setText("✖ Insufficient Score to upgrade Speed!");
            }
        });

        card.getChildren().addAll(desc, speedLevelLabel, speedStatLabel, speedCostLabel, speedUpgradeBtn);
        return card;
    }

    private VBox createDamageCard() {
        VBox card = createCardBase("💥 WEAPON DAMAGE", "#FFA726");

        Label desc = new Label("+15 Weapon attack damage per bullet.");
        desc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        desc.setTextFill(Color.web("#B0BEC5"));
        desc.setWrapText(true);
        desc.setAlignment(Pos.CENTER);

        damageLevelLabel = new Label("Level: Lvl 0");
        damageLevelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        damageLevelLabel.setTextFill(Color.web("#E0E0E0"));

        damageStatLabel = new Label("Current: 50 DMG");
        damageStatLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        damageStatLabel.setTextFill(Color.web("#81C784"));

        damageCostLabel = new Label("Cost: 250 PTS");
        damageCostLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        damageCostLabel.setTextFill(Color.web("#FFD54F"));

        damageUpgradeBtn = createUpgradeButton();
        damageUpgradeBtn.setOnAction(e -> {
            boolean success = game.buyDamageUpgrade();
            if (success) {
                statusLabel.setTextFill(Color.web("#81C784"));
                statusLabel.setText("✔ Upgraded Weapon Damage (+15 DMG)!");
                refresh();
            } else {
                statusLabel.setTextFill(Color.web("#EF5350"));
                statusLabel.setText("✖ Insufficient Score to upgrade Damage!");
            }
        });

        card.getChildren().addAll(desc, damageLevelLabel, damageStatLabel, damageCostLabel, damageUpgradeBtn);
        return card;
    }

    public void show(int completedStage) {
        stageClearLabel.setText("★ STAGE " + completedStage + " CLEARED! ★");
        if (completedStage == 4) {
            continueBtn.setText("DEPLOY TO FINAL BOSS WAVE ⚔");
            continueBtn.setStyle(
                "-fx-background-color: #C62828; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: #EF5350; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand;"
            );
        } else {
            continueBtn.setText("DEPLOY TO STAGE " + (completedStage + 1) + " ▶");
            continueBtn.setStyle(
                "-fx-background-color: #2E7D32; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: #81C784; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-cursor: hand;"
            );
        }
        statusLabel.setTextFill(Color.web("#81C784"));
        statusLabel.setText("Requisition tactical upgrades before the next offensive wave!");
        refresh();
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    public void refresh() {
        if (game == null || game.getPlayer() == null) return;

        int score = game.getScore();
        scoreLabel.setText("💰 AVAILABLE SCORE: " + score + " PTS");

        // Health
        int hLevel = game.getHealthUpgradeLevel();
        int hCost = game.getHealthUpgradeCost();
        healthLevelLabel.setText("Level: Lvl " + hLevel);
        healthStatLabel.setText("Current: " + (int)game.getPlayer().getMaxHealth() + " Max HP (HP: " + game.getPlayer().getHealth() + ")");
        healthCostLabel.setText("Cost: " + hCost + " PTS");
        updateButtonState(healthUpgradeBtn, score >= hCost);

        // Speed
        int sLevel = game.getSpeedUpgradeLevel();
        int sCost = game.getSpeedUpgradeCost();
        speedLevelLabel.setText("Level: Lvl " + sLevel);
        speedStatLabel.setText("Current: " + game.getNormalSpeed() + " Speed");
        speedCostLabel.setText("Cost: " + sCost + " PTS");
        updateButtonState(speedUpgradeBtn, score >= sCost);

        // Damage
        int dLevel = game.getDamageUpgradeLevel();
        int dCost = game.getDamageUpgradeCost();
        damageLevelLabel.setText("Level: Lvl " + dLevel);
        damageStatLabel.setText("Current: " + game.getPlayer().getDamage() + " DMG");
        damageCostLabel.setText("Cost: " + dCost + " PTS");
        updateButtonState(damageUpgradeBtn, score >= dCost);
    }

    private void updateButtonState(Button btn, boolean canAfford) {
        if (canAfford) {
            btn.setStyle(
                "-fx-background-color: #00838F; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: #4DD0E1; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 6px; " +
                "-fx-background-radius: 6px; " +
                "-fx-cursor: hand;"
            );
        } else {
            btn.setStyle(
                "-fx-background-color: #37474F; " +
                "-fx-text-fill: #90A4AE; " +
                "-fx-border-color: #546E7A; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 6px; " +
                "-fx-background-radius: 6px; " +
                "-fx-cursor: hand;"
            );
        }
    }

    public Label getScoreLabel() {
        return scoreLabel;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public Button getHealthUpgradeBtn() {
        return healthUpgradeBtn;
    }

    public Button getSpeedUpgradeBtn() {
        return speedUpgradeBtn;
    }

    public Button getDamageUpgradeBtn() {
        return damageUpgradeBtn;
    }

    public Button getContinueBtn() {
        return continueBtn;
    }
}
