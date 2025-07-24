package game.general_luna_game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Leaderboard {
    private static final int MAX_ENTRIES = 10;

    private List<ScoreEntry> leaderboard = new ArrayList<>();

    public void addEntry(String name, long time) {
        leaderboard.add(new ScoreEntry(name, time));
        leaderboard.sort(Comparator.comparingLong(ScoreEntry::getTime));

        if (leaderboard.size() > MAX_ENTRIES) {
            leaderboard = new ArrayList<>(leaderboard.subList(0, MAX_ENTRIES));
        }
    }

    public List<ScoreEntry> getEntries() {
        return leaderboard;
    }

    public static class ScoreEntry {
        private final String name;
        private final long time;

        public ScoreEntry(String name, long time) {
            this.name = name;
            this.time = time;
        }

        public String getName() {
            return name;
        }

        public long getTime() {
            return time;
        }
    }
}
