package com.example.mycompass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

public class AcceleratorGraphView extends View {
    private Paint textPaint;
    private Paint linePaint;
    private Path acceleratorPath;
    private Path xPath;
    private Path yPath;
    private Path zPath;

    private ArrayDeque<MovementData> queue = new ArrayDeque<>();
    final float TIME_GAP = 20000;


    public AcceleratorGraphView(Context context, AttributeSet attrs, int i) { super(context,attrs,i); }

    public AcceleratorGraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

        xPath = new Path();
        yPath = new Path();
        zPath = new Path();
        acceleratorPath = new Path();
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setFakeBoldText(true);
        linePaint.setStrokeWidth(2);
        linePaint.setColor(ContextCompat.getColor(this.getContext(), R.color.inner_border_one));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);


        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(ContextCompat.getColor(this.getContext(), R.color.horizon_sky_to));
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(30);
    }

    public AcceleratorGraphView(Context context) {
        super(context);

    }

    public void add(MovementData md){
        queue.add(md);
        if((md.getTime()-queue.peek().getTime()) > TIME_GAP){
            queue.remove();
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas){
        int mMeasuredWidth = getMeasuredWidth();
        int mMeasuredHeight = getMeasuredHeight();
        //int px = mMeasuredWidth / 2;
        //int py = mMeasuredHeight / 2;

        canvas.drawColor(ContextCompat.getColor(this.getContext(), R.color.horizon_ground_to));

        textPaint.setStrokeWidth(2);
        int textHeight = (int) textPaint.measureText("yY");
        String tmp = "Accelerator";
        canvas.drawText(tmp, 0,textHeight,textPaint);
        String tmp1 = "Altitude";
        canvas.drawText(tmp1, 0,(mMeasuredHeight / 2) + textHeight,textPaint);

        canvas.drawLine(0,2*mMeasuredHeight/8,mMeasuredWidth, 2*mMeasuredHeight/8,textPaint);
        canvas.drawLine(0,4*mMeasuredHeight/8,mMeasuredWidth, 4*mMeasuredHeight/8,textPaint);
        canvas.drawLine(0,6*mMeasuredHeight/8,mMeasuredWidth, 6*mMeasuredHeight/8,textPaint);

        textPaint.setColor(ContextCompat.getColor(this.getContext(), R.color.horizon_sky_from));
        textPaint.setStrokeWidth(5);
        canvas.drawLine(0,mMeasuredHeight/8,20, mMeasuredHeight/8,textPaint);
        canvas.drawLine(0,3*mMeasuredHeight/8,20, 3*mMeasuredHeight/8,textPaint);
        canvas.drawLine(0,5*mMeasuredHeight/8,20, 5*mMeasuredHeight/8,textPaint);
        canvas.drawLine(0,7*mMeasuredHeight/8,20, 7*mMeasuredHeight/8,textPaint);

        canvas.drawLine(mMeasuredWidth/4,mMeasuredHeight,mMeasuredWidth/4, mMeasuredHeight-20,textPaint);
        canvas.drawLine(2*mMeasuredWidth/4,mMeasuredHeight,2*mMeasuredWidth/4, mMeasuredHeight-30,textPaint);
        canvas.drawLine(3*mMeasuredWidth/4,mMeasuredHeight,3*mMeasuredWidth/4, mMeasuredHeight-20,textPaint);

        canvas.drawLine(mMeasuredWidth/4,mMeasuredHeight/2,mMeasuredWidth/4, mMeasuredHeight/2-20,textPaint);
        canvas.drawLine(2*mMeasuredWidth/4,mMeasuredHeight/2,2*mMeasuredWidth/4, mMeasuredHeight/2-30,textPaint);
        canvas.drawLine(3*mMeasuredWidth/4,mMeasuredHeight/2,3*mMeasuredWidth/4, mMeasuredHeight/2-20,textPaint);
        textPaint.setColor(ContextCompat.getColor(this.getContext(), R.color.horizon_sky_to));


        if(queue.size()==0){
            return;
        }


        Iterator<MovementData> iterator = queue.iterator();
        long totalAmount = (queue.getLast().getTime() - queue.getFirst().getTime());
        if(totalAmount==0){
            return;
        }

        double standardScale = ((float)mMeasuredWidth)/totalAmount;
        long startTime = queue.getFirst().getTime();
        float xPosition = 0;
        float yPosition = 0;
        float x = 0;
        float y = 0;
        float z = 0;
        double relativeSpeed = 40.0;

        acceleratorPath.reset();
        xPath.reset();
        yPath.reset();
        zPath.reset();
        if(iterator.hasNext()){
            MovementData md = iterator.next();
            xPosition = (float) ((md.getTime()-startTime)*standardScale);
            yPosition = (float) md.getAcceleration();
            yPosition = (float) (float) ((mMeasuredHeight/4) - (yPosition/relativeSpeed)*(mMeasuredHeight/4));

            x = (float) md.getV0();
            x = (float) (float) ((mMeasuredHeight/4) - (x/relativeSpeed)*(mMeasuredHeight/4));

            y = (float) md.getV1();
            y = (float) (float) ((mMeasuredHeight/4) - (y/relativeSpeed)*(mMeasuredHeight/4));

            z = (float) md.getV2();
            z = (float) (float) ((mMeasuredHeight/4) - (z/relativeSpeed)*(mMeasuredHeight/4));
            acceleratorPath.moveTo(xPosition,yPosition);
            xPath.moveTo(xPosition,x);
            yPath.moveTo(xPosition,y);
            zPath.moveTo(xPosition,z);
        }

        while (iterator.hasNext()){
            MovementData md = iterator.next();
            xPosition = (float) ((md.getTime()-startTime)*standardScale);
            yPosition = (float) md.getAcceleration();
            yPosition = (float) ((mMeasuredHeight/4) - (yPosition/relativeSpeed)*(mMeasuredHeight/8));
            acceleratorPath.lineTo(xPosition,yPosition);

            x = (float) md.getV0();
            x = (float) (float) ((mMeasuredHeight/4) - (x/relativeSpeed)*(mMeasuredHeight/4));

            y = (float) md.getV1();
            y = (float) (float) ((mMeasuredHeight/4) - (y/relativeSpeed)*(mMeasuredHeight/4));

            z = (float) md.getV2();
            z = (float) (float) ((mMeasuredHeight/4) - (z/relativeSpeed)*(mMeasuredHeight/4));

            xPath.lineTo(xPosition,x);
            yPath.lineTo(xPosition,y);
            zPath.lineTo(xPosition,z);
        }
        canvas.drawPath(acceleratorPath, linePaint);
        linePaint.setColor(ContextCompat.getColor(this.getContext(), R.color.horizon_sky_from));
        canvas.drawPath(xPath, linePaint);
        linePaint.setColor(ContextCompat.getColor(this.getContext(), R.color.horizon_sky_to));
        canvas.drawPath(yPath, linePaint);
        linePaint.setColor(ContextCompat.getColor(this.getContext(), R.color.colorAccent));
        canvas.drawPath(zPath, linePaint);

    }
}
