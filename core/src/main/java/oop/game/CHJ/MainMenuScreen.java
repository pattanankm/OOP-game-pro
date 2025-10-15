package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class MainMenuScreen implements Screen {

    final Main game;
    Texture backgroundTexture, nameBoxTexture, characterButtonTexture, startButtonTexture;
    Texture maleTexture, femaleTexture;
    Rectangle nameInputBox, characterButton, startButton, maleButton, femaleButton, selectedCharacterDisplay;
    Vector3 touchPos;
    GlyphLayout layout;
    String playerName = "";
    boolean isInputActive = false, nameConfirmed = false;
    boolean showCharacterSelection = false;
    String selectedGender = "";
    boolean isCharacterButtonPressed = false, isStartButtonPressed = false;
    boolean isMalePressed = false, isFemalePressed = false;
    float cursorBlinkTime = 0;
    private Music menuMusic;

    public MainMenuScreen(final Main game) {
        this.game = game;

        backgroundTexture        = new Texture("Menu/menu.png");
        nameBoxTexture           = new Texture("Menu/name.png");
        characterButtonTexture   = new Texture("Menu/character.png");
        startButtonTexture       = new Texture("Menu/start.png");
        maleTexture              = new Texture("Menu/male.png");
        femaleTexture            = new Texture("Menu/female.png");

        float nameBoxWidth = 2.2f, nameBoxHeight = 0.4f;
        float nameBoxX = (game.viewport.getWorldWidth() - nameBoxWidth) / 2;
        nameInputBox = new Rectangle(nameBoxX, 1.5f, nameBoxWidth, nameBoxHeight);

        characterButton = new Rectangle(1.2f, 0.4f, 2.8f, 0.7f);
        float startButtonX = game.viewport.getWorldWidth() - 2.8f - 1.1f;
        startButton = new Rectangle(startButtonX, 0.4f, 2.8f, 0.7f);

        float centerX = game.viewport.getWorldWidth() / 2;
        maleButton = new Rectangle(centerX - 2f - 0.5f, 1.7f, 2f, 2.5f);
        femaleButton = new Rectangle(centerX + 0.5f, 1.7f, 2f, 2.5f);

        selectedCharacterDisplay = new Rectangle(0.5f, 1.5f, 1.6f, 2.2f);

        touchPos = new Vector3();
        layout = new GlyphLayout();

        setupInputProcessor();
    }

    private void setupInputProcessor() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (!isInputActive) return false;
                if (character == '\b' && playerName.length() > 0) playerName = playerName.substring(0, playerName.length() - 1);
                else if (character == '\r' || character == '\n') { nameConfirmed = true; isInputActive = false; }
                else if (character >= 32 && character < 127 && playerName.length() < 15) playerName += character;
                return true;
            }
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY, 0);
                game.viewport.unproject(touchPos);
                if (nameInputBox.contains(touchPos.x, touchPos.y)) {
                    isInputActive = true;
                    showCharacterSelection = false;
                    return true;
                } else isInputActive = false;
                return false;
            }
        });
    }

    @Override
    public void show() {
        setupInputProcessor();
        if (menuMusic == null) {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/StageMusic/Menu.mp3"));
            menuMusic.setLooping(true);
            menuMusic.setVolume(0.35f);
        }
        menuMusic.play();
    }

    @Override
    public void hide() {
        if (menuMusic != null) menuMusic.stop();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        cursorBlinkTime += delta;
        game.batch.begin();

        game.batch.draw(backgroundTexture, 0, 0, game.viewport.getWorldWidth(), game.viewport.getWorldHeight());
        game.font.setColor(Color.BLACK);
        String displayName = playerName + ((isInputActive && (int)(cursorBlinkTime * 2) % 2 == 0) ? "|" : "");
        game.font.draw(game.batch, displayName, nameInputBox.x + 0.5f, nameInputBox.y + nameInputBox.height / 2 + 0.08f);

        game.batch.draw(characterButtonTexture, characterButton.x, characterButton.y, characterButton.width, characterButton.height);
        game.batch.draw(startButtonTexture, startButton.x, startButton.y, startButton.width, startButton.height);

        if (showCharacterSelection) {
            game.batch.setColor(0, 0, 0, 0.7f);
            game.batch.draw(game.pixel, 0, 0, game.viewport.getWorldWidth(), game.viewport.getWorldHeight());
            game.batch.setColor(Color.WHITE);
            game.font.setColor(Color.YELLOW);
            String selectText = "Select Your Character";
            layout.setText(game.font, selectText);
            game.font.draw(game.batch, selectText, (game.viewport.getWorldWidth() - layout.width) / 2f, 4.6f);
            game.batch.draw(maleTexture, maleButton.x, maleButton.y, maleButton.width, maleButton.height);
            game.batch.draw(femaleTexture, femaleButton.x, femaleButton.y, femaleButton.width, femaleButton.height);
            game.font.setColor(Color.CYAN);
            game.font.draw(game.batch, "Male", maleButton.x + maleButton.width / 3f, maleButton.y - 0.2f);
            game.font.draw(game.batch, "Female", femaleButton.x + femaleButton.width / 4f, femaleButton.y - 0.2f);
        }

        if (!selectedGender.isEmpty()) {
            Texture t = selectedGender.equals("male") ? maleTexture : femaleTexture;
            game.batch.draw(t, selectedCharacterDisplay.x, selectedCharacterDisplay.y, selectedCharacterDisplay.width, selectedCharacterDisplay.height);
            if (nameConfirmed && !playerName.isEmpty()) {
                layout.setText(game.font, playerName);
                float pad = 0.15f;
                float boxX = selectedCharacterDisplay.x + (selectedCharacterDisplay.width - layout.width) / 2 - pad;
                float boxY = selectedCharacterDisplay.y;
//                float boxW = layout.width + pad * 2, boxH = layout.height + pad * 2;
//                game.batch.setColor(0, 0, 0, 0.5f);
//                game.batch.draw(game.pixel, boxX, boxY - layout.height, boxW, boxH);
                game.batch.setColor(Color.WHITE);
                drawOutlinedText(playerName, boxX + pad, boxY, Color.WHITE, Color.BLACK);
            }
        }
        game.batch.end();

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.viewport.unproject(touchPos);
            if (showCharacterSelection) {
                if (maleButton.contains(touchPos.x, touchPos.y)) isMalePressed = true;
                else if (femaleButton.contains(touchPos.x, touchPos.y)) isFemalePressed = true;
            } else {
                if (characterButton.contains(touchPos.x, touchPos.y)) isCharacterButtonPressed = true;
                else if (startButton.contains(touchPos.x, touchPos.y)) isStartButtonPressed = true;
            }
        } else {
            if (isCharacterButtonPressed) { showCharacterSelection = true; isCharacterButtonPressed = false; }
            if (isMalePressed) { selectedGender = "male"; showCharacterSelection = false; isMalePressed = false; }
            if (isFemalePressed) { selectedGender = "female"; showCharacterSelection = false; isFemalePressed = false; }

            if (isStartButtonPressed) {
                if (!playerName.isEmpty() && !selectedGender.isEmpty()) {
                    game.playerName = playerName;
                    game.selectedGender = selectedGender;
                    if (menuMusic != null) menuMusic.stop();
                    game.setScreen(new FirstScreen(game));
                    dispose();
                }
                isStartButtonPressed = false;
            }
        }
    }

    private void drawOutlinedText(String text, float x, float y, Color fillColor, Color outlineColor) {
        float offset = 0.02f;
        game.font.setColor(outlineColor);
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                if (!(dx == 0 && dy == 0))
                    game.font.draw(game.batch, text, x + dx * offset, y + dy * offset);
        game.font.setColor(fillColor);
        game.font.draw(game.batch, text, x, y);
    }

    @Override public void resize(int w, int h) { game.viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        backgroundTexture.dispose(); nameBoxTexture.dispose();
        characterButtonTexture.dispose(); startButtonTexture.dispose();
        maleTexture.dispose(); femaleTexture.dispose();
        if (menuMusic != null) menuMusic.dispose();
        Gdx.input.setInputProcessor(null);
    }
}
