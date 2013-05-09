/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fer.labust.diverassistant;


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fer.labust.diverassistant.MainActivity.CommunicationSectionFragment;

import labust.tritech.MmcMessage;
import labust.tritech.MtMessage;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothChatService {
    // Debugging
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_INSECURE =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;

    private ConnectThread mConnectThread;
    private List<ConnectedThread> mConnectedThreads;

    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothChatService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        mConnectedThreads = new ArrayList<BluetoothChatService.ConnectedThread>();
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
     // Cancel any thread currently running a connection
        for (ConnectedThread connectedThread : mConnectedThreads) {
        	connectedThread.cancel();
        	connectedThread = null;
        }
        
//        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_LISTEN);
       
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */

    

    public synchronized void connect(BluetoothDevice device, boolean secure, DeviceType deviceType) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
//        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure, deviceType);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType, DeviceType deviceType) {
        if (D) Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        

        // Start the thread to manage the connection and perform transmissions
        ConnectedThread mConnectedThread = new ConnectedThread(socket, socketType, deviceType);
        mConnectedThread.start();
        mConnectedThreads.add(mConnectedThread);

        // Send the name of the connected device back to the UI Activity
//        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_NAME);
//        Bundle bundle = new Bundle();
//        bundle.putString(BluetoothChat.DEVICE_NAME, device.getName());
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        for(ConnectedThread connectedThread : mConnectedThreads){
        	if(connectedThread != null){
        		connectedThread.cancel();
        		connectedThread = null;
        	}
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out, String message) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = null;
            for(ConnectedThread connectedThread : mConnectedThreads){
            	if(connectedThread.deviceType == DeviceType.COMMUNICATION){
            		r = connectedThread;
            	}
            }
            
        }
        // Perform the write unsynchronized
        r.write(out, message);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
//        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        bundle.putString(BluetoothChat.TOAST, "Unable to connect device");
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothChatService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
//        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        bundle.putString(BluetoothChat.TOAST, "Device connection was lost");
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothChatService.this.start();
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;
        private DeviceType deviceType;

        public ConnectThread(BluetoothDevice device, boolean secure, DeviceType deviceType) {
        	this.deviceType = deviceType;
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }
        
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType, deviceType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private DeviceType deviceType;
      
        public ConnectedThread(BluetoothSocket socket, String socketType, DeviceType deviceType){
        	 Log.d(TAG, "create ConnectedThread: " + socketType);
             mmSocket = socket;
             InputStream tmpIn = null;
             OutputStream tmpOut = null;

             // Get the BluetoothSocket input and output streams
             try {
                 tmpIn = socket.getInputStream();
                 tmpOut = socket.getOutputStream();
             } catch (IOException e) {
                 Log.e(TAG, "temp sockets not created", e);
             }

             mmInStream = tmpIn;
             mmOutStream = tmpOut;
             this.deviceType = deviceType;
        }
        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    //bytes = mmInStream.read(buffer);
                	MmcMessage msg = sync(mmInStream);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(CommunicationSectionFragment.MESSAGE_READ, -1, -1, msg)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    BluetoothChatService.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer, String message) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(CommunicationSectionFragment.MESSAGE_WRITE, -1, -1, message.getBytes())
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }

		private MmcMessage sync(InputStream in) throws IOException
		{
			try
			{
				//system.out.println("In sync.");
				byte[] ringBuffer = new byte[7];      					
				readFull(in,ringBuffer,0,7);
				
				boolean readSingle = true;
				
				try
				{
					while (true)
					{						
						/*System.out.print("Ring buffer:");
						for (int i=0; i<7; ++i)	System.out.print(ringBuffer[i] + ",");
						System.out.println("");*/	
						
						if (ringBuffer[0] == '@')
						{	
							try
							{
							  int hexLen=Integer.parseInt(new String(ringBuffer).substring(1,5), 16);
							  int binLen = ByteBuffer.wrap(ringBuffer,5,2).order(ByteOrder.LITTLE_ENDIAN).getShort();
							  							  						  
							  if ((binLen == hexLen) && (binLen != 0))
							  {
								  readSingle = false;
								  //system.out.println("Msg size:" + hexLen + "," + binLen);
								  MtMessage msg = new MtMessage();
								  byte[] data=new byte[binLen];
								  data[0] = ringBuffer[5];
								  data[1] = ringBuffer[6];
								  readFull(in,data,2,binLen-2);
								  
								  System.out.print("Data buffer:");
								  for (int i=0; i<data.length; ++i)	System.out.print(data[i] + ",");
								  //system.out.println("");	
								  							  
								  msg.deserialize(data);
								  
								  if (msg.msgType == 4)
								  {
								  }
								  
								  if (msg.msgType == 79)
								  {
									  MmcMessage mmsg = new MmcMessage();
									  mmsg.deserialize(msg.payload);
									  
									  return mmsg;
									  //System.out.println("Modem data:" + (mmsg.payload[0] & 0xFF));									  
								  }
								  
								  //system.out.println("Msg type:" + msg.msgType);	
								  
								  byte[] val = new byte[1];
								  readFull(in,val,0,1);
							  }
							}
							catch (NumberFormatException e){};
						}
						
						if (readSingle)
						{
							for (int i=0; i<6; ++i) ringBuffer[i]=ringBuffer[i+1];
							readFull(in,ringBuffer,6,1);
						}
						else
						{
							readFull(in,ringBuffer,0,7);
							readSingle = true;
						}
							
					}
				}
				catch (EOFException e){throw e;}
				
			}
			catch (IOException e)
			{
				e.printStackTrace();
				throw e;
			}
			}

		private int readFull(InputStream in, byte[] val, int offset, int len) throws IOException
		{
			int read=0;
			try
			{
			  while (read < len) read += in.read(val,read+offset,len-read);
			}
			catch (IOException e){throw e;};
								
			return read;
		}
    }
}
