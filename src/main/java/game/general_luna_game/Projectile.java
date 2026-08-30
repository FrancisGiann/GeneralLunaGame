package game.general_luna_game;

public class Projectile {
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
