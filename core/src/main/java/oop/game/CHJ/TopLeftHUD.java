package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class TopLeftHUD {

    public enum Clicked { NONE, SAVE, HOME }

    private final Texture saveTex;
    private final Texture homeTex;
    private final Rectangle saveRect;
    private final Rectangle homeRect;
    private final Vector3 tmp = new Vector3();

    private final float size;
    private final float margin;

    //เสียงคลิก
    private final Sound clickSound;

    //สถานะกดปุ่ม
    private boolean savePressed = false;
    private boolean homePressed = false;
    private float pressAlpha = 0.4f; // ความเข้มของเงา (0=โปร่ง,1=ทึบ)

    public TopLeftHUD(String savePath, String homePath) {
        this(savePath, homePath, 56f, 12f);
    }

    public TopLeftHUD(String savePath, String homePath, float size, float margin) {
        this.size = size;
        this.margin = margin;
        this.saveTex = new Texture(savePath);
        this.homeTex = new Texture(homePath);

        this.saveRect = new Rectangle(margin, Gdx.graphics.getHeight() - margin - size, size, size);
        this.homeRect = new Rectangle(margin + size + 8, Gdx.graphics.getHeight() - margin - size, size, size);

        // โหลดเสียงคลิก
        clickSound = Gdx.audio.newSound(Gdx.files.internal("Music/Click/Click.mp3"));
    }

    //เรียกทุกครั้งก่อนวาด อัพเดตตำแหน่งปุ่มอัตโนมัติ
    public void onResize(int width, int height) {
        saveRect.setPosition(margin, height - margin - size);
        homeRect.setPosition(margin + size + 8, height - margin - size);
    }

    // วาดปุ่ม (ใช้กล้อง UI)
    public void render(SpriteBatch batch) {
        batch.draw(saveTex, saveRect.x, saveRect.y, saveRect.width, saveRect.height);
        batch.draw(homeTex, homeRect.x, homeRect.y, homeRect.width, homeRect.height);

        //วาดเงาทับเมื่อกดปุ่ม
        if (savePressed) {
            batch.setColor(0, 0, 0, pressAlpha);
            batch.draw(saveTex, saveRect.x, saveRect.y, saveRect.width, saveRect.height);
            batch.setColor(Color.WHITE);
        }
        if (homePressed) {
            batch.setColor(0, 0, 0, pressAlpha);
            batch.draw(homeTex, homeRect.x, homeRect.y, homeRect.width, homeRect.height);
            batch.setColor(Color.WHITE);
        }
    }

    //ตรวจคลิกแล้วบอกว่ากดปุ่มไหน (ใช้กล้อง UI)
    public Clicked updateAndHandleInput(OrthographicCamera uiCamera) {
        if (Gdx.input.justTouched()) {
            uiCamera.unproject(tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            if (saveRect.contains(tmp.x, tmp.y)) {
                savePressed = true;
                clickSound.play(1f);
                return Clicked.SAVE;
            }
            if (homeRect.contains(tmp.x, tmp.y)) {
                homePressed = true;
                clickSound.play(1);
                return Clicked.HOME;
            }
        }

        // ปล่อยคลิก → ปิดเงา
        if (!Gdx.input.isTouched()) {
            savePressed = false;
            homePressed = false;
        }
        return Clicked.NONE;
    }

    public void dispose() {
        saveTex.dispose();
        homeTex.dispose();
        clickSound.dispose();
    }
}
