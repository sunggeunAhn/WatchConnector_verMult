package Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import Event.OnWatchTouchEventListener;
import Event.WatchTouchEvent_Ext;
import hcil.kaist.watchconnector_verTouchscreen.MainActivity;
import util.ScreenInfo;
import util.keyboardLayout.MaximizedKeyboard;

/**
 * Created by PCPC on 2016-07-12.
 */
public class KeyboardView extends View implements OnWatchTouchEventListener {
    //const, check from TouchSender project
    public static final int up = 2;
    public static final int down = 6;
    public static final int right = 4;
    public static final int left = 0;

    //System
    Handler mainHandler;
    Context context;

    //Cursor
    WatchTouchEvent_Ext prevEvent, currentEvent;
    protected Paint paintScreenCursor;
    protected Paint paintScreenCursor2;
    int cursorSize, cursorBoundSize;
    public Paint paintEdgeCursor;
    private Rect edgeCursor;
    private int edgeCursorSizeHalf;
    private int edgeCursorSize;

    protected int[] scrCursorPosOnKeyboard;

    //mode
    public boolean isGesture;

    //Drawing Element
    private Rect wholeScreen;
    private Paint paintWholeScreen;
    private Paint paintWait;

    //keyboard
    public MaximizedKeyboard keyboard;

    public KeyboardView(Context paramContext) {
        super(paramContext);
    }

    public KeyboardView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    public KeyboardView(Context paramContext, AttributeSet paramAttributeSet,
                        int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
    }

    public void initialize(Context c, Handler h){
        context = c;
        mainHandler = h;
        isGesture = false;
        cursorSize = ScreenInfo.screenHeight < ScreenInfo.screenWidth ? ScreenInfo.screenHeight : ScreenInfo.screenWidth;
        cursorSize = (int)(cursorSize * 0.01);
        cursorBoundSize = cursorSize * 6;
        paintScreenCursor = new Paint();
        paintScreenCursor.setColor(Color.argb(125, 55, 60, 250));
        paintScreenCursor.setStyle(Paint.Style.STROKE);
        paintScreenCursor.setStrokeWidth(cursorBoundSize);
        paintScreenCursor2 = new Paint();
        paintScreenCursor2.setColor(Color.argb(255, 55, 60, 250));
        paintScreenCursor2.setStyle(Paint.Style.FILL);

        paintEdgeCursor = new Paint();
        paintEdgeCursor.setColor(Color.argb(255, 55, 155, 255));
        paintEdgeCursor.setStyle(Paint.Style.FILL);

        scrCursorPosOnKeyboard = new int[2];

        prevEvent = new WatchTouchEvent_Ext();

        wholeScreen = new Rect(0,0,ScreenInfo.screenWidth, ScreenInfo.screenHeight);
        paintWholeScreen = new Paint();
        paintWholeScreen.setColor(Color.argb(200,0,0,0));


        keyboard = new MaximizedKeyboard(c);

    }
    @Override
    public void onDraw(Canvas c){
        keyboard.drawKeyboard(c);
        drawCursor(c);

        Thread.currentThread();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        postInvalidate();

    }

    public void changeKeyboardMode(){

    }



    public void drawCursor(Canvas c){
        if(prevEvent.state_screen > WatchTouchEvent_Ext.SCREEN_STATE_UP) {
            c.drawCircle(prevEvent.xPos + keyboard.getxOffset(), prevEvent.yPos, cursorBoundSize, paintScreenCursor);
            c.drawCircle(prevEvent.xPos + keyboard.getxOffset(), prevEvent.yPos, cursorBoundSize/6, paintScreenCursor2);

        }
    }

    @Override
    public void OnWatchTouchEvent(WatchTouchEvent_Ext e) {
        prevEvent = e;
        switch (e.state_screen){
            case WatchTouchEvent_Ext.SCREEN_STATE_DOWN:
                isGesture = false;
                keyboard.downScreen(e.xPos, e.yPos);
//                if(keyboard.isActivated()){
//                    calcScrCursorOnKeyboard(e.xPos, e.yPos);
//                }
                break;
            case WatchTouchEvent_Ext.SCREEN_STATE_UP:
                char tempK = keyboard.upScreen(e.xPos, e.yPos);
                if(tempK == '-'){
                    mainHandler.obtainMessage(MainActivity.MESSAGE_REMOVE, null).sendToTarget();
                }else if(!isGesture)
                    mainHandler.obtainMessage(MainActivity.MESSAGE_INPUT, String.valueOf(tempK)).sendToTarget();
                break;
            case WatchTouchEvent_Ext.SCREEN_STATE_MOVE:
                keyboard.moveScreen(e.xPos, e.yPos);
//                if(keyboard.isActivated()){
//                    calcScrCursorOnKeyboard(e.xPos, e.yPos);
//                }
                break;
        }
    }


    public float getKeyboardWidth(){
        return keyboard.getKeyboardWidth();
    }
    public float getXoffset(){
        return keyboard.getxOffset();
    }

    public void gestureInput(){
        isGesture = true;
        keyboard.upScreen(-1, -1);
        prevEvent.state_screen = WatchTouchEvent_Ext.SCREEN_STATE_UP;
    }

}
