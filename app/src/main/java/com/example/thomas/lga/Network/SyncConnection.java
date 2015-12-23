package com.example.thomas.lga.Network;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 02.12.2015.
 */
public class SyncConnection
{
    public static final int Port = 3245;
    public static final String LogKey = "SyncConnection";
    private static SyncConnection instance;
    private boolean connected;
    private ServerSocket serverSocket;
    private Socket clientSocket;

    private MessageHandler messageHandler;
    private boolean listening;

    public static int readMessage(InputStream is, ByteBuffer buffer) throws IOException
    {
        read(is, buffer, 0, 4);

        int length = buffer.getInt();
        Log.d(LogKey, "totalLength: " + length);

        read(is, buffer, 4, length - 4);

        buffer.position(4);
        return length;
    }

    private static void read(InputStream is, ByteBuffer buffer, int offset, int length) throws IOException
    {
        int readSoFar = 0;
        while (readSoFar < length)
        {
            int bytesRead = is.read(buffer.array(), offset + readSoFar, length - readSoFar);
            if (bytesRead <= 0)
            {
                Log.d(LogKey, "No more bytes could be read");
                throw new IOException();
            }

            readSoFar += bytesRead;
        }
    }

    public static SyncConnection getInstance()
    {
        if (instance == null)
        {
            instance = new SyncConnection();
        }

        return instance;
    }

    public boolean connect(String ip) throws IOException
    {
        if (connected)
        {
            Log.d(LogKey, "Already connected");
            return true;
        }
        Log.d(LogKey, "Trying to connect to " + ip);
        clientSocket = new Socket(ip, Port);
        Log.d(LogKey, "Connected to " + ip);
        connected = true;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                listen();
            }
        }).start();
        return connected;
    }

    public boolean isConnected()
    {
        return connected;
    }

    public boolean sendMessage(Message msg)
    {
        if (clientSocket == null)
        {
            return false;
        }

        try
        {
            OutputStream outputStream = clientSocket.getOutputStream();
            byte[] bytes = msg.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(4 + bytes.length);
            buffer.putInt(bytes.length + 4);
            buffer.put(bytes);
            outputStream.write(buffer.array());
            Log.d(LogKey, "Sent Message: " + msg.toString() + ", length: " + msg.getSize());
            return true;

        } catch (IOException e)
        {
            e.printStackTrace();
            connected = false;
            return false;
        }
    }

    public void setMessageHandler(MessageHandler messageHandler)
    {
        this.messageHandler = messageHandler;
    }

    private void listen()
    {
        listening = true;
        try
        {
            InputStream is = clientSocket.getInputStream();
            //Get message from the server
            InputStreamReader isr = new InputStreamReader(is);

            while (listening)
            {
                ByteBuffer buffer = ByteBuffer.allocate(30000);
                int length = readMessage(is, buffer);
                Log.d(LogKey, "Received bytes: " + length);
                Message msg = MessageParser.parse(buffer);
                Log.d(LogKey, "Received Message: " + msg.toString());
                if (messageHandler != null)
                {
                    messageHandler.handleMessage(msg);
                }
            }
        } catch (Exception exception)
        {
            exception.printStackTrace();
            Log.d(LogKey, "Stop listening");
            connected = false;
        }

        connected = false;

    }

    public void startListening()
    {
        if (connected)
        {
            return;
        }
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    serverSocket = new ServerSocket(Port);
                    while (!connected)
                    {
                        System.out.println("Waiting for Client...");
                        clientSocket = serverSocket.accept();
                        Log.d(LogKey, "Connected");
                        connected = true;
                        listen();
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                    clientSocket = null;
                }
            }
        }).start();

    }
}
