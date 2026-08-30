package game.general_luna_game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Hero {
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

    public void heal(int amount) {
        this.health = Math.min(this.maxHealth, this.health + amount);
    }

    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.min(this.maxHealth, Math.max(0, health));
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
            gc.rotate(Math.toDegrees(rotation) + 90);
            gc.drawImage(characterImage, -width / 2, -height / 2, width, height);
            gc.restore();
        } else {
            gc.setFill(Color.GREEN);
            gc.fillRect(x - 15, y - 15, 30, 30);
        }
    }

    public void resetAttributes() {
        initializeCharacterAttributes();
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void increaseDamage(int amount) {
        this.damage += amount;
    }

    public void increaseMaxHealth(int amount, boolean heal) {
        this.maxHealth += amount;
        if (heal) {
            this.health = Math.min(this.maxHealth, this.health + amount);
        }
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }
}
