package game.general_luna_game;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.media.AudioClip;
import javafx.scene.image.Image;
import java.awt.Point;
import java.io.InputStream;
import java.util.*;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Game extends Pane {

    private Hero player;
    private List<Enemy> enemies;
    private List<Projectile> projectiles;
    private AnimationTimer gameTimer;
    private Point mousePosition;
    private int currentStage = 1;
    private long startTime;
    private Set<KeyCode> activeKeys;
    private Image[] mapBackgrounds;
    private int playerSpeed = 4;
    private GeneralLunaGame gameFrame;
    private Canvas gameCanvas;
    private GraphicsContext gc;
    private Scene gameScene;
    private static final int ENEMY_DAMAGE = 5;
    private static final double DAMAGE_COOLDOWN = 1.0;
    private Map<Enemy, Long> lastDamageTime;
    private static final double ENEMY_SPACING = 30.0;
    private boolean isMousePressed = false;
    private long lastShotTime = 0;
    private static final double PISTOL_COOLDOWN = 0.9;
    private static final double RIFLE_COOLDOWN = 0.15;
    private static final double DEFAULT_COOLDOWN = 0.5;
    private static final double PISTOL_SPEED = 8.0;
    private static final double RIFLE_SPEED = 6.0;
    private static final double DEFAULT_SPEED = 5.0;
    private int currentAmmo;
    private int maxAmmo;
    private boolean isReloading = false;
    private long reloadStartTime = 0;
    private static final double RELOAD_TIME = 2.0;
    private static final double SPEED_BOOST_MULTIPLIER = 1.5;
    private int normalSpeed = 4;
    private static final int BOSS_HEALTH_BASE = 500;
    private static final int BOSS_DAMAGE = 15;
    private Boss bossPrefab = null;
    private Image bossImage;
    private boolean gameWon = false;
    private boolean isGameOver = false;
    private Map<String, AudioClip> soundEffects;
    public static MediaPlayer backgroundMusic;




    public Game(Hero hero, GeneralLunaGame gameFrame) {
        this.gameFrame = gameFrame;
        this.player = hero;
        this.enemies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.mousePosition = new Point(0, 0);
        this.activeKeys = new HashSet<>();
        this.lastDamageTime = new HashMap<>();

        gameCanvas = new Canvas(1000, 800);
        gc = gameCanvas.getGraphicsContext2D();
        getChildren().add(gameCanvas);


        startTime = System.currentTimeMillis();
        setupGameLoop();
        loadMapBackgrounds();
        spawnEnemies();
        initializeAmmo();
        loadSoundEffects();
        loadBackgroundMusic();
        if (backgroundMusic != null) {
            backgroundMusic.play();
        }

        this.setOnKeyPressed(this::handleKeyPressed);
        this.setOnKeyReleased(this::handleKeyReleased);
        this.requestFocus();
        gameCanvas.setOnMouseMoved(this::handleMouseMoved);
        gameCanvas.setOnMouseDragged(this::handleMouseMoved);
        gameCanvas.setOnMousePressed(event -> {
            isMousePressed = true;
            handleMouseMoved(event);
        });
        gameCanvas.setOnMouseReleased(event -> {
            isMousePressed = false;
        });

    }
    private void setupGameLoop() {
        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
                renderGame();
            }
        };
        gameTimer.start();
    }
    private void loadSoundEffects() {
        soundEffects = new HashMap<>();
        try {
            soundEffects.put("Shoot_Pistol", new AudioClip(getClass().getResource("/sounds/pistol.mp3").toExternalForm()));
            soundEffects.put("Shoot_Rifle", new AudioClip(getClass().getResource("/sounds/rifle.mp3").toExternalForm()));
            soundEffects.put("Enemy_Hit", new AudioClip(getClass().getResource("/sounds/enemyhit.mp3").toExternalForm()));
            soundEffects.put("Player_Hit", new AudioClip(getClass().getResource("/sounds/playerhit.mp3").toExternalForm()));
        } catch (Exception e) {
            System.err.println("Could not load sound effects: " + e.getMessage());
        }
    }
    private void loadBackgroundMusic() {
        try {
            Media media = new Media(getClass().getResource("/sounds/bgm.mp3").toExternalForm());
            backgroundMusic = new MediaPlayer(media);
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusic.setVolume(0.3);
        } catch (Exception e) {
            System.err.println("Could not load background music: " + e.getMessage());
        }
    }
    private void initializeAmmo() {
        switch (player.getCharacterType()) {
            case "Pistol":
                maxAmmo = 8;
                break;
            case "Rifle":
                maxAmmo = 30;
                break;
            default:
                maxAmmo = 15;
        }
        currentAmmo = maxAmmo;
    }
    private void startReload() {
        if (!isReloading) {
            isReloading = true;
            reloadStartTime = System.currentTimeMillis();
            playerSpeed = (int)(normalSpeed * SPEED_BOOST_MULTIPLIER);
        }
    }

    private void updateGame() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = (currentTime - startTime) / 1000;

        if (activeKeys.contains(KeyCode.W)) player.moveUp(playerSpeed);
        if (activeKeys.contains(KeyCode.S)) player.moveDown(playerSpeed);
        if (activeKeys.contains(KeyCode.A)) player.moveLeft(playerSpeed);
        if (activeKeys.contains(KeyCode.D)) player.moveRight(playerSpeed);

        if (bossPrefab != null) {
            bossPrefab.update(player);
            bossPrefab.shootProjectiles(player);

            for (Projectile projectile : new ArrayList<>(bossPrefab.getBossProjectiles())) {
                projectile.update();

                if (Math.abs(projectile.x - player.x) < 20 && Math.abs(projectile.y - player.y) < 20) {
                    player.takeDamage(BOSS_DAMAGE);
                    bossPrefab.getBossProjectiles().remove(projectile);
                    checkGameOver();
                }

                if (projectile.x < 0 || projectile.x > gameCanvas.getWidth() ||
                        projectile.y < 0 || projectile.y > gameCanvas.getHeight()) {
                    bossPrefab.getBossProjectiles().remove(projectile);
                }
            }
        }

        if (isReloading) {
            if (currentTime - reloadStartTime >= RELOAD_TIME * 1000) {
                isReloading = false;
                currentAmmo = maxAmmo;
                playerSpeed = normalSpeed;
            } else {
                playerSpeed = (int)(normalSpeed * SPEED_BOOST_MULTIPLIER);
            }
        }

        if (isMousePressed && mousePosition != null && !isReloading) {
            if (currentAmmo > 0) {
                double cooldown = getWeaponCooldown();
                if (currentTime - lastShotTime >= cooldown * 1000) {
                    shoot();
                    currentAmmo--;
                    lastShotTime = currentTime;
                    if (currentAmmo == 0) {
                        startReload();
                    }
                }
            } else if (!isReloading) {
                startReload();
            }
        }

        for (Enemy enemy : new ArrayList<>(enemies)) {
            double dx = player.x - enemy.x;
            double dy = player.y - enemy.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                dx /= distance;
                dy /= distance;
            }

            double separationX = 0;
            double separationY = 0;

            for (Enemy other : enemies) {
                if (other != enemy) {
                    double offsetX = enemy.x - other.x;
                    double offsetY = enemy.y - other.y;
                    double dist = Math.sqrt(offsetX * offsetX + offsetY * offsetY);

                    if (dist < ENEMY_SPACING && dist > 0) {
                        double force = (ENEMY_SPACING - dist) / ENEMY_SPACING;
                        separationX += (offsetX / dist) * force;
                        separationY += (offsetY / dist) * force;
                    }
                }
            }

            double separationLength = Math.sqrt(separationX * separationX + separationY * separationY);
            if (separationLength > 0) {
                separationX /= separationLength;
                separationY /= separationLength;
            }

            double finalDx = (dx * 0.7 + separationX * 0.3) * enemy.speed;
            double finalDy = (dy * 0.7 + separationY * 0.3) * enemy.speed;

            enemy.x += finalDx;
            enemy.y += finalDy;

            enemy.x = Math.min(Math.max(enemy.x, 0), gameCanvas.getWidth() - 20);
            enemy.y = Math.min(Math.max(enemy.y, 0), gameCanvas.getHeight() - 20);

            if (Math.abs(enemy.x - player.x) < 10 && Math.abs(enemy.y - player.y) < 10) {
                Long lastDamage = lastDamageTime.getOrDefault(enemy, 0L);
                if (currentTime - lastDamage >= DAMAGE_COOLDOWN * 1000) {
                    player.takeDamage(ENEMY_DAMAGE);
                    lastDamageTime.put(enemy, currentTime);
                    checkGameOver();
                }
            }
        }


        updateProjectiles();

        if (enemies.isEmpty()) {
            advanceToNextStage();
        }
        if (bossPrefab != null && bossPrefab.getHealth() <= 0) {
            Victory();
        }
    }
    private void advanceToNextStage() {
        currentStage++;
        spawnEnemies();
    }

    private void updateProjectiles() {
        for (Projectile projectile : new ArrayList<>(projectiles)) {
            projectile.update();
            for (Enemy enemy : new ArrayList<>(enemies)) {
                double hitboxWidth = enemy instanceof Boss ? 100 : 20;
                double hitboxHeight = enemy instanceof Boss ? 100 : 20;

                if (Math.abs(projectile.x - enemy.x) < hitboxWidth / 2 &&
                        Math.abs(projectile.y - enemy.y) < hitboxHeight / 2) {
                    enemy.takeDamage(player.getDamage());
                    projectiles.remove(projectile);
                    if (enemy.getHealth() <= 0) {
                        enemies.remove(enemy);
                        lastDamageTime.remove(enemy);
                    }
                    break;
                }
            }

            if (projectile.x < 0 || projectile.x > gameCanvas.getWidth() || projectile.y < 0 || projectile.y > gameCanvas.getHeight()) {
                projectiles.remove(projectile);
            }
        }
    }
    private double getWeaponCooldown() {
        switch (player.getCharacterType()) {
            case "Pistol":
                return PISTOL_COOLDOWN;
            case "Rifle":
                return RIFLE_COOLDOWN;
            default:
                return DEFAULT_COOLDOWN;
        }
    }

    private double getProjectileSpeed() {
        switch (player.getCharacterType()) {
            case "Pistol":
                return PISTOL_SPEED;
            case "Rifle":
                return RIFLE_SPEED;
            default:
                return DEFAULT_SPEED;
        }
    }
    private void shoot() {
        if (mousePosition == null) return;

        double dx = mousePosition.getX() - (player.x + 15);
        double dy = mousePosition.getY() - (player.y + 15);
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            dx /= distance;
            dy /= distance;

            double speed = getProjectileSpeed();

            Projectile projectile = new Projectile(
                    player.x + 15,
                    player.y + 15,
                    dx * speed,
                    dy * speed,
                    player.getDamage()
            );

            projectile.weaponType = player.getCharacterType();
            projectiles.add(projectile);
        }
        if (soundEffects != null) {
            AudioClip shootSound = soundEffects.get("Shoot_" + player.getCharacterType());
            if (shootSound != null) {
                shootSound.play(0.3);
            }
        }
    }

    private void renderGame() {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        if (mapBackgrounds != null && mapBackgrounds[0] != null) {
            gc.drawImage(mapBackgrounds[0], 0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        } else {
            gc.setFill(Color.GRAY);
            gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        }
        player.draw(gc);
        drawPlayerUI();
        drawEnhancedEnemies();
        drawProjectiles();
    }

    private void loadMapBackgrounds() {
        mapBackgrounds = new Image[1];
        InputStream is = getClass().getResourceAsStream("/images/map1.jpg");
        mapBackgrounds[0] = new Image(is);

    }

    private void spawnEnemies() {
        if (currentStage == 5 && bossPrefab == null) {
            if (bossImage == null) {
                try {
                    InputStream is = getClass().getResourceAsStream("/images/Boss.gif");
                    if (is != null) {
                        bossImage = new Image(is);
                    } else {
                        System.err.println("Boss image not found!");
                    }
                } catch (Exception e) {
                    System.err.println("Error loading boss image: " + e.getMessage());
                }
            }

            int bossX = (1200);
            int bossY = (800);

            bossPrefab = new Boss(bossX, bossY, bossImage);
            enemies.add(bossPrefab);
            return;
        }

        int baseEnemyCount = 5;
        int enemyCount = baseEnemyCount + (currentStage - 1) * 2;

        for (int i = 0; i < enemyCount; i++) {
            double angle = (Math.PI * 2 * i) / enemyCount;
            double distance = 250 + Math.random() * 100;
            int enemyX = (int) (player.x + Math.cos(angle) * distance);
            int enemyY = (int) (player.y + Math.sin(angle) * distance);

            enemyX = Math.min(Math.max(enemyX, 0), (int)gameCanvas.getWidth() - 20);
            enemyY = Math.min(Math.max(enemyY, 0), (int)gameCanvas.getHeight() - 20);

            if (currentStage != 5) {
                Enemy enemy = new Enemy(enemyX, enemyY);
                enemy.speed = 1.5 + (currentStage - 1) * 0.15;
                enemy.health = 30 + (currentStage - 1) * 10;
                enemies.add(enemy);
                lastDamageTime.put(enemy, 0L);
            }
        }
    }

    public void resetGame() {
        player.resetHealth();
        enemies.clear();
        projectiles.clear();
        spawnEnemies();
        activeKeys.clear();
        bossPrefab = null;
        isGameOver = false;

        currentStage = 1;
        startTime = System.currentTimeMillis();
        player.x = (int) (gameCanvas.getWidth() / 2);
        player.y = (int) (gameCanvas.getHeight() / 2);

        if (gameTimer != null) {
            gameTimer.start();
        }
        initializeAmmo();
        isReloading = false;
        playerSpeed = normalSpeed;

        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.play();
        }
    }

    public void stopGame() {
        gameTimer.stop();
    }
    private void checkGameOver() {
        if (gameWon) {
            return;
        }
        if (player.getHealth() <= 0 && gameTimer != null && !isGameOver) {
            isGameOver = true;
            stopGame();
            try {
                gameFrame.showGameOver((System.currentTimeMillis() - startTime) / 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void Victory() {
        gameWon = true;
        stopGame();
        try {
            gameFrame.showVictoryScreen((System.currentTimeMillis() - startTime) / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawPlayerUI() {

        double maxHealth = player.getMaxHealth();
        double currentHealth = player.getHealth();

        double healthBarWidth = 200;
        double healthBarHeight = 20;
        double x = 10;
        double y = 10;
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRoundRect(x, y, healthBarWidth, healthBarHeight, 10, 10);


        double healthPercentage = Math.max(0, Math.min(1.0, currentHealth / maxHealth));
        gc.setFill(new LinearGradient(0, 0, healthPercentage, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.RED), new Stop(1, Color.DARKRED)));
        gc.fillRoundRect(x, y, healthBarWidth * healthPercentage, healthBarHeight, 10, 10);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, healthBarWidth, healthBarHeight, 10, 10);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("HP: " + (int)currentHealth, x + healthBarWidth / 2 - 20, y + 15);
        double cooldownWidth = 200;
        double cooldownHeight = 8;
        double cooldownY = y + healthBarHeight + 5;
        gc.setFill(Color.rgb(50, 50, 50, 0.8));
        gc.fillRoundRect(x, cooldownY, cooldownWidth, cooldownHeight, 5, 5);
        if (isReloading) {
            double reloadProgress = Math.min(1.0,
                    (System.currentTimeMillis() - reloadStartTime) / (RELOAD_TIME * 1000));
            gc.setFill(Color.rgb(255, 165, 0, 0.8));
            gc.fillRoundRect(x, cooldownY, cooldownWidth * reloadProgress, cooldownHeight, 5, 5);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText("RELOADING", x + cooldownWidth + 5, cooldownY + cooldownHeight - 2);
        } else {
            //cooldown display
            double cooldownPercent = Math.min(1.0,
                    (System.currentTimeMillis() - lastShotTime) / (getWeaponCooldown() * 1000));
            gc.setFill(Color.rgb(255, 255, 0, 0.8));
            gc.fillRoundRect(x, cooldownY, cooldownWidth * cooldownPercent, cooldownHeight, 5, 5);

            // ready indicator
            if (cooldownPercent >= 1.0 && currentAmmo > 0) {
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                gc.fillText("Ready!", x + cooldownWidth + 5, cooldownY + cooldownHeight - 2);
            }
        }
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, cooldownY, cooldownWidth, cooldownHeight, 5, 5);
        double ammoX = x;
        double ammoY = cooldownY + cooldownHeight + 15;
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRoundRect(ammoX, ammoY, 100, 30, 10, 10);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText(currentAmmo + " / " + maxAmmo, ammoX + 20, ammoY + 20);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText("Weapon: " + player.getCharacterType(), x, ammoY + 45);

        if (isReloading) {
            gc.setFill(Color.YELLOW);
            gc.fillText("SPEED BOOST ACTIVE", x, ammoY + 70);
        }

        double rightAlign = gameCanvas.getWidth() - 150;
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRoundRect(rightAlign, 10, 140, 90, 10, 10);

        gc.setFill(Color.WHITE);
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        String timeStr = String.format("%02d:%02d", elapsedTime / 60, elapsedTime % 60);
        gc.fillText("Stage: " + currentStage, rightAlign + 10, 30);
        gc.fillText("Time: " + timeStr, rightAlign + 10, 55);
        gc.fillText("Enemies: " + enemies.size(), rightAlign + 10, 80);
    }
    private void drawEnhancedEnemies() {
        for (Enemy enemy : enemies) {
            if (enemy instanceof Boss) {
                Boss boss = (Boss)enemy;
                if (boss.getBossImage() != null) {
                    double imageWidth = 200;
                    double imageHeight = 200;

                    double imageX = enemy.x - imageWidth / 2;
                    double imageY = enemy.y - imageHeight / 2;

                    gc.drawImage(boss.getBossImage(), imageX, imageY, imageWidth, imageHeight);
                } else {
                    gc.setFill(Color.DARKRED);
                    gc.fillRect(enemy.x, enemy.y, 40, 40);
                }

                double healthBarWidth = 100;
                double healthBarHeight = 10;
                double maxHealth = enemy.getMaxHealth();
                double healthPercentage = enemy.getHealth() / maxHealth;

                double healthBarX = enemy.x + 5 - (healthBarWidth / 2);
                double healthBarY = enemy.y - 50;

                gc.setFill(Color.BLACK);
                gc.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);

                gc.setFill(Color.CRIMSON);
                gc.fillRect(healthBarX, healthBarY, healthBarWidth * healthPercentage, healthBarHeight);

                // Boss Skill
                for (Projectile projectile : ((Boss)enemy).getBossProjectiles()) {
                    gc.setFill(Color.PURPLE);
                    gc.fillOval(projectile.x - 5, projectile.y - 5, 10, 10);
                }
            } else {
                if (enemy.getEnemyImage() != null) {
                    double imageWidth = 60;
                    double imageHeight = 60;

                    double imageX = enemy.x - imageWidth / 2;
                    double imageY = enemy.y - imageHeight / 2;

                    gc.drawImage(enemy.getEnemyImage(), imageX, imageY, imageWidth, imageHeight);
                } else {
                    gc.setFill(Color.RED);
                    gc.fillRect(enemy.x, enemy.y, 20, 20);
                }

                double healthBarWidth = 30;
                double healthBarHeight = 4;
                double maxHealth = enemy.getMaxHealth();
                double healthPercentage = enemy.getHealth() / maxHealth;

                double healthBarX = enemy.x + 10 - (healthBarWidth / 2);
                double healthBarY = enemy.y - 10;

                gc.setFill(Color.BLACK);
                gc.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
                gc.setFill(Color.GREEN);
                gc.fillRect(healthBarX, healthBarY, healthBarWidth * healthPercentage, healthBarHeight);
            }
        }
    }
    private void drawProjectiles() {
        for (Projectile projectile : projectiles) {
            switch (projectile.weaponType) {
                case "Pistol":
                    gc.setFill(Color.rgb(255, 200, 0, 0.8));
                    gc.fillOval(projectile.x - 4, projectile.y - 4, 8, 8);

                    gc.setFill(Color.rgb(255, 200, 0, 0.3));
                    gc.fillOval(projectile.x - 6, projectile.y - 6, 12, 12);
                    gc.setStroke(Color.rgb(255, 200, 50, 0.3));
                    gc.setLineWidth(4);
                    gc.strokeLine(projectile.x, projectile.y,
                            projectile.x - projectile.dx * 2,
                            projectile.y - projectile.dy * 2);
                    break;

                case "Rifle":
                    gc.setFill(Color.rgb(255, 129, 50, 0.9));
                    gc.fillOval(projectile.x - 3, projectile.y - 3, 7, 7);
                    gc.setStroke(Color.rgb(255, 50, 50, 0.4));
                    gc.setLineWidth(3);
                    gc.strokeLine(projectile.x, projectile.y,
                            projectile.x - projectile.dx * 3,
                            projectile.y - projectile.dy * 3);
                    break;

                default:
                    gc.setFill(Color.rgb(255, 255, 0, 0.8));
                    gc.fillOval(projectile.x - 3, projectile.y - 3, 6, 6);
                    break;
            }
        }
    }

    class Enemy {
        double x, y;
        int health;
        double maxHealth;
        double speed = 2;
        private Image enemyImage;

        public Enemy(int x, int y) {
            this.x = x;
            this.y = y;
            this.health = 30;
            this.maxHealth = 30;

            try {
                InputStream is = getClass().getResourceAsStream("/images/Monster.gif");
                if (is != null) {
                    enemyImage = new Image(is);
                } else {
                    System.err.println("Enemy GIF not found!");
                }
            } catch (Exception e) {
                System.err.println("Error loading enemy GIF: " + e.getMessage());
            }
        }

        public Image getEnemyImage() {
            return enemyImage;
        }

        public void takeDamage(int damage) {
            this.health -= damage;
            if (soundEffects != null) {
                AudioClip hitSound = soundEffects.get("Enemy_Hit");
                if (hitSound != null) {
                    hitSound.play(0.4);
                }
            }
        }

        public int getHealth() {
            return health;
        }

        public double getMaxHealth() {
            return maxHealth;
        }
    }
    class Boss extends Enemy {
        private double shotCooldown;
        private long lastShotTime;
        private List<Projectile> bossProjectiles;
        private boolean isCharging;
        private long chargeStartTime;
        private static final double BOSS_CHARGE_DURATION = 2.0;
        private static final double BOSS_CHARGE_SPEED = 6.0;
        private Point chargeTarget;
        private Image bossImage;

        public Boss(int x, int y, Image bossImage) {
            super(x, y);
            this.health = BOSS_HEALTH_BASE;
            this.maxHealth = BOSS_HEALTH_BASE;
            this.speed = 1.0;
            this.shotCooldown = 1.5;
            this.lastShotTime = 0;
            this.bossProjectiles = new ArrayList<>();
            this.isCharging = false;
            this.bossImage = bossImage;
        }

        public void update(Hero player) {
            if (isCharging) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - chargeStartTime > BOSS_CHARGE_DURATION * 1000) {
                    isCharging = false;
                } else {
                    // Execute charge
                    double dx = chargeTarget.x - x;
                    double dy = chargeTarget.y - y;
                    double distance = Math.sqrt(dx * dx + dy * dy);

                    if (distance > 0) {
                        dx /= distance;
                        dy /= distance;
                        x += dx * BOSS_CHARGE_SPEED;
                        y += dy * BOSS_CHARGE_SPEED;
                    }
                }
            } else {
                double dx = player.x - x;
                double dy = player.y - y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance > 0) {
                    dx /= distance;
                    dy /= distance;
                }

                x += dx * speed;
                y += dy * speed;

                if (Math.random() < 0.02) {
                    startCharge(player);
                }
            }
        }

        private void startCharge(Hero player) {
            isCharging = true;
            chargeStartTime = System.currentTimeMillis();
            chargeTarget = new Point((int)player.x, (int)player.y);
        }

        public List<Projectile> getBossProjectiles() {
            return bossProjectiles;
        }

        public void shootProjectiles(Hero player) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShotTime > shotCooldown * 1000) {
                int numProjectiles = 20;
                for (int i = 0; i < numProjectiles; i++) {
                    double angle = (Math.PI * 2 * i) / numProjectiles;
                    double dx = Math.cos(angle);
                    double dy = Math.sin(angle);

                    Projectile projectile = new Projectile(
                            x + 10,
                            y + 10,
                            dx * 4,
                            dy * 4,
                            BOSS_DAMAGE
                    );
                    projectile.weaponType = "Boss";
                    bossProjectiles.add(projectile);
                }
                lastShotTime = currentTime;
            }
        }
        public Image getBossImage() {
            return bossImage;
        }
        public int getHealth() {
            return health;
        }
    }


    class Projectile {
        double x, y;
        double dx, dy;
        int damage;
        String weaponType = "Default";

        public Projectile(double x, double y, double dx, double dy, int damage) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.damage = damage;
        }

        public void update() {
            x += dx;
            y += dy;
        }
    }
    public void initializeKeyHandling(Scene scene) {
        this.gameScene = scene;

        scene.setOnKeyPressed(event -> {
            activeKeys.add(event.getCode());
            event.consume();
        });

        scene.setOnKeyReleased(event -> {
            activeKeys.remove(event.getCode());
            event.consume();
        });

        Platform.runLater(() -> {
            this.requestFocus();
            scene.getRoot().requestFocus();
        });
    }
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        activeKeys.add(code);
        event.consume();
    }

    private void handleKeyReleased(KeyEvent event) {
        KeyCode code = event.getCode();
        activeKeys.remove(code);
        event.consume();
    }

    private void handleMouseMoved(MouseEvent event) {
        mousePosition.setLocation(event.getX(), event.getY());
        player.updateRotation(event.getX(), event.getY());
    }

}
class Hero {
    private String characterType;
    private int health;
    private int maxHealth;
    private int damage;
    int x;
    int y;
    private Image characterImage;
    private double rotation = 0;

    public Hero(String characterType, int startX, int startY, Image characterImage) {
        this.characterType = characterType;
        this.x = startX;
        this.y = startY;
        this.characterImage = characterImage;
        initializeCharacterAttributes();
        this.maxHealth = this.health;
    }

    private void initializeCharacterAttributes() {
        switch (characterType) {
            case "Pistol":
                this.health = 120;
                this.damage = 50;
                break;
            case "Rifle":
                this.health = 100;
                this.damage = 20;
                break;
            default:
                this.health = 80;
                this.damage = 10;
                break;
        }
        this.maxHealth = this.health;
    }

    public void resetHealth() {
        this.health = this.maxHealth;
    }

    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }

    public int getHealth() {
        return health;
    }

    public int getDamage() {
        return damage;
    }

    public String getCharacterType() {
        return characterType;
    }

    public void moveUp(int speed) {
        int newY = y - speed;
        y = Math.max(100, newY);
    }
    public void moveDown(int speed) {
        int newY = y + speed;
        y = Math.min(700 - 30, newY);
    }

    public void moveLeft(int speed) {
        int newX = x - speed;
        x = Math.max(100, newX);
    }

    public void moveRight(int speed) {
        int newX = x + speed;
        x = Math.min(900 - 30, newX);
    }
    public void updateRotation(double mouseX, double mouseY) {
        double dx = mouseX - x;
        double dy = mouseY - y;
        this.rotation = Math.atan2(dy, dx);
    }


    public void draw(GraphicsContext gc) {
        if (characterImage != null) {
            double width = 100;
            double height = 100;

            gc.save();
            gc.translate(x, y);
            gc.rotate(Math.toDegrees(rotation)+90);
            gc.drawImage(characterImage, -width / 2, -height / 2, width, height);
            gc.restore();
        } else {
            gc.setFill(Color.GREEN);
            gc.fillRect(x - 15, y - 15, 30, 30);
        }
    }

    public double getMaxHealth() {
        return maxHealth;
    }
}
