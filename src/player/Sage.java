package player;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Sage extends Unit {

    public Sage(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        tryDestroyArchon();
        tryAttack();
        goToClosestEnemyCluster();
        goToClosetEnemyArchon();
        explore();
        spreadOut(RobotType.SAGE);
    }
}
