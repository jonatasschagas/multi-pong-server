package com.jc.multipong.bootstrap.utils;

import java.util.Random;

/**
 * Created by jonataschagas on 25/01/17.
 */
public class MathUtils {

    public static float GetRandom(float lowerBound, float higherBound) {
        Random r = new Random();
        return r.nextInt((int)higherBound - (int)lowerBound) + lowerBound;
    }

    public static float Tan(float angleInRad) {
        return (float) Math.tan(angleInRad);
    }

    public static float DegreesToRad(float angleInDegrees) {
        return (float) Math.toRadians(angleInDegrees);
    }

    public static float Abs(double val1, double val2){
        return Math.abs((float)val1 - (float)val2);
    }

}
