package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Gear {
    private Texture texture;
    private Vector2 position;
    private float rotation;
    private float scale;
    private float rotationSpeed;

    public Gear(float x, float y) {
        try {
            texture = new Texture(Gdx.files.internal("images/gear.PNG"));
            position = new Vector2(x, y);
            rotation = 0;
            scale = 1.0f;
            rotationSpeed = 30 + (float)Math.random() * 60;
        } catch (Exception e) {
            Gdx.app.error("Gear", "Cannot load gear texture: " + e.getMessage());
        }
    }

    public void update(float delta) {
        rotation += delta * rotationSpeed;
        scale = 0.8f + (float)Math.sin(Gdx.graphics.getFrameId() * 0.05f + position.x) * 0.3f;
    }

    public void render(SpriteBatch batch) {
        if (texture == null) return;

        float width = texture.getWidth();
        float height = texture.getHeight();

        batch.draw(texture,
            position.x - width/2, position.y - height/2,
            width/2, height/2,
            width, height,
            scale, scale,
            rotation,
            0, 0,
            texture.getWidth(), texture.getHeight(),
            false, false
        );
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
