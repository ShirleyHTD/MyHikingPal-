package com.example.mycompass;

public class MovementData {
    double acceleration;
    long time;
    float v0;
    float v1;
    float v2;
    public MovementData(float[] values, long time){
        this.time = time;
        v0 = values[0];
        v1 = values[1];
        v2 = values[2];
        mCalculate(values);
    }

    public long getTime(){
        return time;
    }

    public float getV0(){
        return v0;
    }

    public float getV1(){
        return v1;
    }

    public float getV2(){
        return v2;
    }

    public double getAcceleration(){
        return acceleration;
    }

    private void mCalculate(float[] values){
        double sumOfSquares = (values[0] * values[0]) + (values[1] * values[1]) + (values[2] * values[2]);
        acceleration = Math.sqrt(sumOfSquares);
    }
}
