package playerV2;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Debug extends Robot {

    static RobotController rc;

    public Debug(RobotController r) {
        super(r);
    }

    public static void init(RobotController r) {
        rc = r;
    }

    static final boolean showMovementLines = false; //Yellow
    static final boolean showEnemyClusterDots = false; //purple

    static final boolean showRetreatLines = false; //pink

    static final boolean showMining = false; //black


    public static void drawMovementTargetLine (MapLocation target) throws GameActionException{
        if(showMovementLines) rc.setIndicatorLine(rc.getLocation(),target, 255,255,153);
    }

    public static void drawRetreatTargetLine (MapLocation target) throws GameActionException{
        if(showRetreatLines) rc.setIndicatorLine(rc.getLocation(),target, 255, 192, 203);
    }

    public static void drawEnemyClusterDot (MapLocation target) throws GameActionException{
        if(showEnemyClusterDots) rc.setIndicatorDot(target, 148,0,211 );
    }

    public static void drawMiningDot (MapLocation target) throws GameActionException{
        if(showMining) rc.setIndicatorDot(target,     0,0,0 );
    }





}



