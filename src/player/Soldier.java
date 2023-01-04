package player;
import java.util.Arrays;
import battlecode.common.*;

public class Soldier extends Unit {

    public Soldier(RobotController r) {
        super(r);
    }

    static MapLocation[] enemyArchonLocations;

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        tryDestroyArchon();
        tryAttack();
        goToClosestEnemyCluster();
        goToClosetEnemyArchon();
        explore();
        spreadOut(RobotType.SOLDIER);
    }

    public void goToClosestEnemyCluster () throws GameActionException {
        MapLocation closestCluster = nearestLocation(enemyClusterLocations);
        if(closestCluster != null) goTo(closestCluster);
    }

    public void breakpoint () throws GameActionException {
    }

    public void goToClosetEnemyArchon () throws GameActionException {
        enemyArchonLocations = Comms.getEnemyArchonLocations();

        if(rc.getRoundNum() > 246){
            breakpoint();
        }

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







}


