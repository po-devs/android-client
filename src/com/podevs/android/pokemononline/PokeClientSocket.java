package com.podevs.android.pokemononline;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.text.ParseException;

import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;

public class PokeClientSocket {
	// Socket channel, true connection.
	private SocketChannel schan = null;
	@SuppressWarnings("unused")
	private final static String TAG = "PokeClientSocket";
	public final static int CONNECT_TIMEOUT = 10000;

	public PokeClientSocket(String inIpAddr, int inPortNum) throws SocketTimeoutException, IOException
	{
		schan = SocketChannel.open();
		schan.socket().connect(new InetSocketAddress(inIpAddr, inPortNum), CONNECT_TIMEOUT);
	}

	public boolean isConnected() {
		boolean ret = false;
		try {
			ret = schan.finishConnect() && schan.isConnected();
		} catch (IOException e) {
			return false;
		}
		return ret;
	}

	class NetSender implements Runnable {
		Baos msgToSend;
		byte msgType;
		
		public NetSender(Baos msgToSend, int msgType) {
			this.msgToSend = msgToSend;
			this.msgType = (byte)msgType;
		}
		
		public void run() {
			try {
		        Baos bytesToSend = new Baos();
		        if (msgToSend == null) {
		            // Empty message, just need byte for msgType.
		            bytesToSend.putInt(1);
		        } else {
		            bytesToSend.putInt(msgToSend.size() + 1);
		        }
		        bytesToSend.write(msgType);
		        
		        try {
		            bytesToSend.write(msgToSend.toByteArray());
		        } catch (IOException e) {
		            e.printStackTrace();
		            try {
		                bytesToSend.close();
		            } catch (IOException e1) {
		                e1.printStackTrace();
		            }
		        }
		        
				ByteBuffer b = ByteBuffer.allocate(bytesToSend.size());
				b.order(ByteOrder.BIG_ENDIAN);
				b.put(bytesToSend.toByteArray());
				b.rewind();
				schan.write(b);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
	public void sendMessage(Baos msgToSend, Command msgType) {
		new Thread(new NetSender(msgToSend, msgType.ordinal())).start();
	}

	/**
	 * Reads exactly one message from the socket and returns it as a Bais.
	 * 
	 * @throws IOException forwarded from the socket functions
	 * @throws ParseException if the server sent a packet too big for us to handle
	 */
	public Bais getMsg() throws IOException, ParseException {
	    ByteBuffer packetLength = ByteBuffer.allocate(4);
        ByteBuffer data = ByteBuffer.allocate(4096);

        while (packetLength.position() < packetLength.capacity()) {
            schan.read(packetLength);
        }
        packetLength.rewind();
        int remaining = packetLength.getInt();

        if (remaining < 0) {
            throw new ParseException("The message length overflowed the length of a signed int", 0);
        }

        Baos msg = new Baos();
        while (remaining > 0) {
		    // Don't read more than this message.
            data.clear();
            data.limit(Math.min(remaining, data.capacity()));
            schan.read(data);
            remaining -= data.position();

            data.flip();
            msg.write(data.array(), 0, data.limit());
        }
        
        Bais ret = new Bais(msg.toByteArray());
        msg.close();
        return ret;
	}

	public void close() {
		try {
			schan.close();
		} catch (IOException e) {
			// TODO Should this be thrown or caught?
		}
	}
}