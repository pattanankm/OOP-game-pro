package oop.game.CHJ;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/** อินเวนทอรีมุมซ้ายล่าง: ยังไม่เก็บ = เงาสีเทา, เก็บแล้ว = ไอคอนจริง */
public class InventoryUI {

    public static class Item {
        public final String id, name;
        public final Texture texture;
        public boolean collected = false;
        public Item(String id, String name, String texturePath) {
            this.id = id; this.name = name;
            this.texture = new Texture(texturePath);
        }
        public void dispose(){ texture.dispose(); }
    }

    private final Array<Item> items = new Array<>();
    private final float x, y, slot, pad;
    private final Texture px;

    /** startX,startY = มุมซ้ายล่างของ “จอ” (หน่วยพิกเซลจอ ไม่ผูกกับกล้องโลก) */
    public InventoryUI(float startX, float startY, float slotSize, float slotPadding) {
        this.x = startX; this.y = startY; this.slot = slotSize; this.pad = slotPadding;

        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        px = new Texture(pm); pm.dispose();

        // ปรับพาธให้ตรง assets ของคุณ (ตามรูปในโฟลเดอร์)
        items.add(new Item("gear", "Gear", "Icons/gear.PNG"));
        items.add(new Item("book", "Book", "Icons/book.PNG"));
    }

    public boolean collectItem(String id){
        for (Item it: items) if (it.id.equals(id) && !it.collected){ it.collected = true; return true; }
        return false;
    }

    public void render(SpriteBatch batch){
        for (int i=0;i<items.size;i++){
            float sx = x + i*(slot+pad), sy = y;

            // เงากรอบช่อง
            batch.setColor(0,0,0,0.35f); batch.draw(px, sx-2, sy-2, slot+4, slot+4);
            batch.setColor(1,1,1,0.28f); batch.draw(px, sx-1, sy-1, slot+2, slot+2);

            // ไอคอน (ยังไม่เก็บ = เทา)
            if (!items.get(i).collected){
                batch.setColor(0.45f,0.45f,0.45f,0.9f);
                batch.draw(items.get(i).texture, sx, sy, slot, slot);
                batch.setColor(0.6f,0.6f,0.6f,0.25f); // overlay เทาอ่อน
                batch.draw(px, sx, sy, slot, slot);
            } else {
                batch.setColor(Color.WHITE);
                batch.draw(items.get(i).texture, sx, sy, slot, slot);
            }
            batch.setColor(Color.WHITE);
        }
    }

    public String getClickedItem(float cx, float cy){
        for (int i=0;i<items.size;i++){
            float sx = x + i*(slot+pad), sy = y;
            if (new Rectangle(sx,sy,slot,slot).contains(cx,cy)) return items.get(i).id;
        }
        return null;
    }

    public void reset(){ for (Item it: items) it.collected = false; }
    public void dispose(){ for (Item it: items) it.dispose(); px.dispose(); }
}
