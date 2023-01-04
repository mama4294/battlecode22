package playerV2;
import battlecode.common.*;

public class Comms extends Robot {



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

    static RobotController rc;

    public Comms(RobotController r) {
        super(r);
    }

    public static void init(RobotController r) {
        rc = r;
    }

    static final int mapOffset = 1;


    private static int locationToInt(MapLocation loc){
        // 0 is a placeholder for unused
        if(loc == null) return  0;

        //All values have +1 offset
        return mapOffset + loc.x + loc.y * rc.getMapWidth();
    }

    private static MapLocation intToLocation(int m){
        if(m == 0) return null;
        m = m-mapOffset; //Remove the offset
        return new MapLocation(m % rc.getMapWidth(), m/rc.getMapWidth());
    }

    // The upper half of 16 bits = last count
    // The lower half of 16 bits = current count




    public static void reportAlive() throws GameActionException{

        int roundNum = rc.getRoundNum();
        int sharedArrRoundNum = rc.readSharedArray(IDX_ROUND_NUM);
        boolean roundIsEven = roundNum % 2 == 0;

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
            int newCount = currentCount+1;
            //write updated count including you
            rc.writeSharedArray(typeCountIndex,newCount);

            int pastLocInt;
            int currentLocInt = locationToInt(rc.getLocation());


            //Update mapLocation if an archon
            if(type == RobotType.ARCHON){
                switch(newCount){
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



