package edu.uchicago.gerber._08final.mvc.view;

import edu.uchicago.gerber._08final.mvc.controller.CommandCenter;
import edu.uchicago.gerber._08final.mvc.controller.Game;
import edu.uchicago.gerber._08final.mvc.controller.Utils;
import edu.uchicago.gerber._08final.mvc.model.Movable;
import edu.uchicago.gerber._08final.mvc.model.PolarPoint;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;


public class GamePanel extends Panel {

    // ==============================================================
    // FIELDS
    // ==============================================================
    private final Font fontNormal = new Font("SansSerif", Font.BOLD, 12);
    private final Font fontBig = new Font("SansSerif", Font.BOLD + Font.ITALIC, 100);
    private FontMetrics fontMetrics;
    private int fontWidth;
    private int fontHeight;

    //used to draw number of ships remaining
    private final Point[] points;

    //used for double-buffering
    private Image imgOff;
    private Graphics grpOff;


    // ==============================================================
    // CONSTRUCTOR
    // ==============================================================

    public GamePanel(Dimension dim) {

        GameFrame gameFrame = new GameFrame();

        gameFrame.getContentPane().add(this);

        // Robert Alef's awesome falcon design
        List<Point> heartPoints = new ArrayList<>();
        heartPoints.add(new Point(300, 300));
        heartPoints.add(new Point(240, 240));
        heartPoints.add(new Point(180, 300));
        heartPoints.add(new Point(300, 420));
        heartPoints.add(new Point(420, 300));
        heartPoints.add(new Point(360, 240));
        heartPoints.add(new Point(300, 300));

        points = heartPoints.toArray(new Point[0]);

        gameFrame.pack();
        initFontInfo();
        gameFrame.setSize(dim);
        //change the name of the game-frame to your game name
        gameFrame.setTitle("DeepSeaCoinCollecting");
        gameFrame.setResizable(false);
        gameFrame.setVisible(true);
        setFocusable(true);
    }


    // ==============================================================
    // METHODS
    // ==============================================================

    private void drawFalconStatus(final Graphics graphics){

        graphics.setColor(Color.white);
        graphics.setFont(fontNormal);

        //draw score always
        //21 15
        graphics.drawString("Score :  " + CommandCenter.getInstance().getScore(), 30, 30);

        //draw the level upper-left corner always
        String levelText = "Level: " + CommandCenter.getInstance().getLevel();
        graphics.drawString(levelText, 30, 50); //upper-left corner

        //build the status string array with possible messages in middle of screen
        List<String> statusArray = new ArrayList<>();
        if (CommandCenter.getInstance().getSub().getShowLevel() > 0)
        {statusArray.add(levelText);}
        if (CommandCenter.getInstance().getSub().isMaxSpeedAttained()) statusArray.add("WARNING - SLOW DOWN");
        //draw the statusArray strings to middle of screen
        if (statusArray.size() > 0)
            displayTextOnScreen(graphics, statusArray.toArray(new String[0]));



    }

    private void drawTime(Graphics g) {
        g.setColor(Color.white);
        g.drawRect(Game.DIM.width/2-50,Game.DIM.height/8-30,100,20);
        g.setFont(fontNormal);
        g.drawString("TIME :  " + CommandCenter.getInstance().getMTimeLeft(), Game.DIM.width/2-30,
                Game.DIM.height/8-15);



    }

    private void drawOneMeter(Graphics g, Color color, int offSet, int percent) {

        int xVal = Game.DIM.width - (100 + 120 * offSet);
        int yVal = Game.DIM.height - 45;

        //draw meter
        g.setColor(color);
        g.fillRect(xVal, yVal, percent, 10);

        //draw gray box
        g.setColor(Color.DARK_GRAY);
        g.drawRect(xVal, yVal, 100, 10);
    }

    @Override
    public void update(Graphics g) {

        // The following "off" vars are used for the off-screen double-buffered image.
        imgOff = createImage(Game.DIM.width, Game.DIM.height);
        //get its graphics context
        grpOff = imgOff.getGraphics();

        //Fill the off-screen image background with black.
        grpOff.setColor(new Color(7,22,48));
        grpOff.fillRect(0, 0, Game.DIM.width, Game.DIM.height);

        //this is used for development, you may remove drawNumFrame() in your final game.
        drawTime(grpOff);

        if (CommandCenter.getInstance().isGameOver()) {
            displayTextOnScreen(grpOff,
                    "GAME OVER",
                    "use the arrow keys to turn and thrust",
                    "use the space bar to fire",
                    "'S' to Start",
                    "'P' to Pause",
                    "'Q' to Quit",
                    "'M' to toggle music"

            );
        } else if (CommandCenter.getInstance().isPaused()) {

            displayTextOnScreen(grpOff, "Game Paused");

        }

        //playing and not paused!
        else {


            moveDrawMovables(grpOff,
                    CommandCenter.getInstance().getMovDebris(),
                    CommandCenter.getInstance().getMovFloaters(),
                    CommandCenter.getInstance().getMovFoes(),
                    CommandCenter.getInstance().getMovFriends());


            drawNumberShipsRemaining(grpOff);
            drawFalconStatus(grpOff);


        }

        //after drawing all the movables or text on the offscreen-image, copy it in one fell-swoop to graphics context
        // of the game panel, and show it for ~40ms. If you attempt to draw sprites directly on the gamePanel, e.g.
        // without the use of a double-buffered off-screen image, you will see flickering.
        g.drawImage(imgOff, 0, 0, this);
    }


    //this method causes all sprites to move and draw themselves
    @SafeVarargs
    private final void moveDrawMovables(final Graphics g, List<Movable>... teams) {

        BiConsumer<Movable, Graphics> moveDraw = (mov, grp) -> {
            mov.move();
            mov.draw(grp);
        };


        Arrays.stream(teams) //Stream<List<Movable>>
                //we use flatMap to flatten the teams (List<Movable>[]) passed-in above into a single stream of Movables
                .flatMap(Collection::stream) //Stream<Movable>
                .forEach(m -> moveDraw.accept(m, g));


    }




    // Draw the number of falcons remaining on the bottom-right of the screen.
    private void drawNumberShipsRemaining(Graphics g) {
        int numFalcons = CommandCenter.getInstance().getNumFalcons();
        while (numFalcons > 0) {
            drawOneShip(g, numFalcons--);
        }
    }


    private void drawOneShip(Graphics g, int offSet) {

        g.setColor(Color.RED);

        //rotate the ship 90 degrees
        double degrees90 = 270.0;
        int radius = 50;
        int xVal = 35+Game.DIM.width - (30 * offSet);
        int yVal = Game.DIM.height - 80;

        //the reason we convert to polar-points is that it's much easier to rotate polar-points.
        List<PolarPoint> polars = Utils.cartesianToPolar(points);

        Function<PolarPoint, PolarPoint> rotatePolarBy90 =
                pp -> new PolarPoint(
                        pp.getR(),
                        pp.getTheta() + Math.toRadians(degrees90) //rotated Theta
                );

        Function<PolarPoint, Point> polarToCartesian =
                pp -> new Point(
                        (int)  (pp.getR() * radius * Math.sin(pp.getTheta())),
                        (int)  (pp.getR() * radius * Math.cos(pp.getTheta())));

        Function<Point, Point> adjustForLocation =
                pnt -> new Point(
                        pnt.x + xVal,
                        pnt.y + yVal);


        g.fillPolygon(

                polars.stream()
                        .map(rotatePolarBy90)
                        .map(polarToCartesian)
                        .map(adjustForLocation)
                        .map(pnt -> pnt.x)
                        .mapToInt(Integer::intValue)
                        .toArray(),

                polars.stream()
                        .map(rotatePolarBy90)
                        .map(polarToCartesian)
                        .map(adjustForLocation)
                        .map(pnt -> pnt.y)
                        .mapToInt(Integer::intValue)
                        .toArray(),

                polars.size());


    }

    private void initFontInfo() {
        Graphics g = getGraphics();            // get the graphics context for the panel
        g.setFont(fontNormal);                        // take care of some simple font stuff
        fontMetrics = g.getFontMetrics();
        fontWidth = fontMetrics.getMaxAdvance();
        fontHeight = fontMetrics.getHeight();
        g.setFont(fontBig);                    // set font info
    }


    // This method draws some text to the middle of the screen
    private void displayTextOnScreen(final Graphics graphics, String... lines) {

        //AtomicInteger is safe to pass into a stream
        final AtomicInteger spacer = new AtomicInteger(0);
        Arrays.stream(lines)
                .forEach(str ->
                            graphics.drawString(str, (Game.DIM.width - fontMetrics.stringWidth(str)) / 2,
                                    Game.DIM.height / 4 + fontHeight + spacer.getAndAdd(40))

                );


    }


}
