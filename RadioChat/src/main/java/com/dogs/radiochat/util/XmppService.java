package com.dogs.radiochat.util;

import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**
 * Created by XRPQ48 on 12/15/13.
 */
public class XmppService {

    public static final String HOST = "dogsgroup.mooo.com";
    public static final int PORT = 5222;
    public static final String SERVICE = "example.com";
    public static final String USERNAME = "babu";
    public static final String PASSWORD = "babu";


    public void connect()
    {
        ConnectionConfiguration connConfig = new ConnectionConfiguration(HOST, PORT, SERVICE);
        XMPPConnection connection = new XMPPConnection(connConfig);

        try {
            //Connect to the server
            connection.connect();
            Log.v(this.getClass().getName(),"xmpp connection ok!!");
        } catch (XMPPException ex) {
            connection = null;
            Log.v(this.getClass().getName(),"xmpp connection failed :(");
            return;
            //Unable to connect to server
        }
        try {

            connection.login(USERNAME, PASSWORD);
            Log.v(this.getClass().getName(),"xmpp login ok!!");
        } catch (XMPPException e) {
            Log.v(this.getClass().getName(),"xmpp login failed :(");
            e.printStackTrace();
            return;
        }
        PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
        connection.addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
                Message message = (Message) packet;
                String body = message.getBody();
                String from = message.getFrom();
                Log.v(this.getClass().getName(),"Received from :" + from);
                Log.v(this.getClass().getName(),"Received msg :" + body);
            }
        }, filter);
    }

}
