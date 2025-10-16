package oop.game.CHJ;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;


public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("OOP-game");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        new Lwjgl3Application(new Main(), config);
    }
}
