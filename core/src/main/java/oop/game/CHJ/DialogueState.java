package oop.game.CHJ;

import com.badlogic.gdx.graphics.Texture;

public class DialogueState {
    public String text;
    public Texture image;
    public float x, y;

    public DialogueState(String text, Texture image, float x, float y) {
        this.text = text;
        this.image = image;
        this.x = x;
        this.y = y;
    }
}
