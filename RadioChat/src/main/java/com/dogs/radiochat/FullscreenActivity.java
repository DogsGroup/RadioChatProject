package com.dogs.radiochat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.dogs.radiochat.util.SystemUiHider;
import com.dogs.radiochat.util.XmppService;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static android.media.MediaPlayer.OnPreparedListener;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private static Boolean playing = new Boolean(Boolean.FALSE);
    private static Boolean initDone = new Boolean(Boolean.FALSE);
    private static Boolean radioCtrlMsgRcvd = new Boolean(Boolean.FALSE);

    private static String streamUrl = null;
    private static MediaPlayer mMediaPlayer = null;//new MediaPlayer(); // initialize it here
    private Context context;
    private static ProgressDialog pd;
    private static EditText urlTextBox;
    private static ListView srcStreamListView;
    private static TextView statusText;

    private Handler mHandler = new Handler();

    public static  XmppService xmppconnect;
    public static XMPPConnection xmppconnection;
    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> messages = new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> srcStreamAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        context = this;
        //urlTextBox = (EditText)findViewById(R.id.directUrlTextBox);
        statusText = (TextView)findViewById(R.id.statusText);
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        xmppconnect = new XmppService();
        srcStreamListView = (ListView)findViewById(R.id.ui_streamSrcList);

        srcStreamAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                messages);
        srcStreamListView.setAdapter(srcStreamAdapter);
        //srcStreamList.add(0, "http://dogsgroup.mooo.com:8000/ashok.mp3");
        //srcStreamList.add(1,"http://s6.myradiostream.com:5804");
        /*
        srcStreamListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                urlTextBox.setText(srcStreamList.get(i));
            }
        });
        */
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        //Connect xmpp
        xmppconnect();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    public void playStream(View view)
    {
        if (streamUrl != null)
            streamMusic(streamUrl);
        else{
            Intent intent = new Intent(this, NetworkService.class);
            startActivityForResult(intent,0);
        }

    }

    public void connectToServer(View view)
    {
        xmppconnect();
        Intent intent = new Intent(this, NetworkService.class);
        startActivityForResult(intent, 0);
    }

    public void directLinkServer(View view)
    {
        if (urlTextBox.getText().length() > 5)
        {
            streamUrl = urlTextBox.getText().toString();
            streamMusic(streamUrl);
        }
    }

    public void connectChatWindow(View view)
    {
        xmppconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intentResult) {

        String Result = intentResult.getStringExtra("RESULT");
        streamUrl = Result;
        streamMusic(Result);
        Log.v(this.getClass().getSimpleName(),"Got IT ! gotcha !!!!" + Result);
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }
        }
    }

    public void stopMusic()
    {
        if ( mMediaPlayer != null) {
            if ( mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            statusText.setText("status:Music stopped");
        }

    }

    public void streamMusic(String url)
    {
     if (url == null || url.length() < 4)
            return;

      if ( url.compareTo("0") == 0) {
            AlertDialog alertDialog1 = new AlertDialog.Builder(
                    FullscreenActivity.this).create();

            // Setting Dialog Title
            alertDialog1.setTitle("Server Connection");

            // Setting Dialog Message
            alertDialog1.setMessage("Oops! No one is streaming");


            alertDialog1.show();

            return;
        }
        if (mMediaPlayer == null){

            Log.v(this.getClass().getName(),"Opening" + url);
            pd = new ProgressDialog(context);
            pd.setTitle("Streaming...");
            pd.setMessage("Please wait.");
            pd.setCancelable(true);
            pd.setIndeterminate(true);
            pd.show();
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mMediaPlayer.setDataSource(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                    if(pd !=null)
                        pd.dismiss();
                    AlertDialog alertDialog1 = new AlertDialog.Builder(
                            FullscreenActivity.this).create();

                    // Setting Dialog Title
                    alertDialog1.setTitle("Playing...");

                    // Setting Dialog Message
                    alertDialog1.setMessage("Oops! No one is streaming");
                    alertDialog1.show();
                    mMediaPlayer = null;

                    return false;
                }
            });
            mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    pd.dismiss();
                    statusText.setText("status: Playing..");
                }
            });
           mMediaPlayer.prepareAsync();
        }
        else if ( mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            statusText.setText("status: Stopped");
            return;
        }
        else {
            mMediaPlayer.start();
        }
        Log.v(this.getLocalClassName(), "button over");
    }


    /**
     * Called by Settings dialog when a connection is establised with
     * the XMPP server
     */
    public void setConnection(XMPPConnection connection) {
        this.xmppconnection = connection;
        if (connection != null) {
            // Add a packet listener to get messages sent to us
            PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
            connection.addPacketListener(new PacketListener() {
                @Override
                public void processPacket(Packet packet) {
                    String msg;
                    String cmd;
                    String url;
                    Message message = (Message) packet;
                    if (message.getBody() != null) {
                        String fromName = StringUtils.parseBareAddress(message.getFrom());
                        Log.i("XMPPChatDemoActivity ", " Text Recieved " + message.getBody() + " from " +  fromName);
                        messages.add(fromName + ":");
                        messages.add(message.getBody());
                        msg = message.getBody();
                        if ( msg.startsWith("@CTRL@")) {
                            Log.v(this.getClass().getName(),"Recevied CTRL message");
                            cmd = msg.substring(6,7);
                            Log.v(this.getClass().getName(),"Recevied = " + cmd);
                            if (cmd.compareTo("0") == 0){
                                Log.v(this.getClass().getName(),"Recevied START message");
                                //start the music
                                streamUrl = msg.substring(7);
                                radioCtrlMsgRcvd = Boolean.TRUE;
                            }
                            else {
                                Log.v(this.getClass().getName(),"Recevied STOP message");
                                stopMusic();
                            }
                        }
                        // Add the incoming message to the list view
                        mHandler.post(new Runnable() {
                            public void run() {
                                setListAdapter();
                                if ( radioCtrlMsgRcvd == Boolean.TRUE ) {
                                    radioCtrlMsgRcvd = Boolean.FALSE;
                                    stopMusic();
                                    streamMusic(streamUrl);
                                }
                            }
                        });
                    }
                }
            }, filter);
        }
    }

    private void setListAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages);
        srcStreamListView.setAdapter(adapter);
    }

    public void xmppconnect() {

         final String HOST = "dogsgroup.mooo.com";
         final int PORT = 5222;
         final String SERVICE = "example.com";
         final String USERNAME = "babu";
         final String PASSWORD = "babu";

        final ProgressDialog dialog = ProgressDialog.show(this, "Connecting...", "Please wait...", false);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Create a connection
                ConnectionConfiguration connConfig = new ConnectionConfiguration(HOST, PORT, SERVICE);
                XMPPConnection connection = new XMPPConnection(connConfig);
                try {
                    connection.connect();
                    Log.i("XMPPChatDemoActivity",  "[SettingsDialog] Connected to "+connection.getHost());
                } catch (XMPPException ex) {
                    Log.e("XMPPChatDemoActivity",  "[SettingsDialog] Failed to connect to "+ connection.getHost());
                    Log.e("XMPPChatDemoActivity", ex.toString());
                    setConnection(null);
                }
                try {
                    connection.login(USERNAME, PASSWORD);
                    Log.i("XMPPChatDemoActivity",  "Logged in as" + connection.getUser());

                    // Set the status to available
                    Presence presence = new Presence(Presence.Type.available);
                    connection.sendPacket(presence);
                    setConnection(connection);

                    Roster roster = connection.getRoster();
                    Collection<RosterEntry> entries = roster.getEntries();
                    for (RosterEntry entry : entries) {

                        Log.d("XMPPChatDemoActivity",  "--------------------------------------");
                        Log.d("XMPPChatDemoActivity", "RosterEntry " + entry);
                        Log.d("XMPPChatDemoActivity", "User: " + entry.getUser());
                        Log.d("XMPPChatDemoActivity", "Name: " + entry.getName());
                        Log.d("XMPPChatDemoActivity", "Status: " + entry.getStatus());
                        Log.d("XMPPChatDemoActivity", "Type: " + entry.getType());
                        Presence entryPresence = roster.getPresence(entry.getUser());

                        Log.d("XMPPChatDemoActivity", "Presence Status: "+ entryPresence.getStatus());
                        Log.d("XMPPChatDemoActivity", "Presence Type: " + entryPresence.getType());

                        Presence.Type type = entryPresence.getType();
                        if (type == Presence.Type.available)
                            Log.d("XMPPChatDemoActivity", "Presence AVIALABLE");
                        Log.d("XMPPChatDemoActivity", "Presence : " + entryPresence);
                        statusText.setText("status:Chat online");
                    }
                } catch (XMPPException ex) {
                    Log.e("XMPPChatDemoActivity", "Failed to log in as "+  USERNAME);
                    Log.e("XMPPChatDemoActivity", ex.toString());
                    statusText.setText("status:Chat server failed");
                    setConnection(null);
                }
                dialog.dismiss();
            }
        });
        t.start();
        dialog.show();
    }
}
