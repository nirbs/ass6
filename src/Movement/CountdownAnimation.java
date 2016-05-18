package Movement;

import Items.SpriteCollection;
import biuoop.DrawSurface;
import biuoop.Sleeper;

import java.awt.*;

/**
 * Created by user on 18/05/2016.
 */
// The CountdownAnimation will display the given gameScreen,
// for numOfSeconds seconds, and on top of them it will show
// a countdown from countFrom back to 1, where each number will
// appear on the screen for (numOfSeconds / countFrom) secods, before
// it is replaced with the next one.
public class CountdownAnimation implements Animation {
    private double numOfSeconds;
    private int counter;
    private int countFrom;
    private SpriteCollection screen;
    private boolean running;

    public CountdownAnimation(double numOfSeconds, int countFrom, SpriteCollection gameScreen) {
        this.numOfSeconds=numOfSeconds;
        counter=countFrom;
        this.countFrom = countFrom;
        screen=gameScreen;
        running = false;
    }
    public void doOneFrame(DrawSurface d) {


        String s;
        screen.drawAllOn(d);

        if (counter == 0) {
            s = "GO";
            running = true;
        } else {
            s = Integer.toString(counter);
        }
        d.drawText(d.getWidth() / 2, 30, s, 32);
        --counter;
        Sleeper sleeper = new Sleeper();
        sleeper.sleepFor((long) (3000));


    }
    public boolean shouldStop() {
        return running;
    }
}