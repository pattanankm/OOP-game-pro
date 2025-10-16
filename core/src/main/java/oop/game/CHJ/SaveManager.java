package oop.game.CHJ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SaveManager {
    private static final String PREF = "OOPGameSave";

    public static class SaveState {
        public String screen = "FirstScreen";
        public float x = 0, y = 0;
        public boolean gear = false, book = false, airOff = false;
        public int questStep = 0; // 0 start, 1 talk giraffe, 2 touch hand, 3 got gear, 4 go library, 5 air off
    }

    public static boolean hasSave() { return Gdx.app.getPreferences(PREF).contains("screen"); }

    public static SaveState load() {
        Preferences p = Gdx.app.getPreferences(PREF);
        SaveState s = new SaveState();
        s.screen    = p.getString("screen", "FirstScreen");
        s.x         = p.getFloat("x", 0);
        s.y         = p.getFloat("y", 0);
        s.gear      = p.getBoolean("gear", false);
        s.book      = p.getBoolean("book", false);
        s.airOff    = p.getBoolean("airOff", false);
        s.questStep = p.getInteger("questStep", 0);
        return s;
    }

    public static void save(SaveState s) {
        Preferences p = Gdx.app.getPreferences(PREF);
        p.putString("screen", s.screen);
        p.putFloat("x", s.x);
        p.putFloat("y", s.y);
        p.putBoolean("gear", s.gear);
        p.putBoolean("book", s.book);
        p.putBoolean("airOff", s.airOff);
        p.putInteger("questStep", s.questStep);
        p.flush();
    }

    // helpers
    public static void setScreen(String screen){
        SaveState s = load(); s.screen = screen; save(s);
    }
    public static void setPosition(float x, float y){
        SaveState s = load(); s.x = x; s.y = y; save(s);
    }
    public static void setItems(boolean gear, boolean book){
        SaveState s = load(); s.gear = gear; s.book = book; save(s);
    }
    public static void setQuestStep(int step){
        SaveState s = load(); s.questStep = Math.max(s.questStep, step); save(s);
    }
    public static void markAirOff(){
        SaveState s = load(); s.airOff = true; s.questStep = Math.max(s.questStep, 5); save(s);
    }


    public static void clearAll() {
        Preferences p = Gdx.app.getPreferences(SaveManager.PREF);
        p.clear();
        p.flush();
    }
}
