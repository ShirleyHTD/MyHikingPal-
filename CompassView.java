package com.example.mycompass;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;

import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

public class CompassView extends View {
    private float mBearing = 0;
    private float mPitch = 0;
    private float mRoll = 0;
    private Paint markerPaint;
    private Paint textPaint;
    private Paint circlePaint;
    private String northString;
    private String eastString;
    private String southString;
    private String westString;
    private int textHeight;

    int[] borderGradientColors;
    float[] borderGradientPositions;
    int[] glassGradientColors;
    float[] glassGradientPositions;

    int skyHorizonColorFrom;
    int skyHorizonColorTo;
    int groundHorizonColorFrom;
    int groundHorizonColorTo;

    private Paint skyPaint;
    private Paint groundPaint;
    private Path skyPath;

    private int walkCount = 0;




    public CompassView(Context context) {
        this(context, null);
    }

    public CompassView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CompassView, defStyleAttr, 0);
        if (a.hasValue(R.styleable.CompassView_bearing)) {
            setBearing(a.getFloat(R.styleable.CompassView_bearing, 0));
        }
        a.recycle();
        Context c = this.getContext();
        Resources r = this.getResources();

        northString = r.getString((R.string.cardinal_north));
        eastString = r.getString(R.string.cardinal_east);
        southString = r.getString(R.string.cardinal_south);
        westString = r.getString(R.string.cardinal_west);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(ContextCompat.getColor(c, R.color.text_color));
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(50);
        textHeight = (int) textPaint.measureText("yY");
        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(ContextCompat.getColor(c, R.color.marker_color));
        markerPaint.setStrokeWidth(5);

        borderGradientColors = new int[4];
        borderGradientPositions = new float[4];
        borderGradientColors[3] = ContextCompat.getColor(c,    R.color.outer_border);
        borderGradientColors[2] = ContextCompat.getColor(c,    R.color.inner_border_one);
        borderGradientColors[1] = ContextCompat.getColor(c,    R.color.inner_border_two);
        borderGradientColors[0] = ContextCompat.getColor(c,    R.color.inner_border);
        borderGradientPositions[3] = 1.0f;
        borderGradientPositions[2] = 1-0.03f;
        borderGradientPositions[1] = 1-0.06f;
        borderGradientPositions[0] = 0.0f;

        skyPaint = new Paint();
        groundPaint = new Paint();
        skyHorizonColorFrom = ContextCompat.getColor(c,    R.color.horizon_sky_from);
        skyHorizonColorTo = ContextCompat.getColor(c,    R.color.horizon_sky_to);
        groundHorizonColorFrom = ContextCompat.getColor(c,    R.color.horizon_ground_from);
        groundHorizonColorTo = ContextCompat.getColor(c,    R.color.horizon_ground_to);

        skyPath = new Path();

        glassGradientColors = new int[5];
        glassGradientPositions = new float[5];
        int glassColor = 245;
        glassGradientColors[4] = Color.argb(65, glassColor, glassColor, glassColor);
        glassGradientColors[3] = Color.argb(100, glassColor, glassColor, glassColor);
        glassGradientColors[2] = Color.argb(50, glassColor, glassColor, glassColor);
        glassGradientColors[1] = Color.argb(0, glassColor, glassColor, glassColor);
        glassGradientColors[0] = Color.argb(0, glassColor, glassColor, glassColor);
        glassGradientPositions[4] = 1-0.0f;
        glassGradientPositions[3] = 1-0.06f;
        glassGradientPositions[2] = 1-0.10f;
        glassGradientPositions[1] = 1-0.20f;
        glassGradientPositions[0] = 1-1.0f;



    }

    public void refresh(float bearing, float pitch, float roll){
        setBearing(bearing);
        setPitch(pitch);
        setRoll(roll);
        invalidate();
    }

    private void setBearing(float bearing) {
        mBearing = bearing;
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    private void setPitch(float pitch) {
        float tiltDegree = pitch;
        while (tiltDegree > 90 || tiltDegree < -90) {
            if (tiltDegree > 90) tiltDegree = -90 + (tiltDegree - 90);
            if (tiltDegree < -90) tiltDegree = 90 - (tiltDegree + 90);
        }
        mPitch = tiltDegree;
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    public void setRoll(float roll) {
        float rollDegree = roll;
        while (rollDegree > 180 || rollDegree < -180) {
            if (rollDegree > 180) rollDegree = -180 + (rollDegree - 180);
            if (rollDegree < -180) rollDegree = 180 - (rollDegree + 180);
        }
        mRoll = rollDegree;
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    @Override public boolean dispatchPopulateAccessibilityEvent(final AccessibilityEvent event) {
        super.dispatchPopulateAccessibilityEvent(event);
        if (isShown()) {
            String bearingStr = String.valueOf(mBearing);
            event.getText().add(bearingStr);
            return true;
        } else{
            return false;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // The compass is a circle that fills as much space as possible.
        // Set the measured dimensions by figuring out the shortest boundary,
        // height or width.
        int measureWidth = measure(widthMeasureSpec);
        int measureHeight = measure(heightMeasureSpec);
        int d = Math.min(measureHeight, measureWidth);
        //setMeasuredDimension(d, d);
        setMeasuredDimension(measureWidth, measureHeight);
    }

    private int measure(int measureSpec) {
        int result = 0;
        // Decode the measurement specifications.
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.UNSPECIFIED) {
            // Return a default size of 200 if no bounds are specified.
            result = 200;
        } else {
            // As you want to fill the available space
            // always return the full available bounds.
            result = specSize;
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        int mMeasuredWidth = getMeasuredWidth();
        int mMeasuredHeight = getMeasuredHeight();
        int px = mMeasuredWidth / 2;
        int py = mMeasuredHeight / 2;
        int radius = Math.min(px, py);

        //int radius = Math.min(px, py)-2*textHeight;

        RadialGradient borderGradient = new RadialGradient(px, py, radius,  borderGradientColors, borderGradientPositions, Shader.TileMode.CLAMP);
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setShader(borderGradient);
        circlePaint.setColor(ContextCompat.getColor(this.getContext(), R.color.background_color));
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        //line and walk count
        textPaint.setColor(ContextCompat.getColor(this.getContext(), R.color.horizon_ground_to));
        String tmp = "count: " + walkCount;
        canvas.drawText(tmp, px - textPaint.measureText(tmp)/2, py-radius-textHeight,textPaint);
        canvas.drawLine(px-textPaint.measureText(tmp)/2,py-radius-textHeight+4,px+textPaint.measureText(tmp)/2, py-radius-textHeight+4,textPaint);
        textPaint.setColor(ContextCompat.getColor(this.getContext(), R.color.text_color));
        //background
        canvas.drawCircle(px, py, radius, circlePaint);

        //ground
        canvas.save();
        canvas.rotate(-mRoll, px, py);
        LinearGradient groundShader = new LinearGradient(px,    py - radius + 2*textHeight, px, py + radius - 2*textHeight,    groundHorizonColorFrom, groundHorizonColorTo, Shader.TileMode.CLAMP);
        Paint groundPaint = new Paint();
        groundPaint.setShader(groundShader);
        canvas.drawCircle(px, py, radius - 2*textHeight, groundPaint);
        //sky
        LinearGradient skyShader = new LinearGradient(px,    py - radius + 2*textHeight, px, py + radius - 2*textHeight,    skyHorizonColorFrom, skyHorizonColorTo, Shader.TileMode.CLAMP);
        Paint skyPaint = new Paint();
        skyPaint.setShader(skyShader);
        skyPath.reset();
        skyPath.addArc(px-radius+2*textHeight, py - radius + 2*textHeight, px+radius-2*textHeight,py+radius-2*textHeight,-mPitch, (180 + (2 * mPitch)));
        canvas.drawPath(skyPath, skyPaint);
        //lines
        int markWidth = radius / 3;
        int startX = px - markWidth;
        int endX = px + markWidth;
        double h = (radius-2*textHeight)*Math.cos(Math.toRadians(90-mPitch));
        double justTiltY = py - h;
        float pxPerDegree = (radius-2*textHeight)/45f;
        markerPaint.setStrokeWidth(1);
        for (int i = 90; i >= -90; i -= 10){
            double ypos = justTiltY + i*pxPerDegree;
            // Only display the scale within the inner face.
            if ((ypos < (py-radius+2*textHeight)) ||        (ypos > py+radius-2*textHeight))      continue;

            canvas.drawLine(startX, (float)ypos, endX, (float)ypos, markerPaint);
            int displayPos = (int)(mPitch - i);
            String displayString = String.valueOf(displayPos);
            float stringSizeWidth = textPaint.measureText(displayString);
            canvas.drawText(displayString, (int)(px-stringSizeWidth/2),(int)(ypos)+1, textPaint);

        }
        markerPaint.setStrokeWidth(4);
        canvas.drawLine(px - radius / 2, (float)justTiltY,px + radius / 2, (float)justTiltY, markerPaint);
        markerPaint.setStrokeWidth(1);
        canvas.restore();

        //label circle
        canvas.save();
        canvas.rotate(-mBearing, px, py);
        int textWidth = (int)textPaint.measureText("W");
        int cardinalX = px - textWidth / 2;
        int cardinalY = py - radius + textHeight;
        for (int i = 0; i < 24; i++) {
            // Draw a marker.
            // Draw the cardinal points
            if (i % 6 == 0) {
                canvas.drawLine(px, py - radius, px, py - radius + 35, markerPaint);
                canvas.save();
                canvas.translate(0, textHeight);
                String dirString = "";
                switch (i) {
                    case (0):   {
                                    dirString = northString;
                                    int arrowY = 2 * textHeight;
                                    canvas.drawLine(px, arrowY, px - 5, 3 * textHeight, markerPaint);
                                    canvas.drawLine(px, arrowY, px + 5, 3 * textHeight, markerPaint);
                                    break;
                                }
                    case(6) : dirString = eastString; break;
                    case(12) : dirString = southString; break;
                    case(18) : dirString = westString; break;
                }
                canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
            } else if (i % 3 == 0) {
                canvas.drawLine(px, py - radius, px, py - radius + 25, markerPaint);
                canvas.save();
                canvas.translate(0, textHeight);
                // Draw the text every alternate 45deg
                String angle = String.valueOf(i*15);
                float angleTextWidth = textPaint.measureText(angle);
                int angleTextX = (int)(px-angleTextWidth/2);
                int angleTextY = py-radius+textHeight;
                canvas.drawText(angle, angleTextX, angleTextY, textPaint);
            } else{
                canvas.drawLine(px, py - radius, px, py - radius + 15, markerPaint);
                canvas.save();
                canvas.translate(0, textHeight);
            }
            canvas.restore();
            canvas.rotate(15, px, py);
        }
        canvas.restore();

        //glass
        RadialGradient glassShader = new RadialGradient(px, py, radius - 2*textHeight,glassGradientColors, glassGradientPositions, Shader.TileMode.CLAMP);
        Paint glassPaint = new Paint();
        glassPaint.setShader(glassShader);
        canvas.drawOval(px-radius+2*textHeight, py - radius + 2*textHeight, px+radius-2*textHeight,py+radius-2*textHeight, glassPaint);


    }


    public void refreshWalkCount(float s) {
        walkCount = (int) s;
    }
}
