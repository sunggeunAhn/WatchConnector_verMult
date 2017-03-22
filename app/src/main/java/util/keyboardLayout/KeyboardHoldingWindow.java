package util.keyboardLayout;

import android.content.Context;
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
 * Created by PCPC on 2016-08-04.
 */
public class KeyboardHoldingWindow extends KeyboardLayout {

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
    public KeyboardHoldingWindow(Context c){
        initKeyboard(KEYBOARD_SPLITBOARD);

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

        keyboardRatio = 0.6f;

        windowSize *= keyboardRatio;
        int xoff = (int)(ScreenInfo.screenWidth -keyboardWidth * keyboardRatio) / 2;
        int temp = ScreenInfo.screenWidth/2 - windowSize/2;

        xOffset1  = ScreenInfo.screenWidth/2  - windowSize * 5/ 2 - gap;
        xOffset1  = ScreenInfo.screenWidth/2  - windowSize * 5/ 2 - gap;
        //xOffset1 = 0;
        xOffset2 = xOffset1 + windowSize + gap / 2;
        xOffset3 = xOffset2 + windowSize + gap / 2;
        xOffset = xOffset2;
        keyboardResize(keyboardRatio);
        window.set(temp, window.top, temp + windowSize, window.bottom);

        line1X = window.left + gap + keySize;
        line2X = line1X + gap + keySize;
        line1Y = window.top+ gap + keySize;
        line2Y = line1Y + gap + keySize;


        cursorYoffset = 0;

        initGuideArrow();


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

        setGradientKeys(c);
        for(int i = 0; i < 27; i++){
            gradientKeysBound[i].setAlpha(baseAlphaKey);
        }
        gradientKeysBound[26].setAlpha(baseAlphaSpace);
        gradientKeysBound[27].setAlpha(baseAlphaSpace);
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
                    gradientKeysBound[id].setAlpha(alpha);
                    try {
                        Thread.currentThread();
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                gradientKeysBound[id].setAlpha(threshold);
                return null;
            }
        }.execute();
    }
    public void initGuideArrow(){

        int len = windowSize / 4;
        leftArr = new Path();
        leftArr.moveTo(window.centerX() - len, window.bottom+gap);
        leftArr.lineTo(window.left, window.bottom + gap + keySize / 4);
        leftArr.lineTo(window.centerX() - len, window.bottom + gap + keySize / 2);
        leftArr.lineTo(window.centerX() - len, window.bottom + gap + keySize / 2 - keySize / 8);
        leftArr.lineTo(window.centerX() - len / 3, window.bottom + gap + keySize/2 - keySize/8);
        leftArr.lineTo(window.centerX() - len/3, window.bottom + gap + keySize/8);
        leftArr.lineTo(window.centerX() - len, window.bottom + gap + keySize/8);
        leftArr.lineTo(window.centerX() - len, window.bottom + gap);

        rightArr = new Path();
        rightArr.moveTo(window.centerX() + len, window.bottom + gap);
        rightArr.lineTo(window.right, window.bottom + gap + keySize / 4);
        rightArr.lineTo(window.centerX() + len, window.bottom + gap + keySize / 2);
        rightArr.lineTo(window.centerX() + len, window.bottom + gap + keySize / 2 - keySize / 8);
        rightArr.lineTo(window.centerX() + len / 3, window.bottom + gap + keySize / 2 - keySize / 8);
        rightArr.lineTo(window.centerX() + len / 3, window.bottom + gap + keySize / 8);
        rightArr.lineTo(window.centerX() + len, window.bottom + gap + keySize / 8);
        rightArr.lineTo(window.centerX() + len, window.bottom+gap);

        paintArrowL = new Paint();
        paintArrowL.setStrokeWidth(gap);
        paintArrowL.setColor(Color.RED);
        paintArrowL.setStyle(Paint.Style.FILL);
        paintArrowR = new Paint();
        paintArrowR.setStrokeWidth(gap);
        paintArrowR.setColor(Color.RED);
        paintArrowR.setStyle(Paint.Style.FILL);


        leftGuideText = "Delete";
        rightGuideText = "Space";

        guideTextL = new Point(window.centerX() - len/3 - 2*gap, window.bottom+gap + keySize/4 + keySize/8 - gap *2);
        guideTextR = new Point(window.centerX() + len/3 + 2*gap, window.bottom+gap + keySize/4 + keySize/8 - gap*2);
        paintArrowL_text = new Paint();
        paintArrowL_text.setTextSize(keySize / 3);
        paintArrowL_text.setColor(Color.WHITE);
        paintArrowL_text.setTextAlign(Paint.Align.RIGHT);
        paintArrowR_text = new Paint();
        paintArrowR_text.setTextSize(keySize / 3);
        paintArrowR_text.setColor(Color.WHITE);
        paintArrowR_text.setTextAlign(Paint.Align.LEFT);

    }



    public void setKeyLayoutDefault(){

        String keyArray = "qwertyuioasdfghjkpzxcvbnml.";
        for(i = 0; i < numberOfKey; i++){
            keys[i] = String.valueOf((char)(keyArray.charAt(i)));

        }
    }
    public void setKeyLayoutUFO(){

        String keyArray = "ufotheqvwbpmarsx.zljyingcdk";
        for(i = 0; i < numberOfKey; i++){
            keys[i] = String.valueOf((char)(keyArray.charAt(i)));

        }
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
                if(temp > 25)
                    gradientKeysBound[temp].setAlpha(baseAlphaSpace);
                else
                    gradientKeysBound[temp].setAlpha(baseAlphaKey);
                //gradientKeysBound[temp].clearColorFilter();
            }catch (Exception e){
                focusedID = -1;
            }
            try {
                focusedID = prevScrID;
                keysPaint[focusedID].setColor(Color.RED);
                gradientKeysBound[focusedID].setAlpha(255);
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
                if(focusedID > 25)
                    gradientKeysBound[focusedID].setAlpha(baseAlphaSpace);
                else
                    gradientKeysBound[focusedID].setAlpha(baseAlphaKey);
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
            gradientKeysBound[focusedID].setAlpha(255);
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
