package playerV4;
import battlecode.common.*;

import java.util.Arrays;

public class Unit extends Robot {

    MapLocation exploreTargetLocation;
    MapLocation[] allyArchonLocations;

    static MapLocation[] enemyArchonLocations;
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

    public void goToClosestEnemyCluster () throws GameActionException {
        MapLocation closestCluster = nearestLocation(enemyClusterLocations);
        if(closestCluster != null) goTo(closestCluster);
    }

    public void goToClosetEnemyArchon () throws GameActionException {
        enemyArchonLocations = Comms.getEnemyArchonLocations();


        for(int i=0; i<enemyArchonLocations.length; i++){
            //Reset enemy archon location if you can see the target and there is no enemy archon
            if(enemyArchonLocations[i] != null && rc.canSenseLocation(enemyArchonLocations[i]) && !nearbyEnemyArchon ){
                enemyArchonLocations[i] = null;
                rc.writeSharedArray(Comms.IDX_Enemy_Archon_1_Loc + i, 0);
            }
        }

        if(nearbyEnemyArchonLoc != null){
            //Check if the enemy archon is already stored in the shared array.
            boolean sharedArrContainsEnemyLoc = Arrays.asList(enemyArchonLocations).contains(nearbyEnemyArchonLoc);
            if(!sharedArrContainsEnemyLoc){
                for(int i=0; i<enemyArchonLocations.length; i++){
                    if(enemyArchonLocations[0] == null) {
                        rc.writeSharedArray(Comms.IDX_Enemy_Archon_1_Loc + i, Comms.locationToInt(nearbyEnemyArchonLoc));
                        break;
                    }
                }
            }
        }


        MapLocation closestLoc = nearestLocation(enemyArchonLocations);

        if(closestLoc != null){
            goTo(closestLoc);
        }
    }

    public void tryAttack() throws GameActionException{
        if (nearbyEnemies.length > 0) {
            MapLocation toAttack = nearestEnemyLocation(nearbyEnemies);
            MapLocation toPrioritize = prioritizedEnemyLoc(nearbyEnemies);
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }else{
                goTo(toPrioritize);
            }
        }
    }

    public MapLocation prioritizedEnemyLoc(RobotInfo[] enemies) throws GameActionException{
        MapLocation enemyLoc = null;
        int highestPriority = 0;
        int priority = 0;
        for(int i=0; i < enemies.length; i++){
            priority = getEnemyPriority(enemies[i].type);
            if(priority > highestPriority){
                enemyLoc = enemies[i].location;
                highestPriority = priority;
            }
        }
        return enemyLoc;
    }

    public int getEnemyPriority (RobotType type) throws GameActionException{
        int priority = 0;
        switch (type){
            case MINER:
                priority = 1;
                break;
            case ARCHON:
                priority = 2;
                break;
            case SOLDIER:
                priority = 3;
                break;
            case SAGE:
                priority = 4;
                break;
        }
        return priority;
    }

    public void tryDestroyArchon () throws GameActionException {
        if(nearbyEnemyArchon){
            MapLocation toAttack = currentLocation;
            //find enemy Archon location
            for (RobotInfo enemy : nearbyEnemies) {
                if(enemy.type == RobotType.ARCHON){
                    toAttack = enemy.location;
                    break;
                }
            }

            //If there is an enemy archon
            if(!currentLocation.equals(toAttack)){
                //Attach enemy Archon
                if (rc.canAttack(toAttack)) {
                    rc.attack(toAttack);
                }else
                    //Go to Archon
                    goTo(toAttack);
            }
        }
    }



}
