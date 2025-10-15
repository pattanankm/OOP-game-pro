package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

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

    public static class Player {
        public Texture playerTexture;
        public Texture bookTexture;
        public Texture gearTexture;
        private float x, y;
        private int health = 500;
        private float speed = 300;
        private List<Projectile> projectiles;
        private float attackCooldown = 0.3f;
        private float attackTimer = 0;
        private String currentWeapon = "BOOK";
        private float animationTimer = 0;
        private float bounceTimer = 0;
        private float wiggleTimer = 0;
        private boolean isMoving = false;

        public Player(float startX, float startY) {
            playerTexture = new Texture(Gdx.files.internal("images/player.PNG"));
            bookTexture = new Texture(Gdx.files.internal("images/book.PNG"));
            try {
                gearTexture = new Texture(Gdx.files.internal("images/gear.PNG"));
                System.out.println("โหลดเกียร์ละ");
            } catch (Exception e) {
                System.out.println("ไม่พบไฟล์gearใช้ book.PNGแทน"); //ป้องกัน
                gearTexture = bookTexture;
            }

            projectiles = new ArrayList<>();
            x = startX;
            y = startY;

            System.out.println("ผู้เล่นสร้างที่ตำแหน่ง " + x + ", " + y);
        }

        public void update(float delta) {
            animationTimer += delta;
            bounceTimer += delta;
            wiggleTimer += delta;

            boolean wasMoving = isMoving;
            isMoving = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D);

            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                x -= speed * delta;
                wiggleTimer += delta * 10;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                x += speed * delta;
                wiggleTimer += delta * 10;
            }

            x = Math.max(50, Math.min(x, 600));

            if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
                currentWeapon = "BOOK";
                System.out.println("เปลี่ยนเป็นหนังสือ");
            }
            if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
                currentWeapon = "GEAR";
                System.out.println("เปลี่ยนเป็นเกียร์");
            }
            attackTimer += delta;
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && attackTimer >= attackCooldown) {
                float projectileY = y + 60 + (float)Math.sin(animationTimer * 10) * 5;
                projectiles.add(new Projectile(x + 80, projectileY, currentWeapon));
                attackTimer = 0;
            }

            for (int i = projectiles.size() - 1; i >= 0; i--) {
                projectiles.get(i).update(delta);
                if (projectiles.get(i).isOffScreen()) {
                    projectiles.remove(i);
                }
            }
        }

        public void render(SpriteBatch batch) {
            float bounceY = (float)Math.sin(bounceTimer * 4) * 5;
            float wiggleX = (float)Math.sin(wiggleTimer * 15) * 2;
            float moveEffect = isMoving ? (float)Math.sin(wiggleTimer * 20) * 3 : 0;

            float drawX = x + wiggleX + moveEffect;
            float drawY = y + bounceY;

            batch.draw(playerTexture, drawX, drawY, 150, 150);
            for (Projectile projectile : projectiles) {
                projectile.render(batch);
            }
        }
        public void takeDamage(int damage) {
            health -= damage;
            health = Math.max(0, health);
        }

        public List<Projectile> getProjectiles() { return projectiles; }

        public Rectangle getBounds() {
            float bounceY = (float)Math.sin(bounceTimer * 4) * 5;
            return new Rectangle(x + 10, y + 10 + bounceY, 130, 130);
        }

        public int getHealth() { return health; }
        public boolean isAlive() { return health > 0; }
        public String getCurrentWeapon() { return currentWeapon; }

        public void dispose() {
            playerTexture.dispose();
            bookTexture.dispose();
            if (gearTexture != bookTexture) {
                gearTexture.dispose();
            }
        }

        public class Projectile {
            private float x, y;
            private float speed = 500;
            private String type;
            private int damage;
            private float rotation = 0;
            private float scale = 1.0f; // 🔽 ลดขนาดจาก 1.5 เป็น 1.0
            private float floatTimer = 0;

            public Projectile(float startX, float startY, String weaponType) {
                this.x = startX;
                this.y = startY;
                this.type = weaponType;
                this.damage = weaponType.equals("BOOK") ? 10 : 15;
            }

            public void update(float delta) {
                x += speed * delta;
                floatTimer += delta;

                if (type.equals("GEAR")) {
                    rotation += 400 * delta;
                    y += (float)Math.sin(floatTimer * 10) * 2;
                } else {
                    y += (float)Math.sin(floatTimer * 8) * 3;
                }
            }

            public void render(SpriteBatch batch) {
                if (type.equals("BOOK")) {
                    float bookWiggle = (float)Math.sin(floatTimer * 12) * 5;
                    batch.draw(bookTexture, x, y + bookWiggle, 60 * scale, 45 * scale);
                } else {
                    batch.setColor(1, 1, 0, 1);
                    batch.draw(gearTexture, x, y, 45 * scale, 45 * scale);
                    batch.setColor(1, 1, 1, 1);
                }
            }

            public Rectangle getBounds() {
                if (type.equals("BOOK")) {
                    return new Rectangle(x, y, 50, 35); // จาก 70x50
                } else {
                    return new Rectangle(x, y, 35, 35); // จาก 50x50
                }
            }
            public boolean isOffScreen() { return x > 850; }
            public int getDamage() { return damage; }
        }
    }
}
