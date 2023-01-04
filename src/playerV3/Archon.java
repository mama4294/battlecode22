package playerV3;
import battlecode.common.*;

public class Archon extends Robot {


    static RobotInfo lastRobotHealed;
    static RobotInfo robotToHeal;

    static int MAX_NUM_MINERS;

    static int MAX_MAP_SIZE_TO_MINER_RATIO = 16;


    public Archon(RobotController r) {

        super(r);
        MAX_NUM_MINERS = Math.min(128, rc.getMapWidth() * rc.getMapHeight() / MAX_MAP_SIZE_TO_MINER_RATIO);
    }

    static int archonCount;
    static int minerCount;
    static int soldierCount;
    static int labCount;
    static int watchtowerCount;

    static int builderCount;
    static int sageCount;

    static int turnsSinceLastBuild;

    static int maxTurnsSinceLastBuild;

    static buildOption toBuildThisRound;
    static buildOption toBuildNextRound;

    static enum buildOption {
        NONE,
        SOLDER,
        MINER,
        BUILDER,

    }




    public void takeTurn() throws GameActionException {
        super.takeTurn();
        updateUnitCounts();
        determineWhatToBuild();
        determineWhatToBuildNextRound();
        turnsSinceLastBuild = Comms.getTurnsSinceLastBuild(robotNumber);
        maxTurnsSinceLastBuild = Comms.getMaxTurnsSinceLastBuild();
        tryBuildUnit();
        tryHealRobot();
    }



public void determineWhatToBuild() throws GameActionException{
        //Get from shared array
    toBuildThisRound = Comms.getNextRoundToBuildValue(robotNumber);

}

    public void getTurnsSinceLastBuild() throws GameActionException{
        //Get from shared array
        toBuildThisRound = Comms.getNextRoundToBuildValue(robotNumber);

    }


    public void determineWhatToBuildNextRound() throws GameActionException{
        //Write to shared array what will be built next round
        buildOption toBuildNextRound = buildOption.NONE;
        boolean buildMiner = rng.nextBoolean();

        if(rc.isActionReady()){
            if((buildMiner && minerCount < MAX_NUM_MINERS) || rc.getRoundNum() < 10){
                toBuildNextRound = buildOption.MINER;
            }else{
                toBuildNextRound = buildOption.SOLDER;
            }
        }

        Comms.setNextRoundToBuildValue(toBuildNextRound, robotNumber);
    }



    public RobotInfo findAllyToHeal() throws GameActionException{
        RobotInfo friendly;
        RobotInfo robotToHeal = null;
        RobotInfo maybeSage = null;
        RobotInfo maybeSoldier = null;
        RobotInfo maybeBuilder = null;
        RobotInfo maybeMiner = null;
        int qty_allies = nearbyAllies.length;
        for (int i= qty_allies; --i > 0;){
            friendly = nearbyAllies[i];
            switch(friendly.type) {
                case SAGE:
                    if((maybeSage == null || maybeSage.health > friendly.health)
                            && friendly.health != RobotType.SAGE.health) {
                        maybeSage = friendly;
                    }
                    break;
                case SOLDIER:
                    if((maybeSoldier == null || maybeSoldier.health > friendly.health)
                            && friendly.health != RobotType.SOLDIER.health) {
                        maybeSoldier = friendly;
                    }
                    break;
                case BUILDER:
                    if((maybeBuilder == null || maybeBuilder.health > friendly.health)
                            && friendly.health != RobotType.BUILDER.health) {
                        maybeBuilder = friendly;
                    }
                    break;
                case MINER:
                    if((maybeMiner == null || maybeMiner.health > friendly.health)
                            && friendly.health != RobotType.MINER.health) {
                        maybeMiner = friendly;
                    }
                    break;
                default:
                    break;
            }
        }
        if(maybeMiner != null && rc.canRepair(maybeMiner.location)) robotToHeal = maybeMiner;
        if(maybeBuilder != null && rc.canRepair(maybeBuilder.location)) robotToHeal = maybeBuilder;
        if(maybeSoldier != null && rc.canRepair(maybeSoldier.location)) robotToHeal = maybeSoldier;
        if(maybeSage != null && rc.canRepair(maybeSage.location)) robotToHeal = maybeSage;
        return robotToHeal;
    }

    public void updateUnitCounts() throws GameActionException {
        archonCount = Comms.getUnitCount(RobotType.ARCHON);
        minerCount = Comms.getUnitCount(RobotType.MINER);
        soldierCount = Comms.getUnitCount(RobotType.SOLDIER);
        labCount = Comms.getUnitCount(RobotType.LABORATORY);
        watchtowerCount = Comms.getUnitCount(RobotType.WATCHTOWER);
        builderCount = Comms.getUnitCount(RobotType.BUILDER);
        sageCount = Comms.getUnitCount(RobotType.SAGE);

        if(debug) System.out.println(
                "Round: " + rc.getRoundNum() + " | " + "Archon Count: " + archonCount + " | " + "Miner Count: " + minerCount + " | " +  "Soldier Count: " + soldierCount
        );
    }

    public void tryBuildUnit() throws GameActionException{
        boolean buildSuccessful = false;


        if(!rc.isActionReady() || toBuildThisRound == buildOption.NONE || turnsSinceLastBuild < maxTurnsSinceLastBuild){
            Comms.incrementTurnsSinceBuild(robotNumber);
            return; //skip if not ready
        }

        Direction[] optimalDirectionArray = getBuildDirections(); //get optimal build direction array
        RobotType toBuild = null;

        //Set what to build
        switch (toBuildThisRound){
            case MINER:
                toBuild = RobotType.MINER;
                break;
            case SOLDER:
                toBuild = RobotType.SOLDIER;
                break;
        }

        //Build it by trying the optimal build directions
        if(toBuild != null  && rc.getTeamLeadAmount(rc.getTeam()) > toBuild.buildCostLead){
            for(int i=0; i< optimalDirectionArray.length; i++){
                if(buildRobot(toBuild, optimalDirectionArray[i])){
                    buildSuccessful=true;
                    break;
                }
            }
        }

        if(buildSuccessful){
            Comms.resetTurnsSinceBuild(robotNumber);
        }else{
            Comms.incrementTurnsSinceBuild(robotNumber);
        }


    }

    public boolean buildRobot (RobotType type, Direction dir) throws GameActionException{
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        }
        return false;
    }



    // Get an array of directions in order with the least amount of rubble.
    public Direction[] getBuildDirections() throws GameActionException {
        boolean[] usedDir = new boolean[directions.length];
        Direction[] dirs = new Direction[directions.length];
        int numDirections = 0;
        int rubble;
        int minRubble;
        int numEqual;
        int bestDir;
        MapLocation loc;
        for(int i = 0; i < directions.length; i++) {
            minRubble = 101;
            bestDir = -1;
            numEqual = 0;
            for(int j = 0; j < directions.length; j++) {
                loc = rc.adjacentLocation(directions[j]);
                if(usedDir[j] || !rc.onTheMap(loc)) continue;
                rubble = rc.senseRubble(loc);
                if(rubble < minRubble) {
                    minRubble = rubble;
                    bestDir = j;
                } else if(rubble == minRubble) {
                    numEqual++;

                    if(rng.nextBoolean()) {// get a random boolean
                        minRubble = rubble;
                        bestDir = j;
                    }
                }
            }

            if(bestDir != -1) {
                usedDir[bestDir] = true;
                dirs[numDirections++] = directions[bestDir];
            }
        }

        Direction[] buildDirctions = new Direction[numDirections];
        System.arraycopy(dirs, 0, buildDirctions, 0, numDirections);
        return buildDirctions;
    }


    public void tryHealRobot() throws GameActionException {


        //Completely heal last robot before moving to next one
        if (lastRobotHealed != null && rc.canSenseRobot(lastRobotHealed.ID) && lastRobotHealed.health < getMaxHealth(lastRobotHealed.type)) {
            robotToHeal = lastRobotHealed;
        }

        //Find new Robot to heal if no previous
        robotToHeal = findAllyToHeal();

        if(robotToHeal!= null && rc.canRepair(robotToHeal.location) && robotToHeal.health < getMaxHealth(robotToHeal.type)) {
            lastRobotHealed = robotToHeal;
            rc.repair(robotToHeal.location);
        }
    }

    public int getMaxHealth(RobotType robotType) {
        switch(robotType) {
            case SAGE: return RobotType.SAGE.health;
            case MINER: return RobotType.MINER.health;
            case SOLDIER: return RobotType.SOLDIER.health;
            case BUILDER: return RobotType.BUILDER.health;
            default: return 0;
        }
    }
}



