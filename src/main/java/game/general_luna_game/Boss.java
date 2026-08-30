package game.general_luna_game;

import javafx.scene.image.Image;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Boss extends Enemy {
    public static final int BOSS_HEALTH_BASE = 500;
    public static final int BOSS_DAMAGE = 15;
    private static final double BOSS_CHARGE_DURATION = 2.0;
    private static final double BOSS_CHARGE_SPEED = 6.0;

    private double shotCooldown;
    private long lastShotTime;
    private List<Projectile> bossProjectiles;
    private boolean isCharging;
    private long chargeStartTime;
    private Point chargeTarget;
    private Image bossImage;

    public Boss(int x, int y, Image bossImage) {
        super(x, y);
        this.setEnemyType(EnemyType.BOSS);
        this.setScoreValue(1000);
        this.health = BOSS_HEALTH_BASE;
        this.maxHealth = BOSS_HEALTH_BASE;
        this.speed = 1.0;
        this.setDamage(BOSS_DAMAGE);
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
        chargeTarget = new Point((int) player.x, (int) player.y);
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
                        getDamage()
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

    @Override
    public int getHealth() {
        return health;
    }
}
