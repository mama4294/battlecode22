package playerV2;
import battlecode.common.*;

public class Soldier extends Unit {

    public Soldier(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        tryDestroyArchon();
        tryAttack();
        goToClosestEnemyCluster();
        explore();
        spreadOut(RobotType.SOLDIER);
    }

    public void goToClosestEnemyCluster () throws GameActionException {
        MapLocation closestCluster = nearestLocation(enemyClusterLocations);
        if(closestCluster != null) goTo(closestCluster);
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


    public void tryAttack() throws GameActionException{
        if (nearbyEnemies.length > 0) {
            MapLocation toAttack = nearbyEnemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }else{
                goTo(nearbyEnemies[0].location);
            }
        }
    }






}


