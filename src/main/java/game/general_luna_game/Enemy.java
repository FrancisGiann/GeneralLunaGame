package game.general_luna_game;

import javafx.scene.image.Image;
import java.io.InputStream;

public class Enemy {
    public enum EnemyType {
        NORMAL,
        RUNNER,
        BOSS
    }

    double x, y;
    int health;
    double maxHealth;
    double speed = 2.0;
    private Image enemyImage;
    private EnemyType enemyType = EnemyType.NORMAL;
    private int scoreValue = 100;
    private int damage = 5;

    public Enemy(int x, int y) {
        this(x, y, EnemyType.NORMAL);
    }

    public Enemy(int x, int y, EnemyType type) {
        this.x = x;
        this.y = y;
        this.enemyType = type;

        if (type == EnemyType.RUNNER) {
            this.health = 18;
            this.maxHealth = 18;
            this.speed = 3.0; // 50% faster than normal (2.0)
            this.scoreValue = 150;
            this.damage = 4;
        } else {
            this.health = 30;
            this.maxHealth = 30;
            this.speed = 2.0;
            this.scoreValue = 100;
            this.damage = 5;
        }

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
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public EnemyType getEnemyType() {
        return enemyType;
    }

    public void setEnemyType(EnemyType enemyType) {
        this.enemyType = enemyType;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(int scoreValue) {
        this.scoreValue = scoreValue;
    }

    public boolean isRunner() {
        return enemyType == EnemyType.RUNNER;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}
