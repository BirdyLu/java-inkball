package inkball;
import processing.data.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException; 
import java.util.*;

import processing.core.PVector;
import processing.data.JSONArray;

/**
 * The {@code Level} class represents a game level in the Inkball game. 
 * It contains information about the level layout, time constraints, 
 * spawn intervals, score modifiers, and manages the balls available 
 * for the level.
 * 
 * <p>This class provides methods for loading level data from a file, 
 * managing ball spawning, and retrieving level information, making it 
 * essential for gameplay mechanics.</p>
 */
public class Level extends App {
    /** The file path for the level layout. */
    private String filePath;
    
    /** The File object representing the level layout file. */
    private File f;
    
    /** The layout of the level represented as a list of strings. */
    private ArrayList<String> layout;
    
    /** The time limit for the level in seconds. */
    private int time; 
    
    /** The interval at which balls spawn during the level. */
    private int spawnInterval;
    
    /** The modifier for score increases from hole captures. */
    private float scoreIncreaseModifier;
    
    /** The modifier for score decreases from wrong hole captures. */
    private float scoreDecreaseModifier;
    
    /** The JSON array representing balls available in the level. */
    private JSONArray ballsJSON;
    
    /** A queue of balls that are loaded for the level. */
    private Queue<Ball> loadedBalls = new LinkedList<>();
    
    /** A list of spawn points for the balls. */
    private ArrayList<PVector> spawners;

    /**
     * Constructs a {@code Level} object from the provided JSON object 
     * representing level data.
     * 
     * @param level the {@code JSONObject} containing level attributes.
     */
    public Level(JSONObject level) {
        filePath = level.getString("layout");
        time = level.getInt("time");
        spawnInterval = level.getInt("spawn_interval");
        scoreIncreaseModifier = level.getFloat("score_increase_from_hole_capture_modifier");
        scoreDecreaseModifier = level.getFloat("score_decrease_from_wrong_hole_modifier");
        ballsJSON = level.getJSONArray("balls");
        spawners = new ArrayList<>();

        readFile(filePath);
    }

    /**
     * Returns the score increase modifier for the level.
     * 
     * @return the score increase modifier as a {@code float}.
     */
    public float getIncreaseModifier(){
        return scoreIncreaseModifier;
    }

    /**
     * Returns the score decrease modifier for the level.
     * 
     * @return the score decrease modifier as a {@code float}.
     */
    public float getDecreaseModifier(){
        return scoreDecreaseModifier;
    }

    /**
     * Reads the level layout from the specified file path.
     * 
     * @param filePath the path to the layout file.
     * @return {@code true} if the file was read successfully; 
     *         {@code false} otherwise.
     */
    public boolean readFile(String filePath) {
        try {
            f = new File(filePath);
            Scanner scan = new Scanner(f);
            layout = new ArrayList<>();
            while (scan.hasNextLine()) {
                String line = scan.nextLine(); // Read each line
                if (!line.isEmpty()){
                    layout.add(line);
                }
            }
            scan.close();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    /**
     * Loads balls into the level from the predefined JSON array.
     */
    public void loadBalls(){
        for (int i=0; i<ballsJSON.size(); i++){
            PVector spawner = getRandSpawner();
            switch (ballsJSON.get(i).toString()) {
                case "grey":
                    loadedBalls.add(new Ball("B0", spawner.x * CELLSIZE+4, spawner.y * CELLSIZE+4));
                    break;
                case "orange":
                    loadedBalls.add(new Ball("B1", spawner.x * CELLSIZE+4, spawner.y * CELLSIZE+4));
                    break;
                case "blue":
                    loadedBalls.add(new Ball("B2", spawner.x * CELLSIZE+4, spawner.y * CELLSIZE+4));
                    break;
                case "green":
                    loadedBalls.add(new Ball("B3", spawner.x * CELLSIZE+4, spawner.y * CELLSIZE+4));
                    break;
                case "yellow":
                    loadedBalls.add(new Ball("B4", spawner.x * CELLSIZE+4, spawner.y * CELLSIZE+4));
                    break;
            }
        }
    }

    /**
     * Prints information about the level to the console, including layout, 
     * time, spawn interval, score modifiers, and ball types.
     */
    public void printInfo(){
        // Print level information
        System.out.println("  Layout: " + layout);
        System.out.println("  Time: " + time);
        System.out.println("  Spawn Interval: " + spawnInterval);
        System.out.println("  Score Increase Modifier: " + scoreIncreaseModifier);
        System.out.println("  Score Decrease Modifier: " + scoreDecreaseModifier);
        System.out.println("  Balls: ");
        for (int j = 0; j < ballsJSON.size(); j++) {
            System.out.println("    " + ballsJSON.getString(j));
        }

        //print layout
        for (int i=0; i<layout.size(); i++){
            System.out.println(layout.get(i) + "!");
        }
    }

    /**
     * Returns the layout of the level as a list of strings.
     * 
     * @return an {@code ArrayList} containing the level layout.
     */
    public ArrayList<String> getLayout(){
        return layout;
    }

    /**
     * Returns the time limit for the level.
     * 
     * @return the time limit in seconds.
     */
    public int getTime(){
        return time;
    }

    /**
     * Returns the spawn interval for the level.
     * 
     * @return the spawn interval in milliseconds.
     */
    public int getSpawnInterval(){
        return spawnInterval;
    }

    /**
     * Returns the queue of loaded balls for the level.
     * 
     * @return a {@code Queue} containing the loaded balls.
     */
    public Queue<Ball> getLoadedBalls(){
        return loadedBalls;
    }

    /**
     * Reloads the balls for the level by clearing the current queue 
     * and loading new balls from the JSON array.
     * 
     * @return the updated queue of loaded balls.
     */
    public Queue<Ball> reloadBalls(){
        loadedBalls = new LinkedList<>();
        loadBalls();
        return loadedBalls;
    }

    /**
     * Returns the score increase modifier for the level.
     * 
     * @return the score increase modifier as a {@code float}.
     */
    public float getScoreIncreaseModifier(){
        return scoreIncreaseModifier;
    }

    /**
     * Returns the score decrease modifier for the level.
     * 
     * @return the score decrease modifier as a {@code float}.
     */
    public float getScoreDecreaseModifier(){
        return scoreDecreaseModifier;
    }

    /**
     * Adds a spawner point at the specified coordinates.
     * 
     * @param x the x-coordinate of the spawner.
     * @param y the y-coordinate of the spawner.
     */
    public void addSpawner(int x, int y){
        spawners.add(new PVector(x, y));
    }

    /**
     * Returns a random spawner point from the list of spawners.
     * 
     * @return a random {@code PVector} representing the spawner location, 
     *         or {@code null} if there are no spawners.
     */
    public PVector getRandSpawner(){
        // return a random PVector from spawners
        if (spawners.isEmpty()) {
            return null; // or handle the case when there are no spawners
        }
        int randomIndex = random.nextInt(spawners.size()); // Get a random index
        return spawners.get(randomIndex); // Return the PVector at that index
    }
}
