package playerV2;
import battlecode.common.*;
import java.util.Random;
public class Archon extends Robot {


    static RobotInfo lastRobotHealed;
    static RobotInfo robotToHeal;
    public Archon(RobotController r) {
        super(r);
    }

    static int archonCount;
    static int minerCount;
    static int soldierCount;
    static int labCount;
    static int watchtowerCount;

    static int builderCount;
    static int sageCount;

    static enum State {
        CHILLING,
        OBESITY,
        UNDER_ATTACK,
        INIT,
        MOVING,
        FINDING_GOOD_SPOT,
        BUILDING_LAB,
    };


    public void takeTurn() throws GameActionException {
        super.takeTurn();
        updateUnitCounts();
        tryBuildUnit();
        tryHealRobot();
    }


    public void tryBuildUnit() throws GameActionException{
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rng.nextBoolean()) {
            // Let's try to build a miner.
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                rc.buildRobot(RobotType.MINER, dir);
            }
        }else {
            // Let's try to build a soldier.
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
            }
        }
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



