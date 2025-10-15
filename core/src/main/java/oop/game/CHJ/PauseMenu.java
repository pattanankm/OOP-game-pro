package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

/** เมนู Pause ใช้ปุ่มรูปภาพ: Begin, Resume, Exit */
public class PauseMenu {

    public enum Action { NONE, BEGIN, RESUME, EXIT }

    // พื้นหลังมืดโปร่งใส
    private final Texture dimPx;

    // ปุ่มเป็นรูปภาพ
    private final Texture beginTex;
    private final Texture resumeTex;
    private final Texture exitTex;

    // กรอบตรวจคลิก (คำนวณตามสเกล)
    private final Rectangle beginBounds = new Rectangle();
    private final Rectangle resumeBounds = new Rectangle();
    private final Rectangle exitBounds = new Rectangle();

    // ui
    private final Vector3 tmp = new Vector3();
    private final BitmapFont font = new BitmapFont(); // เผื่ออนาคตอยากแคปชันเพิ่ม

    // ควบคุมการแสดงผล
    private boolean visible = false;

    // สเกลของปุ่มและช่องไฟ
    private float buttonScale = 0.7f;   // ปรับขนาดปุ่มจากไฟล์ต้นฉบับ (0.7 = 70%)
    private float gapY = 22f;           // ช่องว่างระหว่างปุ่ม

    public PauseMenu() {
        // สร้างพิกเซลโปร่งใสสำหรับพื้นหลังมืด
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        dimPx = new Texture(pm);
        pm.dispose();

        // โหลดรูปปุ่ม (ใช้ไฟล์ในโฟลเดอร์ Botton)
        beginTex  = new Texture("Botton/BeginBT.png");
        resumeTex = new Texture("Botton/ResumeBT.png");
        exitTex   = new Texture("Botton/ExitBT.png");

        font.setColor(Color.WHITE);
        font.getData().setScale(1f);

        // จัดวางครั้งแรกตามหน้าจอปัจจุบัน
        layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void show()  { visible = true; }
    public void hide()  { visible = false; }
    public boolean isVisible() { return visible; }

    /** จัดวางปุ่มให้อยู่กึ่งกลางจอ เรียงจากบนลงล่าง: Begin → Resume → Exit */
    private void layout(float screenW, float screenH) {
        // ขนาดปุ่มหลังสเกล (ใช้จากไฟล์จริง)
        float bw = beginTex.getWidth()  * buttonScale;
        float bh = beginTex.getHeight() * buttonScale;
        float rw = resumeTex.getWidth() * buttonScale;
        float rh = resumeTex.getHeight()* buttonScale;
        float ew = exitTex.getWidth()   * buttonScale;
        float eh = exitTex.getHeight()  * buttonScale;

        // ใช้ความกว้างมากสุดเป็นฐานกึ่งกลาง
        float maxW = Math.max(bw, Math.max(rw, ew));
        float cx = (screenW - maxW) / 2f;

        // จุดเริ่มแกน Y (ให้ทั้ง stack อยู่กลางแนวตั้ง)
        float totalH = bh + gapY + rh + gapY + eh;
        float startY = (screenH + totalH) / 2f - bh; // เริ่มต้นตำแหน่งปุ่มบนสุด

        beginBounds.set(cx + (maxW - bw)/2f, startY, bw, bh);
        resumeBounds.set(cx + (maxW - rw)/2f, startY - (bh + gapY), rw, rh);
        exitBounds.set(cx + (maxW - ew)/2f, startY - (bh + gapY + rh + gapY), ew, eh);
    }

    /** วาดเมนู */
    public void render(SpriteBatch batch, OrthographicCamera uiCamera) {
        if (!visible) return;

        // ฉากหลังมืด
        batch.setColor(0, 0, 0, 0.75f);
        batch.draw(dimPx, 0, 0, uiCamera.viewportWidth, uiCamera.viewportHeight);
        batch.setColor(Color.WHITE);

        // ตรวจ hover
        boolean hBegin  = isHovered(beginBounds, uiCamera);
        boolean hResume = isHovered(resumeBounds, uiCamera);
        boolean hExit   = isHovered(exitBounds, uiCamera);

        // วาดปุ่ม (hover = สว่างขึ้นเล็กน้อย)
        drawButton(batch, beginTex,  beginBounds,  hBegin);
        drawButton(batch, resumeTex, resumeBounds, hResume);
        drawButton(batch, exitTex,   exitBounds,   hExit);
    }

    private void drawButton(SpriteBatch batch, Texture tex, Rectangle b, boolean hover) {
        if (hover) batch.setColor(1f, 1f, 1f, 1f);
        else       batch.setColor(0.92f, 0.92f, 0.92f, 1f);
        batch.draw(tex, b.x, b.y, b.width, b.height);
        batch.setColor(Color.WHITE);
    }

    private boolean isHovered(Rectangle r, OrthographicCamera uiCamera) {
        if (!Gdx.input.isTouched()) return false;
        uiCamera.unproject(tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        return r.contains(tmp.x, tmp.y);
    }

    /** คืนค่าการคลิกปุ่ม (เรียกในเฟรมเดียวกับ render) */
    public Action checkInput(OrthographicCamera uiCamera) {
        if (!visible || !Gdx.input.justTouched()) return Action.NONE;
        uiCamera.unproject(tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        if (beginBounds.contains(tmp.x, tmp.y))  return Action.BEGIN;
        if (resumeBounds.contains(tmp.x, tmp.y)) return Action.RESUME;
        if (exitBounds.contains(tmp.x, tmp.y))   return Action.EXIT;
        return Action.NONE;
    }

    /** เรียกจาก Screen.resize(...) */
    public void onResize(int width, int height) {
        layout(width, height);
    }

    public void dispose() {
        dimPx.dispose();
        beginTex.dispose();
        resumeTex.dispose();
        exitTex.dispose();
        font.dispose();
    }
}
