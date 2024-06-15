package com.mygdx.game.bot;

import java.util.Arrays;
import java.util.Random;

import com.mygdx.game.physics.PhysicsEngine;


public class AI_bot{

    private double x0;//initial x coordinate of ball
    private double y0;//initial y coordinate of ball
    private double xf;//final x coordinate of ball
    private double yf;//final y coordinate of ball
    private final double minVel;//minimum velocity allowed (0)
    private final double maxVel;//maximum velocity allowed (5)
    private final double radius;//radius of hole/target
    private double[] angleRange;//range of shooting (180°)
    private double angleHole;//angle of where the hole/target is placed (based on the north)
    private double halfAngleTarget;//half of the target angle
    private  double distOriginHole;//distance from origin to target
    double[] bestCombo = new double[7]; //distance, angle, vel, vel vector x, vel vector y, posX, posY
    private boolean solutionFound;//is a solution found? true if yes, false otherwise
    private final String expression; //equation of the green as a string
    int[] direction;
    private boolean puddle; //if hole and origin separated by a lake
    private final PhysicsEngine engine;
    double pi = Math.PI;
    //PhysicsEngine engine;
    calculateDistance DistCalculator;
    private Random random;
    private double[][] results;

    public AI_bot(String expression, double x0, double y0, double xf, double yf, double radius){
        this.x0 = x0;
        this.y0 = y0;
        this.xf = xf;
        this.yf = yf;

        System.out.println( x0 + " " + y0 + " " + xf + " " + yf + " " + radius);
        minVel = 0;
        maxVel = 5;

        results = new double[5][3];

        this.radius = radius;
        this.expression = expression;
        this.engine  = new PhysicsEngine(expression);
        bestCombo[0] = Double.MAX_VALUE;

        calculateAngleAndDist();

    }

    public void calculateAngleAndDist(){
        this.DistCalculator = new calculateDistance(x0, y0, xf, yf);
        distOriginHole = DistCalculator.distance();

        random = new Random();

        calculateAngle calcAngle = new calculateAngle(x0, y0, xf, yf);
        calcAngle.angle();
        angleRange = calcAngle.getRange();
        /*if(angleRange[0] >= pi){
            angleRange[0] -= (2*pi); // makes the range continuous
        }*/
        angleHole = (angleRange[0] + angleRange[1])/2;
        halfAngleTarget = Math.atan(radius/distOriginHole);
        /*for(int i =1; i<11; i++){
            vectorsForce vectors = new vectorsForce(angleHole, i*0.5);
            vectors.calculateVectors();
            double [] pos = engine.computeFinalVectorState(0.01, vectors.getXVelocityVector(), vectors.getYVelocityVector(), x0, y0);
            if(pos[0] == x0 && pos[1] == y0){
                puddle = true;
            }
        }*/
        System.out.println("initial angle" +Arrays.toString(angleRange) + " " + angleHole + ", " + halfAngleTarget);
    }

    public static void main(String args[]){
        AI_bot bot = new AI_bot("0.4*(0.9-e^(-(x^2+y^2)/8))", -3, 0, 4, 1, 0.15);
        bot.findBestShot();
    }

    public double[][] findBestShot(){

        int iterations = 500;
        double[] newPosition = engine.computeFinalVectorState(0.01F, x0, y0, 0, 5);
        double maxDistance = DistCalculator.distanceFromOrigin(newPosition[0], newPosition[1]);
        if(maxDistance < distOriginHole){
            iterations = 50;
        }

        for(int k = 0; k < 5 && !solutionFound; k++) {
            for (int i = 0; i < iterations && !solutionFound; i++) {

                double randomAngle = random.nextDouble(Math.abs(angleRange[1] - angleRange[0])) + angleRange[0];
                double randomVel = random.nextDouble(maxVel - minVel) + minVel;

                HillClimbing(randomAngle, randomVel);

                if (solutionFound) {
                    System.out.println(bestCombo[0] + ", " + k);
                    results[k][0] = bestCombo[3];
                    results[k][1] = bestCombo[4];
                    results[0][2] = k+1;
                    return results;
                }
            }
            results[k][0] = bestCombo[3];
            results[k][1] = bestCombo[4];
            results[0][2] = 5;
            x0 = bestCombo[5];
            y0 = bestCombo[6];
            calculateAngleAndDist();
        }

        System.out.println( "Final distance from hole " + bestCombo[0]);
        return  results;
    }

    public int [] costFunction(double currentAngle, double currentVelocity, double xPosition, double yPosition){
        int [] direction= new int[2]; //[angle, velocity], -1 = decrease value, 1 = increase value, 0 = keep that value
        double distFromOrigin = DistCalculator.distanceFromOrigin(xPosition, yPosition);
        double distFromHole = DistCalculator.distanceFromHole(xPosition, yPosition);
        boolean bouncing = distOriginHole - distFromHole < 0;
        boolean endInPuddle = distFromOrigin == 0.0;


        if (distOriginHole - distFromOrigin < 0 && !bouncing ){
            direction[1] = -1;
        } else if (distOriginHole - distFromOrigin > 0) {
            direction[1] = 1;
        }

        if (currentAngle >= angleRange[0] && currentAngle < (angleHole)){//ball ended up to the left of the hole
            if(endInPuddle){
                direction[0] = -1;
            } else if(bouncing){
                direction[0] = 2;
            } else {
                direction[0] = 1;
            }
        } else if (currentAngle <= angleRange[1] && currentAngle > (angleHole)){//ball ended up to the right of the hole
            if(endInPuddle){
                direction[0] = 1;
            } else if(bouncing){
                direction[0] = -2;
            } else {
                direction[0] = -1;
            }
        }

        return direction;

    }

    public void HillClimbing(double angle, double velocity){

        System.out.println("new");

        for(int i =0; i<200 && !solutionFound; i++){

            vectorsForce vectors = new vectorsForce(angle, velocity);
            vectors.calculateVectors();
            //System.out.println("vectors: " + vectors.getXVelocityVector() + " "+ vectors.getYVelocityVector());

            //System.out.println("vel: " + velocity + " angle: " + angle);


            double[] newPosition = engine.computeFinalVectorState(0.01, x0, y0, vectors.getXVelocityVector(), vectors.getYVelocityVector());
            double distanceFromHole = DistCalculator.distanceFromHole(newPosition[0], newPosition[1]);
            //System.out.println("Position: " + newPosition[0] + " " + newPosition[1]);
            //System.out.println("Distance: " +distanceFromHole);
            if(distanceFromHole < bestCombo[0]){
                bestCombo[0] = distanceFromHole;
                bestCombo[1] = angle;
                bestCombo[2] = velocity;
                bestCombo[3] = vectors.getXVelocityVector();
                bestCombo[4] = vectors.getYVelocityVector();
                bestCombo[5] = newPosition[0];
                bestCombo[6] = newPosition[1];
                //System.out.println("positions: "+Arrays.toString(newPosition));
                if (bestCombo[0] <radius){
                    solutionFound = true;
                    //System.out.println("x final: "+newPosition[0] + "y final: "+newPosition[1]);
                    //System.out.println("Distance from hole "+bestCombo[0]);
                    return;
                }
            }else if( direction[0] != 2 && direction[0] != -2){
                //return;
            }

            direction = costFunction(angle, velocity, (double) newPosition[0], (double) newPosition[1]);
            //System.out.println(Arrays.toString(direction));
            if(direction[0] == 2 && angle + (Math.PI/4) < angleRange[1] ){
                angle = (angle + (Math.PI/4));
            } else if (direction[0] == -2 && angle - (Math.PI/4) > angleRange[0] ){
                angle = (angle - (Math.PI/4));
            }else if(direction[0] == -1 && angle - halfAngleTarget > angleRange[0]){
                angle -= halfAngleTarget ;
            } else if(direction[0] == 1 && angle + halfAngleTarget < angleRange[1]){
                angle += halfAngleTarget;
            }

            if(direction[1] == -1 && velocity - (0.1*distanceFromHole) > 0){
                velocity -= (0.1*distanceFromHole);
            } else if (direction[1] == 1 && velocity + (0.1*distanceFromHole) < 5){
                velocity += (0.1*distanceFromHole);
            }

        }


    }
}