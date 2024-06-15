package com.mygdx.game.bot;

public class calculateDistance {

    private double x0;
    private double y0;
    private double xf;
    private double yf;

    public calculateDistance(double x0, double y0, double xf, double yf){
        this.x0 = x0;
        this.y0 = y0;
        this.xf = xf;
        this.yf = yf;
    }
    

    public double distance(){
        return Math.sqrt(Math.pow(x0-xf, 2) + Math.pow(y0-yf, 2));
    }

    public double distanceFromOrigin(double newx, double newy){
        return Math.sqrt(Math.pow(x0-newx, 2) + Math.pow(y0-newy, 2));
    }

    public double distanceFromHole(double newx, double newy){
        return Math.sqrt(Math.pow(newx-xf, 2) + Math.pow(newy-yf, 2));
    }
}
