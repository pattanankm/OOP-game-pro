package oop.game.CHJ;

import com.badlogic.gdx.graphics.Texture;

public class NPC {
    public Texture texture;
    public float x, y;
    public float width, height;
    public String dialogue;
    public String name;

    public NPC(float x, float y, String texturePath, String dialogue, String name) {
        this.texture = new Texture(texturePath);
        this.x = x;
        this.y = y;
        this.width = texture.getWidth() / 8f;  // ปรับขนาดให้เท่าตัวละคร
        this.height = texture.getHeight() / 8f;
        this.dialogue = dialogue;
        this.name = name;
    }

    public NPC(Texture npcTex, float x, float y) {
    }

    //เช็คว่า player อยู่ใกล้ NPC
    public boolean isPlayerNear(float playerX, float playerY, float interactionRange) {
        float dx = (x + width/2) - (playerX + width/2);
        float dy = (y + height/2) - (playerY + height/2);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance < interactionRange;
    }

    public void dispose() {
        texture.dispose();
    }
}
