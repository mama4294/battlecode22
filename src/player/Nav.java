package player;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;


import java.util.Random;

public class Nav {
    static RobotController rc;

    public static void init(RobotController r) {
        rc = r;
    }

    //l: location
    //r: rubble score
    //s: score. Lower is better
    //d: direction to move

    static MapLocation l00;


    static MapLocation l10;
    static double r10;
    static double s10;
    static Direction d10;

    static MapLocation l01;
    static double r01;
    static double s01;
    static Direction d01;

    static MapLocation l_10;
    static double r_10;
    static double s_10;
    static Direction d_10;

    static MapLocation l0_1;
    static double r0_1;
    static double s0_1;
    static Direction d0_1;

    static MapLocation l11;
    static double r11;
    static double s11;
    static Direction d11;

    static MapLocation l_11;
    static double r_11;
    static double s_11;
    static Direction d_11;

    static MapLocation l_1_1;
    static double r_1_1;
    static double s_1_1;
    static Direction d_1_1;

    static MapLocation l1_1;
    static double r1_1;
    static double s1_1;
    static Direction d1_1;

    static MapLocation l20;
    static double r20;
    static double s20;
    static Direction d20;

    static MapLocation l02;
    static double r02;
    static double s02;
    static Direction d02;

    static MapLocation l_20;
    static double r_20;
    static double s_20;
    static Direction d_20;

    static MapLocation l0_2;
    static double r0_2;
    static double s0_2;
    static Direction d0_2;

    static MapLocation l21;
    static double r21;
    static double s21;
    static Direction d21;

    static MapLocation l12;
    static double r12;
    static double s12;
    static Direction d12;

    static MapLocation l_12;
    static double r_12;
    static double s_12;
    static Direction d_12;

    static MapLocation l_21;
    static double r_21;
    static double s_21;
    static Direction d_21;

    static MapLocation l_2_1;
    static double r_2_1;
    static double s_2_1;
    static Direction d_2_1;

    static MapLocation l_1_2;
    static double r_1_2;
    static double s_1_2;
    static Direction d_1_2;

    static MapLocation l1_2;
    static double r1_2;
    static double s1_2;
    static Direction d1_2;

    static MapLocation l2_1;
    static double r2_1;
    static double s2_1;
    static Direction d2_1;


    public static MapLocation findClosestLocFromArray(MapLocation[] targets){
        int closestDist = Integer.MAX_VALUE;
        MapLocation closestLoc = null;
        int dist;
        for(int i=0; i<targets.length ; i++){
            dist = rc.getLocation().distanceSquaredTo(targets[i]);
            if(dist < closestDist) {
                closestDist = dist;
                closestLoc = targets[i];
            }
        }
        return closestLoc;
    }

    //fuzzy nav returns a direction to move in


    public static Direction getDirFromBFS(MapLocation target){

        //Current Location
        l00= rc.getLocation();

        if (l00 == null) return null;
        int dx = target.x - l00.x;
        int dy = target.y - l00.y;


        //Adjecent Locations
        l10 = l00.add(Direction.EAST);
        l01 = l00.add(Direction.NORTH);
        l_10 = l00.add(Direction.WEST);
        l0_1 = l00.add(Direction.SOUTH);
        l11 = l00.add(Direction.NORTHEAST);
        l_11 = l00.add(Direction.NORTHWEST);
        l_1_1 = l00.add(Direction.SOUTHWEST);
        l1_1 = l00.add(Direction.SOUTHEAST);
        
        s10 = 100000;
        s01 = 100000;
        s_10 = 100000;
        s0_1 = 100000;
        s11 = 100000;
        s_11 = 100000;
        s_1_1 = 100000;
        s1_1 = 100000;

        d10 = null;
        d01 = null;
        d_10 = null;
        d0_1 = null;
        d11 = null;
        d_11 = null;
        d_1_1 = null;
        d1_1 = null;


        //First Ring
        l20 = l10.add(Direction.EAST);
        l02 = l01.add(Direction.NORTH);
        l_20 = l_10.add(Direction.WEST);
        l0_2 = l0_1.add(Direction.SOUTH);

        l21 = l11.add(Direction.EAST);
        l12 = l11.add(Direction.NORTH);
        l_12 = l_11.add(Direction.NORTH);
        l_21 = l_11.add(Direction.WEST);
        l_2_1 = l_1_1.add(Direction.WEST);
        l_1_2 = l_1_1.add(Direction.SOUTH);
        l1_2 = l1_1.add(Direction.SOUTH);
        l2_1 = l1_1.add(Direction.EAST);

        l20 = l10.add(Direction.EAST);
        l02 = l01.add(Direction.NORTH);
        l_20 = l_10.add(Direction.WEST);
        l0_2 = l0_1.add(Direction.SOUTH);

        s21 = 100000;
        s12 = 100000;
        s_12 = 100000;
        s_21 = 100000;
        s_2_1 = 100000;
        s_1_2 = 100000;
        s1_2 = 100000;
        s2_1 = 100000;
        s20 = 100000;
        s02 = 100000;
        s_20 = 100000;
        s0_2 = 100000;

        d21 = null;
        d12 = null;
        d_12 = null;
        d_21 = null;
        d_2_1 = null;
        d_1_2 = null;
        d1_2 = null;
        d2_1 = null;
        d20 = null;
        d02 = null;
        d_20 = null;
        d0_2 = null;

        try{
            //Get scores for cardinal directions

            if (rc.canMove(l00.directionTo(l10))) {  //Check East
                r10 = 1.0 + rc.senseRubble(l10) / 10.0; //Rubble score
                s10 = r10;                              //Location score. Same as rubble score for adjacent locations
                d10 = l00.directionTo(l10);                    //Direction to move to get here

            }
            if (rc.canMove(l00.directionTo(l01))) {  //Check North
                r01 = 1.0 + rc.senseRubble(l01) / 10.0;
                s01 = r01;
                d01 = l00.directionTo(l01);
            }
            if (rc.canMove(l00.directionTo(l_10))) {  //Check West
                r_10 = 1.0 + rc.senseRubble(l_10) / 10.0;
                s_10 = r_10;
                d_10 = l00.directionTo(l_10);
            }
            if (rc.canMove(l00.directionTo(l0_1))) {  //Check South
                r0_1 = 1.0 + rc.senseRubble(l0_1) / 10.0;
                s0_1 = r0_1;
                d0_1 = l00.directionTo(l0_1);
            }
            if (rc.canMove(l00.directionTo(l11))) {  //Check North East
                r11 = 1.0 + rc.senseRubble(l11) / 10.0;
                s11 = r11;
                d11 = l00.directionTo(l11);
            }
            if (rc.canMove(l00.directionTo(l_11))) {  //Check North West
                r_11 = 1.0 + rc.senseRubble(l_11) / 10.0;
                s_11 = r_11;
                d_11 = l00.directionTo(l_11);
            }
            if (rc.canMove(l00.directionTo(l_1_1))) {  //Check South West
                r_1_1 = 1.0 + rc.senseRubble(l_1_1) / 10.0;
                s_1_1 = r_1_1;
                d_1_1 = l00.directionTo(l_1_1);
            }
            if (rc.canMove(l00.directionTo(l1_1))) {  //Check South East
                r1_1 = 1.0 + rc.senseRubble(l1_1) / 10.0;
                s1_1 = r_1_1;
                d1_1 = l00.directionTo(l1_1);
            }



            //Check first ring
            if (rc.canSenseLocation(l20)) {  //East-East
                r20 = 1.0 + rc.senseRubble(l20) / 10.0;
                if (s20 > s11 + r20) {  //Check each of the pathways to get to this location
                    s20 = s11 + r20;
                    d20 = d11;
                }
                if (s20 > s10 + r20) {
                    s20 = s10 + r20;
                    d20 = d10;
                }
                if (s20 > s1_1 + r20) {
                    s20 = s1_1 + r20;
                    d20 = d1_1;
                }
            }

            if (rc.canSenseLocation(l02)) {  // North-North
                r02 = 1.0 + rc.senseRubble(l02) / 10.0;
                if (s02 > s_11 + r02) {  //Check each of the pathways to get to this location
                    s02 = s_11 + r02;
                    d02 = d_11;
                }
                if (s02 > s01 + r02) {
                    s02 = s01 + r02;
                    d02 = d01;
                }
                if (s02 > s11 + r02) {
                    s02 = s11 + r02;
                    d02 = d11;
                }
            }

            if (rc.canSenseLocation(l_20)) {  // West-West
                r_20 = 1.0 + rc.senseRubble(l_20) / 10.0;
                if (s_20 > s_11 + r_20) {  //Check each of the pathways to get to this location
                    s_20 = s_11 + r_20;
                    d_20 = d_11;
                }
                if (s_20 > s_10 + r_20) {
                    s_20 = s_10 + r_20;
                    d_20 = d_10;
                }
                if (s_20 > s_1_1 + r_20) {
                    s_20 = s_1_1 + r_20;
                    d_20 = d_1_1;
                }
            }

            if (rc.canSenseLocation(l0_2)) {  // South-South
                r0_2 = 1.0 + rc.senseRubble(l0_2) / 10.0;
                if (s0_2 > s_1_1 + r0_2) {
                    s0_2 = s_1_1 + r0_2;
                    d0_2 = d_1_1;
                }
                if (s0_2 > s0_1 + r0_2) {
                    s0_2 = s0_1 + r0_2;
                    d0_2 = d0_1;
                }
                if (s0_2 > s1_1 + r0_2) {
                    s0_2 = s1_1 + r0_2;
                    d0_2 = d1_1;
                }
            }

            if (rc.canSenseLocation(l21)) {  
                r21 = 1.0 + rc.senseRubble(l21) / 10.0;
                if (s21 > s11 + r21) {
                    s21 = s11 + r21;
                    d21 = d11;
                }
                if (s21 > s10 + r21) {
                    s21 = s10 + r21;
                    d21 = d10;
                }
                if (s21 > s20 + r21) {
                    s21 = s20 + r21;
                    d21 = d20;
                }
            }

            if (rc.canSenseLocation(l12)) {
                r12 = 1.0 + rc.senseRubble(l12) / 10.0;
                if (s12 > s11 + r12) {
                    s12 = s11 + r12;
                    d12 = d11;
                }
                if (s12 > s01 + r12) {
                    s12 = s01 + r12;
                    d12 = d01;
                }
                if (s12 > s02 + r12) {
                    s12 = s02 + r12;
                    d12 = d02;
                }
            }

            if (rc.canSenseLocation(l_12)) {
                r_12 = 1.0 + rc.senseRubble(l_12) / 10.0;
                if (s_12 > s_11 + r_12) {
                    s_12 = s_11 + r_12;
                    d_12 = d_11;
                }
                if (s_12 > s01 + r_12) {
                    s_12 = s01 + r_12;
                    d_12 = d01;
                }
                if (s_12 > s02 + r_12) {
                    s_12 = s02 + r_12;
                    d_12 = d02;
                }
            }

            if (rc.canSenseLocation(l_21)) {
                r_21 = 1.0 + rc.senseRubble(l_21) / 10.0;
                if (s_21 > s_11 + r_21) {
                    s_21 = s_11 + r_21;
                    d_21 = d_11;
                }
                if (s_21 > s_10 + r_21) {
                    s_21 = s_10 + r_21;
                    d_21 = d_10;
                }
                if (s_21 > s_20 + r_21) {
                    s_21 = s_20 + r_21;
                    d_21 = d_20;
                }
            }

            if (rc.canSenseLocation(l_2_1)) {
                r_2_1 = 1.0 + rc.senseRubble(l_2_1) / 10.0;
                if (s_2_1 > s_1_1 + r_2_1) {
                    s_2_1 = s_1_1 + r_2_1;
                    d_2_1 = d_1_1;
                }
                if (s_2_1 > s_10 + r_2_1) {
                    s_2_1 = s_10 + r_2_1;
                    d_2_1 = d_10;
                }
                if (s_2_1 > s_20 + r_2_1) {
                    s_2_1 = s_20 + r_2_1;
                    d_2_1 = d_20;
                }
            }

            if (rc.canSenseLocation(l_1_2)) {
                r_1_2 = 1.0 + rc.senseRubble(l_1_2) / 10.0;
                if (s_1_2 > s_1_1 + r_1_2) {
                    s_1_2 = s_1_1 + r_1_2;
                    d_1_2 = d_1_1;
                }
                if (s_1_2 > s0_1 + r_1_2) {
                    s_1_2 = s0_1 + r_1_2;
                    d_1_2 = d0_1;
                }
                if (s_1_2 > s_2_1 + r_1_2) {
                    s_1_2 = s_2_1 + r_1_2;
                    d_1_2 = d_2_1;
                }
            }

            if (rc.canSenseLocation(l1_2)) {
                r1_2 = 1.0 + rc.senseRubble(l1_2) / 10.0;
                if (s1_2 > s1_1 + r1_2) {
                    s1_2 = s1_1 + r1_2;
                    d1_2 = d1_1;
                }
                if (s1_2 > s0_1 + r1_2) {
                    s1_2 = s0_1 + r1_2;
                    d1_2 = d0_1;
                }
                if (s1_2 > s0_2 + r1_2) {
                    s1_2 = s0_2 + r1_2;
                    d1_2 = d0_2;
                }
            }

            if (rc.canSenseLocation(l2_1)) {
                r2_1 = 1.0 + rc.senseRubble(l2_1) / 10.0;
                if (s2_1 > s1_1 + r2_1) {
                    s2_1 = s1_1 + r2_1;
                    d2_1 = d1_1;
                }
                if (s2_1 > s10 + r2_1) {
                    s2_1 = s10 + r2_1;
                    d2_1 = d10;
                }
                if (s2_1 > s1_2 + r2_1) {
                    s2_1 = s1_2 + r2_1;
                    d2_1 = d1_2;
                }
                if (s2_1 > s20 + r2_1) {
                    s2_1 = s20 + r2_1;
                    d2_1 = d20;
                }
            }

            switch(dx){
                case -2:
                    switch (dy) {
                        case -1:
                            return d_2_1;
                        case 0:
                            return d_20;
                        case 1:
                            return d_21;
                    }
                    break;
                case -1:
                    switch (dy) {
                        case -2:
                            return d_1_2;
                        case -1:
                            return d_1_1;
                        case 0:
                            return d_10;
                        case 1:
                            return d_11;
                        case 2:
                            return d_12;
                    }
                    break;
                case 0:
                    switch (dy) {
                        case -2:
                            return d0_2;
                        case -1:
                            return d0_1;
                        case 1:
                            return d01;
                        case 2:
                            return d02;
                    }
                    break;
                case 1:
                    switch (dy) {
                        case -2:
                            return d1_2;
                        case -1:
                            return d1_1;
                        case 0:
                            return d10;
                        case 1:
                            return d11;
                        case 2:
                            return d12;
                    }
                    break;
                case 2:
                    switch (dy) {
                        case -1:
                            return d2_1;
                        case 0:
                            return d20;
                        case 1:
                            return d21;
                    }
                    break;
            }


            //If the target location is not within the search, find the closest searched point to the target with a decent score and go there.

            Direction bestDir = null;
            double currentDistance = Math.sqrt(l00.distanceSquaredTo(target));
            double bestLocationScore = 0;  //higher the better
            int COLOR_MULTIPLYER = 10;
            boolean debug = true;

            double t20 = (currentDistance - Math.sqrt(l20.distanceSquaredTo(target))) / s20 ;
//            if(debug) rc.setIndicatorDot(l20, 255 - (int)Math.round(t20)*COLOR_MULTIPLYER, 255, 255);
            if (t20 > bestLocationScore) {
                bestLocationScore = t20;
                bestDir = d20;
            }

            double t21 = (currentDistance - Math.sqrt(l21.distanceSquaredTo(target))) / s21;
//            if(debug) rc.setIndicatorDot(l21, 255 - (int)Math.round(t21)*COLOR_MULTIPLYER, 255, 255);
            if (t21 > bestLocationScore) {
                bestLocationScore = t21;
                bestDir = d21;
            }

            double t12 = (currentDistance - Math.sqrt(l12.distanceSquaredTo(target))) / s12 ;
//            if(debug) rc.setIndicatorDot(l12, 255 - (int)Math.round(t12)*COLOR_MULTIPLYER, 255, 255);
            if (t12 > bestLocationScore) {
                bestLocationScore = t12;
                bestDir = d12;
            }

            double t02 = (currentDistance - Math.sqrt(l02.distanceSquaredTo(target))) / s02;
//            if(debug) rc.setIndicatorDot(l02, 255 - (int)Math.round(t02)*COLOR_MULTIPLYER, 255, 255);
            if (t02 > bestLocationScore) {
                bestLocationScore = t02;
                bestDir = d02;
            }

            double t_12 = (currentDistance - Math.sqrt(l_12.distanceSquaredTo(target))) / s_12 ;
//            if(debug) rc.setIndicatorDot(l_12, 255 - (int)Math.round(t_12)*COLOR_MULTIPLYER, 255, 255);
            if (t_12 > bestLocationScore) {
                bestLocationScore = t_12;
                bestDir = d_12;
            }

            double t_21 = (currentDistance -Math.sqrt(l_21.distanceSquaredTo(target))) / s_21 ;
//            if(debug) rc.setIndicatorDot(l_21, 255 - (int)Math.round(t_21)*COLOR_MULTIPLYER, 255, 255);
            if (t_21 > bestLocationScore) {
                bestLocationScore = t_21;
                bestDir = d_21;
            }

            double t_20 = (currentDistance -Math.sqrt(l_21.distanceSquaredTo(target))) / s_20 ;
//            if(debug) rc.setIndicatorDot(l_21, 255 - (int)Math.round(t_20)*COLOR_MULTIPLYER, 255, 255);
            if (t_20 > bestLocationScore) {
                bestLocationScore = t_20;
                bestDir = d_20;
            }

            double t_2_1 = (currentDistance -Math.sqrt(l_2_1.distanceSquaredTo(target))) / s_2_1 ;
//            if(debug) rc.setIndicatorDot(l_2_1, 255 - (int)Math.round(t_2_1)*COLOR_MULTIPLYER, 255, 255);
            if (t_2_1 > bestLocationScore) {
                bestLocationScore = t_2_1;
                bestDir = d_2_1;
            }

            double t_1_2 = (currentDistance -Math.sqrt(l_1_2.distanceSquaredTo(target))) / s_1_2 ;
//            if(debug) rc.setIndicatorDot(l_1_2, 255 - (int)Math.round(t_1_2)*COLOR_MULTIPLYER, 255, 255);
            if (t_1_2 > bestLocationScore) {
                bestLocationScore = t_1_2;
                bestDir = d_1_2;
            }

            double t0_2 = (currentDistance -Math.sqrt(l0_2.distanceSquaredTo(target))) / s0_2 ;
//            if(debug) rc.setIndicatorDot(l0_2, 255 - (int)Math.round(t0_2)*COLOR_MULTIPLYER, 255, 255);
            if (t0_2 > bestLocationScore) {
                bestLocationScore = t0_2;
                bestDir = d0_2;
            }

            double t1_2 = (currentDistance -Math.sqrt(l1_2.distanceSquaredTo(target))) / s1_2 ;
//            if(debug) rc.setIndicatorDot(l1_2, 255 - (int)Math.round(t1_2)*COLOR_MULTIPLYER, 255, 255);
            if (t1_2 > bestLocationScore) {
                bestLocationScore = t1_2;
                bestDir = d1_2;
            }

            double t2_1 = (currentDistance -Math.sqrt(l2_1.distanceSquaredTo(target))) / s2_1 ;
//            if(debug) rc.setIndicatorDot(l2_1, 255 - (int)Math.round(t2_1)*COLOR_MULTIPLYER, 255, 255);
            if (t2_1 > bestLocationScore) {
                bestLocationScore = t2_1;
                bestDir = d2_1;
            }

            return bestDir;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

    }

