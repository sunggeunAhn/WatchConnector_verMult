package util.keyboardLayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;

import util.ScreenInfo;

/**
 * Created by PCPC on 2017-03-13.
 */

public class MaximizedKeyboard extends KeyboardLayout {

    protected int line1X, line1Y, line2X, line2Y, lineStartX, lineEndX , lineStartY, lineEndY, line0, line3;
    protected Paint linePaint;

    protected Path leftArr, rightArr;
    protected Paint paintArrowL, paintArrowR;
    protected Point guideTextL, guideTextR;
    protected String leftGuideText;
    protected String rightGuideText;
    protected Paint paintArrowL_text, paintArrowR_text;


    protected Rect window;
    protected Paint windowPaint;
    private int windowSize;

    private float keyboardRatio;
    private int xOffset1, xOffset2, xOffset3;
    private int windowBlock1, windowBlock2;


    private float widthR, heightR;
    private int activatedID3;

    private float convertX, convertY;
    private CountDownTimer cdt;

    private boolean gripActivated;
    private float currentRatio;
    private int prevX, prevY;

    private int tempFocusedID;
    private int prevScrID;

    private int cursorYoffset;
    public int alphaLevel = 130;
    public boolean isDefaultOppaque255 = true;

    private final int baseAlphaKey = 20;
    private final int baseAlphaSpace = 50;

    public char currentTarget;
    public boolean modeDefault;
    public MaximizedKeyboard(Context c){
        initKeyboard(KEYBOARD_MAXBOARD);

        modeDefault = true;
        currentTarget = ' ';
        currentRatio = 1;
        gripActivated = false;
        windowSize = keySize*3 + gap *2;
        windowBlock1 = windowSize + gap;
        windowBlock2 = windowBlock1 + windowSize + gap;
        window = new Rect(xOffset, yOffset, xOffset + windowSize, yOffset + keyboardHeight);
        windowPaint = new Paint();
        windowPaint.setColor(Color.RED);
        windowPaint.setAlpha(0);
        windowPaint.setStyle(Paint.Style.STROKE);
        windowPaint.setStrokeWidth(10);
        linePaint = new Paint();
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(10);
        //line0 = xOffset;
        keyboard_line_CDgainDefault();
        widthR = (float)9 / keyboardWidth;
        heightR = widthR;
        convertX = (float)window.width() / ScreenInfo.screenWidth;
        convertY = (float)window.height() / ScreenInfo.screenHeight;


        cursorYoffset = 0;



        cdt = new CountDownTimer(20, 2) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(isActivated()) this.cancel();
            }

            @Override
            public void onFinish() {
                tempFocusedID = focusedID;
                focusedID = -1;
            }
        };

    }

    public void setAfterImage(final int id){

        final AsyncTask async= new AsyncTask(){
            int alpha = 255;
            int delta = 30;
            int threshold;
            @Override
            protected Object doInBackground(Object[] params) {
                if(id > 25){
                    threshold = baseAlphaSpace;
                }else{
                    threshold = baseAlphaKey;
                }
                while(alpha > threshold){
                    alpha -= delta;
                    try {
                        keysBoundPaint[focusedID].setAlpha(alpha);
                        Thread.currentThread();
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }catch (Exception e){

                    }
                }
                keysPaint[id].setColor(Color.WHITE);
                //keysPaint[temp].setAlpha(alphaLevel);
                keysBoundPaint[id].setStyle(Paint.Style.STROKE);
                keysBoundPaint[id].setColor(Color.YELLOW);
                return null;
            }
        }.execute();
    }



    public int getxOffset(){
        return xOffset;
    }
    public int getyOffset(){
        return yOffset;
    }

    public char moveScreen(int xPos, int yPos){
        int temp = focusedID;
        int xID = -1;
        if(xPos > -1){
            xID = (int)(xPos / keySize);
        }
        int yID = yPos - cursorYoffset;
        if(yID > -1) {
            yID = (int) ((yPos - cursorYoffset) / keyHeight);
        }else{
            yID = -1;
        }
        /*
        keysPaint[prevScrID].setAlpha(70);
        keysPaint[prevScrID + 3].setAlpha(70);
        keysPaint[prevScrID + 6].setAlpha(70);
        */

        prevScrID= getIDfromXY(xID, yID);
        focusedID = prevScrID ;
        /*
        keysPaint[prevScrID].setAlpha(170);
        keysPaint[prevScrID + 3].setAlpha(170);
        keysPaint[prevScrID + 6].setAlpha(170);
        */
        if(focusedID != temp){
            //keysBoundPaint[prevFocusedID].setColor(Color.LTGRAY);
            try {
                keysPaint[temp].setColor(Color.WHITE);
                //keysPaint[temp].setAlpha(alphaLevel);
                keysBoundPaint[temp].setStyle(Paint.Style.STROKE);
                keysBoundPaint[temp].setColor(Color.YELLOW);
                switch (temp) {
                    case 25://Back
                    case 29://cap
                    case 31://space
                    case 32://enter
                        keysBoundPaint[temp].setAlpha(baseAlphaSpace);
                        break;
                    default:
                        keysBoundPaint[temp].setAlpha(baseAlphaKey);
                        break;
                }
                //gradientKeysBound[temp].clearColorFilter();
            }catch (Exception e){
                focusedID = -1;
            }
            try {
                focusedID = prevScrID;
                keysPaint[focusedID].setColor(Color.RED);
                keysBoundPaint[focusedID].setAlpha(255);
                keysBoundPaint[focusedID].setStyle(Paint.Style.FILL);
                keysBoundPaint[focusedID].setColor(Color.WHITE);
            }catch (Exception e){
                focusedID = -1;
            }
        }
        //keysBoundPaint[prevFocusedID].setColor(Color.DKGRAY);



        if(focusedID < 0){
            return (char)0;
        }else return  keys[focusedID].charAt(0);
    }

    public char downScreen(int xPos, int yPos){
        int xID = -1;
        if(xPos > -1){
            xID = (int)(xPos / keySize);
        }
        int yID = yPos - cursorYoffset;
        if(yID > -1) {
            yID = (int) ((yPos - cursorYoffset) / keyHeight);
        }else{
            yID = -1;
        }
        prevScrID= getIDfromXY(xID, yID);
        if(focusedID > -1){

            try {
                keysPaint[focusedID].setColor(Color.WHITE);
                //keysPaint[temp].setAlpha(alphaLevel);
                keysBoundPaint[focusedID].setStyle(Paint.Style.STROKE);
                keysBoundPaint[focusedID].setColor(Color.YELLOW);
                switch (focusedID) {
                    case 25://Back
                    case 29://cap
                    case 31://space
                    case 32://enter
                        keysBoundPaint[focusedID].setAlpha(baseAlphaSpace);
                        break;
                    default:
                        keysBoundPaint[focusedID].setAlpha(baseAlphaKey);
                        break;
                }
                //gradientKeysBound[focusedID].clearColorFilter();
            }catch(Exception e){
            }
        }
        focusedID = prevScrID ;
            /*
            keysPaint[prevScrID].setAlpha(255);
            keysPaint[prevScrID + 3].setAlpha(255);
            keysPaint[prevScrID + 6].setAlpha(255);*/
        try {
            keysPaint[focusedID].setColor(Color.RED);
            keysBoundPaint[focusedID].setAlpha(255);
            keysBoundPaint[focusedID].setStyle(Paint.Style.FILL);
            keysBoundPaint[focusedID].setColor(Color.WHITE);
        }catch (Exception e){
            focusedID = -1;
        }


        if(focusedID < 0){
            return (char)0;
        }else return  keys[focusedID].charAt(0);
    }
    public char upScreen(int xPos, int yPos){
        boolean isActivated = activatedID > -1;
        int temp = focusedID;
        //keysBoundPaint[temp].setColor(Color.LTGRAY);
            /*
            keysPaint[prevScrID].setAlpha(alphaLevel);
            keysPaint[prevScrID + 3].setAlpha(alphaLevel);
            keysPaint[prevScrID + 6].setAlpha(alphaLevel);
            */
        try {
            keysPaint[temp].setColor(Color.WHITE);
            //keysPaint[temp].setAlpha(alphaLevel);
            setAfterImage(temp);
            //gradientKeysBound[temp].clearColorFilter();
        }catch(Exception e){
        }
        focusedID = -1;
        if(temp < 0){
            return (char)0;
        }else return  keys[temp].charAt(0);
    }

    public void doneActivate(){
        boolean isActivated = activatedID > -1;
        int temp = focusedID;
        if(isActivated){
            //keysBoundPaint[temp].setColor(Color.LTGRAY);
            /*
            keysPaint[prevScrID].setAlpha(alphaLevel);
            keysPaint[prevScrID + 3].setAlpha(alphaLevel);
            keysPaint[prevScrID + 6].setAlpha(alphaLevel);
            */
            try {
                keysPaint[focusedID].setColor(Color.WHITE);
                setAfterImage(focusedID);
                //gradientKeysBound[focusedID].clearColorFilter();
            }catch(Exception e){
            }
        }
        focusedID = -1;
    }


    public int[] scrCursorOnKeyboard(int x, int y){
        int[] result = new int[2];
        result[0] = (int)(window.left + x *convertX);
        result[1] = (int)(window.top + y*convertY);
        return result;
    }
    public boolean isActivated(){
        return activatedID > -1;
    }

    public Rect getWindow(){
        return window;
    }



    public void keyboard_line_CDgainAdjust(int offset, float ratio){
        int defaultUnit = keySize * 3 + gap*3;
        int unit = (int)(defaultUnit * ratio);
        /*
        line0 = offset -1;
        line1 = line0 + 1 + unit;
        line2 = line1 + unit;
        line3 = line2 + unit + 1;
        */
        line1X = offset + unit;
        line2X = line1X + unit;
    }

    public void keyboard_line_CDgainDefault(){
        line0 = 0;
        line1X = xOffset + keySize * 3 + gap*3;
        line2X = xOffset + keySize * 6 + gap*6;
        line3 = ScreenInfo.screenWidth+1;
    }

    @Override
    public void keyboardResize(float viewRatio){
        currentRatio = viewRatio;
        keyboardResize(xOffset, yOffset, viewRatio);
        windowSize = keySize*3 + gap *2;
        windowBlock1 = xOffset + windowSize + gap;
        windowBlock2 = windowBlock1 + windowSize + gap;
        window.set(xOffset, yOffset, xOffset + windowSize, yOffset + keyboardHeight);
        linePaint.setStrokeWidth(gap * 2);
        windowPaint.setStrokeWidth(gap*2);

        convertX = (float)window.width() / ScreenInfo.screenWidth;
        convertY = (float)window.height() / ScreenInfo.screenHeight;
    }

    public void keyboardReposition(float ratio, int moveX, int moveY){
        xOffset += moveX;
        yOffset += moveY;
        keyboardResize(ratio);
        keyboard_line_CDgainAdjust(xOffset, 1);
    }

    public boolean onTouch(int x, int y){
        if(gripActivated || contains(x,y)){
            if(!gripActivated){
                gripActivated = true;
                windowPaint.setColor(Color.MAGENTA);
                prevX = x;
                prevY = y;
            }else{
                keyboardReposition(currentRatio, x - prevX, y - prevY);
                prevX = x;
                prevY = y;
            }
            return true;
        }else{
            gripActivated = false;
            windowPaint.setColor(Color.argb(0,0,0,0));
            return false;
        }

    }

    public void onTouchUp(){
        if(gripActivated) {
            gripActivated = false;
            windowPaint.setColor(Color.argb(0,0,0,0));
        }
    }
    public boolean contains(int x, int y){
        if(x > xOffset && x < line3){
            if(y > window.top && y < window.bottom){
                return true;
            }
        }
        return false;
    }
    public void boundReset(){
        activatedID = -1;
        activatedID3 = -1;
        int alpha;
        if(isDefaultOppaque255){
            alpha = 255;
        }else{
            alpha = alphaLevel;
        }
        for(int i = 0 ; i < numberOfPhysicalKey; i++){
            gradientKeysBound[i].setAlpha(alpha);
            gradientKeysBound[i].clearColorFilter();
            keysPaint[i].setColor(Color.argb(255,255,255,255));
        }
        xOffset = xOffset2;

        keyboardResize(xOffset, yOffset, keyboardRatio);
        windowPaint.setColor(Color.argb(0, 0, 0, 0));
        linePaint.setColor(windowPaint.getColor());
    }


    private void setWindow(){

    }

    public void resetKeysPaint(){

        for(int i = 0 ; i < numberOfPhysicalKey; i++){
            if(i > 25)
                gradientKeysBound[i].setAlpha(baseAlphaSpace);
            else
                gradientKeysBound[i].setAlpha(baseAlphaKey);
            keysPaint[i].setColor(Color.WHITE);
        }
    }


    @Override
    public void drawKeyboard(Canvas c){
        //c.drawRect(keyboardBound, boundPaint);

        for(k = 0; k < numberOfPhysicalKey; k++){
            c.drawPath(diamondKeysBound[k], keysBoundPaint[k]);
        }
        int offsetY = (int)(keysPaint[0].getTextSize() / 4);
        for (i = 0; i < 33; i++) {
            switch (i) {
                case 25://Back
                    c.drawText("Bk", keysPosition[i].x, keysPosition[i].y + offsetY, keysPaint[i]);
                    break;
                case 29://cap
                    c.drawText("Cap", keysPosition[i].x, keysPosition[i].y + offsetY, keysPaint[i]);
                    break;
                case 31://space
                    //c.drawText(" ", keysPosition[i].x, keysPosition[i].y, keysPaint[i]);
                    break;
                case 32://enter
                    c.drawText("Ent", keysPosition[i].x, keysPosition[i].y + offsetY, keysPaint[i]);
                    break;
                default:
                    c.drawText(keys[i], keysPosition[i].x, keysPosition[i].y + offsetY, keysPaint[i]);
                    break;
            }
        }
    }


    //여기 작업
    public int getIDfromXY(int xid, int yid){
        if(yid < -1 || yid > 4){
            Log.d("exit", ""+yid);
        }
        if(xid < 0 || xid > 8 || yid < 0 || yid > 3) return -1;
        int result = yid * 9 + xid;
        if(result == 26){
            result = -1;
        }
        if(yid > -1) {
            if (yid == 3) {
                if (xid > 5) {
                    result = 26;
                } else {
                    result = 27;
                }
            }
        }else result = -1;
        return result;
    }
}
