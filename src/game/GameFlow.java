package game;

import animations.*;
import animations.Menu;
import biuoop.DialogManager;

import biuoop.KeyboardSensor;
import gamelevels.LevelInformation;
import geometry.Rectangle;
import leveldevelopment.LevelSpecificationReader;
import sprites.LiveIndicator;
import sprites.ScoreIndicator;


import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The GameFlow class controls the different levels of the game.
 *
 * @author Matan Ben Noach Nir Ben Shalom
 * @version 1.0 22 May 2016
 */
public class GameFlow {
    private AnimationRunner ar;
    private KeyboardSensor ks;
    private int lives;
    private ScoreIndicator score;
    private HighScoresTable scoresTable;
    private List<LevelInformation> levels;
    private String levelSets;
    private Map<String, ArrayList<String>> levelsMap;


    /**
     * Constructor for the GameFlow class.
     *
     * @param ar          The Animation Runner of the game.
     * @param ks          The KeyboardSensor of the game.
     * @param lives       is the number of live.
     * @param scoresTable is the HighScores table of the game.
     * @param fileName      is a list with the levels of the game.
     */
    public GameFlow(AnimationRunner ar, KeyboardSensor ks, int lives, HighScoresTable scoresTable, String fileName) {
        this.ar = ar;
        this.ks = ks;
        this.lives = lives;
        this.score = new ScoreIndicator();
        this.scoresTable = scoresTable;
        this.levelSets = fileName;


    }

    /**
     * chooseTask creates the tasks of the game, calls the menu to display them,
     * and runs the task that the user chooses.
     */
    public void chooseTask() {
        try {
            Menu<Task<Void>> menu = new MenuAnimation<Task<Void>>(ks);


            //Anonymous classes that defines each task's run method.
            Task<Void> hiScores = new Task<Void>() {
                @Override
                public Void run() {
                    ar.run(new HighScoresAnimation(scoresTable, "i", ks));
                    if (ks.isPressed(KeyboardSensor.SPACE_KEY)) {
                        chooseTask();
                    }
                    return null;
                }
            };

            Task<Void> playGame = new Task<Void>() {

                @Override
                public Void run() {
                    Menu<Task<Void>> subMenu = new MenuAnimation<Task<Void>>(ks);
                    List<LevelSet> myLevelSets = readSubLevels(levelSets);
                    for (LevelSet set : myLevelSets) {
                        subMenu.addSelection(set.getKey(), set.getName(), set.getSetTask());
                    }
                    Task<Void> task = subMenu.getStatus();
                    task.run();
                    return null;
                }

            };

            Task<Void> quitGame = new Task<Void>() {
                @Override
                public Void run() {
                    Runtime.getRuntime().exit(1);
                    return null;
                }
            };

            //Adding the tasks to the tasks list.
            menu.addSelection("h", "High scores", hiScores);
            menu.addSelection("s", "Play", playGame);
            menu.addSelection("q", "Quit", quitGame);

            /*Menu<Task<Void>> subMenu = new MenuAnimation<Task<Void>>(ks);
            levelsMap = readSubLevels(levelSets);
            subMenu.addSelection("h", levelsMap.get("h").get(0), hardSet);*/
            while (true) {
                ar.run(menu);
                // wait for user selection
                Task<Void> task = menu.getStatus();
                task.run();
            }
        } catch (Exception e) {
            System.out.print("Task Error");
        }
    }

    /**
     * LevelSet class is a private class for a level set.
     */
    private class LevelSet {
        private String key; // The key to press to choose the level set.
        private String name; // The name of the level set.
        private String levelPath; // The file of the levels for the level set.
        Task<Void> setTask;

        /**
         * Constructor for the level set.
         *
         * @param key  the key to press to choose the level set.
         * @param name // The name of the level set.
         */
        public LevelSet(String key, String name) {
            this.key = key;
            this.name = name;
        }

        /**
         * getKey returns the key to choose the level set.
         *
         * @return the key to choose the level set.
         */
        public String getKey() {
            return key;
        }

        /**
         * getName returns the name of the level set.
         *
         * @return the name of the level set.
         */
        public String getName() {
            return name;
        }

        /**
         * getLevelPath returns the file of the levels for the level set.
         *
         * @return The file of the levels for the level set.
         */
        public String getLevelPath() {
            return levelPath;
        }

        /**
         * setLevelPath sets the path for the file of the levels for the level set.
         *
         * @param levelPath the file of the levels for the level set.
         */
        public void setLevelPath(String levelPath) {
            this.levelPath = levelPath;
        }

        public Task<Void> getSetTask() {
            return setTask;
        }

        public void setSetTask() {
            setTask = new Task<Void>() {
                @Override
                public Void run() {
                    levels = getListOfLevels(getLevelPath());
                    runLevels(levels);
                    return null;
                }
            };
        }
    }

    /**
     * runLevels method runs a given list of levels.
     *
     * @param levels a list of levels.
     */
    public void runLevels(List<LevelInformation> levels) {
        LiveIndicator liveIndicator = new LiveIndicator(lives);
        score = new ScoreIndicator();
        for (LevelInformation levelInfo : levels) {
            GameLevel level = new GameLevel(levelInfo, this.ks, this.ar);
            level.initialize(liveIndicator, score);
            while (level.getBlockCounter().getValue() != 0 && liveIndicator.getValue() != 0) {
                level.playOneTurn();
            }

            if (level.getBlockCounter().getValue() == 0) {
                score.increase(100);
            }

            if (liveIndicator.getValue() == 0) {
                break;
            }
        }
        ar.run(new StopScreenDecorator(ks, "o", new EndScreen(ks, liveIndicator, score)));
        DialogManager dialog = ar.getGui().getDialogManager();
        String name = dialog.showQuestionDialog("Name", "What is your name?", "");
        scoresTable.add(new ScoreInfo(name, score.getScore().getValue()));
        ar.run(new StopScreenDecorator(ks, "t", new HighScoresAnimation(scoresTable, "t", ks)));
        try {
            scoresTable.save(new File("highscores"));
        } catch (IOException e) {
            System.out.println("Error saving file");
        }
        this.chooseTask();
    }

    /**
     * readSubLevels method returns information on the keys of the level sets and the path of the files for them.
     *
     * @param fileName the file name for level sets.
     * @return a map which will include the key to navigate through the sub menu and the value will be an array list
     * of the name of the level set and the path of the file for them.
     */

    public List<LevelSet> readSubLevels(String fileName) {
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("resources\\" + fileName);
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
        List<LevelSet> levelSets = new ArrayList<LevelSet>();
        String[] levelKeyName = null;
        String line;
        LevelSet currentLevelSet = null;
        try {
            while ((line = reader.readLine()) != null) {
                if (reader.getLineNumber() % 2 == 1) {
                    levelKeyName = line.split(":");
                    currentLevelSet = new LevelSet(levelKeyName[0], levelKeyName[1]);
                } else {
                    currentLevelSet.setLevelPath(line);
                    currentLevelSet.setSetTask();
                    levelSets.add(currentLevelSet);
                }
            }
        } catch (IOException e) {
            System.out.println("Unable to read set files");
        }
        return levelSets;
    }

    public List<LevelInformation> getListOfLevels(String levelFileNames) {
        BufferedReader buffer = null;
        List<LevelInformation> levels = null;
        LevelSpecificationReader levelReader = new LevelSpecificationReader(new Rectangle(0, 0, 800, 600));
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream
                ("resources\\" + levelFileNames);
        buffer = new BufferedReader(new InputStreamReader(is));
        levels = levelReader.fromReader(buffer);
        try {
            buffer.close();
        } catch (IOException e) {
            System.out.println("Error closing level file");
        }
        return levels;
    }

}
