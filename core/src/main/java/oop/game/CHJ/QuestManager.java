package oop.game.CHJ;

public class QuestManager {
    private boolean quest1Started = false;
    private boolean quest1Completed = false;
    private boolean quest2Started = false;
    private boolean quest2Completed = false;

    public void startQuest1() { quest1Started = true; }
    public void completeQuest1() { quest1Completed = true; }

    public void startQuest2() { quest2Started = true; }
    public void completeQuest2() { quest2Completed = true; }

    public boolean isQuest1Started() { return quest1Started; }
    public boolean isQuest1Completed() { return quest1Completed; }

    public boolean isQuest2Started() { return quest2Started; }
    public boolean isQuest2Completed() { return quest2Completed; }
}
