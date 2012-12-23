package com.pokebros.android.pokemononline;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.LinkedList;

import android.util.Log;

public class PokeClientSocket {
	/* Socket channel, true connection */
	private SocketChannel schan = null;
	private final static String TAG = "PokeClientSocket";

	private Baos thisMsg = new Baos();
	/* Length of the packet to come. Stored there before being put in remainingLen */
	private ByteBuffer packetLength = ByteBuffer.allocate(4);
	private int remainingLen = 0;
	private boolean readingPacket;
	private ByteBuffer currentData = ByteBuffer.allocate(4096);
	private LinkedList<Baos> msgs = new LinkedList<Baos>();
	private int dataLen = 0;
	public final static int CONNECT_TIMEOUT=10000;

	public PokeClientSocket(String inIpAddr, int inPortNum) throws SocketTimeoutException, IOException
	{
		schan = SocketChannel.open();
		//schan.connect(new InetSocketAddress(inIpAddr, inPortNum));
		schan.socket().connect(new InetSocketAddress(inIpAddr, inPortNum), CONNECT_TIMEOUT);
 		schan.configureBlocking(false);
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
		/* Retrieves next message in queue or null if none */
		
		return msgs.poll();
	}

	public void recvMessagePoll() throws IOException, ParseException {
		//Log.d(TAG, "Socket polled");
		if (readingPacket) {
			dataLen = schan.read(currentData);
		} else {
			dataLen = schan.read(packetLength);
		}
		// Loop while there's still data in the buffer.
		while (dataLen > 0) {
			Log.d(TAG, "read " + dataLen + " bytes");
			/* We're trying to get the length of a new packet */
			if (!readingPacket) {
				/* Wait until we get the length of a packet */
				if (packetLength.position() < 4) {
					/* Do nothing */
				} else {
					packetLength.rewind();
					remainingLen = packetLength.getInt();
					packetLength.clear();
					
					Log.d(TAG, "packet length determined: " + remainingLen + " bytes");
					
					if (remainingLen == 0) {
						msgs.add(new Baos());
					} else if (remainingLen < 0) {
						throw new ParseException("The message length overflowed the length of a signed int", 0);
					} else {
						readingPacket = true;
						currentData.limit(Math.min(remainingLen, currentData.capacity()));
					}
				}
			} else {
				/* We're reading packet data */
				remainingLen -= dataLen;
				currentData.flip();
				byte[] bytes = new byte[dataLen];
				currentData.get(bytes, 0, dataLen);
				thisMsg.write(bytes);
				currentData.clear();

				/* Did we read it all? */
				if (remainingLen == 0) {
					Log.d(TAG, "Packet fully read");
					msgs.add(thisMsg);
					thisMsg = new Baos();
					
					readingPacket = false;
				} else {
					currentData.limit(Math.min(remainingLen, currentData.capacity()));
				}
			}

			if (readingPacket) {
				dataLen = schan.read(currentData);
			} else {
				dataLen = schan.read(packetLength);
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