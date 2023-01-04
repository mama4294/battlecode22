package player;
import battlecode.common.*;

import java.util.Random;

public class Unit extends Robot{

    MapLocation exploreTargetLocation;
    MapLocation[] allyArchonLocations;
    static final int EXPLORE_BOREDOM = 50;
    static int boredom=0;

    public Unit(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        findAllyArchons();
        Comms.getEnemyClusterLocations();
        Comms.getEnemyClusterCounts();
        Comms.reportEnemyCluster(nearbyEnemies);
        retreatIfNeedsHealing();
    }

    public void retreatIfNeedsHealing() throws GameActionException{
        if(rc.getHealth() < rc.getType().health/4){
            //Go to home archon if at less than quarter health

            //find closest ally archon
            MapLocation closestAllyArchon = Nav.findClosestLocFromArray(allyArchonLocations);

            if(closestAllyArchon != null){
                Debug.drawRetreatTargetLine(closestAllyArchon);
                if(currentLocation.distanceSquaredTo(closestAllyArchon) <= RobotType.ARCHON.actionRadiusSquared){
                    //Stop when in Archon's action radius so to not crowd
                    goTo(Direction.CENTER);
                }else{
                    goTo(closestAllyArchon);
                }
            }


        }
    }


    public void explore() throws GameActionException{
        if(exploreTargetLocation == null || boredom > EXPLORE_BOREDOM || currentLocation.distanceSquaredTo(exploreTargetLocation) < rc.getType().actionRadiusSquared){
            //create new exploration target
            int width = rc.getMapWidth();
            int height = rc.getMapHeight();

            double randomX = Math.random()*width;
            double randomY = Math.random()*height;

            int randX = (int) randomX;
            int randY = (int) randomY;

            exploreTargetLocation = new MapLocation(randX, randY);
            boredom = 0;
        }

        if(exploreTargetLocation != null){
            boredom++;
            goTo(exploreTargetLocation);
        }
    }


//    public void findHomeArchon() throws GameActionException {
//        if (homeArchonLocation == null) {
//            RobotInfo friendly = null;
//            //search for home archon
//            int qty_allies = nearbyAllies.length;
//            for (int i = qty_allies; --i > 0; ) {
//                friendly = nearbyAllies[i];
//                switch (friendly.type){
//                    case ARCHON:
//                        homeArchonLocation = friendly.getLocation();
//                        break;
//                }
//            }
//        }
//    }


    public void findAllyArchons() throws GameActionException {
        if(allyArchonLocations == null){
            allyArchonLocations = Comms.getAllyArchonLocations();
        }
    }

    public boolean closeToAllyArchon() throws GameActionException {
        if(allyArchonLocations != null){
            for (int i = allyArchonLocations.length; --i > 0; ) {
                if(allyArchonLocations[i] != null){
                    if(rc.getLocation().distanceSquaredTo(allyArchonLocations[i]) < 10) return true;
                }
            }

            }
        return false;

    }



}
