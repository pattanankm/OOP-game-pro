package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class HintTextUI {

    private final BitmapFont font;
    private final GlyphLayout layout;
    private final Texture bgPixel;

    private String currentHint = "";
    private float showTimer = 0f;
    private float fadeDuration = 0.5f;  // เวลา fade in/out
    private float displayDuration = 8f; // เวลาแสดงข้อความ
    private float alpha = 0f;

    public HintTextUI() {
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        layout = new GlyphLayout();

        // สร้างพื้นหลังสีดำโปร่งใส
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.BLACK);
        pm.fill();
        bgPixel = new Texture(pm);
        pm.dispose();
    }

    //แสดงข้อความใหม่
    public void showHint(String text) {
        if (!text.equals(currentHint)) {
            currentHint = text;
            showTimer = 0f;
        }
    }

    //อัพเดตเวลา
    public void update(float delta) {
        if (currentHint.isEmpty()) return;

        showTimer += delta;

        // Fade in
        if (showTimer < fadeDuration) {
            alpha = showTimer / fadeDuration;
        }
        // แสดงเต็มที่
        else if (showTimer < fadeDuration + displayDuration) {
            alpha = 1f;
        }
        // Fade out
        else if (showTimer < fadeDuration * 2 + displayDuration) {
            alpha = 1f - ((showTimer - fadeDuration - displayDuration) / fadeDuration);
        }
        // จบการแสดง
        else {
            currentHint = "";
            alpha = 0f;
            showTimer = 0f;
        }
    }

    // วาดข้อความ (ใช้กล้อง UI)
    public void render(SpriteBatch batch, float screenWidth, float screenHeight) {
        if (currentHint.isEmpty() || alpha <= 0) return;

        layout.setText(font, currentHint);

        float textX = (screenWidth - layout.width) / 2;
        float textY = 100; // ด้านล่างหน้าจอ
        float padding = 15f;

        // พื้นหลังสีดำ
        batch.setColor(0, 0, 0, alpha * 0.7f);
        batch.draw(bgPixel,
            textX - padding,
            textY - layout.height - padding,
            layout.width + padding * 2,
            layout.height + padding * 2);

        // ข้อความสีขาว
        font.setColor(1, 1, 1, alpha);
        font.draw(batch, currentHint, textX, textY);

        // Reset color
        batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
    }

    public void dispose() {
        font.dispose();
        bgPixel.dispose();
    }
}
