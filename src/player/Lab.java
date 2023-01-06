package player;
import battlecode.common.*;

public class Lab extends Robot {

    public Lab(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        if(rc.canTransmute()) {
            rc.transmute();
        }

    }
}
