package game.general_luna_game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Lightweight particle system rendering geometric shapes (squares/circles)
 * directly to the JavaFX Canvas for combat visual effects.
 */
public class ParticleSystem {

    public static class Particle {
        double x, y;
        double vx, vy;
        double size;
        double maxLife;
        double currentLife;
        Color color;
        boolean isCircle;

        public Particle(double x, double y, double vx, double vy, double size, double life, Color color, boolean isCircle) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.maxLife = life;
            this.currentLife = life;
            this.color = color;
            this.isCircle = isCircle;
        }

        public void update(double dt) {
            x += vx;
            y += vy;
            vx *= 0.96; // drag
            vy *= 0.96;
            currentLife -= dt;
        }

        public boolean isDead() {
            return currentLife <= 0;
        }

        public void render(GraphicsContext gc) {
            if (gc == null || isDead()) return;

            double progress = Math.max(0.0, currentLife / maxLife);
            double alpha = Math.min(1.0, Math.max(0.0, progress * color.getOpacity()));
            Color renderColor = Color.color(
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    alpha
            );

            gc.setFill(renderColor);
            double renderSize = Math.max(1.0, size * progress);
            if (isCircle) {
                gc.fillOval(x - renderSize / 2, y - renderSize / 2, renderSize, renderSize);
            } else {
                gc.fillRect(x - renderSize / 2, y - renderSize / 2, renderSize, renderSize);
            }
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getCurrentLife() { return currentLife; }
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    public void update() {
        update(0.016);
    }

    public void update(double dt) {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle p = iterator.next();
            p.update(dt);
            if (p.isDead()) {
                iterator.remove();
            }
        }
    }

    public void render(GraphicsContext gc) {
        if (gc == null) return;
        for (Particle p : particles) {
            p.render(gc);
        }
    }

    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    public void spawnHitParticles(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = 1.0 + random.nextDouble() * 3.5;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            double size = 3.0 + random.nextDouble() * 3.0;
            double life = 0.2 + random.nextDouble() * 0.25;
            boolean isCircle = random.nextBoolean();

            particles.add(new Particle(x, y, vx, vy, size, life, color, isCircle));
        }
    }

    public void spawnBlood(double x, double y, int count) {
        Color[] bloodColors = {
                Color.rgb(180, 20, 20),
                Color.rgb(220, 30, 30),
                Color.rgb(130, 10, 10),
                Color.rgb(255, 60, 60)
        };
        for (int i = 0; i < count; i++) {
            Color c = bloodColors[random.nextInt(bloodColors.length)];
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = 1.5 + random.nextDouble() * 4.0;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            double size = 2.5 + random.nextDouble() * 3.5;
            double life = 0.25 + random.nextDouble() * 0.3;

            particles.add(new Particle(x, y, vx, vy, size, life, c, false));
        }
    }

    public void spawnSparks(double x, double y, int count) {
        Color[] sparkColors = {
                Color.rgb(255, 255, 100),
                Color.rgb(255, 200, 50),
                Color.rgb(255, 150, 0),
                Color.WHITE
        };
        for (int i = 0; i < count; i++) {
            Color c = sparkColors[random.nextInt(sparkColors.length)];
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = 2.0 + random.nextDouble() * 5.0;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            double size = 2.0 + random.nextDouble() * 3.0;
            double life = 0.15 + random.nextDouble() * 0.25;

            particles.add(new Particle(x, y, vx, vy, size, life, c, true));
        }
    }

    public void spawnExplosion(double x, double y, int count) {
        Color[] explosionColors = {
                Color.rgb(255, 220, 0),
                Color.rgb(255, 120, 0),
                Color.rgb(220, 40, 20),
                Color.rgb(100, 100, 100),
                Color.WHITE
        };
        for (int i = 0; i < count; i++) {
            Color c = explosionColors[random.nextInt(explosionColors.length)];
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = 1.5 + random.nextDouble() * 6.0;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            double size = 4.0 + random.nextDouble() * 6.0;
            double life = 0.3 + random.nextDouble() * 0.4;
            boolean isCircle = random.nextBoolean();

            particles.add(new Particle(x, y, vx, vy, size, life, c, isCircle));
        }
    }

    public void spawnPowerUpCollect(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            double angle = -Math.PI / 2 + (random.nextDouble() - 0.5) * Math.PI; // upwards
            double speed = 1.5 + random.nextDouble() * 4.0;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            double size = 3.0 + random.nextDouble() * 3.0;
            double life = 0.3 + random.nextDouble() * 0.3;

            particles.add(new Particle(x, y, vx, vy, size, life, color, true));
        }
    }

    public int getParticleCount() {
        return particles.size();
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public void clear() {
        particles.clear();
    }
}
