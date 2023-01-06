package playerV4;
import battlecode.common.*;

public class Miner extends Unit {

    public Miner(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        runAwayFromEnemies();
        tryMine();
        lookForGold();
        lookForLead();
        explore();
        spreadOut(RobotType.MINER);
    }

    public void lookForGold() throws GameActionException{
        MapLocation[] goldLocations = rc.senseNearbyLocationsWithGold(rc.getType().visionRadiusSquared);
        MapLocation locationInQuestion;
        MapLocation bestgoldLocation = null;
        double bestScore = Integer.MIN_VALUE;

        for(int i = goldLocations.length - 1; i >= 0; i--) {
            locationInQuestion = goldLocations[i];
            int goldAmount = rc.senseGold(locationInQuestion);
            if(goldAmount > 1){
                double goldScore = getGoldLocationScore(locationInQuestion, goldAmount);
                if(goldScore > bestScore){
                    bestScore = goldScore;
                    bestgoldLocation=locationInQuestion;
                }
            }
        }

        if(bestgoldLocation!=null){
            goTo(bestgoldLocation);
        }
    }

    public void lookForLead() throws GameActionException{
        MapLocation[] leadLocations = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared);
        MapLocation locationInQuestion;
        MapLocation bestLeadLocation = null;
        double bestScore = Integer.MIN_VALUE;

        for(int i = leadLocations.length - 1; i >= 0; i--) {
            locationInQuestion = leadLocations[i];
            int leadAmount = rc.senseLead(locationInQuestion);
            if(leadAmount > 1){
                double leadScore = getLeadLocationScore(locationInQuestion, leadAmount);
                if(leadScore > bestScore){
                    bestScore = leadScore;
                    bestLeadLocation=locationInQuestion;
                }
            }
        }

        if(bestLeadLocation!=null){
            goTo(bestLeadLocation);
        }
    }

    public double getLeadLocationScore(MapLocation LeadLocation, int leadAmount){
        int radiusSquared = currentLocation.distanceSquaredTo(LeadLocation);
        double currentDistanceToLead = Math.sqrt((double) radiusSquared);
        double AmountMinedBeforeWeGetThere = 0;
        for (RobotInfo nearbyAlly: nearbyAllies){
            if(nearbyAlly.type == RobotType.MINER){
                MapLocation alliedMinerLoc = nearbyAlly.getLocation();
                double allyDistanceToLead = Math.sqrt((double)alliedMinerLoc.distanceSquaredTo(LeadLocation));
                if(allyDistanceToLead < currentDistanceToLead) {
                    AmountMinedBeforeWeGetThere += (currentDistanceToLead - allyDistanceToLead) * 5;
                }
            }
        }

        if(radiusSquared == 0 && leadAmount > 1 ){return Integer.MAX_VALUE;}
        return (float)leadAmount - currentDistanceToLead * 5  - AmountMinedBeforeWeGetThere;
    }

    public double getGoldLocationScore(MapLocation goldLocation, int goldAmount){
        int radiusSquared = currentLocation.distanceSquaredTo(goldLocation);
        double currentDistanceTogold = Math.sqrt((double) radiusSquared);
        double AmountMinedBeforeWeGetThere = 0;
        for (RobotInfo nearbyAlly: nearbyAllies){
            if(nearbyAlly.type == RobotType.MINER){
                MapLocation alliedMinerLoc = nearbyAlly.getLocation();
                double allyDistanceTogold = Math.sqrt((double)alliedMinerLoc.distanceSquaredTo(goldLocation));
                if(allyDistanceTogold < currentDistanceTogold) {
                    AmountMinedBeforeWeGetThere += (currentDistanceTogold - allyDistanceTogold) * 5;
                }
            }
        }

        if(radiusSquared == 0 && goldAmount > 1 ){return Integer.MAX_VALUE;}
        return (float)goldAmount - currentDistanceTogold * 5  - AmountMinedBeforeWeGetThere;
    }

    public void runAwayFromEnemies() throws GameActionException{
        for (int i = 0; i <nearbyEnemies.length;i++ ) {
            RobotInfo info = nearbyEnemies[i];
            if(info.type == RobotType.SOLDIER || info.type == RobotType.SAGE){
                //Run away
                tryMove(currentLocation.directionTo(nearbyEnemies[i].location).opposite());
            }
        }

    }

    // Deplete unit lead sources if far away from home and more enemies than friends
    public boolean shouldDepleteUnitLead() throws GameActionException {
        return nearbyEnemies.length > nearbyAllies.length;
        //todo: dont deplete if close to friendly archon

    }


    public void tryMine() throws GameActionException {

        // Try to mine on squares around us.
        MapLocation me = rc.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                // Notice that the Miner's action cooldown is very low.
                // You can mine multiple times per turn!
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                    Debug.drawMiningDot(mineLocation);
                }
                boolean closetoAllyArchons = closeToAllyArchon();
                boolean shouldCompletelyMine = (nearbyEnemies.length > nearbyAllies.length && !closetoAllyArchons);

                while (rc.canMineLead(mineLocation) && (rc.senseLead(mineLocation) > 1 || shouldCompletelyMine)) {
                    rc.mineLead(mineLocation);
                    Debug.drawMiningDot(mineLocation);
                }
            }
        }

    }
}


