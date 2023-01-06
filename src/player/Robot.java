package player;
import battlecode.common.*;
import java.util.Random;

public class Robot {

    //TODO create archon state signals... Chilling, obease, under attack, moving, built a unit.
    RobotController rc;
    MapLocation currentLocation;


    int turnCount = 0;
    boolean debug = true;

    int robotNumber;
    String actionPerformed = "No action";
    Random rng;

    RobotInfo[] nearbyEnemies;
    RobotInfo[] nearbyAllies;

    static MapLocation[] enemyClusterLocations;
    static int[] enemyClusterCounts;

    boolean nearbyEnemyArchon = false;
    MapLocation nearbyEnemyArchonLoc = null;



    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public Robot(RobotController r) {
        this.rc = r;
        rng = new Random(rc.getRoundNum()*23981 + rc.getID()*10289);
        Nav.init(rc);
        Comms.init(rc);
        Debug.init(rc);
    }



    public void takeTurn() throws GameActionException {
        if(turnCount==0){
            turnCount=rc.getRoundNum();
        }
        turnCount += 1;
        currentLocation = rc.getLocation();
        robotNumber = Comms.reportAlive();
        scoutNearby(); //gets nearby enemies and nearby allies
        logRobotInfo();
    }

    public void scoutNearby() throws GameActionException{
        int radius = rc.getType().visionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        Team friend = rc.getTeam();
        nearbyEnemies = rc.senseNearbyRobots(radius, opponent);
        nearbyAllies = rc.senseNearbyRobots(radius, friend);
        nearbyEnemyArchonLoc = null;
        nearbyEnemyArchon = false;

        for (RobotInfo enemy : nearbyEnemies) {
            if(enemy.type == RobotType.ARCHON){
                nearbyEnemyArchon = true;
                nearbyEnemyArchonLoc = enemy.location;
            }
        }
    }

    public void logRobotInfo() {
        if (debug) {
                System.out.println(
                        "Type: " + rc.getType() +
                        ": " + rc.getID() +
                         " | " + actionPerformed + " - Bytecode used: " + Clock.getBytecodeNum() +
                        " | Bytecode left: " + Clock.getBytecodesLeft());
            }
        }

    public Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    public MapLocation nearestLocation(MapLocation[] locations){
        int closestDistance =  Integer.MAX_VALUE;
        MapLocation closestLocation = null;

        for (MapLocation location : locations){
            if(location != null &&  currentLocation.distanceSquaredTo(location) < closestDistance){
                closestDistance = currentLocation.distanceSquaredTo(location);
                closestLocation = location;
            }
        }
        return closestLocation;
    }

    public MapLocation nearestEnemyLocation(RobotInfo[] robots){
        int closestDistance = Integer.MAX_VALUE;
        MapLocation closestLocation = null;

        for (RobotInfo enemy : robots){
            if( currentLocation.distanceSquaredTo(enemy.location) < closestDistance){
                closestDistance = currentLocation.distanceSquaredTo(enemy.location);
                closestLocation = enemy.location;
            }
        }
        return closestLocation;
    }

    public int nearestLocationIndex(MapLocation[] locations){
        int closestDistance = 9999;
        int i;

        if(locations.length < 1) return -1;
        for (i=0; i<locations.length; i++){
            if(currentLocation.distanceSquaredTo(locations[i]) < closestDistance){
                closestDistance = currentLocation.distanceSquaredTo(locations[i]);
            }
        }
        return i;
    }


    boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    // tries to move in the general direction of dir
    boolean goTo(Direction dir) throws GameActionException {
        if(!rc.isMovementReady()) return false;
        if(dir == Direction.CENTER){
            if(tryMove(dir)) return true;
        }

        Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateLeft().rotateLeft(), dir.rotateRight(), dir.rotateRight().rotateRight()};
        for (Direction d : toTry) {
            if (tryMove(d))
                return true;
        }
        return false;
    }

    // navigate towards a particular location
    boolean goTo(MapLocation destination) throws GameActionException {
        if(!rc.isMovementReady())  return false;

        if (rc.getLocation().equals(destination)) {
            return goTo(Direction.CENTER);
        } else {

            Debug.drawMovementTargetLine(destination);

            //BFS to determine best direction
            int bcStart = Clock.getBytecodesLeft();
            Direction bestDir = Nav.getDirFromBFS(destination);
            int bcEnd = Clock.getBytecodesLeft();
            int bcUsed = bcStart - bcEnd;
            System.out.println("----Bytecode used for BFS: " + bcUsed);
            if(bestDir != null) return goTo(bestDir);
            return goTo(rc.getLocation().directionTo(destination));
        }
    }



    public void spreadOut(RobotType type) throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        boolean alliesNearby = false;

        int sumXVectors = 0;
        int sumYVectors = 0;


        for (int i = 0; i <nearbyAllies.length;i++ ) {
            RobotInfo info = nearbyAllies[i];
            if(info.type == type){
                alliesNearby = true;
                sumXVectors = sumXVectors + info.location.x - currentLocation.x;
                sumYVectors = sumYVectors +  info.location.y - currentLocation.y;
            }
        }

        if(alliesNearby){
            MapLocation vacantLocation = new MapLocation(currentLocation.x - sumXVectors, currentLocation.y - sumYVectors);
            goTo(vacantLocation);
        }else{
            tryMove(randomDirection());
        }
    }


}
