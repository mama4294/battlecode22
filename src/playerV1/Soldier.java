package playerV1;
import battlecode.common.*;

public class Soldier extends Robot {

    public Soldier(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        tryDestroyArchon();
        tryAttack();
        spreadOut(RobotType.SOLDIER);
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
                    rc.setIndicatorDot(toAttack, 220,20,60);
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
                rc.setIndicatorDot(toAttack, 220,20,60);
            }else{
                goTo(nearbyEnemies[0].location);
                rc.setIndicatorLine(currentLocation,nearbyEnemies[0].location , 255,160,122);
            }
        }
    }






}


