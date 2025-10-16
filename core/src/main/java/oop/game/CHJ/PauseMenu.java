package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
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

    // กรอบตรวจคลิก
    private final Rectangle beginBounds = new Rectangle();
    private final Rectangle resumeBounds = new Rectangle();
    private final Rectangle exitBounds = new Rectangle();

    // ui
    private final Vector3 tmp = new Vector3();
    private final BitmapFont font = new BitmapFont();

    // ควบคุมการแสดงผล
    private boolean visible = false;

    // สเกลของปุ่มและช่องไฟ
    private float buttonScale = 0.7f;
    private float gapY = 22f;

    // 🔊 เสียงคลิก (ใช้ไฟล์เดียวกับ HUD)
    private final Sound clickSound;

    // เงาตอนกด
    private final float pressAlpha = 0.4f;

    public PauseMenu() {
        // พิกเซลสีขาว 1×1 สำหรับ tint เป็นพื้นหลัง/เงา
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        dimPx = new Texture(pm);
        pm.dispose();

        beginTex  = new Texture("Botton/BeginBT.png");
        resumeTex = new Texture("Botton/ResumeBT.png");
        exitTex   = new Texture("Botton/ExitBT.png");

        font.setColor(Color.WHITE);
        font.getData().setScale(1f);

        // โหลดเสียงคลิก
        clickSound = Gdx.audio.newSound(Gdx.files.internal("Music/Click/Click.mp3"));

        layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void show()  { visible = true; }
    public void hide()  { visible = false; }
    public boolean isVisible() { return visible; }

    /** จัดวางปุ่มให้อยู่กึ่งกลางจอ */
    private void layout(float screenW, float screenH) {
        float bw = beginTex.getWidth()  * buttonScale;
        float bh = beginTex.getHeight() * buttonScale;
        float rw = resumeTex.getWidth() * buttonScale;
        float rh = resumeTex.getHeight()* buttonScale;
        float ew = exitTex.getWidth()   * buttonScale;
        float eh = exitTex.getHeight()  * buttonScale;

        float maxW = Math.max(bw, Math.max(rw, ew));
        float cx = (screenW - maxW) / 2f;

        float totalH = bh + gapY + rh + gapY + eh;
        float startY = (screenH + totalH) / 2f - bh;

        beginBounds.set(cx + (maxW - bw)/2f, startY, bw, bh);
        resumeBounds.set(cx + (maxW - rw)/2f, startY - (bh + gapY) - 0.5f, rw, rh);
        exitBounds.set(cx + (maxW - ew)/2f, startY - (bh + gapY + rh + gapY + 1f), ew, eh);
    }

    /** วาดเมนู */
    public void render(SpriteBatch batch, OrthographicCamera uiCamera) {
        if (!visible) return;

        // ฉากหลังมืด
        batch.setColor(0, 0, 0, 0.75f);
        batch.draw(dimPx, 0, 0, uiCamera.viewportWidth, uiCamera.viewportHeight);
        batch.setColor(Color.WHITE);

        // สถานะกด (กำหนดจากการแตะและตำแหน่งเมาส์)
        boolean pressingBegin  = isPressed(beginBounds, uiCamera);
        boolean pressingResume = isPressed(resumeBounds, uiCamera);
        boolean pressingExit   = isPressed(exitBounds, uiCamera);

        // วาดปุ่ม (ถ้าไม่ได้กด ให้ทำเหมือน hover เล็กน้อยตอนแตะค้าง)
        drawButton(batch, beginTex,  beginBounds,  !pressingBegin);
        drawButton(batch, resumeTex, resumeBounds, !pressingResume);
        drawButton(batch, exitTex,   exitBounds,   !pressingExit);

        // เงาทับเมื่อ "กำลังกด"
        if (pressingBegin) {
            batch.setColor(0, 0, 0, pressAlpha);
            batch.draw(dimPx, beginBounds.x, beginBounds.y, beginBounds.width, beginBounds.height);
            batch.setColor(Color.WHITE);
        }
        if (pressingResume) {
            batch.setColor(0, 0, 0, pressAlpha);
            batch.draw(dimPx, resumeBounds.x, resumeBounds.y, resumeBounds.width, resumeBounds.height);
            batch.setColor(Color.WHITE);
        }
        if (pressingExit) {
            batch.setColor(0, 0, 0, pressAlpha);
            batch.draw(dimPx, exitBounds.x, exitBounds.y, exitBounds.width, exitBounds.height);
            batch.setColor(Color.WHITE);
        }
    }

    private void drawButton(SpriteBatch batch, Texture tex, Rectangle b, boolean lit) {
        if (lit) batch.setColor(0.95f, 0.95f, 0.95f, 1f);
        else     batch.setColor(0.92f, 0.92f, 0.92f, 1f);
        batch.draw(tex, b.x, b.y, b.width, b.height);
        batch.setColor(Color.WHITE);
    }

    private boolean isPressed(Rectangle r, OrthographicCamera uiCamera) {
        if (!Gdx.input.isTouched()) return false;
        uiCamera.unproject(tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        return r.contains(tmp.x, tmp.y);
    }

    /** คืนค่าการคลิกปุ่ม + เล่นเสียงคลิก */
    public Action checkInput(OrthographicCamera uiCamera) {
        if (!visible || !Gdx.input.justTouched()) return Action.NONE;

        uiCamera.unproject(tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0));

        if (beginBounds.contains(tmp.x, tmp.y))  { clickSound.play(0.6f); return Action.BEGIN;  }
        if (resumeBounds.contains(tmp.x, tmp.y)) { clickSound.play(0.6f); return Action.RESUME; }
        if (exitBounds.contains(tmp.x, tmp.y))   { clickSound.play(0.6f); return Action.EXIT;   }
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
        clickSound.dispose();
    }
}
