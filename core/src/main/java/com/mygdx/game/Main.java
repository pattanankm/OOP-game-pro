package com.mygdx.game;

import com.badlogic.gdx.Game;
import oop.game.CHJ.FirstScreen;

public class Main extends Game {
    @Override
    public void create() {
        setScreen(new FirstScreen());
    }
}
