package player;
import battlecode.common.*;

public class Lab extends Robot {

    public Lab(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        RobotMode mode = rc.getMode();
        if (mode == RobotMode.PORTABLE && rc.canTransform()) rc.transform();
        if(mode == RobotMode.TURRET){
            if(rc.canTransmute()) {
                rc.transmute();
            }
        }



    }
}
