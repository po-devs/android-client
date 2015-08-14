package com.podevs.android.poAndroid;

import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.text.ParseException;

public class PokeClientSocket {
	// Socket channel, true connection.
	private SocketChannel schan = null;
	@SuppressWarnings("unused")
	private final static String TAG = "PokeClientSocket";
	// 4 bytes for length and 1 for type.
	private final static int OVERHEAD_SIZE = 5;
	public final static int CONNECT_TIMEOUT = 10000;

	public PokeClientSocket(String inIpAddr, int inPortNum) throws SocketTimeoutException, IOException
	{
		schan = SocketChannel.open();
		schan.socket().connect(new InetSocketAddress(inIpAddr, inPortNum), CONNECT_TIMEOUT);
	}

	public boolean isConnected() {
		try {
			return schan.finishConnect() && schan.isConnected();
		} catch (IOException e) {
			return false;
		}
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
				ByteBuffer buf;

				if (msgToSend == null) {
					// "Empty" message
					buf = ByteBuffer.allocate(OVERHEAD_SIZE);
					buf.order(ByteOrder.BIG_ENDIAN);
					buf.putInt(1);
					buf.put(msgType);
				} else {
					buf = ByteBuffer.allocate(OVERHEAD_SIZE + msgToSend.size());
					buf.order(ByteOrder.BIG_ENDIAN);
					buf.putInt(1 + msgToSend.size());
					buf.put(msgType);
					buf.put(msgToSend.toByteArray());
				}

				buf.rewind();
				schan.write(buf);
			} catch (IOException e) {
				e.printStackTrace();
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