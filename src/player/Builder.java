package player;

import battlecode.common.*;


public class Builder extends Unit {

    static boolean nearMapEdge = false;

    public Builder(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        nearMapEdge = isNearMapEdge();
        if(!nearMapEdge){
            goTo(findMapEdgeLoc());
        }else{
            tryBuildLab();
        }
    }


    private boolean isNearMapEdge() throws GameActionException {
        //check add directions and check if they are on the map
        for(Direction dir : Direction.values()) {
            MapLocation loc = rc.getLocation().add(dir);
            if(!rc.onTheMap(loc)) {
                return true;
            }
        }
        return false;
    }

    private MapLocation findMapEdgeLoc() throws GameActionException {
        MapLocation aveEnemyLoc = Comms.getAverageEnemyLocation();

        //goto map edge
        if(aveEnemyLoc != null) {
            //get direction away from enemy
            Direction dirAwayFromEnemy = rc.getLocation().directionTo(aveEnemyLoc).opposite();
            //get location away from enemy
            MapLocation locAwayFromEnemy = rc.getLocation().add(dirAwayFromEnemy);
            return locAwayFromEnemy;
        }
        return null;
    }


    private Boolean tryBuildLab() throws GameActionException {

        //return false if not ready or don't have enough money
        if(!rc.isActionReady() || rc.getTeamLeadAmount(rc.getTeam()) < RobotType.LABORATORY.buildCostLead) {
            return false;
        }

        Direction[] optimalDirectionArray = getBuildDirections(); //get optimal build direction array

        //Build it by trying the optimal build directions
            for(int i=0; i< optimalDirectionArray.length; i++){
                if(buildRobot(RobotType.LABORATORY, optimalDirectionArray[i])){
                    return true;
                }
            }

        return false;

    }

}
