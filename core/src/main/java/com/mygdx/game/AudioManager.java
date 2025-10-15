package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;

public class AudioManager {
    private HashMap<String, Sound> sounds;
    private Music bgm;

    public AudioManager() {
        sounds = new HashMap<>();
        loadSounds();
    }

    private void loadSounds() {
        try {
            sounds.put("shoot", Gdx.audio.newSound(Gdx.files.internal("audio/Shoot.mp3")));
            sounds.put("win", Gdx.audio.newSound(Gdx.files.internal("audio/Win.mp3")));
            sounds.put("lose", Gdx.audio.newSound(Gdx.files.internal("audio/Lose.mp3")));
            bgm = Gdx.audio.newMusic(Gdx.files.internal("audio/Hero.mp3"));
        } catch (Exception e) {
            System.out.println("Audio files not found, continuing without audio");
        }
    }

    public void playSound(String soundName) {
        if (sounds.containsKey(soundName)) {
            sounds.get(soundName).play(0.7f);
        }
    }

    public void playBGM() {
        if (bgm != null) {
            bgm.setLooping(true);
            bgm.setVolume(0.3f);
            bgm.play();
        }
    }

    public void stopBGM() {
        if (bgm != null) {
            bgm.stop();
        }
    }

    public void dispose() {
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        if (bgm != null) {
            bgm.dispose();
        }
    }
}
