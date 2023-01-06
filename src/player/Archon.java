package player;
import battlecode.common.*;

import java.util.Map;
import java.util.Random;

public class Archon extends Robot{


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

    static final int MAX_RUBBLE = 30;

    static boolean transformedToMove = false;

    static MapLocation targetLoc = null;

    static buildOption toBuildThisRound;
    static buildOption toBuildNextRound;

    enum State {
        Chilling,
        UnderAttack,

        ClosestToEnemy,
    }

    static State state;

    static enum buildOption {
        NONE,
        SOLDER,
        MINER,

        BUILDER,

        SAGE,

    }




    public void takeTurn() throws GameActionException {
        super.takeTurn();
        updateUnitCounts();
        if(rc.getRoundNum() == 1) useSymetryToGuessEnemyArchon();
        determineState();
        tryMoveToBetterLoc();
        determineWhatToBuild();
        determineWhatToBuildNextRound();
        turnsSinceLastBuild = Comms.getTurnsSinceLastBuild(robotNumber);
        maxTurnsSinceLastBuild =Comms.getMaxTurnsSinceLastBuild();
        if(!transformedToMove){
            tryBuildUnit();
            tryHealRobot();
        }

    }

    private void determineState() throws GameActionException{
        state = State.Chilling;

        if(checkIfClosestToEnemy()){
            state = State.ClosestToEnemy;
            rc.setIndicatorDot(currentLocation.add(Direction.WEST), 255,0,255);
            rc.setIndicatorDot(currentLocation.add(Direction.NORTH), 255,0,255);
            rc.setIndicatorDot(currentLocation.add(Direction.SOUTH), 255,0,255);
            rc.setIndicatorDot(currentLocation.add(Direction.EAST), 255,0,255);
        }

        if(checkUnderAttack()){
            state = State.UnderAttack;
            rc.setIndicatorDot(currentLocation.add(Direction.WEST), 233,150,122);
            rc.setIndicatorDot(currentLocation.add(Direction.NORTH), 233,150,122);
            rc.setIndicatorDot(currentLocation.add(Direction.SOUTH), 233,150,122);
            rc.setIndicatorDot(currentLocation.add(Direction.EAST), 233,150,122);
        }

        Comms.reportArchonStatus(robotNumber, state.ordinal());

    }



    private MapLocation getAverageEnemyLocation() throws GameActionException {
        if(enemyClusterLocations == null) return null;

        int sumX = 0;
        int sumY = 0;
        int sumWeight = 0;

        for(int i=0; i<enemyClusterLocations.length; i++){
            if(enemyClusterLocations[i] != null){
                int weight = enemyClusterCounts[i];
                sumX = sumX + enemyClusterLocations[i].x * weight;
                sumY = sumY + enemyClusterLocations[i].y * weight;
                sumWeight = sumWeight + weight;
            }
        }

        return new MapLocation(sumX/sumWeight, sumY/sumWeight);
    }

    private boolean checkUnderAttack() throws GameActionException {
        int numEnemyCombatants = 0;
        int numAllyCombatants = 0;

        for(int i=0; i< nearbyAllies.length; i++){
            if(isACombatant(nearbyAllies[i].type)){
                numAllyCombatants++;
            }
        }

        for(int i=0; i< nearbyEnemies.length; i++){
            if(isACombatant(nearbyEnemies[i].type)){
                numEnemyCombatants++;
            }
        }

        return numEnemyCombatants > numAllyCombatants;
    }




    private boolean checkIfClosestToEnemy() throws GameActionException {
        MapLocation averageEnemyLoc = getAverageEnemyLocation();
        if(averageEnemyLoc == null) return false;
        rc.setIndicatorDot(averageEnemyLoc, 255, 0, 255);
        MapLocation[] allyArchonLocations = Comms.getAllyArchonLocations();

        int nearestDist = Integer.MAX_VALUE;
        MapLocation nearestLoc = null;


        for(int i=0; i< allyArchonLocations.length; i++){
            int dist = averageEnemyLoc.distanceSquaredTo(allyArchonLocations[i]);
            if(dist < nearestDist){
                nearestDist = dist;
                nearestLoc = allyArchonLocations[i];
            }
        }

        return nearestLoc == currentLocation;
    }

    public boolean isACombatant(RobotType type) throws  GameActionException{
        switch (type){
            case SAGE: return true;
            case SOLDIER: return true;
            case WATCHTOWER: return true;
        }
        return false;
    }

    public void useSymetryToGuessEnemyArchon() throws GameActionException{

        //try to use map symmetry to determine the enemy archon locations
        MapLocation guessLoc = new MapLocation(rc.getMapWidth() - currentLocation.x -1, rc.getMapHeight() - currentLocation.y-1);

        //set archon location guess
        rc.writeSharedArray(Comms.IDX_Enemy_Archon_1_Loc + robotNumber -1, Comms.locationToInt(guessLoc));
        if (debug) rc.setIndicatorDot(guessLoc, 255, 255, 255);
    }

    public void determineWhatToBuild() throws GameActionException{
            //Get from shared array
        if(state == State.UnderAttack){
            toBuildThisRound = buildOption.SOLDER;
        }else{
            toBuildThisRound = Comms.getNextRoundToBuildValue(robotNumber);
        }

        //if we are the closest to the enemy, build solders
        if(checkIfClosestToEnemy() && toBuildThisRound == buildOption.MINER){
            toBuildThisRound = buildOption.SOLDER;
        }
    }

    public void getTurnsSinceLastBuild() throws GameActionException{
        //Get from shared array
        toBuildThisRound = Comms.getNextRoundToBuildValue(robotNumber);
    }


    public void determineWhatToBuildNextRound() throws GameActionException{
        //Write to shared array what will be built next round
        buildOption toBuildNextRound = buildOption.NONE;

        buildOption[] otherArchonPlans = Comms.getOtherArchonsToBuilds(robotNumber);

        //determine if other archonplans contains BUILDER
        boolean otherArchonMakingABuilder = false;
        for(int i=0; i<otherArchonPlans.length; i++){
            if(otherArchonPlans[i] == buildOption.BUILDER){
                otherArchonMakingABuilder = true;
            }
        }

        //random number between 0 and 100
        int randomNum = rng.nextInt(100);

        if(rc.isActionReady()){
            if((randomNum < 25 && minerCount < MAX_NUM_MINERS) || rc.getRoundNum() < 10){
                toBuildNextRound = buildOption.MINER;
            }else if(rc.getTeamGoldAmount(rc.getTeam()) >= RobotType.SAGE.buildCostGold){
                toBuildNextRound = buildOption.SAGE;
            }else if(builderCount < 1 && !checkIfClosestToEnemy() && state != State.UnderAttack && !otherArchonMakingABuilder){
                toBuildNextRound = buildOption.BUILDER;
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


        if(!rc.isActionReady() || toBuildThisRound == buildOption.NONE || (turnsSinceLastBuild < maxTurnsSinceLastBuild & state != State.UnderAttack)){
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
            case SAGE:
                toBuild = RobotType.SAGE;
                break;
            case BUILDER:
                toBuild = RobotType.BUILDER;
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

    public void tryMoveToBetterLoc() throws GameActionException{
        //Move if current location has rubble more than max rubble constant
        if(rc.senseRubble(currentLocation) > MAX_RUBBLE) {

            //transform to a mobile unit
            if (!transformedToMove && rc.isTransformReady()) {
                rc.transform();
                transformedToMove = true;
            }
        }

        if(transformedToMove){
            //Don't increment turns since last build if in mobile mode
            Comms.resetTurnsSinceBuild(robotNumber);

            //transform back into a stationary unit
            if(rc.senseRubble(currentLocation) <= MAX_RUBBLE && rc.isTransformReady() ){
                rc.transform();
                transformedToMove = false;
            }

            //Find a better target location to move
            if(targetLoc==null) targetLoc = findBestRubbleLoc();


            //if at location, reset target loc
            if(targetLoc!=null && currentLocation.distanceSquaredTo(targetLoc) < 1){
                targetLoc = null;
            }

            //Move to target loc
            if(targetLoc != null) goTo(targetLoc);
        }

    }


    public MapLocation findBestRubbleLoc() throws GameActionException{
        MapLocation bestLoc = null;
        MapLocation locToCheck = null;
        int lowestRubble = Integer.MAX_VALUE;

        for(int x = -2; x <= 2; x++){
            for(int y = -2; y <= 2; y++){
                locToCheck = rc.getLocation().translate(x,y);
                if(rc.canSenseLocation(locToCheck) && rc.senseRubble(locToCheck) < lowestRubble){
                    lowestRubble = rc.senseRubble(locToCheck);
                    bestLoc = locToCheck;
                    if(lowestRubble == 0) break;
                }
            }
        }
        return bestLoc;
    }

}



