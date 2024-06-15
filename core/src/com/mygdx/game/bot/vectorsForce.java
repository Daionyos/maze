package com.mygdx.game.bot;

public class vectorsForce {
    
    private final double angle;
    private final double velocity;
    private double yvelocity;
    private double xvelocity;

    public vectorsForce(double angle, double velocity){
        this.angle = angle;
        this.velocity = velocity;
    }

    public void calculateVectors(){
        
        xvelocity = Math.cos(angle) * velocity;
        yvelocity = Math.sin(angle) * velocity;

    }

    public double getYVelocityVector(){
        return yvelocity;
    }

    public double getXVelocityVector(){
        return xvelocity;
    }

}
