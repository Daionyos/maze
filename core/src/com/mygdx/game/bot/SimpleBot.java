package com.mygdx.game.bot;

import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.bot.calculateDistance;
import com.mygdx.game.physics.PhysicsEngine;
import com.mygdx.game.physics.PhysicsSettings;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
/**
 * SimpleBot class represents a basic bot that computes the next move and path details for a ball rolling towards a hole
 * based on physics calculations and provided expressions. The bot has two main methods the Makethenextmove() and the getpath().
 * The makethenextmove method return the angle and the velocity it would hit the ball with given it current position.
 * The getPath method returns the list of moves it would do to reach the hole from a starting point.
 */
public class SimpleBot {
    private PhysicsEngine engine;
    private Vector3 initialPosition;
    private Vector3 holePosition;
    private double minVel = 0;
    private double maxVel = 5;
    private double radius;
    private calculateDistance distCalculator;
    private boolean solutionFound;
    private double[][] path;

    /**
     * Constructor for a Simple Rule Based Bot .
     * @param expression Expression of the terrain that goes into the physics engine
     * @param x0 Initial  x coordinates of the ball
     * @param y0 Inital y coordinate of the ball
     * @param xf X coordinate of the hole
     * @param yf Y coordinate of the hole
     * @param radius Radius of the hole
     */
    public SimpleBot(String expression, double x0, double y0, double xf, double yf, double radius) {
        engine = new PhysicsEngine(expression);
        initialPosition = new Vector3((float) x0, (float) y0, 0);
        holePosition = new Vector3((float) xf, (float) yf, 0);
        this.radius = radius;
        distCalculator = new calculateDistance(x0, y0, xf, yf);
    }

    /**
     * Method that return the next move the ball will make from coordinates given.
     * Includes logic to avoid puddles ( final height is negative) by rotating the angle.
     * @param x current ball coordinate
     * @param y current ball coordinate
     * @return an array that store the angle and the velocity that the ball was hit from x, y initial coordinates to reach x and y final coordinates.
     */
    public double[] makeNextMove(double x, double y) {
        double angle = chooseAngle(x, y);
        double velocity = chooseVelocity(x, y);
        double initialX = x;
        double initialY = y;

        vectorsForce vectors = new vectorsForce(angle, velocity);
        vectors.calculateVectors();
        double[] newPosition = engine.computeFinalVectorState(0.01F, (float) initialX, (float) initialY, (float) vectors.getXVelocityVector(), (float) vectors.getYVelocityVector());
        //System.out.println("Positions: " + Arrays.toString(newPosition));
        double finalX = newPosition[0];
        double finalY = newPosition[1];
        double velocityX=vectors.getXVelocityVector();
        double velocityY= vectors.getYVelocityVector();
        if (engine.getHeight(finalX, finalY) < 0) {
            double maxAngleChange = Math.toRadians(90);
            double incrementAngle = angle;
            double decrementAngle = angle;
            boolean foundClearPath = false;
            System.out.println("Puddle");
            while (!foundClearPath && Math.abs(incrementAngle - angle) <= maxAngleChange) {
                incrementAngle += Math.toRadians(5);
                vectorsForce vectorsInc = new vectorsForce(incrementAngle, velocity);
                vectorsInc.calculateVectors();
                newPosition = engine.computeFinalVectorState(0.01F, initialX, initialY, vectorsInc.getXVelocityVector(), vectorsInc.getYVelocityVector());
                finalX = newPosition[0];
                finalY = newPosition[1];
                velocityX = vectorsInc.getXVelocityVector();
                velocityY = vectorsInc.getYVelocityVector();
                if (engine.getHeight(finalX, finalY) >= 0) {
                    angle = incrementAngle;
                    foundClearPath = true;
                    System.out.println("rotated right");
                    break;
                }
                decrementAngle -= Math.toRadians(5);
                vectorsForce vectorsDec = new vectorsForce(decrementAngle, velocity);
                vectorsDec.calculateVectors();
                newPosition = engine.computeFinalVectorState(0.01F, (float) initialX, (float) initialY, (float) vectorsDec.getXVelocityVector(), (float) vectorsDec.getYVelocityVector());
                finalX = newPosition[0];
                finalY = newPosition[1];
                velocityX = vectorsDec.getXVelocityVector();
                velocityY = vectorsDec.getYVelocityVector();
                if (engine.getHeight(finalX, finalY) >= 0) {
                    angle = decrementAngle;
                    System.out.println("rotated left");
                    foundClearPath = true;
                    break;
                }
            }
        }

        // Adjust velocity if necessary
        double adjustedVelocity = adjustVelocity(initialX, initialY, finalX, finalY, velocity);

        // If velocity was adjusted, recalculate vectors and compute new position
        if (adjustedVelocity != velocity) {
            System.out.println("adjusted:" + adjustedVelocity + "before velocity " + velocity);
            vectors = new vectorsForce(angle, adjustedVelocity);
            vectors.calculateVectors();
            newPosition = engine.computeFinalVectorState(0.01F, initialX, initialY, vectors.getXVelocityVector(), vectors.getYVelocityVector());
            finalX = newPosition[0];
            finalY = newPosition[1];
            velocityX = vectors.getXVelocityVector();
            velocityY = vectors.getYVelocityVector();
            velocity=adjustedVelocity;
        }

        return new double[]{angle, velocity, initialX, initialY, finalX, finalY,velocityX,velocityY};
    }

    /**
     * Method that chooses angle from a coordinate.
     * @param currentX
     * @param currentY
     * @return angle in radians to hit the ball
     */

    private double chooseAngle(double currentX, double currentY) {
        double deltaX = holePosition.x - currentX;
        double deltaY = holePosition.y - currentY;
        double angle = Math.atan2(deltaY, deltaX);
        return angle;
    }

    /**
     * Chooses the velocity to ht the ball. If its too far away if will use the max velocity.
     * Otherwise, it scales the force in scale to how close the ball is form the hole.
     * @param x current x coordinate of the ball
     * @param y current y coordinate of the ball
     * @return velocity of the ball as a double
     */
    private double chooseVelocity(double x, double y) {
        double distance = distCalculator.distanceFromHole(x, y);
        double thresholdDistance = 15 ;
        double minVelocity = 0.15;
        double velocity;

        if (distance > thresholdDistance) {
            velocity = maxVel;
        } else {
            double scalingFactor = distance / thresholdDistance;
            velocity = maxVel * scalingFactor;
            velocity = Math.max(velocity, minVelocity);
        }
        double staticFricGrass = PhysicsSettings.STATIC_FRIC_GRASS*velocity;
        velocity +=staticFricGrass;
        return velocity;
    }
    private double adjustVelocity(double initialX, double initialY, double finalX, double finalY, double currentVelocity) {
        double distanceToHole = distCalculator.distanceFromHole(finalX, finalY);
        double targetDistance = radius; // Adjust this value as needed based on the desired proximity to the hole
        if(targetDistance<=1){
            targetDistance=1;
        }
        // Calculate how much the ball overshot the target
        double overshootDistance = distanceToHole - targetDistance;

        // Check if the overshoot distance is greater than a certain threshold (e.g., 2 times the radius)
        if (overshootDistance > 5 * targetDistance) {
            // Define a factor to adjust the velocity based on the overshoot distance
            double velocityAdjustmentFactor = 0.5; // Adjust this factor as needed

            // Reduce the velocity based on the overshoot distance
            double adjustedVelocity = currentVelocity * velocityAdjustmentFactor;

            // Ensure the adjusted velocity is not below the minimum velocity
            double minVelocity = 0.01; // Adjust this value as needed
            adjustedVelocity = Math.max(adjustedVelocity, minVelocity);

            return adjustedVelocity;
        } else {
            // If the overshoot distance is within the threshold, don't adjust the velocity
            return currentVelocity;
        }
    }

    /**
     *  Gets the sequence of moves to make from a starting position
     * @return an array with te information of each move and its angle, velocity and positions
     */
    public double[][] getPath() {
        List<double[]> pathList = new ArrayList<>();
        double moveCount =0;
        double newX = initialPosition.x; // starts with the initial position from the bot
        double newY = initialPosition.y; // starts with the initial position form the bot
        while (moveCount < 1000) {
            double[] move = makeNextMove(newX, newY);
            double angle = move[0];
            double velocity = move[1];
            double finalX = move[4];
            double finalY = move[5];
            double distanceFromHole = distCalculator.distanceFromHole(finalX, finalY);
            double velocityX= move[6];
            double velocityY= move[7];

            pathList.add(new double[]{angle, velocity, finalX, finalY, distanceFromHole,velocityX,velocityY});

            if (distanceFromHole <= radius) {
                //System.out.println("Destination reached");
                break;
            }
            newX = finalX;
            newY = finalY;
            moveCount++;
        }

        if (moveCount >= 1000) {
            System.out.println("Maximum move count reached");
        }

        return listAsArray(pathList);
    }

    /**
     * Prints each move of the path
     * @param path
     */
    public void printPath(double[][] path) {
        System.out.println("Path Details:");
        for (int i = 0; i < path.length; i++) {
            double angle = Math.toDegrees(path[i][0]);
            double velocity = path[i][1];
            double newX = path[i][2];
            double newY = path[i][3];
            double distanceFromHole = path[i][4];
            System.out.printf("Step %d: Angle=%.2f degrees, Velocity=%.2f, New Position after Last Hit=(%.2f, %.2f), Distance from Hole=%.2f%n",
                    i + 1, angle, velocity, newX, newY, distanceFromHole);
        }
        System.out.println("Path tracing complete.");
    }

    /**
     * Gets the velocity vectors of the path.
     * @return an array containing the velocity vectors of the path on x and y axes.
     */
    public double[][] getVelocityVectors(){
        double[][] path = getPath();
        double[][] velocityVectors = new double[path.length][2]; // 2D array to store velocity vectors on x and y axes
        for (int i = 0; i < path.length; i++) {
            velocityVectors[i][0] = path[i][6]; // Velocity vector on x axis switched for ingame axis
            velocityVectors[i][1] = path[i][5]; // Velocity vector on y axis switched for in game axis
            //System.out.println("vx: "+ path[i][5] + " vy :" + path[i][6]);
        }
        return velocityVectors;
    }

    public double[] getVelocityVectorsShot(int shot){
        double[][] path = getPath();
        double[] velocityVectors = new double[2];
        velocityVectors[0] = path[shot][5];
        velocityVectors[1] = path[shot][6];
        return velocityVectors;
    }

    /**
     * helper method
     * @param list
     * @return
     */
    private double[][] listAsArray(List<double[]> list) {
        return list.toArray(new double[0][]);
    }

    /**
     * Prints the next move method
     * @param x
     * @param y
     */
    public void printNextMove(double x, double y) {
        double[] move = makeNextMove(x, y);
        double angle = move[0];
        double velocity = move[1];
        double finalX = move[4];
        double finalY = move[5];
        System.out.println("Next Move lands in: " + finalX + ", " + finalY);
        System.out.println("Angle: " + Math.toDegrees(angle) + " degrees");
        System.out.println("Velocity: " + velocity);
    }
    /**
     * Method to print the number of steps of the path and the last distance from the hole.
     * @return A string containing the number of steps and the last distance from the hole.
     */
    public void printResult() {
        Instant startTime = Instant.now(); // Record the start time
        double[][] path = getPath();
        Instant endTime = Instant.now(); // Record the end time

        int steps = path.length;
        double lastDistance = path[path.length - 1][4]; // Last distance from the hole

        // Calculate the elapsed time
        Duration timeElapsed = Duration.between(startTime, endTime);

        System.out.println("Number of steps: " + steps);
        System.out.println("Last distance from hole: " + lastDistance);
        System.out.println("Time taken to get the path: " + timeElapsed.toMillis() + " milliseconds");
    }


    /**
     * Test the bot with different values
     * @param args
     */
    public static void main(String[] args) {
        SimpleBot bot = new SimpleBot("0.4*(0.9-e^(-(x^2+y^2)/8))", -3,0,4,1,0.15);
        bot.printPath(bot.getPath());
        //bot.getVelocityVectors();
    }

}
