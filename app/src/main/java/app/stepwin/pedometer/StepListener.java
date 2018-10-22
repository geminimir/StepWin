package app.stepwin.pedometer;

/**
 * Created by natraj on 14/9/17.
 */

// Will listen to step alerts
public interface StepListener {

    public void step(long timeNs);

}
