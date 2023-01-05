package player;
import java.util.Arrays;
import battlecode.common.*;

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


