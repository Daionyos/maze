package com.mygdx.game.bot;

import com.badlogic.gdx.math.Interpolation;

import java.time.Duration;
import java.time.Instant;

public class BotExperiment {
    public static void main(String args[]) {
        // Test with Flat Terrain
        testTerrain("Flat Terrain", "0", 0, 0, 5, 10, 0.15);
        // Test with Sloped Terrain
        testTerrain("Sloped Terrain", "0.01 * (x^2 + y^2)", 0, 0, 3, 4, 0.15);
        // Test with Ridge Terrain
        testTerrain("Ridge Terrain", "0.05/(1+0.03*(x - 3)^2 + (y - 3)^2)",-3,0,4,6,0.5);
        //Test with Given Terrain
        testTerrain("Manual Terrain","0.4*(0.9-e^(-(x^2+y^2)/8))", -3,0,4,1,0.15);

    }

    public static void testTerrain(String terrainName, String terrainEquation, double x0, double y0, double xf, double yf, double radius) {
        System.out.println("Testing " + terrainName + ":");
        // Initialize AI bot
        System.out.println("Ai Bot Results for " +terrainName);

       AI_bot ai = new AI_bot(terrainEquation, x0, y0, xf, yf, radius);
        Instant startTime = Instant.now(); // Record the start time
        ai.findBestShot();
        Instant endTime = Instant.now(); // Record the end time
        // Calculate the elapsed time
        Duration timeElapsed = Duration.between(startTime, endTime);
        System.out.println("Number of steps: 1 " );
        System.out.println("Time elapsed : " + timeElapsed.toMillis());
        System.out.println(" ");


        // Initialize Simple bot
        SimpleBot simple = new SimpleBot(terrainEquation, x0, y0, xf, yf, radius);
        // Run Simple bot to find the path
        System.out.println("SimpleBot Results for " +terrainName);
        simple.printResult();
        System.out.println("");
        System.out.println("");
    }
}
