package player;

import battlecode.common.*;


public class Builder extends Unit {

    static boolean boolNearMapEdge = false;
    static int countLabs = 0;

    static RobotInfo lastRobotHealed;
    static RobotInfo robotToHeal;

    public Builder(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        boolNearMapEdge = isNearMapEdge();
        countLabs = Comms.getUnitCount(RobotType.LABORATORY);
        if(!boolNearMapEdge){
            MapLocation mapEdge = findEdgeOfMap();
            if(mapEdge != null){
                goTo(mapEdge);
            }else{
                goTo(Direction.EAST);
            }

        }else{
            if(countLabs < 3){
                tryBuildLab();
            }
        }
        tryRepair();
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


    public void tryRepair() throws GameActionException {

        if(!rc.isActionReady()) return;

        //Completely heal last robot before moving to next one
        if (lastRobotHealed != null && rc.canSenseRobot(lastRobotHealed.ID) && lastRobotHealed.health < getMaxHealth(lastRobotHealed.type)) {
            robotToHeal = lastRobotHealed;
        }else{
            //Find new Robot to heal if no previous
            robotToHeal = findAllyToHeal();
        }


        if(robotToHeal!= null && rc.canRepair(robotToHeal.location) && robotToHeal.health < getMaxHealth(robotToHeal.type)) {
            lastRobotHealed = robotToHeal;
            rc.repair(robotToHeal.location);
        }
    }

    public RobotInfo findAllyToHeal() throws GameActionException{
        RobotInfo friendly;
        RobotInfo robotToHeal = null;
        RobotInfo maybeArchon = null;
        RobotInfo maybeLab = null;
        int qty_allies = nearbyAllies.length;
        for (int i= qty_allies; --i > 0;){
            friendly = nearbyAllies[i];
            switch(friendly.type) {
                case LABORATORY:
                    if((maybeLab == null || friendly.health < maybeLab.health ) && friendly.health != RobotType.LABORATORY.health) {
                        maybeLab = friendly;
                    }
                    break;
                case ARCHON:
                    if((maybeArchon == null || friendly.health <  maybeArchon.health ) && friendly.health != RobotType.ARCHON.health) {
                        maybeArchon = friendly;
                    }
                    break;
                default:
                    break;
            }
        }
        if(maybeArchon != null && rc.canRepair(maybeArchon.location)) robotToHeal = maybeArchon;
        if(maybeLab != null && rc.canRepair(maybeLab.location)) robotToHeal = maybeLab;
        return robotToHeal;
    }

    private MapLocation findEdgeOfMap() throws GameActionException{

        MapLocation lastloc;
        MapLocation nextloc;

        for(int x = -4; x<0; x++){ //check West
             lastloc = rc.getLocation().translate(x, 0);
             nextloc = rc.getLocation().translate(x+1, 0);
            if(!rc.onTheMap(lastloc) && rc.onTheMap(nextloc)) return nextloc;
        }

        for(int x = 4; x>0; x--){ //check East
             lastloc = rc.getLocation().translate(x, 0);
             nextloc = rc.getLocation().translate(x-1, 0);
            if(!rc.onTheMap(lastloc) && rc.onTheMap(nextloc)) return nextloc;
        }

        for(int y = 4; y>0; y--){ //check North
            lastloc = rc.getLocation().translate(0, y);
            nextloc = rc.getLocation().translate(0, y-1);
            if(!rc.onTheMap(lastloc) && rc.onTheMap(nextloc)) return nextloc;
        }

        for(int y = -4; y<0; y++){ //check South
            lastloc = rc.getLocation().translate(0, y);
            nextloc = rc.getLocation().translate(0, y+1);
            if(!rc.onTheMap(lastloc) && rc.onTheMap(nextloc)) return nextloc;
        }

        return null;
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
