package hcil.kaist.watchconnector_verTouchscreen;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import Event.WatchTouchEvent_Ext;
import Views.KeyboardView;
import comm.bluetooth.BluetoothChatService;
import comm.bluetooth.DeviceListActivity;
import util.ExperimentManager;
import util.Measure;
import util.PhraseSets.ExamplePhrases;
import util.ScreenInfo;

public class MainActivity extends Activity {

    private final int COMM_METHOD_BLUETOOTH = 1;
    private final int COMM_METHOD_SERIAL = 2;

    private int commMethod = COMM_METHOD_BLUETOOTH;

    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECT = 6;
    public static final int MESSAGE_VIBRATE = 7;
    public static final int MESSAGE_END = 8;
    public static final int MESSAGE_TOAST2 = 9;
    public static final int MESSAGE_PHASE = 10;
    public static final int MESSAGE_PHASE_SET_2 = 20;
    public static final int MESSAGE_TASK = 11;
    public static final int MESSAGE_EYE_CONDITION = 12;
    public static final int MESSAGE_INPUT = 20;
    public static final int MESSAGE_REMOVE = 21;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private KeyboardView gv;
    private CountDownTimer cdt;
    private boolean end = false;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;



    private Button setting, mode, modeRelativeBtn, modePositionReverseBtn;
    private boolean modeTouchUp = true; //1 finger
    private boolean modePositionReverse = false; //1 finger
    private boolean modeRelative = false; //1 finger

    private Vibrator vib;
    int actions[];
    int point[];
    int x, y, e;
    int minSize;
    int touchUpX, touchUpY;
    int appendX, appendY;

    private WatchTouchEvent_Ext event;
    private TextView inputText, exampleText, resultView;
    private float keyboardOffset;

    //Experiment
    Measure measure;
    boolean isFirstInput;
    boolean doneActivate;
    boolean isWait;
    ExamplePhrases examples;
    ExperimentManager experimentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                        | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        measure = new Measure();
        isFirstInput = true;
        doneActivate = false;
        isWait = false;
        examples = new ExamplePhrases();
        examples.readTxt(getResources().openRawResource(R.raw.phrases));
        experimentManager = new ExperimentManager(5, 5); //trial, block

        actions = new int[5];
        point = new int[4];

        setting = (Button)findViewById(R.id.setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serverIntent = null;
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        });
        mode = (Button)findViewById(R.id.mode);
        mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gv.changeKeyboardMode();
                modeTouchUp = !modeTouchUp;
                if(modeTouchUp){

                    mode.setBackgroundColor(Color.BLUE);
                    mode.setText("1 F");
                }else {
                    mode.setBackgroundColor(Color.RED);
                    mode.setText("2 F");
                }

            }
        });
        if(modeTouchUp){
            mode.setBackgroundColor(Color.BLUE);
            mode.setText("1 F");
        }else {
            mode.setBackgroundColor(Color.RED);
            mode.setText("2 F");
        }


        modeRelativeBtn = (Button)findViewById(R.id.mode2);
        modeRelativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gv.changeKeyboardMode();
                modeRelative = !modeRelative;
                if(modeRelative){

                    modeRelativeBtn.setBackgroundColor(Color.CYAN);
                    modeRelativeBtn.setText("Rltv");
                }else {
                    modeRelativeBtn.setBackgroundColor(Color.MAGENTA);
                    modeRelativeBtn.setText("Abst");
                }

            }
        });
        if(modeRelative){

            modeRelativeBtn.setBackgroundColor(Color.CYAN);
            modeRelativeBtn.setText("Rltv");
        }else {
            modeRelativeBtn.setBackgroundColor(Color.MAGENTA);
            modeRelativeBtn.setText("Abst");
        }
        modePositionReverseBtn = (Button)findViewById(R.id.mode3);
        modePositionReverseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gv.changeKeyboardMode();
                modePositionReverse = !modePositionReverse;
                if(modePositionReverse){

                    modePositionReverseBtn.setBackgroundColor(Color.YELLOW);
                    modePositionReverseBtn.setText("Rev");
                }else {
                    modePositionReverseBtn.setBackgroundColor(Color.GREEN);
                    modePositionReverseBtn.setText(" ");
                }

            }
        });
        if(modePositionReverse){

            modePositionReverseBtn.setBackgroundColor(Color.YELLOW);
            modePositionReverseBtn.setText("Rev");
        }else {
            modePositionReverseBtn.setBackgroundColor(Color.GREEN);
            modePositionReverseBtn.setText(" ");
        }
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        inputText = (TextView)findViewById(R.id.input);
        exampleText = (TextView)findViewById(R.id.example);
        resultView = (TextView)findViewById(R.id.result);

        Display mdisp = getWindowManager().getDefaultDisplay();

        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);

        minSize = mdispSize.y < mdispSize.x ? mdispSize.y : mdispSize.x;

        ScreenInfo.screenHeight = mdispSize.y;
        ScreenInfo.screenWidth = mdispSize.x;

//        ScreenInfo.screenHeight = 540;
//        ScreenInfo.screenWidth = 960;



        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        gv = (KeyboardView)findViewById(R.id.gv);
        gv.initialize(this, mHandler);
        event = new WatchTouchEvent_Ext();
        keyboardOffset = gv.getXoffset();
        ScreenInfo.widthRatio = (float) gv.getKeyboardWidth() / 280;
        ScreenInfo.heightRatio = ScreenInfo.widthRatio;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }

    }


    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the compose field with a listener for the return key


        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();

        if (D) Log.e(TAG, "--- ON DESTROY ---");
    }


    private void ensureDiscoverable() {
        if (D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }


    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_CONNECT:
                    switch (commMethod) {
                        case COMM_METHOD_SERIAL:
                            break;
                        case COMM_METHOD_BLUETOOTH:
                            Intent serverIntent = null;
                            // Launch the DeviceListActivity to see devices and do scan
                            serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                            break;
                    }
                    break;
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    //byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = String.valueOf(msg.arg1) + "\r\n";
                    Log.d("MainActivity", String.valueOf(msg.arg1));
                    mChatService.write(writeMessage.getBytes());
                    break;
                case MESSAGE_READ:
                    //long start = System.currentTimeMillis();
                    interpretPacket((byte[]) msg.obj, msg.arg1);
                    //Log.d("interpret", ""+ (System.currentTimeMillis() - start));
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    break;
                case MESSAGE_TOAST:
                    if (!end)
                        Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                                Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST2:
                    Toast.makeText(getApplicationContext(), (String) msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_VIBRATE:
                    vib.vibrate(msg.arg2);
                    break;
                case MESSAGE_END:
                    end = true;
                    vib.vibrate(100);
                    Toast.makeText(getApplicationContext(), "End!",
                            Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();
                    break;
                case MESSAGE_PHASE:
                    //gv.changeKeyboard();
                    break;
                case MESSAGE_INPUT:
                    String inputed = (String)msg.obj;

                    Log.d("input", inputed);
                    if(isFirstInput){
                        inputStart(System.currentTimeMillis());
                    }
                    inputText.append(inputed);
                    inputText.postInvalidate();
                    measure.done(inputed.charAt(0), System.currentTimeMillis());
                    break;
                case MESSAGE_REMOVE:
                    measure.done('-', System.currentTimeMillis());
                    inputText.setText(inputText.getText().subSequence(0, inputText.getText().length() - 1));
                    inputText.postInvalidate();
                    doneActivate = false;
                    break;
            }
        }
    };

    public void inputStart(long current){
        measure.start(current);
        isFirstInput = false;
    }

    public void inputDone(){
        isWait = true;
        String example = exampleText.getText().toString();
        String inputed = inputText.getText().toString();
        Log.d("example", example);
        Log.d("inputed", inputed);
        exampleText.setText("(file write)");
        inputText.setText("");
        exampleText.postInvalidate();
        inputText.postInvalidate();

        isFirstInput = true;
        int numOfInput = inputed.length();
        if(numOfInput > 0) {
            int eLen = example.length();
            int maxLen = eLen > numOfInput ? eLen : numOfInput;
            int MSD = measure.getMSD(example, inputed);
            float ER = (float)MSD * 100/ maxLen;

            measure.userInputAnalyse(maxLen);
            String result = String.format("CT: %d / WPM: %.2f / MSD: %d / ER: %.2f / TER: %.2f (CER: %.2f, UER: %.2f)", measure.getCompletionTime(), measure.getWPM(numOfInput),MSD, ER, measure.getTER(), measure.getCER(), measure.getUER());
            resultView.setText(result);
            resultView.postInvalidate();
            //isRecording = true;

        }else {
            resultView.setText("Cannot Measure performance..");
            resultView.postInvalidate();
            //isRecording = true;
        }


        Toast.makeText(this, "fileOpen", Toast.LENGTH_SHORT).show();
        measure.recordResult(inputed);
        Toast.makeText(this, "recordResult", Toast.LENGTH_SHORT).show();
//        measure.recordSideTouchEvent();
//        Toast.makeText(this, "recordSideTouchEvent", Toast.LENGTH_SHORT).show();
        measure.recordUserInput();
        Toast.makeText(this, "recordUserInput", Toast.LENGTH_SHORT).show();
        measure.recordingDone();
        Toast.makeText(this, "recordingDone", Toast.LENGTH_SHORT).show();

        //garbage collect
        //Runtime.getRuntime().gc();

        if(experimentManager.addTrial()){
            //block is done
            if(experimentManager.isDone()){
                measure.fileClose();
            }
        }

        Log.d("exampleText", "new");
        exampleText.setText(examples.getRandomPhrase());

        measure.setBlock(experimentManager.getBlock());
        measure.setTrial(experimentManager.getTrial());
        measure.recordExample(exampleText.getText().toString());
        exampleText.postInvalidate();
        isWait = false;

    }

    //from touch position on a watch to position on the glasses
    public int positionRemappingX(int raw) {
        int result = -1;
//        int ref = (int) (raw / 1000) * ScreenInfo.screenWidth / 8;
//        int inter = (int) ((raw % 1000) * ScreenInfo.widthRatio);
//        result = ref + inter;
        result = (int)(raw * ScreenInfo.widthRatio);
        if(modeRelative){
            result += appendX;
        }
        return result;
    }

    public int positionRemappingY(int raw) {
        int result = -1;
//        int ref = (int) (raw / 1000) * ScreenInfo.screenHeight / 8;
//        int inter = (int) ((raw % 1000) * ScreenInfo.heightRatio);
//        result = ref + inter;
        if(modePositionReverse){
            raw = 281 - raw;
        }else{
            raw = raw - 62;
        }
        result = (int) ((raw) * ScreenInfo.heightRatio);
        if(modeRelative){
            result += appendY;
        }
        return result;

    }

    public void interpretPacket(byte[] readBuf, int byteLength) {
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
        //long start = System.currentTimeMillis();
        String readMessage = new String(readBuf,0, byteLength);
        Log.d("msg", readMessage);
        try{
            String[] str = readMessage.split(" ");
            //public SideTouchEvent(int actions[], int point[], int x, int y)
            //Log.d("get", str[0] + ", " + str[1] + ", " + str[2]);
            switch(str[0].charAt(0)) {
                case 'y':
                    doneActivate = false;
                    if(modeTouchUp) {
                        event.state_screen = WatchTouchEvent_Ext.SCREEN_STATE_UP;
                    }else{
                        if(isFirstInput){
                            inputStart(System.currentTimeMillis());
                        }
                        measure.done(' ', System.currentTimeMillis());
                        gv.gestureInput();
                        inputText.append(" ");
                        inputText.postInvalidate();
                        doneActivate = false;
                    }
                    break;
                case 'v':
                    doneActivate = false;
                    if(!modeTouchUp) {
                        event.state_screen = WatchTouchEvent_Ext.SCREEN_STATE_UP;
                    }else {
                        event.xPos = positionRemappingX(Integer.parseInt(str[1]));
                        event.yPos = positionRemappingY(Integer.parseInt(str[2]));
                    }
                    break;
                case 'x':
                    gv.isGesture = false;
                    event.state_screen = WatchTouchEvent_Ext.SCREEN_STATE_DOWN;
                    int tempX = event.xPos;
                    int tempY = event.yPos;
                    appendX = 0;
                    appendY = 0;
                    event.xPos = positionRemappingX(Integer.parseInt(str[1]));
                    event.yPos = positionRemappingY(Integer.parseInt(str[2]));
                    if(modeRelative){
                        appendX = tempX - event.xPos ;
                        appendY = tempY - event.yPos;
                        event.xPos += appendX;
                        event.yPos += appendY;
                    }
                    break;

                case 's':
                    gv.gestureInput();
                    int what = (int) ((Integer.parseInt(str[1])));
                    //Log.d("inter", readMessage);
                    event.xPos = -1;
                    event.yPos = -1;
                    switch (what) {
                        case 0: //left
                            measure.done('-', System.currentTimeMillis());

                            inputText.setText(inputText.getText().subSequence(0, inputText.getText().length() - 1));
                            inputText.postInvalidate();
                            doneActivate = false;
                            break;
                        case 2: //right
                            if(isFirstInput){
                                inputStart(System.currentTimeMillis());
                            }
                            measure.done(' ', System.currentTimeMillis());
                            inputText.append(" ");
                            inputText.postInvalidate();
                            doneActivate = false;
                            break;
                        case 1: //up
                            if(doneActivate)
                                inputDone();
                            doneActivate = false;
                            break;
                        case 3: //down
                            doneActivate = true;
                            break;
                    }
                    break;
                case 'z':
                    event.state_screen = WatchTouchEvent_Ext.SCREEN_STATE_MOVE;
                    event.xPos = positionRemappingX(Integer.parseInt(str[1]));
                    event.yPos = positionRemappingY(Integer.parseInt(str[2]));
                    break;
                }
                gv.OnWatchTouchEvent(event);
        }

        catch(NumberFormatException e){
            Log.e("Exception", e.toString());
            Log.e("Exception", readMessage);
        }catch (Exception ee){
            Log.e("Exception", ee.toString());
            Log.e("Exception", readMessage);

        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }


}



