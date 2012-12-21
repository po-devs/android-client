package com.pokebros.android.pokemononline;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import com.pokebros.android.pokemononline.poke.ShallowBattlePoke;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

public class PokeClientSocket {
	private SocketChannel schan = null;
	private final static String TAG = "PokeClientSocket";

	private Baos thisMsg = new Baos();
	private ByteBuffer currentData = ByteBuffer.allocate(4096);
	private LinkedList<Baos> msgs = new LinkedList<Baos>();
	int remaining = 0, dataLen = 0;
	private byte lenRead = 0;
	public final static int CONNECT_TIMEOUT=10000;

	public PokeClientSocket(String inIpAddr, int inPortNum) throws SocketTimeoutException, IOException
	{
		schan = SocketChannel.open();
		//schan.connect(new InetSocketAddress(inIpAddr, inPortNum));
		schan.socket().connect(new InetSocketAddress(inIpAddr, inPortNum), CONNECT_TIMEOUT);
//		schan.configureBlocking(false);
	}

	public boolean isConnected() { 
		boolean ret = false;
		try {
			ret = schan.finishConnect();
		} catch (IOException e) {
			System.exit(-1);
		}
		return ret;
	}

	class NetSender implements Runnable {
		Baos bytesToSend;
		
		public NetSender(Baos bytesToSend) {
			this.bytesToSend = bytesToSend;
		}
		
		public void run() {
			synchronized(this) {
				try {
					ByteBuffer b = ByteBuffer.allocate(bytesToSend.size());
					b.order(ByteOrder.BIG_ENDIAN);
					b.put(bytesToSend.toByteArray());
					b.rewind();
					schan.write(b);
				} catch (IOException e) {
					System.out.println("Caught IOException Writing To Socket Stream!");
					System.exit(-1);
				} finally {
					this.notify();
				}
			}
		}
	}
	
	public boolean sendMessage(Baos msgToSend, Command msgType) {
		Baos bytesToSend = new Baos();
		bytesToSend.putInt(msgToSend.size() + 1);
		bytesToSend.write((byte)msgType.ordinal());
		try {
			bytesToSend.write(msgToSend.toByteArray());
		} catch (IOException e) {
			System.out.println("Caught IOException Writing message");
			System.exit(-1);
		}
		
		// fuck you NetworkOnMainThreadException
		NetSender netSender = new NetSender(bytesToSend);
		synchronized(netSender) {
			new Thread(netSender).start();
			try {
				netSender.wait();
			} catch (InterruptedException e) {
				return false;
			}
		}
		return true;
	}

	public Baos getMsg() {
		// This retrieves and removes the first
		// item in the queue, returning null 
		// if there is none.
		// I know, great name, right?
		
		return msgs.poll();
	}

	public void recvMessagePoll() throws IOException, ParseException {
		Log.d(TAG, "Socket polled");
		currentData.clear();
		dataLen = schan.read(currentData);
		currentData.flip();
		// Loop while there's still data in the buffer.
		while (dataLen > 0) {
			Log.d(TAG, "dataLen " + dataLen + " lenRead " + lenRead + " remaining " + remaining);
			// Read in the message's length.
			while (lenRead < 4) {
				// If we haven't read in the length, try and get it.
				if (dataLen == 0)
					break;
				remaining |= (((int)currentData.get() & 0xff) << (8 * (3 - lenRead)));
				if (remaining < 0) {
					// Length overflowed, something has gone horribly wrong.
					throw new ParseException("The message length overflowed signed int", 0);
				}
				lenRead++;
				dataLen--;
				if (lenRead == 4) {
					Log.d(TAG, "Next message is of len " + remaining);
				}
			}

			if(remaining <= dataLen) {
				// There's enough data in the buffer to finish the current message.
				byte[] bytes = new byte[remaining];
				currentData.get(bytes, 0, remaining);
				thisMsg.write(bytes);

				// Add the read in message to the queue of
				// unprocessed messages.
				msgs.add(thisMsg);
				thisMsg = new Baos();
				dataLen -= remaining;
				remaining = 0;
				lenRead = 0; // Start the next new message
			}
			else {
				// Otherwise, read what we can and put it into
				// the incomplete message.
				byte[] bytes = new byte[dataLen];
				currentData.get(bytes, 0, dataLen);
				thisMsg.write(bytes);
				remaining -= dataLen;
				dataLen = 0;
			}
		}
	}

	public void close() {
		try {
			schan.close();
		} catch (IOException e) {
			// TODO Should this be thrown or caught?
		}
	}
}