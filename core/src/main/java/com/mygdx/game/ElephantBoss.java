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
    private int health = 500;
    private List<IcecreamProjectile> icecreams;
    private float attackCooldown = 3.0f;
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
    private Texture whitePixel;

    public ElephantBoss(float startX, float startY) {
        try {
            basicTexture1 = new Texture(Gdx.files.internal("images/elephantbasic.PNG"));
            basicTexture2 = new Texture(Gdx.files.internal("images/elephantbasic2.PNG"));
            angryTexture1 = new Texture(Gdx.files.internal("images/elephant.PNG"));
            angryTexture2 = new Texture(Gdx.files.internal("images/elephant2.PNG"));
            icecreamTexture = new Texture(Gdx.files.internal("images/icecream.PNG"));
            System.out.println("โหลดช้าง");
        } catch (Exception e) {
            System.out.println("ช้างไม่สำเร็จ" + e.getMessage());
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
        whitePixel = createWhitePixel();
    }

    private Texture createWhitePixel() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
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

        if (health < 300 && !useAngryTexture && !isTransforming) {
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
                System.out.println("ช้างแปลงร่าง2");
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
                if (health < 50) {
                    attackCooldown = 2.0f;
                } else if (health < 100) {
                    attackCooldown = 2.2f;
                } else if (health < 150) {
                    attackCooldown = 2.5f;
                } else {
                    attackCooldown = 2.8f;
                }
            } else if (health < 200) {
                attackCooldown = 3.0f;
            } else if (health < 300) {
                attackCooldown = 3.2f;
            } else {
                attackCooldown = 3.5f;
            }
        }
        if (health < 50 && useAngryTexture && !isTransforming) {
            if (random.nextFloat() < 0.1f) {
                float icecreamX = x - 30;
                float icecreamY = y + 80;
                icecreams.add(new IcecreamProjectile(icecreamX, icecreamY, playerX, playerY));
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

        // เเสง
        drawLightEffects(batch, bounceY, swayX);

        if (isTransforming) {
            float transformScale = 1.0f + (float)Math.sin(transformTimer * 8) * 0.1f;
            float transformShake = (float)Math.sin(transformTimer * 20) * 5;

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
            batch.draw(currentTexture, x + swayX, y + bounceY, 150, 150);
        }

        for (IcecreamProjectile icecream : icecreams) {
            icecream.render(batch);
        }
    }
    private void drawLightEffects(SpriteBatch batch, float bounceY, float swayX) {
        float centerX = x + 75 + swayX;
        float centerY = y + 75 + bounceY;

        if (useAngryTexture) {
            // แสงแดงโกรธ
            float redPulse = (float)Math.sin(animationTimer * 6) * 0.3f + 0.7f;
            batch.setColor(1, 0.3f, 0.3f, redPulse * 0.4f);
            for (int i = 0; i < 3; i++) {
                float size = 180 + i * 20;
                float offsetX = centerX - size/2;
                float offsetY = centerY - size/2;
                batch.draw(whitePixel, offsetX, offsetY, size, size);
            }

            // แสงกระพริบ
            if (health < 300) {
                float dangerFlash = ((int)(animationTimer * 10) % 2) * 0.5f;
                batch.setColor(1, 0.2f, 0.2f, dangerFlash);
                batch.draw(whitePixel, centerX - 100, centerY - 100, 100, 100);
            }
        }

        // แสงกำลังแปลงร่าง
        if (isTransforming) {
            float transformGlow = (float)Math.sin(transformTimer * 8) * 0.4f + 0.6f;
            batch.setColor(0.3f, 0.6f, 1, transformGlow * 0.5f);

            for (int i = 0; i < 4; i++) {
                float size = 160 + i * 25;
                float pulse = (float)Math.sin(transformTimer * 5 + i) * 10;
                float offsetX = centerX - (size + pulse)/2;
                float offsetY = centerY - (size + pulse)/2;
                batch.draw(whitePixel, offsetX, offsetY, size + pulse, size + pulse);
            }
        }
        if (!useAngryTexture && !isTransforming) {
            float yellowGlow = (float)Math.sin(animationTimer * 3) * 0.2f + 0.3f;
            batch.setColor(1, 1, 0.5f, yellowGlow * 0.3f);
            batch.draw(whitePixel, centerX - 90, centerY - 90, 180, 180);
        }

        batch.setColor(1, 1, 1, 1);
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
        whitePixel.dispose();
    }

    public class IcecreamProjectile {
        private float x, y;
        private float speedX, speedY;
        private int damage = 15;
        private float scale = 1.0f;
        private float wobbleTimer = 0;
        private float baseSpeed;

        public IcecreamProjectile(float startX, float startY, float targetX, float targetY) {
            this.x = startX;
            this.y = startY;
            if (ElephantBoss.this.health < 50) {
                baseSpeed = 280f;
            } else if (ElephantBoss.this.health < 100) {
                baseSpeed = 260f;
            } else if (ElephantBoss.this.health < 200) {
                baseSpeed = 240f;
            } else {
                baseSpeed = 220f;
            }
            float dx = targetX - startX;
            float dy = targetY - startY;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);

            this.speedX = (dx / distance) * baseSpeed;
            this.speedY = (dy / distance) * baseSpeed;
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
