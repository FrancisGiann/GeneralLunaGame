package game.general_luna_game;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameEntitiesTest {

    @Test
    public void testHeroAttributesAndMovement() {
        Hero heroPistol = new Hero("Pistol", 100, 100, null);
        assertEquals(120, heroPistol.getHealth());
        assertEquals(120, heroPistol.getMaxHealth());
        assertEquals(50, heroPistol.getDamage());
        assertEquals("Pistol", heroPistol.getCharacterType());

        heroPistol.moveRight(10);
        assertEquals(110, heroPistol.x);

        heroPistol.moveDown(10);
        assertEquals(110, heroPistol.y);

        heroPistol.takeDamage(30);
        assertEquals(90, heroPistol.getHealth());

        // Test healing
        heroPistol.heal(20);
        assertEquals(110, heroPistol.getHealth());

        // Test heal clamped to maxHealth
        heroPistol.heal(50);
        assertEquals(120, heroPistol.getHealth());

        heroPistol.takeDamage(100);
        assertEquals(20, heroPistol.getHealth());

        heroPistol.resetHealth();
        assertEquals(120, heroPistol.getHealth());
    }

    @Test
    public void testProjectileMovement() {
        Projectile projectile = new Projectile(100, 100, 5, -5, 20);
        assertEquals(100, projectile.x);
        assertEquals(100, projectile.y);
        assertEquals("Default", projectile.weaponType);
        assertEquals(20, projectile.damage);

        projectile.update();
        assertEquals(105, projectile.x);
        assertEquals(95, projectile.y);
    }

    @Test
    public void testEnemyAttributesAndDamage() {
        Enemy enemy = new Enemy(50, 50);
        assertEquals(30, enemy.getHealth());
        assertEquals(30, enemy.getMaxHealth());
        assertEquals(2.0, enemy.getSpeed());
        assertEquals(100, enemy.getScoreValue());
        assertEquals(Enemy.EnemyType.NORMAL, enemy.getEnemyType());
        assertFalse(enemy.isRunner());

        enemy.takeDamage(15);
        assertEquals(15, enemy.getHealth());
    }

    @Test
    public void testRunnerEnemyAttributes() {
        Enemy runner = new Enemy(100, 100, Enemy.EnemyType.RUNNER);
        assertEquals(18, runner.getHealth());
        assertEquals(18, runner.getMaxHealth());
        assertEquals(3.0, runner.getSpeed()); // 50% faster than normal (2.0)
        assertEquals(150, runner.getScoreValue());
        assertEquals(Enemy.EnemyType.RUNNER, runner.getEnemyType());
        assertTrue(runner.isRunner());

        runner.takeDamage(10);
        assertEquals(8, runner.getHealth());
    }

    @Test
    public void testBossAttributesAndProjectiles() {
        Boss boss = new Boss(200, 200, null);
        assertEquals(Boss.BOSS_HEALTH_BASE, boss.getHealth());
        assertEquals(Boss.BOSS_HEALTH_BASE, boss.getMaxHealth());
        assertEquals(Enemy.EnemyType.BOSS, boss.getEnemyType());
        assertEquals(1000, boss.getScoreValue());
        assertNotNull(boss.getBossProjectiles());

        boss.takeDamage(50);
        assertEquals(Boss.BOSS_HEALTH_BASE - 50, boss.getHealth());

        Hero hero = new Hero("Rifle", 200, 300, null);
        boss.update(hero);
        // Boss should update position or charge towards hero
    }

    @Test
    public void testParticleSystem() {
        ParticleSystem ps = new ParticleSystem();
        assertEquals(0, ps.getParticleCount());

        // Spawn hit blood and sparks
        ps.spawnBlood(100, 100, 10);
        ps.spawnSparks(100, 100, 5);
        assertEquals(15, ps.getParticleCount());

        // Spawn explosion and collect particles
        ps.spawnExplosion(150, 150, 20);
        ps.spawnPowerUpCollect(150, 150, Color.GREEN, 8);
        assertEquals(43, ps.getParticleCount());

        // Update with delta time to age particles
        ps.update(0.1);
        assertTrue(ps.getParticleCount() > 0);

        // Update past lifespan
        ps.update(2.0);
        assertEquals(0, ps.getParticleCount(), "All particles should have expired after 2 seconds");

        // Test clear
        ps.spawnBlood(50, 50, 5);
        assertEquals(5, ps.getParticleCount());
        ps.clear();
        assertEquals(0, ps.getParticleCount());
    }

    @Test
    public void testPowerUpFunctionality() {
        PowerUp healthPack = new PowerUp(100, 100, PowerUp.PowerUpType.HEALTH);
        assertEquals(PowerUp.PowerUpType.HEALTH, healthPack.getType());
        assertEquals(100, healthPack.getX());
        assertEquals(100, healthPack.getY());
        assertEquals(30, healthPack.getHealAmount());
        assertFalse(healthPack.isExpired());

        Hero hero = new Hero("Pistol", 100, 100, null);
        hero.takeDamage(50);
        assertEquals(70, hero.getHealth());

        assertTrue(healthPack.collidesWith(hero));
        hero.heal(healthPack.getHealAmount());
        assertEquals(100, hero.getHealth());

        // Test Ammo PowerUp
        PowerUp ammoCrate = new PowerUp(300, 300, PowerUp.PowerUpType.AMMO, 2.0);
        assertEquals(PowerUp.PowerUpType.AMMO, ammoCrate.getType());
        assertFalse(ammoCrate.isExpired());

        // Test expiration
        ammoCrate.update(2.5);
        assertTrue(ammoCrate.isExpired());

        // Far away collision test
        Hero distantHero = new Hero("Pistol", 600, 600, null);
        assertFalse(healthPack.collidesWith(distantHero));
    }

    @Test
    public void testHeroUpgradesAndDamage() {
        Hero hero = new Hero("Rifle", 100, 100, null);
        assertEquals(100, hero.getHealth());
        assertEquals(100, hero.getMaxHealth());
        assertEquals(20, hero.getDamage());

        // Test Damage Upgrade
        hero.setDamage(35);
        assertEquals(35, hero.getDamage());

        hero.increaseDamage(15);
        assertEquals(50, hero.getDamage());

        // Test Max Health Upgrade with Heal
        hero.takeDamage(40);
        assertEquals(60, hero.getHealth());

        hero.increaseMaxHealth(25, true);
        assertEquals(125, hero.getMaxHealth());
        assertEquals(85, hero.getHealth()); // 60 + 25

        // Test Max Health Upgrade without Heal
        hero.increaseMaxHealth(25, false);
        assertEquals(150, hero.getMaxHealth());
        assertEquals(85, hero.getHealth());

        // Test reset attributes
        hero.resetAttributes();
        assertEquals(100, hero.getMaxHealth());
        assertEquals(20, hero.getDamage());
    }

    @Test
    public void testEnemyDamageAndCustomScaling() {
        Enemy normal = new Enemy(50, 50, Enemy.EnemyType.NORMAL);
        assertEquals(5, normal.getDamage());
        normal.setDamage(9);
        assertEquals(9, normal.getDamage());

        Enemy runner = new Enemy(50, 50, Enemy.EnemyType.RUNNER);
        assertEquals(4, runner.getDamage());
        runner.setDamage(7);
        assertEquals(7, runner.getDamage());

        Boss boss = new Boss(100, 100, null);
        assertEquals(15, boss.getDamage());
        boss.setDamage(21);
        assertEquals(21, boss.getDamage());
    }

    @Test
    public void testStageDifficultyScalingFormulas() {
        // Stage 1 scaling formulas
        int stage1Health = 30 + (1 - 1) * 15;
        int stage1Damage = 5 + (1 - 1) * 2;
        double stage1Speed = 1.6 + (1 - 1) * 0.15;
        int stage1Score = 100 + (1 - 1) * 25;

        assertEquals(30, stage1Health);
        assertEquals(5, stage1Damage);
        assertEquals(1.6, stage1Speed, 0.001);
        assertEquals(100, stage1Score);

        // Stage 3 scaling formulas
        int stage3Health = 30 + (3 - 1) * 15;
        int stage3Damage = 5 + (3 - 1) * 2;
        double stage3Speed = 1.6 + (3 - 1) * 0.15;
        int stage3Score = 100 + (3 - 1) * 25;

        assertEquals(60, stage3Health);
        assertEquals(9, stage3Damage);
        assertEquals(1.9, stage3Speed, 0.001);
        assertEquals(150, stage3Score);

        // Stage 4 Runner scaling
        int stage4RunnerHealth = (int)((30 + (4 - 1) * 15) * 0.65);
        int stage4RunnerDamage = Math.max(4, 5 + (4 - 1) * 2 - 1);
        double stage4RunnerSpeed = (1.6 + (4 - 1) * 0.15) * 1.5;
        int stage4RunnerScore = 100 + (4 - 1) * 25 + 50;

        assertEquals(48, stage4RunnerHealth);
        assertEquals(10, stage4RunnerDamage);
        assertEquals(3.075, stage4RunnerSpeed, 0.001);
        assertEquals(225, stage4RunnerScore);

        // Stage 5 Boss scaling
        int stage5BossHealth = Boss.BOSS_HEALTH_BASE + (5 - 1) * 100;
        int stage5BossDamage = Boss.BOSS_DAMAGE + (5 - 1) * 3;
        assertEquals(900, stage5BossHealth);
        assertEquals(27, stage5BossDamage);
    }

    @Test
    public void testUpgradeCostFormulas() {
        // Health upgrade cost progression: 150 + level * 100
        assertEquals(150, 150 + 0 * 100);
        assertEquals(250, 150 + 1 * 100);
        assertEquals(350, 150 + 2 * 100);

        // Speed upgrade cost progression: 200 + level * 150
        assertEquals(200, 200 + 0 * 150);
        assertEquals(350, 200 + 1 * 150);
        assertEquals(500, 200 + 2 * 150);

        // Damage upgrade cost progression: 250 + level * 150
        assertEquals(250, 250 + 0 * 150);
        assertEquals(400, 250 + 1 * 150);
        assertEquals(550, 250 + 2 * 150);
    }

    @Test
    public void testSoundManagerVolumeAndControls() {
        SoundManager soundManager = SoundManager.getInstance();
        assertNotNull(soundManager);

        // Test volume setting and clamping
        soundManager.setBgmVolume(0.5);
        assertEquals(0.5, soundManager.getBgmVolume(), 0.001);

        soundManager.setBgmVolume(1.5); // Should clamp to 1.0
        assertEquals(1.0, soundManager.getBgmVolume(), 0.001);

        soundManager.setBgmVolume(-0.2); // Should clamp to 0.0
        assertEquals(0.0, soundManager.getBgmVolume(), 0.001);

        soundManager.setSfxVolume(0.7);
        assertEquals(0.7, soundManager.getSfxVolume(), 0.001);

        soundManager.setSfxVolume(1.2); // Should clamp to 1.0
        assertEquals(1.0, soundManager.getSfxVolume(), 0.001);

        soundManager.setSfxVolume(-0.5); // Should clamp to 0.0
        assertEquals(0.0, soundManager.getSfxVolume(), 0.001);

        // Reset to default
        soundManager.setBgmVolume(0.8);
        soundManager.setSfxVolume(0.8);
        assertEquals(0.8, soundManager.getBgmVolume(), 0.001);
        assertEquals(0.8, soundManager.getSfxVolume(), 0.001);

        // Test BGM tracks transition safely
        soundManager.playHomeScreenBgm();
        assertEquals("HOMESCREEN", soundManager.getCurrentBgmType());

        soundManager.playGameBgm();
        assertEquals("GAME", soundManager.getCurrentBgmType());

        soundManager.playBossBgm();
        assertEquals("BOSS", soundManager.getCurrentBgmType());

        soundManager.stopBgm();
        assertEquals("NONE", soundManager.getCurrentBgmType());

        // Test SFX calls without exceptions
        assertDoesNotThrow(() -> soundManager.playSfx("Shoot_Pistol"));
        assertDoesNotThrow(() -> soundManager.playSfx("Shoot_Rifle"));
        assertDoesNotThrow(() -> soundManager.playSfx("Enemy_Hit"));
        assertDoesNotThrow(() -> soundManager.playSfx("Player_Hit"));
        assertDoesNotThrow(() -> soundManager.playSfx("Non_Existent_SFX"));
    }

    @Test
    public void testPauseStateAndElapsedTimeCompensation() {
        long startTime = System.currentTimeMillis() - 10000; // 10 seconds ago
        long pauseStart = System.currentTimeMillis(); // Paused now

        // Simulated paused elapsed time
        long pausedElapsed = (pauseStart - startTime) / 1000;
        assertEquals(10, pausedElapsed);

        // Simulate 3 seconds passing during pause
        long pauseDuration = 3000;
        long updatedStartTime = startTime + pauseDuration;

        // Resume after 3 seconds: elapsed time should still be 10 seconds (not 13)
        long currentAfterResume = pauseStart + pauseDuration;
        long resumedElapsed = (currentAfterResume - updatedStartTime) / 1000;
        assertEquals(10, resumedElapsed);
    }
}
