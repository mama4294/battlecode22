package player;
import battlecode.common.*;

public class Comms extends Robot{



    static final int NUM_TYPES_OF_ROBOTS = 7;

    static final int IDX_ROUND_NUM = 0;

    static final int IDX_NUM_ARCHON_EVEN = 1;
    static final int IDX_NUM_MINERS_EVEN = 2;
    static final int IDX_NUM_SOLDIERS_EVEN = 3;
    static final int IDX_NUM_LABS_EVEN = 4;
    static final int IDX_NUM_WATCHTOWERS_EVEN = 5;
    static final int IDX_NUM_BUILDERS_EVEN = 6;
    static final int IDX_NUM_SAGES_EVEN = 7;

    static final int IDX_NUM_ARCHON_ODD = 8;
    static final int IDX_NUM_MINERS_ODD = 9;
    static final int IDX_NUM_SOLDIERS_ODD = 10;
    static final int IDX_NUM_LABS_ODD = 11;
    static final int IDX_NUM_WATCHTOWERS_ODD = 12;
    static final int IDX_NUM_BUILDERS_ODD = 13;
    static final int IDX_NUM_SAGES_ODD = 14;

    static final int IDX_firstArchonLoc = 15;
    static final int IDX_secondArchonLoc = 16;
    static final int IDX_thirdArchonLoc = 17;
    static final int IDX_fourthArchonLoc = 18;


    static final int IDX_ENEMY_CLUSTER_LOC_1 = 19;
    static final int IDX_ENEMY_CLUSTER_LOC_2 = 20;
    static final int IDX_ENEMY_CLUSTER_LOC_3 = 21;
    static final int IDX_ENEMY_CLUSTER_LOC_4 = 22;
    static final int IDX_ENEMY_CLUSTER_COUNT_1 = 23;
    static final int IDX_ENEMY_CLUSTER_COUNT_2 = 24;
    static final int IDX_ENEMY_CLUSTER_COUNT_3 = 25;
    static final int IDX_ENEMY_CLUSTER_COUNT_4 = 26;

    static final int IDX_ARCHON_1_STATUS = 27;
    static final int IDX_ARCHON_2_STATUS = 28;
    static final int IDX_ARCHON_3_STATUS = 29;
    static final int IDX_ARCHON_4_STATUS = 30;

    static final int IDX_ARCHON_BUILD_VALUES = 31;  //2-BITS FOR EACH ARCHON

    static final int IDX_ARCHON_12_TURNS_SINCE_BUILT = 32;  //8-BITS FOR EACH ARCHON 1 & 2

    static final int IDX_ARCHON_34_TURNS_SINCE_BUILT = 33;  //8-BITS FOR EACH ARCHON 3 & 4

    static final int IDX_PRIORITIEZED_ARCHON = 34;  //Which archon is prioritized to build first

    static final int IDX_Enemy_Archon_1_Loc = 35;
    static final int IDX_Enemy_Archon_2_Loc = 36;
    static final int IDX_Enemy_Archon_3_Loc = 37;
    static final int IDX_Enemy_Archon_4_Loc = 38;



    static RobotController rc;

    public Comms(RobotController r) {
        super(r);
    }

    public static void init(RobotController r) {
        rc = r;
    }

    static final int mapOffset = 1;


    public static int locationToInt(MapLocation loc){
        // 0 is a placeholder for unused
        if(loc == null) return  0;

        //All values have +1 offset
        return mapOffset + loc.x + loc.y * rc.getMapWidth();
    }

    public static MapLocation intToLocation(int m){
        if(m == 0) return null;
        m = m-mapOffset; //Remove the offset
        return new MapLocation(m % rc.getMapWidth(), m/rc.getMapWidth());
    }

    // The upper half of 16 bits = last count
    // The lower half of 16 bits = current count

    public static int reportAlive() throws GameActionException{

        int roundNum = rc.getRoundNum();
        int sharedArrRoundNum = rc.readSharedArray(IDX_ROUND_NUM);
        boolean roundIsEven = roundNum % 2 == 0;
        int robotNumber=0;

        //Check if you are the first robot to access the array for the round and reset if you are
        if(roundNum != sharedArrRoundNum){
            //reset all current round unit totals to 0
            for (int i= NUM_TYPES_OF_ROBOTS; --i > 0;){
                if(roundIsEven) { //even rounds
                    rc.writeSharedArray(i, 0); //reset to zero
                }else{ //odd rounds
                    rc.writeSharedArray(i+NUM_TYPES_OF_ROBOTS, 0); //reset to zero
                }
            }
            rc.writeSharedArray(IDX_ROUND_NUM, roundNum); //correct round number
        }
        
        //Find correct index in shared array for robot type
        int typeCountIndex = 0;
        RobotType type = rc.getType();
        switch(type){
            case ARCHON:
                if(roundIsEven) typeCountIndex= IDX_NUM_ARCHON_EVEN;
                else typeCountIndex= IDX_NUM_ARCHON_ODD;
                break;
            case MINER:
                if(roundIsEven) typeCountIndex= IDX_NUM_MINERS_EVEN;
                else typeCountIndex= IDX_NUM_MINERS_ODD;
                break;
            case SOLDIER:
                if(roundIsEven) typeCountIndex= IDX_NUM_SOLDIERS_EVEN;
                else typeCountIndex= IDX_NUM_SOLDIERS_ODD;
                break;
            case LABORATORY:
                if(roundIsEven) typeCountIndex= IDX_NUM_LABS_EVEN;
                else typeCountIndex= IDX_NUM_LABS_ODD;
                break;
            case WATCHTOWER:
                if(roundIsEven) typeCountIndex= IDX_NUM_WATCHTOWERS_EVEN;
                else typeCountIndex= IDX_NUM_WATCHTOWERS_ODD;
                break;
            case BUILDER:
                if(roundIsEven) typeCountIndex= IDX_NUM_BUILDERS_EVEN;
                else typeCountIndex= IDX_NUM_BUILDERS_ODD;
                break;
            case SAGE:
                if(roundIsEven) typeCountIndex= IDX_NUM_SAGES_EVEN;
                else typeCountIndex= IDX_NUM_SAGES_ODD;
                break;

        }
        if(typeCountIndex !=0 ){
            //read current number of units
            int currentCount = rc.readSharedArray(typeCountIndex);
            robotNumber = currentCount+1;
            //write updated count including you
            rc.writeSharedArray(typeCountIndex,robotNumber);

            int pastLocInt;
            int currentLocInt = locationToInt(rc.getLocation());


            //Update mapLocation if an archon
            if(type == RobotType.ARCHON){
                switch(robotNumber){
                    case 1: //first reported Archon
                        pastLocInt = rc.readSharedArray(IDX_firstArchonLoc);
                        if(currentLocInt != pastLocInt) rc.writeSharedArray(IDX_firstArchonLoc,currentLocInt);
                    case 2: //second reported Archon
                        pastLocInt = rc.readSharedArray(IDX_secondArchonLoc);
                        if(currentLocInt != pastLocInt) rc.writeSharedArray(IDX_secondArchonLoc,currentLocInt);
                    case 3: //third reported Archon
                        pastLocInt = rc.readSharedArray(IDX_thirdArchonLoc);
                        if(currentLocInt != pastLocInt) rc.writeSharedArray(IDX_thirdArchonLoc,currentLocInt);
                    case 4: //fourth reported Archon
                        pastLocInt = rc.readSharedArray(IDX_fourthArchonLoc);
                        if(currentLocInt != pastLocInt) rc.writeSharedArray(IDX_fourthArchonLoc,currentLocInt);
                }
            }
            }
        return robotNumber;
        }


    public static MapLocation[] getAllyArchonLocations() throws GameActionException{

        MapLocation a1 = intToLocation(rc.readSharedArray(IDX_firstArchonLoc));
        MapLocation a2 = intToLocation(rc.readSharedArray(IDX_secondArchonLoc));
        MapLocation a3 = intToLocation(rc.readSharedArray(IDX_thirdArchonLoc));
        MapLocation a4 = intToLocation(rc.readSharedArray(IDX_fourthArchonLoc));

       MapLocation[] locations = {a1, a2, a3, a4} ;
       return locations;

       //todo what if the archon is dead?
    }

    //Writes the archon's status to the shared array

    public static void reportArchonStatus(int archonNumber, int status) throws GameActionException{
        switch(archonNumber){
            case 1:
                rc.writeSharedArray(IDX_ARCHON_1_STATUS, status);
                break;
            case 2:
                rc.writeSharedArray(IDX_ARCHON_2_STATUS, status);
                break;
            case 3:
                rc.writeSharedArray(IDX_ARCHON_3_STATUS, status);
                break;
            case 4:
                rc.writeSharedArray(IDX_ARCHON_4_STATUS, status);
                break;
        }
    }


    public static MapLocation[] getEnemyArchonLocations() throws GameActionException{

        MapLocation a1 = intToLocation(rc.readSharedArray(IDX_Enemy_Archon_1_Loc));
        MapLocation a2 = intToLocation(rc.readSharedArray(IDX_Enemy_Archon_2_Loc));
        MapLocation a3 = intToLocation(rc.readSharedArray(IDX_Enemy_Archon_3_Loc));
        MapLocation a4 = intToLocation(rc.readSharedArray(IDX_Enemy_Archon_4_Loc));

        MapLocation[] locations = {a1, a2, a3, a4} ;
        return locations;
    }





    public static void resetTurnsSinceBuild (int archonNumber) throws GameActionException{
        //reads the current message of (4) 2-bit values encoded in an integer
        //Clears the archon number's bits to 0 0
        //Combines a new "buildInt" integer with the cleared messaged with an or operator

        int index = IDX_ARCHON_12_TURNS_SINCE_BUILT ;
        int offset = 0;

        switch (archonNumber){
            case 1:
                index = IDX_ARCHON_12_TURNS_SINCE_BUILT;
                offset =  0;
                break;
            case 2:
                index = IDX_ARCHON_12_TURNS_SINCE_BUILT;
                offset =  8;
                break;
            case 3:
                index = IDX_ARCHON_34_TURNS_SINCE_BUILT;
                offset =  0;
                break;
            case 4:
                index = IDX_ARCHON_34_TURNS_SINCE_BUILT;
                offset =  8;
                break;
        }

        int currentMessage = rc.readSharedArray(index);
        int clearedFlag = (~(0xff << offset)) & currentMessage; //clears the 8-bit message at the offset location
        rc.setIndicatorString(Integer.toString(0));
        rc.writeSharedArray(index, clearedFlag );
    }

    public static void incrementTurnsSinceBuild (int archonNumber) throws GameActionException{
        //reads the current message of (4) 2-bit values encoded in an integer
        //Clears the archon number's bits to 0 0
        //Combines a new "buildInt" integer with the cleared messaged with an or operator

        int index = IDX_ARCHON_12_TURNS_SINCE_BUILT ;
        int offset = 0;

        switch (archonNumber){
            case 1:
                index = IDX_ARCHON_12_TURNS_SINCE_BUILT;
                offset =  0;
                break;
            case 2:
                index = IDX_ARCHON_12_TURNS_SINCE_BUILT;
                offset =  8;
                break;
            case 3:
                index = IDX_ARCHON_34_TURNS_SINCE_BUILT;
                offset =  0;
                break;
            case 4:
                index = IDX_ARCHON_34_TURNS_SINCE_BUILT;
                offset =  8;
                break;
        }


        int currentMessage = rc.readSharedArray(index);
        int currentTurns = 0xff & (currentMessage >> offset);
        int incrementedTurns = currentTurns + 1;
            if(incrementedTurns > 255) incrementedTurns = 255;
        int clearedMessage = (~(0xff << offset)) & currentMessage; //clears the 8-bit message at the offset location
        int newMessage = clearedMessage | (incrementedTurns << offset);

        rc.setIndicatorString("Archon Number:" + Integer.toString(archonNumber) + " | " + Integer.toString(incrementedTurns));
        rc.writeSharedArray(index, newMessage);
    }

    public static int getTurnsSinceLastBuild (int archonNumber) throws GameActionException{
        //reads the current message of (4) 2-bit values encoded in an integer
        //Clears the archon number's bits to 0 0
        //Combines a new "buildInt" integer with the cleared messaged with an or operator

        int index = IDX_ARCHON_12_TURNS_SINCE_BUILT ;
        int offset = 0;

        switch (archonNumber){
            case 1:
                index = IDX_ARCHON_12_TURNS_SINCE_BUILT;
                offset =  0;
                break;
            case 2:
                index = IDX_ARCHON_12_TURNS_SINCE_BUILT;
                offset =  8;
                break;
            case 3:
                index = IDX_ARCHON_34_TURNS_SINCE_BUILT;
                offset =  0;
                break;
            case 4:
                index = IDX_ARCHON_34_TURNS_SINCE_BUILT;
                offset =  8;
                break;
        }


        int currentMessage = rc.readSharedArray(index);
        return 0xff & (currentMessage >> offset);
    }

    public static int getMaxTurnsSinceLastBuild () throws GameActionException{
        //reads the current message of (4) 2-bit values encoded in an integer
        //Clears the archon number's bits to 0 0
        //Combines a new "buildInt" integer with the cleared messaged with an or operator

        int currentMessage1 = rc.readSharedArray(IDX_ARCHON_12_TURNS_SINCE_BUILT);
        int value1 =  0xff & (currentMessage1);
        int value2 =  0xff & (currentMessage1 >> 8);

        int currentMessage2 = rc.readSharedArray(IDX_ARCHON_34_TURNS_SINCE_BUILT);
        int value3 =  0xff & (currentMessage2);
        int value4 =  0xff & (currentMessage2 >> 8);

        return Math.max(Math.max(Math.max(value1, value2), value3), value4);
    }



    public static void setNextRoundToBuildValue (Archon.buildOption toBuild, int archonNumber) throws GameActionException{
        //reads the current message of (4) 2-bit values encoded in an integer
        //Clears the archon number's bits to 0 0
        //Combines a new "buildInt" integer with the cleared messaged with an or operator

        int buildInt = toBuild.ordinal(); //gets the integer of the enum
        int currentMessage = rc.readSharedArray(IDX_ARCHON_BUILD_VALUES);
        int offset =  2*(archonNumber - 1);
        int clearedFlag = (~(3 << offset)) & currentMessage; //clears the 2-bit message at the offset location

        rc.writeSharedArray(IDX_ARCHON_BUILD_VALUES, clearedFlag | (buildInt << offset));
    }

    public static Archon.buildOption getNextRoundToBuildValue(int archonNum) throws GameActionException {
        //reads the current message of (4) 2-bit values encoded in an integer and returns the robot type associated with the archon number
        int message = rc.readSharedArray(IDX_ARCHON_BUILD_VALUES);
        int offset =  2*(archonNum - 1);
        int value = 3 & (message >> offset);
        return Archon.buildOption.values()[value]; // extract the last 2-bit value and find it in the array of buildOptions
    }









    public static int getUnitCount(RobotType type) throws GameActionException{
        boolean roundIsEven = rc.getRoundNum() % 2 == 0;
        int typeCountIndex = 0;
        switch(type){
            case ARCHON:
                if(!roundIsEven) typeCountIndex= IDX_NUM_ARCHON_EVEN;
                else typeCountIndex= IDX_NUM_ARCHON_ODD;
                break;
            case MINER:
                if(!roundIsEven) typeCountIndex= IDX_NUM_MINERS_EVEN;
                else typeCountIndex= IDX_NUM_MINERS_ODD;
                break;
            case SOLDIER:
                if(!roundIsEven) typeCountIndex= IDX_NUM_SOLDIERS_EVEN;
                else typeCountIndex= IDX_NUM_SOLDIERS_ODD;
                break;
            case LABORATORY:
                if(!roundIsEven) typeCountIndex= IDX_NUM_LABS_EVEN;
                else typeCountIndex= IDX_NUM_LABS_ODD;
                break;
            case WATCHTOWER:
                if(!roundIsEven) typeCountIndex= IDX_NUM_WATCHTOWERS_EVEN;
                else typeCountIndex= IDX_NUM_WATCHTOWERS_ODD;
                break;
            case BUILDER:
                if(!roundIsEven) typeCountIndex= IDX_NUM_BUILDERS_EVEN;
                else typeCountIndex= IDX_NUM_BUILDERS_ODD;
                break;
            case SAGE:
                if(!roundIsEven) typeCountIndex= IDX_NUM_SAGES_EVEN;
                else typeCountIndex= IDX_NUM_SAGES_ODD;
                break;

        }

        if(typeCountIndex !=0 ){
            return rc.readSharedArray(typeCountIndex);
        }
        
        return 0;
    }




    public static void getEnemyClusterLocations () throws GameActionException{
        MapLocation clusterLoc1 = intToLocation(rc.readSharedArray(IDX_ENEMY_CLUSTER_LOC_1));
        MapLocation clusterLoc2 = intToLocation(rc.readSharedArray(IDX_ENEMY_CLUSTER_LOC_2));
        MapLocation clusterLoc3 = intToLocation(rc.readSharedArray(IDX_ENEMY_CLUSTER_LOC_3));
        MapLocation clusterLoc4 = intToLocation(rc.readSharedArray(IDX_ENEMY_CLUSTER_LOC_4));



        if(clusterLoc1!= null) Debug.drawEnemyClusterDot(clusterLoc1);
        if(clusterLoc2!= null) Debug.drawEnemyClusterDot(clusterLoc2);
        if(clusterLoc3!= null) Debug.drawEnemyClusterDot(clusterLoc3);
        if(clusterLoc4!= null) Debug.drawEnemyClusterDot(clusterLoc4);

        MapLocation[] locations = {clusterLoc1, clusterLoc2, clusterLoc3, clusterLoc4};
        enemyClusterLocations = locations;
    }

    public static void getEnemyClusterCounts () throws GameActionException{
        int clusterCount1 = rc.readSharedArray(IDX_ENEMY_CLUSTER_COUNT_1);
        int clusterCount2 = rc.readSharedArray(IDX_ENEMY_CLUSTER_COUNT_2);
        int clusterCount3 = rc.readSharedArray(IDX_ENEMY_CLUSTER_COUNT_3);
        int clusterCount4 = rc.readSharedArray(IDX_ENEMY_CLUSTER_COUNT_4);

        int[] counts = {clusterCount1, clusterCount2, clusterCount3, clusterCount4};
        enemyClusterCounts = counts;
    }

    public static void reportEnemyCluster (RobotInfo[] nearbyEnemies) throws GameActionException{
        //Sum all the x and y locations of each nearby enemy
        int sumX = 0;
        int sumY = 0;
        int numEnemies = nearbyEnemies.length;
        MapLocation currLoc = rc.getLocation();


        if(numEnemies > 0){
            for(int i = 0; i< numEnemies; i++){
                RobotInfo enemy = nearbyEnemies[i];
                sumX = sumX + enemy.location.x;
                sumY = sumY + enemy.location.y;
            }
        }


        // Check if you can sense any of the enemy cluster locations. If there are fewer enemies, reduce the cluster count.
        for(int i = 0; i< 4; i++){
            if(enemyClusterLocations[i] != null && rc.canSenseLocation(enemyClusterLocations[i])){
                int reportedEnemies = enemyClusterCounts[i];
                int newCount = reportedEnemies;

                //reduce count if actual enemies are fewer than reported enemies
                if(numEnemies < reportedEnemies){
                    newCount = reportedEnemies - 1;
                }

                //remove cluster if enemies are less than 0.
                if (newCount < 0){
                    newCount = 0;
                    rc.writeSharedArray(IDX_ENEMY_CLUSTER_COUNT_1 + i, locationToInt(null));
                }
                //Report count
                if(numEnemies != reportedEnemies) rc.writeSharedArray(IDX_ENEMY_CLUSTER_COUNT_1 + i, newCount);

            }
        }


        //Report if there is a grouping of enemies with a larger count than any of the clusters
        if(numEnemies > 0 ){
            //Get the average location of all the enemies
            int aveX = (int)sumX/numEnemies;
            int aveY = (int)sumY/numEnemies;
            MapLocation averageLoc = new MapLocation(aveX, aveY);

            //Don't add the new map location if it is already in the array.
            for (int i=0; i<4; i++){
                if(enemyClusterLocations[i]!=null && enemyClusterLocations[i].equals(averageLoc)){
                    if(numEnemies > enemyClusterCounts[i]) rc.writeSharedArray(IDX_ENEMY_CLUSTER_COUNT_1 + i, numEnemies);
                    return;
                }
            }

            //see if there are more enemies in your view than any of the clusters
            for (int i=0; i<4; i++){
                if(numEnemies > enemyClusterCounts[i]){
                    //if yes, change the array to your cluster count
                    rc.writeSharedArray(IDX_ENEMY_CLUSTER_COUNT_1 + i, numEnemies);
                    rc.writeSharedArray(IDX_ENEMY_CLUSTER_LOC_1+i, locationToInt(averageLoc));
                    break;
                }
            }
        }
    }



}



