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
import javafx.scene.media.MediaPlayer;

public class Game extends Pane {

    private Hero player;
    private List<Enemy> enemies;
    private List<Projectile> projectiles;
    private List<PowerUp> powerUps;
    private ParticleSystem particleSystem;
    private AnimationTimer gameTimer;
    private Point mousePosition;
    private int currentStage = 1;
    private int score = 0;
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
    private int healthUpgradeLevel = 0;
    private int speedUpgradeLevel = 0;
    private int damageUpgradeLevel = 0;
    private boolean isShopOpen = false;
    private ShopMenu shopMenu;
    private boolean isPaused = false;
    private long pauseStartTime = 0;
    private PauseMenu pauseMenu;
    private static final int BOSS_HEALTH_BASE = 500;
    private static final int BOSS_DAMAGE = 15;
    private Boss bossPrefab = null;
    private Image bossImage;
    private boolean gameWon = false;
    private boolean isGameOver = false;
    private Map<String, AudioClip> soundEffects;
    public static MediaPlayer backgroundMusic;

    // Screen Shake variables
    private double shakeDuration = 0;
    private double shakeIntensity = 0;
    private double shakeOffsetX = 0;
    private double shakeOffsetY = 0;
    private final Random random = new Random();

    public Game(Hero hero, GeneralLunaGame gameFrame) {
        this.gameFrame = gameFrame;
        this.player = hero;
        this.enemies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.particleSystem = new ParticleSystem();
        this.mousePosition = new Point(0, 0);
        this.activeKeys = new HashSet<>();
        this.lastDamageTime = new HashMap<>();

        gameCanvas = new Canvas(1000, 800);
        gc = gameCanvas.getGraphicsContext2D();
        getChildren().add(gameCanvas);

        shopMenu = new ShopMenu(this);
        getChildren().add(shopMenu);

        pauseMenu = new PauseMenu(this);
        getChildren().add(pauseMenu);

        startTime = System.currentTimeMillis();
        setupGameLoop();
        loadMapBackgrounds();
        spawnEnemies();
        initializeAmmo();
        loadSoundEffects();
        SoundManager.getInstance().playGameBgm();

        this.setOnKeyPressed(this::handleKeyPressed);
        this.setOnKeyReleased(this::handleKeyReleased);
        this.requestFocus();
        gameCanvas.setOnMouseMoved(this::handleMouseMoved);
        gameCanvas.setOnMouseDragged(this::handleMouseMoved);
        gameCanvas.setOnMousePressed(event -> {
            if (!isShopOpen && !isPaused) {
                isMousePressed = true;
                handleMouseMoved(event);
            }
        });
        gameCanvas.setOnMouseReleased(event -> {
            isMousePressed = false;
        });
    }

    private long lastUpdate = 0;
    private void setupGameLoop() {
        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_666_666) { // ~60 FPS
                    updateGame();
                    renderGame();
                    lastUpdate = now;
                }
            }
        };
        gameTimer.start();
    }

    private void loadSoundEffects() {
        soundEffects = SoundManager.getInstance().getSoundEffects();
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

    public void triggerScreenShake(double duration, double intensity) {
        this.shakeDuration = duration;
        this.shakeIntensity = intensity;
    }

    public void pauseGame() {
        if (isPaused || isShopOpen || isGameOver || gameWon) return;
        isPaused = true;
        pauseStartTime = System.currentTimeMillis();
        activeKeys.clear();
        isMousePressed = false;
        if (pauseMenu != null) {
            pauseMenu.show();
        }
    }

    public void resumeGame() {
        if (!isPaused) return;
        isPaused = false;
        long pauseDuration = System.currentTimeMillis() - pauseStartTime;
        startTime += pauseDuration;
        lastShotTime += pauseDuration;
        if (isReloading) {
            reloadStartTime += pauseDuration;
        }
        for (Map.Entry<Enemy, Long> entry : lastDamageTime.entrySet()) {
            entry.setValue(entry.getValue() + pauseDuration);
        }
        if (pauseMenu != null) {
            pauseMenu.hide();
        }
        this.requestFocus();
    }

    public void togglePause() {
        if (isPaused) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    public void quitToMainMenu() {
        stopGame();
        SoundManager.getInstance().stopBgm();
        if (gameFrame != null) {
            try {
                gameFrame.showHomeScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public PauseMenu getPauseMenu() {
        return pauseMenu;
    }

    public long getElapsedTime() {
        if (isPaused) {
            return Math.max(0, (pauseStartTime - startTime) / 1000);
        }
        return Math.max(0, (System.currentTimeMillis() - startTime) / 1000);
    }

    private void updateGame() {
        if (isPaused) {
            return;
        }

        if (isShopOpen) {
            if (particleSystem != null) {
                particleSystem.update(0.016);
            }
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Screen Shake update
        if (shakeDuration > 0) {
            shakeOffsetX = (random.nextDouble() * 2 - 1) * shakeIntensity;
            shakeOffsetY = (random.nextDouble() * 2 - 1) * shakeIntensity;
            shakeDuration -= 0.016;
            if (shakeDuration <= 0) {
                shakeOffsetX = 0;
                shakeOffsetY = 0;
                shakeIntensity = 0;
            }
        }

        // Particle system update
        if (particleSystem != null) {
            particleSystem.update(0.016);
        }

        // Power-ups update and collection
        for (PowerUp powerUp : new ArrayList<>(powerUps)) {
            powerUp.update(0.016);
            if (powerUp.isExpired()) {
                powerUps.remove(powerUp);
            } else if (powerUp.collidesWith(player)) {
                if (powerUp.getType() == PowerUp.PowerUpType.HEALTH) {
                    player.heal(powerUp.getHealAmount());
                    particleSystem.spawnPowerUpCollect(powerUp.getX(), powerUp.getY(), Color.LIGHTGREEN, 15);
                } else if (powerUp.getType() == PowerUp.PowerUpType.AMMO) {
                    currentAmmo = maxAmmo;
                    if (isReloading) {
                        isReloading = false;
                        playerSpeed = normalSpeed;
                    }
                    particleSystem.spawnPowerUpCollect(powerUp.getX(), powerUp.getY(), Color.GOLD, 15);
                }
                powerUps.remove(powerUp);
            }
        }

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
                    player.takeDamage(bossPrefab.getDamage());
                    bossPrefab.getBossProjectiles().remove(projectile);
                    particleSystem.spawnBlood(player.x, player.y, 12);
                    triggerScreenShake(0.25, 8.0);
                    SoundManager.getInstance().playSfx("Player_Hit");
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

            double finalDx = (dx * 0.7 + separationX * 0.3) * enemy.getSpeed();
            double finalDy = (dy * 0.7 + separationY * 0.3) * enemy.getSpeed();

            enemy.x += finalDx;
            enemy.y += finalDy;

            enemy.x = Math.min(Math.max(enemy.x, 0), gameCanvas.getWidth() - 20);
            enemy.y = Math.min(Math.max(enemy.y, 0), gameCanvas.getHeight() - 20);

            if (Math.abs(enemy.x - player.x) < 10 && Math.abs(enemy.y - player.y) < 10) {
                Long lastDamage = lastDamageTime.getOrDefault(enemy, 0L);
                if (currentTime - lastDamage >= DAMAGE_COOLDOWN * 1000) {
                    player.takeDamage(enemy.getDamage());
                    lastDamageTime.put(enemy, currentTime);
                    particleSystem.spawnBlood(player.x, player.y, 10);
                    triggerScreenShake(0.2, 6.0);
                    SoundManager.getInstance().playSfx("Player_Hit");
                    checkGameOver();
                }
            }
        }

        updateProjectiles();

        if (enemies.isEmpty() && !isGameOver && !gameWon) {
            if (currentStage >= 5) {
                // Handled if boss defeated
            } else {
                openShopMenu();
            }
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

                    // Visual feedback: Hit particles & sparks
                    if (enemy instanceof Boss) {
                        particleSystem.spawnHitParticles(projectile.x, projectile.y, Color.PURPLE, 8);
                        particleSystem.spawnSparks(projectile.x, projectile.y, 6);
                    } else if (enemy.isRunner()) {
                        particleSystem.spawnHitParticles(projectile.x, projectile.y, Color.ORANGE, 6);
                        particleSystem.spawnBlood(projectile.x, projectile.y, 6);
                    } else {
                        particleSystem.spawnBlood(projectile.x, projectile.y, 8);
                        particleSystem.spawnSparks(projectile.x, projectile.y, 4);
                    }

                    SoundManager.getInstance().playSfx("Enemy_Hit");
                    projectiles.remove(projectile);

                    if (enemy.getHealth() <= 0) {
                        // Add score
                        score += enemy.getScoreValue();

                        // Explosion & Power-up drop on defeat
                        if (enemy instanceof Boss) {
                            particleSystem.spawnExplosion(enemy.x, enemy.y, 40);
                            powerUps.add(new PowerUp(enemy.x - 20, enemy.y, PowerUp.PowerUpType.HEALTH));
                            powerUps.add(new PowerUp(enemy.x + 20, enemy.y, PowerUp.PowerUpType.AMMO));
                        } else {
                            particleSystem.spawnExplosion(enemy.x, enemy.y, 16);
                            if (Math.random() < 0.35) {
                                PowerUp.PowerUpType dropType = Math.random() < 0.5 ? PowerUp.PowerUpType.HEALTH : PowerUp.PowerUpType.AMMO;
                                powerUps.add(new PowerUp(enemy.x, enemy.y, dropType));
                            }
                        }

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
        SoundManager.getInstance().playSfx("Shoot_" + player.getCharacterType());
    }

    private void renderGame() {
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        gc.save();
        if (shakeDuration > 0) {
            gc.translate(shakeOffsetX, shakeOffsetY);
        }

        if (mapBackgrounds != null && mapBackgrounds[0] != null) {
            gc.drawImage(mapBackgrounds[0], 0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        } else {
            gc.setFill(Color.GRAY);
            gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        }

        // Draw Collectible Power-Ups
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(gc);
        }

        player.draw(gc);
        drawEnhancedEnemies();
        drawProjectiles();

        // Draw Visual Particles
        if (particleSystem != null) {
            particleSystem.render(gc);
        }

        gc.restore();

        // Draw UI without screen shake for clean readability
        drawPlayerUI();
    }

    private void loadMapBackgrounds() {
        mapBackgrounds = new Image[1];
        InputStream is = getClass().getResourceAsStream("/images/map1.jpg");
        if (is != null) {
            mapBackgrounds[0] = new Image(is);
        }
    }

    private void spawnEnemies() {
        if (currentStage == 5 && bossPrefab == null) {
            SoundManager.getInstance().playBossBgm();
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
            bossPrefab.setHealth(BOSS_HEALTH_BASE + (currentStage - 1) * 100);
            bossPrefab.setMaxHealth(bossPrefab.getHealth());
            bossPrefab.setDamage(BOSS_DAMAGE + (currentStage - 1) * 3);
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
                // Runner spawn chance starting at Stage 2
                boolean spawnRunner = currentStage >= 2 && Math.random() < Math.min(0.5, 0.2 + (currentStage - 2) * 0.15);
                Enemy enemy;
                int normalBaseHealth = 30 + (currentStage - 1) * 15;
                int normalBaseDamage = 5 + (currentStage - 1) * 2;
                double normalBaseSpeed = 1.6 + (currentStage - 1) * 0.15;
                int normalScore = 100 + (currentStage - 1) * 25;

                if (spawnRunner) {
                    enemy = new Enemy(enemyX, enemyY, Enemy.EnemyType.RUNNER);
                    enemy.setSpeed(normalBaseSpeed * 1.5);
                    enemy.setHealth((int)(normalBaseHealth * 0.65));
                    enemy.setMaxHealth(enemy.getHealth());
                    enemy.setDamage(Math.max(4, normalBaseDamage - 1));
                    enemy.setScoreValue(normalScore + 50);
                } else {
                    enemy = new Enemy(enemyX, enemyY, Enemy.EnemyType.NORMAL);
                    enemy.setSpeed(normalBaseSpeed);
                    enemy.setHealth(normalBaseHealth);
                    enemy.setMaxHealth(enemy.getHealth());
                    enemy.setDamage(normalBaseDamage);
                    enemy.setScoreValue(normalScore);
                }
                enemies.add(enemy);
                lastDamageTime.put(enemy, 0L);
            }
        }
    }

    public void resetGame() {
        if (isPaused && pauseMenu != null) {
            pauseMenu.hide();
        }
        isPaused = false;
        if (isShopOpen && shopMenu != null) {
            shopMenu.hide();
        }
        isShopOpen = false;
        healthUpgradeLevel = 0;
        speedUpgradeLevel = 0;
        damageUpgradeLevel = 0;
        normalSpeed = 4;
        playerSpeed = normalSpeed;
        player.resetAttributes();
        player.resetHealth();
        enemies.clear();
        projectiles.clear();
        powerUps.clear();
        if (particleSystem != null) {
            particleSystem.clear();
        }
        score = 0;
        shakeDuration = 0;
        shakeIntensity = 0;
        shakeOffsetX = 0;
        shakeOffsetY = 0;
        bossPrefab = null;
        isGameOver = false;
        gameWon = false;

        currentStage = 1;
        spawnEnemies();
        activeKeys.clear();
        
        startTime = System.currentTimeMillis();
        player.x = (int) (gameCanvas.getWidth() / 2);
        player.y = (int) (gameCanvas.getHeight() / 2);

        if (gameTimer != null) {
            gameTimer.start();
        }
        initializeAmmo();
        isReloading = false;
        playerSpeed = normalSpeed;

        SoundManager.getInstance().playGameBgm();
    }

    public void stopGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    private void checkGameOver() {
        if (gameWon) {
            return;
        }
        if (player.getHealth() <= 0 && gameTimer != null && !isGameOver) {
            isGameOver = true;
            if (isPaused && pauseMenu != null) {
                pauseMenu.hide();
            }
            isPaused = false;
            stopGame();
            try {
                gameFrame.showGameOver(getElapsedTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void Victory() {
        gameWon = true;
        if (isPaused && pauseMenu != null) {
            pauseMenu.hide();
        }
        isPaused = false;
        stopGame();
        try {
            gameFrame.showVictoryScreen(getElapsedTime());
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
            // Cooldown display
            double cooldownPercent = Math.min(1.0,
                    (System.currentTimeMillis() - lastShotTime) / (getWeaponCooldown() * 1000));
            gc.setFill(Color.rgb(255, 255, 0, 0.8));
            gc.fillRoundRect(x, cooldownY, cooldownWidth * cooldownPercent, cooldownHeight, 5, 5);

            // Ready indicator
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
        gc.fillRoundRect(rightAlign, 10, 140, 110, 10, 10);

        gc.setFill(Color.WHITE);
        long elapsedTime = getElapsedTime();
        String timeStr = String.format("%02d:%02d", elapsedTime / 60, elapsedTime % 60);
        gc.fillText("Stage: " + currentStage, rightAlign + 10, 30);
        gc.fillText("Score: " + score, rightAlign + 10, 55);
        gc.fillText("Time: " + timeStr, rightAlign + 10, 80);
        gc.fillText("Enemies: " + enemies.size(), rightAlign + 10, 105);
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
            } else if (enemy.isRunner()) {
                // Runner Enemy: fast, glowing speed aura
                double imageWidth = 50;
                double imageHeight = 50;
                double imageX = enemy.x - imageWidth / 2;
                double imageY = enemy.y - imageHeight / 2;

                gc.setStroke(Color.rgb(255, 140, 0, 0.8));
                gc.setLineWidth(3);
                gc.strokeOval(imageX - 2, imageY - 2, imageWidth + 4, imageHeight + 4);

                if (enemy.getEnemyImage() != null) {
                    gc.drawImage(enemy.getEnemyImage(), imageX, imageY, imageWidth, imageHeight);
                } else {
                    gc.setFill(Color.ORANGERED);
                    gc.fillRect(enemy.x - 10, enemy.y - 10, 20, 20);
                }

                double healthBarWidth = 26;
                double healthBarHeight = 4;
                double maxHealth = enemy.getMaxHealth();
                double healthPercentage = Math.max(0, enemy.getHealth() / maxHealth);

                double healthBarX = enemy.x + 5 - (healthBarWidth / 2);
                double healthBarY = enemy.y - 12;

                gc.setFill(Color.BLACK);
                gc.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
                gc.setFill(Color.GOLD);
                gc.fillRect(healthBarX, healthBarY, healthBarWidth * healthPercentage, healthBarHeight);
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

    public void initializeKeyHandling(Scene scene) {
        this.gameScene = scene;

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (!isShopOpen && !isGameOver && !gameWon) {
                    togglePause();
                }
            } else if (!isShopOpen && !isPaused) {
                activeKeys.add(event.getCode());
            }
            event.consume();
        });

        scene.setOnKeyReleased(event -> {
            if (!isShopOpen && !isPaused) {
                activeKeys.remove(event.getCode());
            }
            event.consume();
        });

        Platform.runLater(() -> {
            this.requestFocus();
            scene.getRoot().requestFocus();
        });
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            if (!isShopOpen && !isGameOver && !gameWon) {
                togglePause();
            }
            event.consume();
            return;
        }
        if (isShopOpen || isPaused) return;
        KeyCode code = event.getCode();
        activeKeys.add(code);
        event.consume();
    }

    private void handleKeyReleased(KeyEvent event) {
        if (isShopOpen || isPaused) return;
        KeyCode code = event.getCode();
        activeKeys.remove(code);
        event.consume();
    }

    private void handleMouseMoved(MouseEvent event) {
        if (isShopOpen || isPaused) return;
        mousePosition.setLocation(event.getX(), event.getY());
        player.updateRotation(event.getX(), event.getY());
    }

    public int getHealthUpgradeCost() {
        return 150 + healthUpgradeLevel * 100;
    }

    public int getSpeedUpgradeCost() {
        return 200 + speedUpgradeLevel * 150;
    }

    public int getDamageUpgradeCost() {
        return 250 + damageUpgradeLevel * 150;
    }

    public int getHealthUpgradeLevel() {
        return healthUpgradeLevel;
    }

    public int getSpeedUpgradeLevel() {
        return speedUpgradeLevel;
    }

    public int getDamageUpgradeLevel() {
        return damageUpgradeLevel;
    }

    public int getNormalSpeed() {
        return normalSpeed;
    }

    public void setNormalSpeed(int speed) {
        this.normalSpeed = speed;
        this.playerSpeed = speed;
    }

    public boolean isShopOpen() {
        return isShopOpen;
    }

    public ShopMenu getShopMenu() {
        return shopMenu;
    }

    public boolean buyHealthUpgrade() {
        int cost = getHealthUpgradeCost();
        if (score >= cost) {
            score -= cost;
            healthUpgradeLevel++;
            player.increaseMaxHealth(25, true);
            if (particleSystem != null) {
                particleSystem.spawnPowerUpCollect(player.x, player.y, Color.LIGHTGREEN, 25);
            }
            return true;
        }
        return false;
    }

    public boolean buySpeedUpgrade() {
        int cost = getSpeedUpgradeCost();
        if (score >= cost) {
            score -= cost;
            speedUpgradeLevel++;
            normalSpeed += 1;
            playerSpeed = normalSpeed;
            if (particleSystem != null) {
                particleSystem.spawnPowerUpCollect(player.x, player.y, Color.GOLD, 25);
            }
            return true;
        }
        return false;
    }

    public boolean buyDamageUpgrade() {
        int cost = getDamageUpgradeCost();
        if (score >= cost) {
            score -= cost;
            damageUpgradeLevel++;
            player.increaseDamage(15);
            if (particleSystem != null) {
                particleSystem.spawnPowerUpCollect(player.x, player.y, Color.ORANGERED, 25);
            }
            return true;
        }
        return false;
    }

    public void openShopMenu() {
        if (isShopOpen) return;
        isShopOpen = true;
        isMousePressed = false;
        activeKeys.clear();
        projectiles.clear();
        if (shopMenu != null) {
            shopMenu.show(currentStage);
            shopMenu.toFront();
        }
    }

    public void closeShopAndNextStage() {
        if (!isShopOpen) return;
        isShopOpen = false;
        if (shopMenu != null) {
            shopMenu.hide();
        }
        if (currentStage + 1 == 5) {
            SoundManager.getInstance().playBossBgm();
        }
        advanceToNextStage();
        this.requestFocus();
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }

    public List<PowerUp> getPowerUps() {
        return powerUps;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public Hero getPlayer() {
        return player;
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public double getShakeDuration() {
        return shakeDuration;
    }

    public double getShakeIntensity() {
        return shakeIntensity;
    }

    public double getShakeOffsetX() {
        return shakeOffsetX;
    }

    public double getShakeOffsetY() {
        return shakeOffsetY;
    }

    public void setCurrentStage(int stage) {
        this.currentStage = stage;
    }
}
