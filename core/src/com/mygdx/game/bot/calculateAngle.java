package com.mygdx.game.bot;

public class calculateAngle {

    private final double x0;
    private final double y0;
    private final double xf;
    private final double yf;
    private final double [] range = new double[2];
    
    public calculateAngle(double x0, double y0, double xf, double yf){
        this.x0 = x0;
        this.y0 = y0;
        this.xf = xf;
        this.yf = yf;
    }

    public void angle(){
        double xLength = xf - x0;
        double yLength = yf - y0;

        double angle = Math.atan(yLength/xLength);

        if(xLength > 0){ //first and fourth quadrant
            range[0] = angle - (Math.PI/2);
        } else if(xLength < 0){ //second and third quadrant
            range[0] = angle + (Math.PI/2);
        } else{
            range[0] = angle;
        }

        range[1] = range[0] + Math.PI;
    }

    public double[] getRange(){
        return range;
    }
}
