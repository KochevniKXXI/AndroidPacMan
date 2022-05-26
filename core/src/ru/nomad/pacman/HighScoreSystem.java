package ru.nomad.pacman;

import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

public class HighScoreSystem {
    private static String[] names;
    private static int[] scores;

    static {
        names = new String[10];
        scores = new int[10];
    }

    public static String[] getNames() {
        return names;
    }

    public static int[] getScores() {
        return scores;
    }

    public static void saveResults() {
        Writer writer = null;
        try {
            writer = Gdx.files.local("score.dat").writer(false);
            for (int i = 0; i < 10; i++) {
                writer.write(names[i] + " " + scores[i] + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createDefaultTable() {
        Writer writer = null;
        try {
            writer = Gdx.files.local("score.dat").writer(false);
            for (int i = 0; i < 10; i++) {
                writer.write("Player 0\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean checkScore(int score) {
        return score > scores[9];
    }

    public static void addResult(String name, int score) {
        int n = -1;
        for (int i = 0; i < 10; i++) {
            if(score >= scores[i]) {
                n = i;
                break;
            }
        }
        for (int i = 9; i > n; i--) {
            names[i] = names[i - 1];
            scores[i] = scores[i - 1];
        }
        names[n] = name;
        scores[n] = score;
        saveResults();
    }

    public static void loadResults() {
        if (!Gdx.files.local("score.dat").exists()) {
            createDefaultTable();
        }
        BufferedReader br = null;
        try {
            br = Gdx.files.local("score.dat").reader(8192);
            for (int i = 0; i < 10; i++) {
                String[] str = br.readLine().split("\\s");
                names[i] = str[0];
                scores[i] = Integer.parseInt(str[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            createDefaultTable();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
