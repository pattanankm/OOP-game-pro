package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class TopLeftHUD {

    public enum Clicked { NONE, SAVE, HOME }

    private final Texture saveTex;
    private final Texture homeTex;
    private final Rectangle saveRect;
    private final Rectangle homeRect;
    private final Vector3 tmp = new Vector3();

    // ขนาดปุ่มและมาร์จิน (ปรับได้)
    private final float size;
    private final float margin;

    public TopLeftHUD(String savePath, String homePath) {
        this(savePath, homePath, 56f, 12f);
    }

    public TopLeftHUD(String savePath, String homePath, float size, float margin) {
        this.size = size;
        this.margin = margin;
        this.saveTex = new Texture(savePath);
        this.homeTex = new Texture(homePath);

        // กำหนดกรอบคลิก (อิงพิกเซลหน้าจอ)
        this.saveRect = new Rectangle(margin, Gdx.graphics.getHeight() - margin - size, size, size);
        this.homeRect = new Rectangle(margin + size + 8, Gdx.graphics.getHeight() - margin - size, size, size);
    }

    /** เรียกทุกครั้งก่อนวาด ถ้าหน้าต่างเปลี่ยนขนาดจะอัพเดตตำแหน่งปุ่มอัตโนมัติ */
    public void onResize(int width, int height) {
        saveRect.setPosition(margin, height - margin - size);
        homeRect.setPosition(margin + size + 8, height - margin - size);
    }

    /** วาดปุ่ม (ใช้กล้อง UI) */
    public void render(SpriteBatch batch) {
        batch.draw(saveTex, saveRect.x, saveRect.y, saveRect.width, saveRect.height);
        batch.draw(homeTex, homeRect.x, homeRect.y, homeRect.width, homeRect.height);
    }

    /** ตรวจคลิกแล้วบอกว่ากดปุ่มไหน (ใช้กล้อง UI) */
    public Clicked updateAndHandleInput(OrthographicCamera uiCamera) {
        if (!Gdx.input.justTouched()) return Clicked.NONE;

        uiCamera.unproject(tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        if (saveRect.contains(tmp.x, tmp.y))  return Clicked.SAVE;
        if (homeRect.contains(tmp.x, tmp.y))  return Clicked.HOME;
        return Clicked.NONE;
    }

    public void dispose() {
        saveTex.dispose();
        homeTex.dispose();
    }
}
