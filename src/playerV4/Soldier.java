package playerV4;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Soldier extends Unit {

    public Soldier(RobotController r) {
        super(r);
    }



    public void takeTurn() throws GameActionException {
        super.takeTurn();
        tryDestroyArchon();
        tryAttack();
        goToClosestEnemyCluster();
        goToClosetEnemyArchon();
        explore();
        spreadOut(RobotType.SOLDIER);
    }

















}


