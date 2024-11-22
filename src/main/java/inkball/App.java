package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class App extends PApplet {
    /**
     * The {@code App} class is the main game application for the Inkball game, 
     * extending the Processing library's {@code PApplet}. It handles game setup, 
     * level management, user input, and rendering of game elements.
     * 
     * <p>This class manages the game board, player interactions, and the 
     * game's graphical interface, including loading resources, processing 
     * user inputs, and updating game states.</p>
     * 
     * <p>Key features include:</p>
     * <ul>
     * <li>Initialization of game settings and resources.</li>
     * <li>Management of levels, including loading and progressing between levels.</li>
     * <li>Handling of user inputs via keyboard and mouse events.</li>
     * <li>Rendering of game elements such as balls, walls, holes, and dynamic shapes.</li>
     * <li>Collision detection and physics-based interactions between game objects.</li>
     * <li>Score tracking and display, as well as a timer for each level.</li>
     * </ul>
     * 
     * <p>The game operates at a fixed frame rate, updates the game state every frame, 
     * and responds to user interactions to manipulate the game environment.</p>
     * 
     * <p>In addition to game logic, the {@code App} class also contains methods for 
     * visualizing game components and tracking game states, including conditions for 
     * winning or losing.</p>
     */

    /** The size of each cell in the game grid. */
    protected static final int CELLSIZE = 32; //8;
    /** The height of the top bar in the game window. */
    protected static final int TOPBAR = 64; //2 cells tall
    /** The width of the game window. */
    protected static final int WIDTH = 576; //CELLSIZE*BOARD_WIDTH;
    /** The height of the game window. */
    protected static final int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    /** The frames per second for the game. */
    protected static final int FPS = 30;
    /** Random number generator for game logic. */
    protected static Random random = new Random();

    //----------------------------------
    // Same throughout all levels
    //----------------------------------
    /** Path to the configuration file for levels and scores. */
    private String configPath;
    /** JSON object containing game configuration data. */
    private JSONObject json;
    /** JSON array containing level data. */
    private JSONArray levelsJSON;
    /** Array of Level objects representing each game level. */
    private Level[] levels;
    /** JSON object for score increases from hole captures. */
    private JSONObject scoreIncreaseJSON;
    /** JSON object for score decreases from wrong hole captures. */
    private JSONObject scoreDecreaseJSON;
    /** Array of score increases for different hole colors. */
    private int[] scoreIncreases = new int[5];
    /** Array of score decreases for different hole colors. */
    private int[] scoreDecreases = new int[5];

    //----------------------------------
    // Images
    //----------------------------------
    private PImage[] ballImages = new PImage[5];
    private String[] ballFileNames = {"ball0.png", "ball1.png", "ball2.png", "ball3.png", "ball4.png"};
    private PImage[] holeImages = new PImage[5];
    private String[] holeFileNames = {"hole0.png", "hole1.png", "hole2.png", "hole3.png", "hole4.png"};
    private PImage[] wallImages = new PImage[5];
    private String[] wallFileNames = {"wall0.png", "wall1.png", "wall2.png", "wall3.png", "wall4.png"};
    private PImage[] tileImages = new PImage[5];
    private String[] tileFileNames = {"tile.png", "up.png", "down.png", "right.png", "left.png"};

    private PImage entry;


    //----------------------------------
    // Dynamic variables, different for each level
    //----------------------------------
    /** The current game level. */
    private Level level;
    /** The current level index. */
    private int levelCount;
    /** Layout of the current level. */
    private ArrayList<String> levelLayout;
    /** List of holes in the current level. */
    private ArrayList<String[]> holes = new ArrayList<>(); //each item is an array
    /** Score increase modifier for the current level. */
    private float scoreIncreaseModifier;
    /** Score decrease modifier for the current level. */
    private float scoreDecreaseModifier;
    /** Interval at which balls spawn. */
    private int spawnInterval;
    /** The previous score before the current level. */
    private int prevScore;
    /** The gameboard represented as a 2D array. */
    private String[][] gameboard = new String[18][18];
    /** List of invisible lines for collision detection. */
    private ArrayList<InvisibleLine> invisibleLines;
    /** List of invisible circles for attraction mechanics. */
    private ArrayList<InvisibleCircle> invisibleCircles;
    /** Indicates if the current level has ended. */
    private boolean levelEnded;
    /** The top-left corner of the game board for winning effects. */
    private PVector top_left = new PVector(0, 0);
    /** The bottom-right corner of the game board for winning effects. */
    private PVector bottom_right = new PVector(17, 17);;

    //----------------------------------
    // Very dynamic variables, changes within the same level
    //----------------------------------
    /** Points for the current drawn shape. */
    private ArrayList<PVector> currentShape; // Points for the current shape
    /** Collection of all shapes drawn by the player. */
    private ArrayList<ArrayList<PVector>> shapes = new ArrayList<>();
    /** The distance for sliding animation in the top bar when a new ball releases */
    private int slideDistance = 0;
    /** Indicates if the game is currently running. */
    private boolean gameRunning;
    /** The current score for the level. */
    private int levelScore;
    /** Time left in the current level. */
    private int timeLeft;
    /** The previous time left for calculating spawn intervals. */
    private float previousRelease;
    /** Queue of balls that are loaded for the level. */
    private Queue<Ball> loadedBalls;
    /** Queue of balls currently on the board. */
    private Queue<Ball> onBoardBalls;

    /**
     * Constructs the {@code App} instance and initializes the configuration path.
     */
    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as images and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);
        prevScore = 0;

        // the image is loaded from relative path: "src/main/resources/inkball/..."
        try {
            entry = loadImage(URLDecoder.decode(this.getClass().getResource("entrypoint"+".png").getPath(), StandardCharsets.UTF_8.name()));

            // Load ball images
            for (int i = 0; i < ballFileNames.length; i++) {
                ballImages[i] = loadImage(URLDecoder.decode(this.getClass().getResource(ballFileNames[i]).getPath(), StandardCharsets.UTF_8.name()));
            }

            // Load hole images
            for (int i = 0; i < holeFileNames.length; i++) {
                holeImages[i] = loadImage(URLDecoder.decode(this.getClass().getResource(holeFileNames[i]).getPath(), StandardCharsets.UTF_8.name()));
            }

            // Load wall images
            for (int i = 0; i < wallFileNames.length; i++) {
                wallImages[i] = loadImage(URLDecoder.decode(this.getClass().getResource(wallFileNames[i]).getPath(), StandardCharsets.UTF_8.name()));
            }

            // Load tile images
            for (int i = 0; i < tileFileNames.length; i++) {
                tileImages[i] = loadImage(URLDecoder.decode(this.getClass().getResource(tileFileNames[i]).getPath(), StandardCharsets.UTF_8.name()));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        //load all levels
        levelCount = 0;
        json = loadJSONObject(configPath);
        levelsJSON = json.getJSONArray("levels");
        levels = new Level[levelsJSON.size()];
        for (int i=0; i<levelsJSON.size(); i++){
            levels[i] = new Level(levelsJSON.getJSONObject(i));
        }

        //scores setting
        scoreIncreaseJSON = json.getJSONObject("score_increase_from_hole_capture");
        scoreDecreaseJSON = json.getJSONObject("score_decrease_from_wrong_hole");
        scoreIncreases[0] = scoreIncreaseJSON.getInt("grey");
        scoreIncreases[1] = scoreIncreaseJSON.getInt("orange");
        scoreIncreases[2] = scoreIncreaseJSON.getInt("blue");
        scoreIncreases[3] = scoreIncreaseJSON.getInt("green");
        scoreIncreases[4] = scoreIncreaseJSON.getInt("yellow");
        scoreDecreases[0] = scoreDecreaseJSON.getInt("grey");
        scoreDecreases[1] = scoreDecreaseJSON.getInt("orange");
        scoreDecreases[2] = scoreDecreaseJSON.getInt("blue");
        scoreDecreases[3] = scoreDecreaseJSON.getInt("green");
        scoreDecreases[4] = scoreDecreaseJSON.getInt("yellow");

        //initialize the 1st level
        readLevel(levelCount);

        level.loadBalls();
        timeLeft = level.getTime();
        previousRelease = timeLeft;
    }

    /**
     * Restarts the current level, resetting the game state and score.
     */
    public void restartLevel(){
        gameRunning = true;
        levelScore = 0;
        shapes = new ArrayList<>();
        loadedBalls = level.reloadBalls();
        onBoardBalls = new LinkedList<>();
        findStatics();

        timeLeft = level.getTime();
        previousRelease = timeLeft;
        levelEnded = false;
    }

    /**
     * Advances to the next level if available.
     */
    public void increaseLevel(){
        if (levelCount == levels.length - 1) {
            return;
        } 
        // increase level
        levelCount += 1;

        // add this level score to total score
        prevScore += levelScore;

        // read level, reload balls
        readLevel(levelCount);
        level.loadBalls();

        // clear some other variables that not covered above
        restartLevel();
    }

    /**
     * Reads and initializes the specified level.
     * 
     * @param levelCount the index of the level to be read.
     */
    public void readLevel(int levelCount){
        gameRunning = true;
        holes = new ArrayList<>(); //each item is an array of size 3
        gameboard = new String[18][18];
    
        //process each level defined in config.json using Level.java
        level = levels[levelCount];
        levelLayout = level.getLayout();
        loadedBalls = level.getLoadedBalls();
        onBoardBalls = new LinkedList<>();
        timeLeft = level.getTime();
        previousRelease = timeLeft;
        spawnInterval = level.getSpawnInterval();
        levelEnded = false; 

        recordBoard();
        setupInvisibleLines();
        findStatics();

        // score modifiers
        scoreIncreaseModifier = level.getIncreaseModifier();
        scoreDecreaseModifier = level.getDecreaseModifier();
        levelScore = 0;

        // prepare for winning effect
        top_left.set(0, 0);
        bottom_right.set(17, 17);
    }

    /**
     * Records the game board layout based on the current level's configuration.
     */
    public void recordBoard(){
        for (int y=0; y<levelLayout.size() && y < gameboard.length; y++){
            String row = levelLayout.get(y);
            for (int i = 0; i < row.length() && i < gameboard[0].length; i++) {
                char ch1 = row.charAt(i); 
                Character ch2 = null;
                switch (ch1) {
                    case 'H':
                        ch2 = row.charAt(i+1);
                        gameboard[y][i] = (ch2 != null) ? String.valueOf(ch1) + ch2 : String.valueOf(ch1);
                        i++;
                        gameboard[y][i] = (ch2 != null) ? String.valueOf(ch1) + ch2 : String.valueOf(ch1);
                        break;
                    case 'B':
                        gameboard[y][i] = " ";
                        i++;
                        gameboard[y][i] = " ";
                        break;
                    default:
                        gameboard[y][i] = (ch2 != null) ? String.valueOf(ch1) + ch2 : String.valueOf(ch1);
                        break;
                }
            }    
        }
    }

    /**
     * Finds static elements such as balls and spawners in the current level.
     */
    public void findStatics(){
        invisibleCircles = new ArrayList<>();
        for (int y=0; y<levelLayout.size(); y++){
            String row = levelLayout.get(y);
            for (int i = 0; i < row.length(); i++) {
                char ch = row.charAt(i); // Get the character at index i
                switch (ch) {
                    case 'B':
                        ch = row.charAt(i+1);
                        Ball ball = new Ball(String.valueOf("B") + ch, i*CELLSIZE + 4, (y+2)*CELLSIZE + 4);
                        ball.setIsOnboard(true); //immediately display and run
                        onBoardBalls.add(ball);
                        i++;
                        break;
                    case 'S':
                        level.addSpawner(i, y+2);
                        break;
                    case 'H':
                        ch = row.charAt(i+1);
                        String[] hole = {String.valueOf("H") + ch, Integer.toString(y+2), Integer.toString(i)};
                        holes.add(hole);
                        recordInvisibleCircle(i, y+2, String.valueOf("H") + ch);
                        i++;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Prints the holes on the game board.
     */
    public void printHoles(){
        for (int i=0; i < holes.size(); i++){
            String[] tokens = holes.get(i);
            int holeColor = tokens[0].charAt(1) - '0';
            int y = Integer.parseInt(tokens[1]);
            int x = Integer.parseInt(tokens[2]);
            image(holeImages[holeColor], x*CELLSIZE, y*CELLSIZE);
        }
    }

    /**
     * Records an invisible circle in the game.
     * 
     * @param x the x-coordinate of the circle in gameboard. 
     * @param y the y-coordinate of the circle in gameboard.
     * @param color the color of the circle.
     */
    public void recordInvisibleCircle(int x, int y, String color) {
        // turn x and y into centre pixels
        x = x * CELLSIZE + CELLSIZE;
        y = y * CELLSIZE + CELLSIZE;
        PVector center = new PVector(x, y);
        int radius = CELLSIZE; // Radius in pixels
        InvisibleCircle circle = new InvisibleCircle(center, radius, color);
        invisibleCircles.add(circle);
    }

    /**
     * Prints a given ball on the game canvas.
     * 
     * @param ball the {@code Ball} object to print.
     */
    public void printBall(Ball ball){
        int ballColor = ball.getColor();
        float x = ball.getX();
        float y = ball.getY();
        float radius = ball.getRadius();

        // Save the current transformation state
        pushMatrix();

        // Translate to the ball's position
        translate(x + radius, y + radius); // Center the scaling on the ball's position

        // Scale based on the ball's radius
        float scaleFactor = radius / ball.getInitialRadius();
        scale(scaleFactor); // Scale the image

        // Draw the ball based on its name
        image(ballImages[ballColor], -radius, -radius);

        // Restore the previous transformation state
        popMatrix();
    }

    /**
     * Prints a given ball at specified coordinates.
     * 
     * @param ball the {@code Ball} object to print.
     * @param x the x-coordinate for printing in pixels.
     * @param y the y-coordinate for printing in pixels.
     */
    public void printBall(Ball ball, float x, float y){
        int ballColor = ball.getColor();
        float radius = ball.getRadius();

        // Save the current transformation state
        pushMatrix();

        // Translate to the ball's position
        translate(x + radius, y + radius); // Center the scaling on the ball's position

        // Scale based on the ball's radius
        float scaleFactor = radius / ball.getInitialRadius();
        scale(scaleFactor); // Scale the image

        // Draw the ball based on its name
        image(ballImages[ballColor], -radius, -radius);

        // Restore the previous transformation state
        popMatrix();
    }

    /**
     * Prints the current game board based on the game state.
     */
    public void printBoard(){
        for (int y=0; y<gameboard.length; y++){
            int yyy = y+2;
            for (int i=0; i<gameboard[y].length; i++) {
                String str = gameboard[y][i]; // Get the character at index i
                if (str.equals("1")||str.equals("2")||str.equals("3")||str.equals("4")){
                    int color=str.charAt(0)-'0';
                    image(wallImages[color], i*CELLSIZE, yyy*CELLSIZE);
                } else {
                    switch (str) {
                        case "X":
                            image(wallImages[0], i*CELLSIZE, yyy*CELLSIZE);
                            break;
                        case "S":
                            image(entry, i*CELLSIZE, yyy*CELLSIZE);
                            break;
                        case "H":
                            i++;
                            break;
                        case "B":
                            image(tileImages[0], i*CELLSIZE, yyy*CELLSIZE);
                            i++;
                            image(tileImages[0], i*CELLSIZE, yyy*CELLSIZE);
                            break;
                        case " ":
                            image(tileImages[0], i*CELLSIZE, yyy*CELLSIZE);
                            break;
                        case "U":
                            image(tileImages[1], i*CELLSIZE, yyy*CELLSIZE);
                            break;
                        case "D":
                            image(tileImages[2], i*CELLSIZE, yyy*CELLSIZE);
                            break;
                        case "R":
                            image(tileImages[3], i*CELLSIZE, yyy*CELLSIZE);
                            break;
                        case "L":
                            image(tileImages[4], i*CELLSIZE, yyy*CELLSIZE);
                            break;
                        default:
                            image(tileImages[0], i*CELLSIZE, yyy*CELLSIZE);
                            break;
                    }
                }
            }
        }
    }

    /**
     * Sets up invisible lines for collision detection.
     */
    public void setupInvisibleLines() {
        invisibleLines = new ArrayList<>();

        for (int y = 0; y < gameboard.length; y++) {
            for (int x = 0; x < gameboard[y].length; x++) {
                String str = gameboard[y][x]; // Get the tile

                // Check if the current tile is a wall block
                if (str.equals("X") || str.equals("1") || str.equals("2") || str.equals("3") || str.equals("4")) {
                    // if the neighbour is out of bound, make it null. 

                    // check left neighbor
                    String left = (x > 0) ? gameboard[y][x - 1] : null;
                    if (x > 0 && (left == null || left.equals(" ") || left.charAt(0) == 'H' || left.equals("S"))) {
                        invisibleLines.add(new InvisibleLine(new PVector(x * CELLSIZE, (y+2) * CELLSIZE), 
                            new PVector(x * CELLSIZE, ((y+2) + 1) * CELLSIZE), str, "LEFT"));
                    }
                    // check right neighbor
                    String right = (x < gameboard[y].length - 1) ? gameboard[y][x + 1] : null;
                    if (x < gameboard[y].length - 1 && (right == null || right.equals(" ") || right.charAt(0) == 'H' || right.equals("S"))) {
                        invisibleLines.add(new InvisibleLine(new PVector((x + 1) * CELLSIZE, (y+2) * CELLSIZE), 
                            new PVector((x + 1) * CELLSIZE, ((y+2) + 1) * CELLSIZE), str, "RIGHT"));
                    }
                    // check up neighbor
                    String top = (y > 0) ? gameboard[y - 1][x] : null;
                    if (y > 0 && (top == null || top.equals(" ") || top.charAt(0) == 'H' || top.equals("S"))) {
                        invisibleLines.add(new InvisibleLine(new PVector(x * CELLSIZE, (y+2) * CELLSIZE), 
                            new PVector((x + 1) * CELLSIZE, (y+2) * CELLSIZE), str, "UP"));
                    }
                    // check bottom neighbor
                    String bottom = (y < gameboard.length - 1) ? gameboard[y + 1][x] : null;
                    if (y < gameboard.length - 1 && (bottom == null || bottom.equals(" ") || bottom.charAt(0) == 'H' || bottom.equals("S"))) {
                        invisibleLines.add(new InvisibleLine(new PVector(x * CELLSIZE, ((y+2) + 1) * CELLSIZE), 
                            new PVector((x + 1) * CELLSIZE, ((y+2) + 1) * CELLSIZE), str, "BOTTOM"));
                    }
                }

                // Set up hitbox if it's at the program borders
                if (x == 0) { // Left border
                    invisibleLines.add(new InvisibleLine(new PVector(0, (y + 2) * CELLSIZE), 
                        new PVector(0, ((y + 2) + 1) * CELLSIZE), "X", "RIGHT"));
                }
                if (x == gameboard[y].length - 1) { // Right border
                    invisibleLines.add(new InvisibleLine(new PVector((x + 1) * CELLSIZE, (y + 2) * CELLSIZE), 
                        new PVector((x + 1) * CELLSIZE, ((y + 2) + 1) * CELLSIZE), "X", "LEFT"));
                }
                if (y == 0) { // Top border
                    invisibleLines.add(new InvisibleLine(new PVector(x * CELLSIZE, ((y+2)) * CELLSIZE), 
                        new PVector((x + 1) * CELLSIZE, ((y+2)) * CELLSIZE), "X", "BOTTOM"));
                }
                if (y == gameboard.length - 1) { // Bottom border
                    invisibleLines.add(new InvisibleLine(new PVector(x * CELLSIZE, ((y + 2) + 1) * CELLSIZE), 
                        new PVector((x + 1) * CELLSIZE, ((y + 2) + 1) * CELLSIZE), "X", "UP"));
                }
            }
        }

    }

    /**
     * Checks for collisions between a ball and various game objects.
     * 
     * @param ball the {@code Ball} object to check for collisions.
     */
    public void checkHit(Ball ball) {
        if (currentShape != null){
            for (int i=0; i<currentShape.size()-1; i++) {
                PVector p1 = currentShape.get(i);
                PVector p2 = currentShape.get(i + 1);
                if (lineIntersectsBall(p1, p2, ball, ball.getVelocity())) {
                    reflectBall(ball, p1, p2, null);
                    currentShape = null;
                    break;
                }
            }
        }

        for (int i = shapes.size() - 1; i >= 0; i--) {
            ArrayList<PVector> shape = shapes.get(i);
            for (int j = 0; j < shape.size() - 1; j++) {
                PVector p1 = shape.get(j);
                PVector p2 = shape.get(j + 1);
                if (lineIntersectsBall(p1, p2, ball, ball.getVelocity())) {
                    reflectBall(ball, p1, p2, null);
                    shapes.remove(i);
                    break;
                }
            }
        }

        for (InvisibleLine line : invisibleLines) {
            // Check if the line intersects with the ball
            String direction = line.getDirection();
            if (lineIntersectsBall(line.start, line.end, ball, ball.getVelocity())) {
                // Reflect the ball off the line
                reflectBall(ball, line.start, line.end, line.getDirection());
                transformBall(ball, line.getColor());
                break;
            }
        }

        for (InvisibleCircle circle : invisibleCircles) {
            float distance = PVector.dist(circle.getCenter(), ball.getCentre());
            if (distance < (circle.getRadius())) {
                applyAttraction(ball, circle);
                break;
            } else {
                ball.setRadius(12);
            }
        }
    }

    /**
     * Checks if a line segment intersects with a ball.
     *
     * @param p1 the starting point of the line segment.
     * @param p2 the ending point of the line segment.
     * @param ball the {@code Ball} object to check for intersection.
     * @param ballVelocity the velocity vector of the ball.
     * @return {@code true} if the line segment intersects with the ball; 
     *         {@code false} otherwise.
     */
    private boolean lineIntersectsBall(PVector p1, PVector p2, Ball ball, PVector ballVelocity) {
        PVector ballCentre = ball.getCentre();
        // Find the closest point on the line segment
        float closestX = constrain(ballCentre.x, Math.min(p1.x, p2.x), Math.max(p1.x, p2.x));
        float closestY = constrain(ballCentre.y, Math.min(p1.y, p2.y), Math.max(p1.y, p2.y));

        // Calculate the squared distance from the ball's center to the closest point
        float distanceSquared = (ballCentre.x - closestX) * (ballCentre.x - closestX) +
                                (ballCentre.y - closestY) * (ballCentre.y - closestY);
        float distance = (float) Math.sqrt(distanceSquared);


        // Compare squared distance with squared radius
        float ballRadius = ball.getRadius();
        return distance <= ballRadius; // Use ballRadius parameter
    }

    /**
     * Reflects a ball off a specified line segment.
     * 
     * @param ball the {@code Ball} object to reflect.
     * @param p1 the starting point of the line segment.
     * @param p2 the ending point of the line segment.
     * @param allowedDirection the direction allowed for reflection.
     */
    public void reflectBall(Ball ball, PVector p1, PVector p2, String allowedDirection) {
        // Calculate the line vector from point p1 to point p2
        PVector lineVector = PVector.sub(p2, p1);
        
        // Compute the normal vector to the line by swapping the x and y components and negating the new x
        PVector normal = new PVector(-lineVector.y, lineVector.x).normalize(); // Get normal vector
        
        // Calculate the dot product of the ball's velocity and the normal vector
        float dotProduct = PVector.dot(ball.getVelocity(), normal);
        
        // Calculate the reflection vector using the formula for reflection off a surface
        if (allowedDirection == null) {
            PVector reflection = PVector.sub(ball.getVelocity(), PVector.mult(normal, 2 * dotProduct));
            // Set the ball's velocity to the newly calculated reflection vector
            ball.setVelocity(reflection);
        } else {
            int ballDirection = ball.getDirection(); // hasn't changed velocity yet at this step
            // System.out.println("ballDirection: "+Integer.toString(ballDirection) + ", allowedDirection: " + allowedDirection);
            if ((ballDirection==1 || ballDirection==4) && allowedDirection.equals("RIGHT")){
                return;
            } else if ((ballDirection==1 || ballDirection==2) && allowedDirection.equals("UP")) {
                return;
            } else if ((ballDirection==2 || ballDirection==3) && allowedDirection.equals("LEFT")) {
                return;
            } else if ((ballDirection==3 || ballDirection==4) && allowedDirection.equals("BOTTOM")) {
                return;
            }
            // at this step, the ball must be moving into an non-empty tile
            PVector reflection = PVector.sub(ball.getVelocity(), PVector.mult(normal, 2 * dotProduct));
            ball.setVelocity(reflection);
        }
    }

    /**
     * Transforms a ball based on its hitbox.
     * 
     * @param ball the {@code Ball} object to transform.
     * @param hitbox the hitbox identifier.
     */
    public void transformBall(Ball ball, String hitbox) {
        switch (hitbox) {
            case "X":
                break;
            case "1":
                ball.setName("B1");
                break;
            case "2":
                ball.setName("B2");
                break;
            case "3":
                ball.setName("B3"); 
                break;
            case "4":
                ball.setName("B4");
                break;
            default:
                break;
        }
    }

    /**
     * Applies attraction mechanics from an invisible circle to a ball.
     * 
     * @param ball the {@code Ball} object to attract.
     * @param circle the {@code InvisibleCircle} object providing attraction.
     */
    public void applyAttraction(Ball ball, InvisibleCircle circle) {
        // Calculate the vector from the ball to the hole
        // System.out.println("problem4");
        PVector direction = PVector.sub(circle.getCenter(), ball.getCentre());
        float distance = direction.mag(); 

        // check if overlapping the centre of the hole
        if (distance < ball.getRadius()) {
            // System.out.println("problem3");
            int colorCode = ball.getColor();
            boolean absorption = ball.absorbed(circle);
            if (absorption) { // if reloaded, parameters are reset at this step
                // System.out.println("problem1");
                levelScore += scoreIncreases[colorCode] * scoreIncreaseModifier;
            } else {
                // System.out.println("problem2");
                // ball.setIsOnboard(false);
                levelScore -= scoreDecreases[colorCode] * scoreDecreaseModifier;
                if (loadedBalls.contains(ball)) return;
                loadedBalls.add(ball);
            }
        }

        // change ball size
        float sizeReductionFactor = distance / circle.getRadius();
        ball.setRadius(Math.max(ball.getInitialRadius() * sizeReductionFactor, 5));
        
        // Normalize the direction vector (unit vector)
        direction.normalize(); 

        // Apply 0.5% scaling to the vector
        PVector attractionVector = direction.mult(0.005f * distance); 
        ball.attractedTo(attractionVector);
    }

    /**
     * Receives key pressed events from the keyboard.
     * 
     * @param event the {@code KeyEvent} object containing the event details.
     */
	@Override
    public void keyPressed(KeyEvent event){
        if (event.getKey() == 'r' || event.getKey() == 'R') {
            if ((levelCount==levels.length-1) && levelEnded){
                setup();
            } else {
                restartLevel();
            }
        } else if (event.getKey() == 'q' || event.getKey() == 'Q') {
            exit();
        }
    }

    /**
     * Receives key released events from the keyboard.
     * 
     * @param event the {@code KeyEvent} object containing the event details.
     */
	@Override
    public void keyReleased(KeyEvent event){
        if (event.getKey() == ' ') {
            if (levelEnded) return;
            if (!gameRunning){
                gameRunning = true;
            } else {
                gameRunning = false;
            }
        } 
    }

    /**
     * Handles mouse pressed events.
     * 
     * @param e the {@code MouseEvent} object containing the event details.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        // create a new player-drawn line object
        if (mouseButton == LEFT) {
            if (currentShape==null) {
                currentShape = new ArrayList<>(); // Initialize a new list for the current shape
                currentShape.add(new PVector(mouseX, mouseY)); // Add the starting point        
            }
        } else if ((mouseButton == RIGHT) || (mouseButton == LEFT && keyCode == CONTROL)) {
            PVector clickPosition = new PVector(mouseX, mouseY);
            removeShapeAtMouse(clickPosition);
        }
    }

    /**
     * Removes a shape at the specified mouse click position.
     * 
     * @param clickPosition the position of the mouse click.
     */
    private void removeShapeAtMouse(PVector clickPosition) {
        for (int i = shapes.size() - 1; i >= 0 ; i--) {
            ArrayList<PVector> shape = shapes.get(i);
            for (int j = 0; j < shape.size() - 1; j++) {
                PVector p1 = shape.get(j);
                PVector p2 = shape.get(j + 1);
                if (isClickOnShape(p1, p2, clickPosition)) {
                    shapes.remove(i);
                    return;
                }
            }
        }
    }
	
    /**
     * Checks if a click occurred on a specific shape.
     * 
     * @param p1 the first point of the shape.
     * @param p2 the second point of the shape.
     * @param clickPosition the position of the mouse click.
     * @return {@code true} if the click intersects with the shape; {@code false} otherwise.
     */
    private boolean isClickOnShape(PVector p1, PVector p2, PVector clickPosition) {
        // Find the closest point on the line segment
        float closestX = constrain(clickPosition.x, Math.min(p1.x, p2.x), Math.max(p1.x, p2.x));
        float closestY = constrain(clickPosition.y, Math.min(p1.y, p2.y), Math.max(p1.y, p2.y));

        // Calculate the squared distance from the ball's center to the closest point
        float distanceSquared = (clickPosition.x - closestX) * (clickPosition.x - closestX) +
                                (clickPosition.y - closestY) * (clickPosition.y - closestY);
        float distance = (float) Math.sqrt(distanceSquared);

        // Compare squared distance with squared radius
        return distance <= 5; // shape has radius of 5
    }
    
    /**
     * Handles mouse dragged events.
     * 
     * @param e the {@code MouseEvent} object containing the event details.
     */
	@Override
    public void mouseDragged(MouseEvent e) {
        // add line segments to player-drawn line object if left mouse button is held
        if (currentShape != null) {
            currentShape.add(new PVector(mouseX, mouseY)); // Add the current mouse position
        }
    }

    /**
     * Handles mouse released events.
     * 
     * @param e the {@code MouseEvent} object containing the event details.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (currentShape != null) {
            shapes.add(currentShape);
            currentShape = null; // Reset the current shape
        }
    }

    /**
     * Draws invisible lines used for collision detection.
     */
    public void drawInvisibleLines() {
        stroke(0); // Set stroke color to black
        strokeWeight(5); // Set line thickness to 5 pixels (adjust as needed)
        noFill(); // Disable filling for the lines
    
        // Iterate through your list of invisible lines
        for (InvisibleLine line : invisibleLines) {
            // Draw the line
            line(line.start.x, line.start.y, line.end.x, line.end.y);
        }
    }

    /**
     * Draws invisible circles used for attraction mechanics.
     */
    public void drawInvisibleCircles() {
        stroke(0, 100); // Set stroke color to black with some transparency
        strokeWeight(2); // Set line thickness (adjust as needed)
        noFill(); // Disable filling for the circles
    
        // Iterate through your list of invisible circles
        if (invisibleCircles.size()==0){
            return;
        }
        for (InvisibleCircle circle : invisibleCircles) {
            // Draw the circle
            ellipse(circle.getCenter().x, circle.getCenter().y, circle.getRadius() * 2, circle.getRadius() * 2);
        }
    }


    /**
     * Draws all elements in the game for every frame.
     */
	@Override
    public void draw() {
        if (gameRunning) {
            // update timeLeft
            if (frameCount % FPS == 0) {
                timeLeft -= 1;
            }
        } 

        // Draw the top bar background
        noStroke();  // Disable the stroke (border)
        fill(206);  // A grey color for the top bar
        rect(0, 0, WIDTH, TOPBAR);  // Draw a rectangle for the top bar

        // check if time is up
        if (timeLeft <= 0){
            if (levelCount==(levels.length-1)){
                // if it's last level
                gameRunning = false;
                levelEnded = true;
                textSize(25); // Set text size
                fill(255, 0, 0); // Set text color to red
                textAlign(CENTER, CENTER); // Center the text
                text("=== ENDED ===", WIDTH / 2, TOPBAR / 2);
            } else {
                // not the last level
                gameRunning = false;
                levelEnded = true;
                textSize(25); // Set text size
                fill(255, 0, 0); // Set text color to red
                textAlign(CENTER, CENTER); // Center the text
                text("=== TIME’S UP ===", WIDTH / 2, TOPBAR / 2);
            }
        } 

        // if game not ended, check if paused
        if (!levelEnded && !gameRunning){
            textSize(25); // Set text size
            fill(255, 0, 0); // Set text color to red
            textAlign(CENTER, CENTER); // Center the text
            text("*** PAUSED ***", WIDTH / 2, TOPBAR / 2); // Draw the paused message in the middle of the top bar
        }

        //----------------------------------
        //display black rectangle
        noStroke();  // Disable the stroke (border)
        fill(0); // Set the text color
        rect(CELLSIZE/2, CELLSIZE/2, CELLSIZE*5, CELLSIZE);
        clip(CELLSIZE / 2, CELLSIZE / 2, CELLSIZE * 5, CELLSIZE);
        
        // Display loaded Balls
        if (slideDistance>0){
            slideDistance -= 1;
        }
        Iterator<Ball> loadedBallIterator = loadedBalls.iterator();
        int k = 0;
        while (loadedBallIterator.hasNext()){
            printBall(loadedBallIterator.next(), CELLSIZE/2 + CELLSIZE*k + 5 + slideDistance, CELLSIZE/2 + 4);
            k++;
        }
        noClip();

        //----------------------------------
        // Display score
        //----------------------------------
        textSize(4 + CELLSIZE/2);
        fill(0); // Set the text color
        textAlign(CENTER, CENTER); // Align text to the right and top
        text("Score: " + (prevScore + levelScore), WIDTH - 70, 17); // Display at top-right (adjust as needed)

        //----------------------------------
        // Display the timer in the top-right corner
        //----------------------------------
        textSize(4 + CELLSIZE/2);
        fill(0); // Set the text color
        textAlign(CENTER, CENTER); // Align text to the right and top
        text("Time: " + (timeLeft), WIDTH - 70, 64-17); // Display at top-right (adjust as needed)

        //----------------------------------
        // Display Board for current level:
        //----------------------------------
        printBoard();
        printHoles();

        // Load balls on to Board by a interval
        if (gameRunning && !loadedBalls.isEmpty() && ((previousRelease - timeLeft) >= spawnInterval)){
            // System.out.println("problem: Load balls on to Board by a interval");
            Ball toLoad = loadedBalls.poll();
            if (!onBoardBalls.contains(toLoad)){
                onBoardBalls.add(toLoad);
            }
            toLoad.setIsOnboard(true);
            toLoad.setVelocity(new PVector(random.nextBoolean() ? 2 : -2, random.nextBoolean() ? 2 : -2));
            // System.out.println("setVelocity() in toLoad");
            previousRelease = timeLeft;
            slideDistance = CELLSIZE;
        }

        // Display all on-board balls
        Iterator<Ball> onBoardBallIterator = onBoardBalls.iterator();
        while (onBoardBallIterator.hasNext()){
            Ball toPrint = onBoardBallIterator.next();
            if (!toPrint.isOnBoard()) continue;
            printBall(toPrint);
            if (gameRunning){
                toPrint.move();
                checkHit(toPrint);
            }
        }
        
		//----------------------------------
        // Display recorded lines
        for (ArrayList<PVector> shape : shapes) {
            stroke(0); // Set stroke color to black
            strokeWeight(10); // Set line width to 10 pixels
            noFill(); // Do not fill shapes
            beginShape(); // Start drawing the shape
            for (PVector point : shape) {
                vertex(point.x, point.y); // Define vertices for the shape
            }
            endShape(); // Complete the shape
        }

        // Display drawing lines
        if (currentShape != null) {
            stroke(0, 255); // Set a semi-transparent stroke color for the current shape
            strokeWeight(10);
            noFill(); // Do not fill shapes
            beginShape();
            for (PVector point : currentShape) {
                vertex(point.x, point.y); // Define vertices for the current shape
            }
            endShape();
        }


        Iterator<Ball> checkFalseIterator = onBoardBalls.iterator();
        boolean winned = true;
        while (checkFalseIterator.hasNext()){
            Ball toCheck = checkFalseIterator.next();
            if (toCheck.isOnBoard()) {
                winned = false;
            }
        }

        // check for winning
        if (loadedBalls.size()==0 && winned) {
            gameRunning = false;
            levelEnded = true;

            image(wallImages[4], top_left.x * CELLSIZE, (top_left.y + 2) * CELLSIZE);
            image(wallImages[4], bottom_right.x * CELLSIZE, (bottom_right.y + 2) * CELLSIZE);

            // for every 0.067 second
            int interval = (int) (FPS*0.067);
            if (timeLeft > 0 && (frameCount % interval == 0)) {
                // convert the time left into scores
                timeLeft -= 1;
                levelScore += 1; 

                // As this is occurring, two yellow tiles which begin in the top left corner and
                // bottom right corner will move in a clockwise direction around the edge of the 
                // game board, also at a rate of 1 tile every 0.067 seconds.
                if (top_left.y == 0 && top_left.x < gameboard[0].length - 1){
                    top_left.x += 1; //move right
                    bottom_right.x -= 1; //move left
                } else if ((top_left.x == gameboard[0].length - 1) && top_left.y < gameboard.length - 1) {
                    top_left.y += 1; //move down
                    bottom_right.y -= 1; //move up
                } else if ((top_left.y == gameboard[0].length - 1) && top_left.x > 0){
                    top_left.x -= 1; //move left
                    bottom_right.x += 1; //move right
                } else if ((top_left.x == 0) && top_left.y > 0) {
                    top_left.y -= 1; //move up
                    bottom_right.y += 1; //move down
                }
            }

            // increaseLevel
            if (timeLeft <= 0){
                increaseLevel();
            }
        }


    }

    /**
     * The main method that launches the Inkball application.
     * 
     * @param args command-line arguments for password access.
     */
    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}
