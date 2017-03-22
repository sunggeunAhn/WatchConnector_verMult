package util.keyboardLayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import hcil.kaist.watchconnector_verTouchscreen.R;
import util.ScreenInfo;

/**
 * Created by PCPC on 2016-07-10.
 */
public class KeyboardLayout {
    //keyboard control params
    public boolean spaceKeyMode = true;
    public static final int KEYBOARD_SPLITBOARD = 10;
    public static final int KEYBOARD_MAXBOARD = 11;
    int keyboardMode;
    int keyboardWidth, keyboardHeight;
    int leftMargin, topMargin;
    protected Drawable[] gradientKeysBound;

    //side keyboard
    protected int[] keyDepth;
    protected boolean[] keyExistAt;

    //screen keyboard
    protected String[] qwertyKey;
    protected int qwertykeyVSize = 32;
    protected int qwertykeyHSize = 26;

    protected Rect keyboardBound;
    protected Paint boundPaint;
    int keyHeight;

    //keys
    protected int numberOfPhysicalKey = 9;
    protected int xOffset;
    protected int yOffset = ScreenInfo.screenHeight / 4;
    protected int keySize;
    protected  int gap;
    protected int numberOfKey = 27;
    protected Rect[] keysBound;
    protected Paint[] keysBoundPaint;
    protected String[] keys;
    protected Point[] keysPosition;
    protected Paint[] keysPaint;
    int yNum;
    int xNum;

    protected Path[] diamondKeysBound;

    //key control
    protected int activatedID;
    protected int focusedID;


    protected int i,k,n,m,x,y;
    public void KeyboardLayout(int screenWidth, int screenHeight){

    }

    public int getMode(){
        return keyboardMode;
    }

    public void initKeyboard(int mode){
        focusedID = -1;
        keyboardMode = mode;
        switch (mode) {
            case KEYBOARD_SPLITBOARD:
                splitboard();
                break;
            case KEYBOARD_MAXBOARD:
                maximizedBoard();
                break;
        }
    }

    public void splitboard(){
        xNum = 9;
        yNum = 3;
        numberOfPhysicalKey = 28;
        numberOfKey = 28;
        keysBound = new Rect[numberOfPhysicalKey];
        keysBoundPaint = new Paint[numberOfPhysicalKey];

        gap = 3;
        //keySize = (ScreenInfo.screenWidth - 8*gap)/9;
        keySize = (ScreenInfo.screenWidth - 8*gap)/9;
        keyHeight = (int)(keySize * 0.7);
        keyboardHeight = keyHeight * yNum + gap *(yNum - 1);
        keyboardWidth = keySize * xNum + gap * (xNum-1);
//        xOffset  = (ScreenInfo.screenWidth - keyboardWidth)/2;
//        yOffset = (int)(ScreenInfo.screenHeight - keyboardHeight*1.22);
        xOffset = 0;
        yOffset = 0;
        int leftPos, topPos;
        for(y = 0 ; y < yNum; y++){
            topPos = yOffset + (y * keyHeight) + y*gap;
            for(x = 0 ; x < xNum; x++){
                //left, top, right, bottom
                leftPos = (x * keySize) + x*gap;
                keysBound[y*xNum+ x]= new Rect(leftPos, topPos, leftPos + keySize, topPos + keyHeight);
            }
        }

        keysBound[27] = new Rect(xOffset, keysBound[25].bottom + gap, keysBound[23].right,  keysBound[25].bottom + gap + keyHeight);
        keysBound[26] = new Rect(keysBound[27].right + gap, keysBound[25].bottom + gap, keysBound[17].right,  keysBound[25].bottom + gap + keyHeight);


        for(i = 0; i < numberOfPhysicalKey; i++){
            keysBoundPaint[i]  = new Paint();
            keysBoundPaint[i].setStrokeWidth(0);
            keysBoundPaint[i].setStyle(Paint.Style.FILL_AND_STROKE);
            keysBoundPaint[i].setColor(Color.LTGRAY);

        }

        keys = new String[numberOfKey];
        keysPaint = new Paint[numberOfKey];
        keysPosition = new Point[numberOfKey];

        String keyArray = "qwertyuioasdfghjkpzxcvbnml- ";
        for(i = 0; i < numberOfKey; i++){
            keys[i] = String.valueOf((char)(keyArray.charAt(i)));

            keysPaint[i] = new Paint();
            keysPaint[i].setTextAlign(Paint.Align.CENTER);
            keysPaint[i].setColor(Color.BLACK);
            keysPaint[i].setTextSize( (float)(keysBound[i].height() * 0.9));

        }

        int margin = (int)(keyHeight - keysPaint[0].getTextSize())*3; //????
        for(i = 0; i<  numberOfKey;i++){
            keysPosition[i]= new Point(keysBound[i].centerX(), (int)(keysBound[i].centerY()+margin));
        }

        activatedID = -1;
        keyboardResize(xOffset, yOffset, 1);

    }


    public void maximizedBoard(){
        numberOfPhysicalKey = 33;
        numberOfKey = 33;

        diamondKeysBound = new Path[numberOfPhysicalKey];
        keysBoundPaint = new Paint[numberOfPhysicalKey];
        keys = new String[numberOfKey];
        keysPaint = new Paint[numberOfKey];
        keysPosition = new Point[numberOfKey];

        gap = 3;
        keyboardHeight = ScreenInfo.screenHeight /4;
        keyboardWidth = ScreenInfo.screenHeight /4;

        //just unit length for diamond shape, half diagonal, horizontal
        keySize = keyboardWidth/10;
        //just unit length for diamond shape, half diagonal, vertical
        keyHeight = keyboardHeight/10;
        xOffset = 0;
        yOffset = 0;

        int xPos, yPos;
        int idCounter = 0;

        //qetuo, id 0 ~ 4
        yPos = keyHeight;
        for(x = 1; x < 10; x = x+2){
            xPos = keySize * x;
            keysPosition[idCounter++]= new Point(xPos, yPos);
        }

        //wryi, id ~ 8
        yPos += keyHeight;
        for(x = 2; x < 10; x = x+2){
            xPos = keySize * x;
            keysPosition[idCounter++]= new Point(xPos, yPos);
        }

        //adgjl, id ~ 13
        yPos += keyHeight;
        for(x = 1; x < 10; x = x+2){
            xPos = keySize * x;
            keysPosition[idCounter++]= new Point(xPos, yPos);
        }

        //sfhk, id ~17
        yPos += keyHeight;
        for(x = 2; x < 10; x = x+2){
            xPos = keySize * x;
            keysPosition[idCounter++]= new Point(xPos, yPos);
        }

        //zcbm, id ~ 21
        yPos += keyHeight;
        for(x = 1; x < 8; x = x+2){
            xPos = keySize * x;
            keysPosition[idCounter++]= new Point(xPos, yPos);
        }

        //xvn, id ~ 24
        yPos += keyHeight;
        for(x = 2; x < 7; x = x+2){
            xPos = keySize * x;
            keysPosition[idCounter++]= new Point(xPos, yPos);
        }
        // -, id 25
        xPos = keySize * 9;
        keysPosition[idCounter++]= new Point(xPos, yPos);

        //{p@.} , id ~ 28
        yPos += keyHeight;
        for(x = 3; x < 8; x = x+2){
            xPos = keySize * x;
            keysPosition[idCounter++]= new Point(xPos, yPos);
        }
        //cap, id 29
        yPos += keyHeight;
        xPos = keySize * 1;
        keysPosition[idCounter++]= new Point(xPos, yPos);

        //{,} id 30
        xPos = keySize * 6;
        keysPosition[idCounter++]= new Point(xPos, yPos);
        //space, id 31
        yPos += keyHeight;
        xPos = keySize * 4;
        keysPosition[idCounter++]= new Point(xPos, yPos);
        //enter, id 32
        xPos = keySize * 8;
        keysPosition[idCounter++]= new Point(xPos, yPos);


        for(idCounter = 0; idCounter < numberOfPhysicalKey; idCounter++){
            diamondKeysBound[idCounter] = new Path();
            switch (idCounter){
                case 25://Back
                case 29://cap
                case 31://space
                case 32://enter
                    diamondKeysBound[idCounter].moveTo(keysPosition[idCounter].x - keySize *2, keysPosition[idCounter].y);
                    diamondKeysBound[idCounter].lineTo(keysPosition[idCounter].x , keysPosition[idCounter].y - keyHeight*2);
                    diamondKeysBound[idCounter].lineTo(keysPosition[idCounter].x + keySize*2, keysPosition[idCounter].y);
                    diamondKeysBound[idCounter].lineTo(keysPosition[idCounter].x , keysPosition[idCounter].y + keyHeight*2);
                    diamondKeysBound[idCounter].lineTo(keysPosition[idCounter].x - keySize*2, keysPosition[idCounter].y);
                    break;
                default:
                    diamondKeysBound[idCounter].moveTo(keysPosition[idCounter].x - keySize, keysPosition[idCounter].y);
                    diamondKeysBound[idCounter].lineTo(keysPosition[idCounter].x , keysPosition[idCounter].y - keyHeight);
                    diamondKeysBound[idCounter].lineTo(keysPosition[idCounter].x + keySize, keysPosition[idCounter].y);
                    diamondKeysBound[idCounter].lineTo(keysPosition[idCounter].x , keysPosition[idCounter].y + keyHeight);
                    diamondKeysBound[idCounter].lineTo(keysPosition[idCounter].x - keySize, keysPosition[idCounter].y);
                    break;
            }
        }



        for(i = 0; i < numberOfPhysicalKey; i++){
            keysBoundPaint[i]  = new Paint();
            keysBoundPaint[i].setStrokeWidth(3);
            keysBoundPaint[i].setStyle(Paint.Style.STROKE);
            keysBoundPaint[i].setColor(Color.YELLOW);

        }


        String keyArray= "qetuowryiadgjlsfhkzcbmxvn-p@.^, #";
        for(i = 0; i < numberOfKey; i++){
            keys[i] = String.valueOf((char)(keyArray.charAt(i)));

            keysPaint[i] = new Paint();
            keysPaint[i].setTextAlign(Paint.Align.CENTER);
            keysPaint[i].setColor(Color.WHITE);
            keysPaint[i].setTextSize( keyHeight);

        }

        activatedID = -1;
//        keyboardResize(xOffset, yOffset, 1);

    }
    public int getKeyboardHeight(){
        return keyboardHeight;

    }

    public int getKeyboardWidth(){
        return keyboardWidth;
    }


    public void drawKeyboard(Canvas c){
        //c.drawRect(keyboardBound, boundPaint);

        for(k = 0; k < numberOfPhysicalKey; k++){
            gradientKeysBound[k].draw(c);
        }
        if(spaceKeyMode) {
            for (i = 0; i < 26; i++) {
                c.drawText(keys[i], keysPosition[i].x, keysPosition[i].y, keysPaint[i]);
            }
            c.drawText("←", keysPosition[26].x, keysPosition[26].y, keysPaint[26]);
            c.drawText("space", keysPosition[27].x, keysPosition[27].y, keysPaint[27]);
        }else{
            for (i = 0; i < 27; i++) {
                c.drawText(keys[i], keysPosition[i].x, keysPosition[i].y, keysPaint[i]);
            }
        }
    }



    public boolean isActivated(){
        return activatedID > -1;
    }

    public void keyboardResize(float viewRatio){
        keyboardResize(xOffset, yOffset, viewRatio);
    }
    public void keyboardResize(int xOffset, int yOffset, float viewRatio){
        this.xOffset = xOffset;
        this.yOffset = yOffset;

        gap = (int)(3 * viewRatio);
        if(gap < 1) gap = 1;
        //keySize = (ScreenInfo.screenWidth - 8*gap)/9;
        keySize = (int)((ScreenInfo.screenWidth - 8*gap)/9 * viewRatio);
        if(viewRatio > 0.8) {
            keyHeight = (int) (keySize * 0.7);
        }
        else{
            keyHeight = keySize;
        }
        keyboardHeight = keyHeight * yNum + gap *(yNum - 1);
        keyboardWidth = keySize * xNum + gap * (xNum-1);

        int leftPos, topPos;
        for(y = 0 ; y < yNum; y++){
            topPos = yOffset + (y * keyHeight) + y*gap;
            for(x = 0 ; x < xNum; x++){
                //left, top, right, bottom
                leftPos = xOffset + (x * keySize) + x*gap;
                int id = y*xNum+ x;
                keysBound[id]= new Rect(leftPos, topPos, leftPos + keySize, topPos + keyHeight);

            }
        }
        keysBound[27] = new Rect(xOffset, keysBound[25].bottom + gap, keysBound[23].right,  keysBound[25].bottom + gap + keyHeight);
        keysBound[26] = new Rect(keysBound[27].right + gap, keysBound[25].bottom + gap, keysBound[17].right,  keysBound[25].bottom + gap + keyHeight);



        for(i = 0; i < numberOfKey; i++){
            keysPaint[i].setTextSize( (float)(keysBound[i].height() * 0.9));
        }
        int margin = (int)(keyHeight - keysPaint[0].getTextSize())*3;
        for(i = 0; i<  numberOfKey;i++){
            keysPosition[i]= new Point(keysBound[i].centerX(), (int)(keysBound[i].centerY()+margin));
        }
        if(gradientKeysBound != null){
            for (int i = 0; i < numberOfPhysicalKey; i++){
                gradientKeysBound[i].setBounds(keysBound[i]);
            }
        }

    }

    public void moveOffset(int x, int y){

    }

    public void setGradientKeys(Context c) {
        gradientKeysBound = new Drawable[numberOfPhysicalKey];
        for (int i = 0; i < 9; i++) {
            gradientKeysBound[i * 3 + 0] = c.getResources().getDrawable(R.drawable.gradation_rb_center);
            gradientKeysBound[i * 3 + 2] = c.getResources().getDrawable(R.drawable.gradation_rb_center);
            gradientKeysBound[i * 3 + 1] = c.getResources().getDrawable(R.drawable.gradation_rb_center);
        }
        if (spaceKeyMode){
            gradientKeysBound[26] = c.getResources().getDrawable(R.drawable.gradation_rb_center);
        }
        gradientKeysBound[27] = c.getResources().getDrawable(R.drawable.gradation_rb_center);
        for(int i = 0; i < numberOfPhysicalKey; i++){
            gradientKeysBound[i].setBounds(keysBound[i]);
            keysPaint[i].setColor(Color.WHITE);

        }
    }

}
