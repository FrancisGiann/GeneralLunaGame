package game.general_luna_game;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private static SoundManager instance;

    private double bgmVolume = 0.8;
    private double sfxVolume = 0.8;
    private MediaPlayer currentBgmPlayer;
    private String currentBgmType = "NONE";
    private final Map<String, AudioClip> soundEffects = new HashMap<>();

    private SoundManager() {
        loadSoundEffects();
    }

    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void loadSoundEffects() {
        loadClip("Shoot_Pistol", "/sounds/pistol.wav");
        loadClip("Shoot_Rifle", "/sounds/rifle.wav");
        loadClip("Enemy_Hit", "/sounds/enemyhit.wav");
        loadClip("Player_Hit", "/sounds/playerhit.wav");
    }

    private void loadClip(String name, String path) {
        try {
            URL resource = getClass().getResource(path);
            if (resource != null) {
                AudioClip clip = new AudioClip(resource.toExternalForm());
                soundEffects.put(name, clip);
            }
        } catch (Throwable t) {
            // Ignored in headless/test environments
        }
    }

    public void playSfx(String name) {
        playSfx(name, 1.0);
    }

    public void playSfx(String name, double volumeMultiplier) {
        if (sfxVolume <= 0.001) return;
        AudioClip clip = soundEffects.get(name);
        if (clip != null) {
            try {
                double effectiveVolume = Math.max(0.0, Math.min(1.0, sfxVolume * volumeMultiplier));
                clip.play(effectiveVolume);
            } catch (Throwable t) {
                // Ignore audio device errors
            }
        }
    }

    public void playHomeScreenBgm() {
        if ("HOMESCREEN".equals(currentBgmType) && currentBgmPlayer != null) {
            currentBgmPlayer.setVolume(bgmVolume);
            return;
        }
        playBgmTrack("/sounds/homescreen_bgm.wav", "HOMESCREEN", 1.0);
    }

    public void playGameBgm() {
        if ("GAME".equals(currentBgmType) && currentBgmPlayer != null) {
            currentBgmPlayer.setVolume(bgmVolume);
            currentBgmPlayer.setRate(1.0);
            return;
        }
        playBgmTrack("/sounds/bgm.wav", "GAME", 1.0);
    }

    public void playBossBgm() {
        if ("BOSS".equals(currentBgmType) && currentBgmPlayer != null) {
            currentBgmPlayer.setVolume(bgmVolume);
            currentBgmPlayer.setRate(1.2);
            return;
        }
        // Dynamic boss music: Fast-paced, high-stakes pitch & rate
        playBgmTrack("/sounds/bgm.wav", "BOSS", 1.2);
    }

    private void playBgmTrack(String resourcePath, String type, double rate) {
        stopBgm();
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource != null) {
                Media media = new Media(resource.toExternalForm());
                currentBgmPlayer = new MediaPlayer(media);
                currentBgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                currentBgmPlayer.setVolume(bgmVolume);
                currentBgmPlayer.setRate(rate);
                currentBgmPlayer.play();
                currentBgmType = type;
                Game.backgroundMusic = currentBgmPlayer;
            }
        } catch (Throwable t) {
            currentBgmType = type;
        }
    }

    public void stopBgm() {
        if (currentBgmPlayer != null) {
            try {
                currentBgmPlayer.stop();
            } catch (Throwable t) {
                // Ignore
            }
            currentBgmPlayer = null;
        }
        currentBgmType = "NONE";
    }

    public void pauseBgm() {
        if (currentBgmPlayer != null) {
            try {
                currentBgmPlayer.pause();
            } catch (Throwable t) {
                // Ignore
            }
        }
    }

    public void resumeBgm() {
        if (currentBgmPlayer != null) {
            try {
                currentBgmPlayer.play();
            } catch (Throwable t) {
                // Ignore
            }
        }
    }

    public void setBgmVolume(double volume) {
        this.bgmVolume = Math.max(0.0, Math.min(1.0, volume));
        if (currentBgmPlayer != null) {
            try {
                currentBgmPlayer.setVolume(this.bgmVolume);
            } catch (Throwable t) {
                // Ignore
            }
        }
    }

    public double getBgmVolume() {
        return bgmVolume;
    }

    public void setSfxVolume(double volume) {
        this.sfxVolume = Math.max(0.0, Math.min(1.0, volume));
    }

    public double getSfxVolume() {
        return sfxVolume;
    }

    public String getCurrentBgmType() {
        return currentBgmType;
    }

    public MediaPlayer getCurrentBgmPlayer() {
        return currentBgmPlayer;
    }

    public Map<String, AudioClip> getSoundEffects() {
        return soundEffects;
    }
}
