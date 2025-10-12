package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ElephantBoss {
    private Texture basicTexture1;
    private Texture basicTexture2;
    private Texture angryTexture1;
    private Texture angryTexture2;
    private Texture icecreamTexture;
    private float x, y;
    private int health = 300;
    private List<IcecreamProjectile> icecreams;
    private float attackCooldown = 2.5f;
    private float attackTimer = 0;
    private Random random;

    private boolean useAngryTexture = false;
    private float animationTimer = 0;
    private boolean isFlashing = false;
    private float flashTimer = 0;
    private float stateTimer = 0;
    private int currentBasicFrame = 0;
    private int currentAngryFrame = 0;
    private float frameTime = 0.2f;
    private float frameTimer = 0;

    private boolean isTransforming = false;
    private float transformTimer = 0;
    private float transformDuration = 3.0f;

    public ElephantBoss(float startX, float startY) {
        try {
            basicTexture1 = new Texture(Gdx.files.internal("images/elephantbasic.PNG"));
            basicTexture2 = new Texture(Gdx.files.internal("images/elephantbasic2.PNG"));
            angryTexture1 = new Texture(Gdx.files.internal("images/elephant.PNG"));
            angryTexture2 = new Texture(Gdx.files.internal("images/elephant2.PNG"));
            icecreamTexture = new Texture(Gdx.files.internal("images/icecream.PNG"));
            System.out.println("โหลดเสร็จละจ๊ะ");
        } catch (Exception e) {
            System.out.println("❌" + e.getMessage());
            basicTexture1 = new Texture(Gdx.files.internal("images/player.PNG"));
            basicTexture2 = basicTexture1;
            angryTexture1 = basicTexture1;
            angryTexture2 = basicTexture1;
            icecreamTexture = new Texture(Gdx.files.internal("images/book.PNG"));
        }

        icecreams = new ArrayList<>();
        x = startX;
        y = startY;
        random = new Random();
    }

    public void update(float delta, float playerX, float playerY) {
        animationTimer += delta;
        stateTimer += delta;
        frameTimer += delta;

        if (frameTimer >= frameTime) {
            if (!useAngryTexture && !isTransforming) {
                currentBasicFrame = (currentBasicFrame + 1) % 2;
            } else if (useAngryTexture) {
                currentAngryFrame = (currentAngryFrame + 1) % 2;
            }
            frameTimer = 0;
        }

        if (health < 200 && !useAngryTexture && !isTransforming) {
            isTransforming = true;
            transformTimer = 0;
            System.out.println("ช้างแปลงร่าง");
        }

        if (isTransforming) {
            transformTimer += delta;
            isFlashing = ((int)(transformTimer * 10) % 2) == 0;

            if (transformTimer >= transformDuration) {
                isTransforming = false;
                useAngryTexture = true;
                isFlashing = false;
                System.out.println("เสร็จ");
            }
        }

        if (useAngryTexture && !isTransforming) {
            flashTimer += delta;
            isFlashing = ((int)(flashTimer * 8) % 2) == 0;
        }

        attackTimer += delta;
        if (attackTimer >= attackCooldown) {
            float icecreamX = x - 30;
            float icecreamY = y + 80;
            icecreams.add(new IcecreamProjectile(icecreamX, icecreamY, playerX, playerY));
            attackTimer = 0;

            if (useAngryTexture) {
                attackCooldown = 1.5f;
            } else if (health < 300) {
                attackCooldown = 2.0f;
            }
        }

        for (int i = icecreams.size() - 1; i >= 0; i--) {
            icecreams.get(i).update(delta);
            if (icecreams.get(i).isOffScreen()) icecreams.remove(i);
        }
    }

    public void render(SpriteBatch batch) {
        Texture currentTexture;

        if (isTransforming) {
            currentTexture = ((int)(transformTimer * 6) % 2 == 0) ?
                getCurrentBasicTexture() : getCurrentAngryTexture();
        } else if (useAngryTexture) {
            currentTexture = getCurrentAngryTexture();
        } else {
            currentTexture = getCurrentBasicTexture();
        }

        float bounceY = (float)Math.sin(animationTimer * 2) * 3;
        float swayX = (float)Math.sin(animationTimer * 1.5f) * 2;

        if (isTransforming) {
            float transformScale = 1.0f + (float)Math.sin(transformTimer * 8) * 0.1f;
            float transformShake = (float)Math.sin(transformTimer * 20) * 5;

            if (isFlashing) {
                batch.setColor(1, 0.5f, 0.5f, 1);
            }

            batch.draw(currentTexture,
                x + swayX + transformShake,
                y + bounceY,
                75, 75,
                150, 150,
                transformScale, transformScale,
                0,
                0, 0,
                currentTexture.getWidth(), currentTexture.getHeight(),
                false, false);
        } else {
            if (isFlashing && useAngryTexture) {
                batch.setColor(1, 0.3f, 0.3f, 1);
            }

            batch.draw(currentTexture, x + swayX, y + bounceY, 150, 150);
        }

        batch.setColor(1, 1, 1, 1);

        for (IcecreamProjectile icecream : icecreams) {
            icecream.render(batch);
        }
    }

    private Texture getCurrentBasicTexture() {
        return currentBasicFrame == 0 ? basicTexture1 : basicTexture2;
    }

    private Texture getCurrentAngryTexture() {
        return currentAngryFrame == 0 ? angryTexture1 : angryTexture2;
    }

    public void takeDamage(int damage) {
        health -= damage;
        health = Math.max(0, health);
        isFlashing = true;
        flashTimer = 0;
    }

    public List<IcecreamProjectile> getIcecreams() { return icecreams; }

    public Rectangle getBounds() {
        return new Rectangle(x + 10, y + 10, 130, 130);
    }

    public int getHealth() { return health; }
    public boolean isAlive() { return health > 0; }
    public boolean isTransforming() { return isTransforming; }

    public void dispose() {
        basicTexture1.dispose();
        basicTexture2.dispose();
        if (angryTexture1 != basicTexture1) angryTexture1.dispose();
        if (angryTexture2 != basicTexture2) angryTexture2.dispose();
        icecreamTexture.dispose();
    }

    public class IcecreamProjectile {
        private float x, y;
        private float speedX, speedY;
        private int damage = 15;
        private float scale = 1.0f;
        private float wobbleTimer = 0;
        private float baseSpeed = 250f;

        public IcecreamProjectile(float startX, float startY, float targetX, float targetY) {
            this.x = startX;
            this.y = startY;

            float dx = targetX - startX;
            float dy = targetY - startY;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);

            this.speedX = (dx / distance) * baseSpeed;
            this.speedY = (dy / distance) * baseSpeed;

            System.out.println("ยิงติม: " + targetX + ", " + targetY);
            System.out.println("ติมเร็ว: " + speedX + ", " + speedY);
        }

        public void update(float delta) {
            x += speedX * delta;
            y += speedY * delta;
            wobbleTimer += delta;
            x += (float)Math.sin(wobbleTimer * 8) * 1.5f;
            y += (float)Math.sin(wobbleTimer * 6) * 1.5f;
        }

        public void render(SpriteBatch batch) {
            float wobbleX = (float)Math.sin(wobbleTimer * 8) * 2;
            float wobbleY = (float)Math.sin(wobbleTimer * 6) * 2;

            float width = 60 * scale;
            float height = 45 * scale;

            batch.setColor(1, 0.9f, 0.95f, 1);
            batch.draw(icecreamTexture, x + wobbleX, y + wobbleY, width, height);
            batch.setColor(1, 1, 1, 1);
        }

        public Rectangle getBounds() {
            return new Rectangle(x + 10, y + 10, 40, 25);
        }

        public boolean isOffScreen() {
            return x < -100 || x > 900 || y < -100 || y > 700;
        }

        public int getDamage() { return damage; }
    }
}
