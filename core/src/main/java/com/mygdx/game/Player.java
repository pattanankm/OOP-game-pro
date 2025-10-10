package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private Texture playerTexture;
    private Texture bookTexture;
    private float x, y;
    private int health = 300;
    private float speed = 300;
    private List<BookProjectile> books;
    private float attackCooldown = 0.3f;
    private float attackTimer = 0;

    public Player(float startX, float startY) {
        playerTexture = new Texture(Gdx.files.internal("images/player.PNG"));
        bookTexture = new Texture(Gdx.files.internal("images/book.PNG")); // เพิ่มgear
        books = new ArrayList<>();
        x = startX;
        y = startY;
    }

    public void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) x -= speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) x += speed * delta;

        x = Math.max(50, Math.min(x, 700));

        attackTimer += delta;
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && attackTimer >= attackCooldown) {
            books.add(new BookProjectile(x + 80, y + 25));
            attackTimer = 0;
        }

        // อัพเดทสมุดทั้งหมด
        for (int i = books.size() - 1; i >= 0; i--) {
            books.get(i).update(delta);
            if (books.get(i).isOffScreen()) books.remove(i);
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(playerTexture, x, y, 120, 120);
        for (BookProjectile book : books) {
            book.render(batch);
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        health = Math.max(0, health);
    }

    public List<BookProjectile> getBooks() { return books; }
    public Rectangle getBounds() { return new Rectangle(x, y, 110, 110); }
    public int getHealth() { return health; }
    public boolean isAlive() { return health > 0; }

    public void dispose() {
        playerTexture.dispose();
        bookTexture.dispose();
    }

    // คลาสสมุด จะเพิ่มเกียร์มาอีก
    public class BookProjectile {
        private float x, y;
        private float speed = 600;

        public BookProjectile(float startX, float startY) {
            this.x = startX;
            this.y = startY;
        }

        public void update(float delta) {
            x += speed * delta; // บินไปทางขวา
        }

        public void render(SpriteBatch batch) {
            batch.draw(bookTexture, x, y, 60, 45);
        }

        public Rectangle getBounds() { return new Rectangle(x, y, 50, 40); }
        public boolean isOffScreen() { return x > 850; }
    }
}
