package playerV1;
import battlecode.common.*;
import java.util.Random;

public class Robot {
    RobotController rc;
    MapLocation currentLocation;
    int turnCount = 0;
    boolean debug = false;
    String actionPerformed = "No action";
    Random rng = new Random(6147);

    RobotInfo[] nearbyEnemies;
    RobotInfo[] nearbyAllies;

    boolean nearbyEnemyArchon = false;



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
        Nav.init(rc);
    }

    public void takeTurn() throws GameActionException {
        if(turnCount==0){
            turnCount=rc.getRoundNum();
        }
        turnCount += 1;
        currentLocation = rc.getLocation();
        scoutNearby();
//        logRobotInfo();
    }

    public void scoutNearby() throws GameActionException{
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        Team friend = rc.getTeam();
        nearbyEnemies = rc.senseNearbyRobots(radius, opponent);
        nearbyAllies = rc.senseNearbyRobots(radius, friend);

        for (RobotInfo enemy : nearbyEnemies) {
            if(enemy.type == RobotType.ARCHON){
                nearbyEnemyArchon = true;
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
        int closestDistance = 9999;
        MapLocation closestLocation = locations[0];

        for (MapLocation location : locations){
            if(currentLocation.distanceSquaredTo(location) < closestDistance){
                closestDistance = currentLocation.distanceSquaredTo(location);
                closestLocation = location;
            }
        }
        return closestLocation;
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
        if (rc.getLocation().equals(destination)) {
            return goTo(Direction.CENTER);
        } else {
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
            if(debug)  rc.setIndicatorLine(currentLocation, vacantLocation, 124,252,0);
            goTo(vacantLocation);
        }else{
            tryMove(randomDirection());
        }


    }
}
