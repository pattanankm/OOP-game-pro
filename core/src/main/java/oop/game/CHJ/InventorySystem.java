//package oop.game.CHJ;
//
//import com.badlogic.gdx.graphics.Color;
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//import com.badlogic.gdx.math.Rectangle;
//import com.badlogic.gdx.utils.Array;
//
//public class InventorySystem {
//
//    public static class Item {
//        public String id;              // รหัสไอเท็ม เช่น "gear", "book"
//        public String name;            // ชื่อไอเท็ม
//        public Texture texture;        // ภาพไอเท็ม
//        public boolean collected;      // เก็บแล้วหรือยัง
//
//        public Item(String id, String name, String texturePath) {
//            this.id = id;
//            this.name = name;
//            this.texture = new Texture(texturePath);
//            this.collected = false;
//        }
//
//        public void dispose() {
//            texture.dispose();
//        }
//    }
//
//    private Array<Item> items;
//    private float inventoryX;    // ตำแหน่ง X ของ inventory
//    private float inventoryY;    // ตำแหน่ง Y ของ inventory
//    private float slotSize;      // ขนาดของช่องแต่ละช่อง
//    private float slotPadding;   // ระยะห่างระหว่างช่อง
//    private Texture pixelTexture; // ใช้สำหรับวาดเงา
//
//    public InventorySystem(float startX, float startY, float slotSize, float padding, Texture pixel) {
//        this.items = new Array<>();
//        this.inventoryX = startX;
//        this.inventoryY = startY;
//        this.slotSize = slotSize;
//        this.slotPadding = padding;
//        this.pixelTexture = pixel;
//    }
//
//    /**
//     * เพิ่มไอเท็มเข้าระบบ (ไม่ต้องมีภาพเงา)
//     */
//    public void addItem(String id, String name, String texturePath) {
//        items.add(new Item(id, name, texturePath));
//    }
//
//    /**
//     * เก็บไอเท็มตาม ID
//     */
//    public boolean collectItem(String id) {
//        for (Item item : items) {
//            if (item.id.equals(id) && !item.collected) {
//                item.collected = true;
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * ตรวจสอบว่าเก็บไอเท็มแล้วหรือยัง
//     */
//    public boolean isCollected(String id) {
//        for (Item item : items) {
//            if (item.id.equals(id)) {
//                return item.collected;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * วาด inventory ที่มุมซ้ายล่าง (ไม่ต้องใช้ภาพเงา)
//     */
//    public void render(SpriteBatch batch) {
//        for (int i = 0; i < items.size; i++) {
//            Item item = items.get(i);
//
//            // คำนวณตำแหน่งของแต่ละช่อง (เรียงแนวนอน)
//            float x = inventoryX + i * (slotSize + slotPadding);
//            float y = inventoryY;
//
//            // วาดภาพไอเท็ม
//            batch.draw(item.texture, x, y, slotSize, slotSize);
//
//            // ถ้ายังไม่เก็บ ทับด้วยสีดำโปร่งใส
//            if (!item.collected) {
//                batch.setColor(0, 0, 0, 0.7f); // สีดำ 70%
//                batch.draw(pixelTexture, x, y, slotSize, slotSize);
//                batch.setColor(Color.WHITE); // รีเซ็ตสี
//            }
//        }
//    }
//
//    /**
//     * ดึงข้อมูลไอเท็มทั้งหมด
//     */
//    public Array<Item> getItems() {
//        return items;
//    }
//
//    /**
//     * ทำลาย textures ทั้งหมด
//     */
//    public void dispose() {
//        for (Item item : items) {
//            item.dispose();
//        }
//    }
//
//    /**
//     * ตรวจสอบว่าคลิกที่ช่องไหน (ใช้เมื่อต้องการแสดงรายละเอียด)
//     */
//    public String getClickedItem(float clickX, float clickY) {
//        for (int i = 0; i < items.size; i++) {
//            Item item = items.get(i);
//            float x = inventoryX + i * (slotSize + slotPadding);
//            float y = inventoryY;
//
//            Rectangle slot = new Rectangle(x, y, slotSize, slotSize);
//            if (slot.contains(clickX, clickY)) {
//                return item.id;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * รีเซ็ต inventory (เริ่มเกมใหม่)
//     */
//    public void reset() {
//        for (Item item : items) {
//            item.collected = false;
//        }
//    }
//}
