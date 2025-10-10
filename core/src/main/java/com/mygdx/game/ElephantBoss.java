package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ElephantBoss {
    private Texture bossTexture;
    private Texture bookTexture;
    private float x, y;
    private int health = 300;
    private float attackTimer = 0;
    private final float ATTACK_COOLDOWN = 2.5f; // โจมตีทุก 2.5 วินาที
    private boolean isAlive = true;
    private List<BossBook> books;
    private Random random;

    public ElephantBoss(float startX, float startY) {
        bossTexture = new Texture(Gdx.files.internal("images/elephant.PNG"));
        bookTexture = new Texture(Gdx.files.internal("images/book.PNG"));
        bookTexture = new Texture(Gdx.files.internal("images/book.PNG")); //เเก้ไขขขข
        books = new ArrayList<>();
        random = new Random();
        x = startX;
        y = startY;
    }

    public void update(float deltaTime, float playerX, float playerY) {
        if (!isAlive) return;

        attackTimer += deltaTime;
        if (attackTimer >= ATTACK_COOLDOWN) {
            attack(playerX, playerY);
            attackTimer = 0;
        }

        // อัพเดทหนังสือช้าง
        for (int i = books.size() - 1; i >= 0; i--) {
            books.get(i).update(deltaTime);
            if (books.get(i).isOffScreen()) books.remove(i);
        }
    }

    public void render(SpriteBatch batch) {
        if (!isAlive) return;
        batch.draw(bossTexture, x, y, 200, 200);

        // วาดหนังสือช้าง
        for (BossBook book : books) {
            book.render(batch);
        }
    }

    private void attack(float playerX, float playerY) {
        // ช้างปาหนังสือไปหาผู้เล่น ไอติมเเทน
        books.add(new BossBook(x, y + 100, playerX, playerY));
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            isAlive = false;
        }
    }

    public Rectangle getBounds() { return new Rectangle(x, y, 180, 180); }
    public List<BossBook> getBooks() { return books; }
    public boolean isAlive() { return isAlive; }
    public int getHealth() { return health; }

    public void dispose() {
        bossTexture.dispose();
        bookTexture.dispose();
    }

    // คลาสหนังสือของช้าง
    public class BossBook {
        private float x, y;
        private float speed = 400; // ช้ากว่าผู้เล่น
        private float targetX, targetY;

        public BossBook(float startX, float startY, float targetX, float targetY) {
            this.x = startX;
            this.y = startY;
            this.targetX = targetX;
            this.targetY = targetY;
        }

        public void update(float delta) {
            x -= speed * delta;

            if (y < targetY) y += 150 * delta;
            else if (y > targetY) y -= 150 * delta;
        }

        public void render(SpriteBatch batch) {
            batch.draw(bookTexture, x, y, 50, 40);
        }

        public Rectangle getBounds() { return new Rectangle(x, y, 45, 35); }
        public boolean isOffScreen() { return x < -50; }
    }
}
