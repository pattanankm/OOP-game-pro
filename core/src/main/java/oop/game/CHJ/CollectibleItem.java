//package oop.game.CHJ;
//
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//import com.badlogic.gdx.math.Rectangle;
//
///**
// * ไอเท็มที่วางในแมพให้ผู้เล่นเก็บ
// */
//public class CollectibleItem {
//    public String id;              // รหัสไอเท็ม ต้องตรงกับใน InventorySystem
//    public String name;            // ชื่อไอเท็ม
//    public float x, y;             // ตำแหน่งในแมพ
//    public float width, height;    // ขนาด
//    public Texture texture;        // ภาพไอเท็ม
//    public boolean collected;      // เก็บแล้วหรือยัง
//    public Rectangle hitbox;       // พื้นที่สำหรับตรวจจับ
//
//    public CollectibleItem(String id, String name, float x, float y,
//                           float width, float height, String texturePath) {
//        this.id = id;
//        this.name = name;
//        this.x = x;
//        this.y = y;
//        this.width = width;
//        this.height = height;
//        this.texture = new Texture(texturePath);
//        this.collected = false;
//        this.hitbox = new Rectangle(x, y, width, height);
//    }
//
//    /**
//     * ตรวจสอบว่าผู้เล่นอยู่ใกล้ไอเท็มหรือไม่
//     */
//    public boolean isPlayerNear(float playerX, float playerY, float range) {
//        float dx = playerX - (x + width / 2);
//        float dy = playerY - (y + height / 2);
//        float distance = (float) Math.sqrt(dx * dx + dy * dy);
//        return distance < range;
//    }
//
//    /**
//     * วาดไอเท็มในแมพ (ถ้ายังไม่เก็บ)
//     */
//    public void render(SpriteBatch batch) {
//        if (!collected) {
//            batch.draw(texture, x, y, width, height);
//        }
//    }
//
//    /**
//     * เก็บไอเท็ม
//     */
//    public void collect() {
//        this.collected = true;
//    }
//
//    /**
//     * ทำลาย texture
//     */
//    public void dispose() {
//        texture.dispose();
//    }
//}
