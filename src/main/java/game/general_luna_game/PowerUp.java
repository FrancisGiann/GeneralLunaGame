package game.general_luna_game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class PowerUp {
    public enum PowerUpType {
        HEALTH,
        AMMO
    }

    double x, y;
    PowerUpType type;
    double lifetime; // in seconds
    double maxLifetime;
    double size = 26;
    int healAmount = 30;

    public PowerUp(double x, double y, PowerUpType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.maxLifetime = 15.0; // 15 seconds
        this.lifetime = this.maxLifetime;
    }

    public PowerUp(double x, double y, PowerUpType type, double lifetime) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.maxLifetime = lifetime;
        this.lifetime = lifetime;
    }

    public void update(double dt) {
        lifetime -= dt;
    }

    public boolean isExpired() {
        return lifetime <= 0;
    }

    public boolean collidesWith(double px, double py) {
        double dist = Math.sqrt((x - px) * (x - px) + (y - py) * (y - py));
        return dist < 30.0;
    }

    public boolean collidesWith(Hero hero) {
        if (hero == null) return false;
        return collidesWith(hero.x, hero.y);
    }

    public void draw(GraphicsContext gc) {
        if (gc == null || isExpired()) return;

        // Blinking effect if lifetime < 3 seconds
        if (lifetime < 3.0) {
            int blink = (int) (lifetime * 6);
            if (blink % 2 == 0) {
                return; // skip drawing frame for blink effect
            }
        }

        // Slight floating/pulse animation
        double pulse = Math.sin((maxLifetime - lifetime) * 4) * 2;
        double currentSize = size + pulse;
        double drawX = x - currentSize / 2;
        double drawY = y - currentSize / 2;

        gc.save();
        if (type == PowerUpType.HEALTH) {
            // Health pack: Green background with white cross
            gc.setFill(Color.rgb(0, 0, 0, 0.4));
            gc.fillOval(drawX - 3, drawY - 3, currentSize + 6, currentSize + 6);

            gc.setFill(Color.rgb(46, 204, 113, 0.95));
            gc.fillRoundRect(drawX, drawY, currentSize, currentSize, 8, 8);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeRoundRect(drawX, drawY, currentSize, currentSize, 8, 8);

            // Draw White Cross
            gc.setFill(Color.WHITE);
            double crossThickness = 4;
            double crossLength = currentSize * 0.6;
            // Vertical bar
            gc.fillRect(x - crossThickness / 2, y - crossLength / 2, crossThickness, crossLength);
            // Horizontal bar
            gc.fillRect(x - crossLength / 2, y - crossThickness / 2, crossLength, crossThickness);
        } else if (type == PowerUpType.AMMO) {
            // Ammo crate: Gold/Orange background with "AMMO" label or icon
            gc.setFill(Color.rgb(0, 0, 0, 0.4));
            gc.fillOval(drawX - 3, drawY - 3, currentSize + 6, currentSize + 6);

            gc.setFill(Color.rgb(243, 156, 18, 0.95));
            gc.fillRoundRect(drawX, drawY, currentSize, currentSize, 8, 8);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeRoundRect(drawX, drawY, currentSize, currentSize, 8, 8);

            // Draw AMMO text or bullets
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("AMMO", x, y + 3);
        }
        gc.restore();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public PowerUpType getType() { return type; }
    public double getLifetime() { return lifetime; }
    public int getHealAmount() { return healAmount; }
    public void setHealAmount(int healAmount) { this.healAmount = healAmount; }
}
