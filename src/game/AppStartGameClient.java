package game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AppStartGameClient {
	public static void main(String[] args) {
		try {
			Socket socket = new Socket("127.0.0.1", 8081);
			final InputStream inStream = socket.getInputStream();
			OutputStream outStream = socket.getOutputStream();
			while (true) {
				try {
					Scanner in = new Scanner(System.in);
					String s = in.nextLine();
					System.out.println(s);
					if (s == null || s.equals("")) {
						continue;
					}
					Message message = AppStartGameClient.inputInfo(s);
					if (message != null) {
						AppStartGameClient.sendMessage(message, outStream);
					}

					new Thread(new Runnable() {
						@Override
						public void run() {
							Message msg = AppStartGameClient.receiveMessage(inStream);
							printMessage(msg);
						}
					}).start();

				} catch (Exception e) {

				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Message inputInfo(String s) {
		String[] array = s.split(" ");
		if (array.length % 2 == 1 && array.length >= 2) {
			Message message = new Message();
			int i = 0;
			short type = Short.parseShort(array[i++]);
			message.setType(type);
			while (i < array.length) {
				String valueType = array[i++];
				String value = array[i++];
				if (valueType.equals("-b")) {
					message.putByte(Byte.parseByte(value));
				} else if (valueType.equals("-B")) {
					message.putBoolean(Boolean.parseBoolean(value));
				} else if (valueType.equals("-s")) {
					message.putShort(Short.parseShort(value));
				} else if (valueType.equals("-i")) {
					message.putInt(Integer.parseInt(value));
				} else if (valueType.equals("-l")) {
					message.putLong(Long.parseLong(value));
				} else if (valueType.equals("-f")) {
					message.putFloat(Float.parseFloat(value));
				} else if (valueType.equals("-d")) {
					message.putDouble(Double.parseDouble(value));
				} else if (valueType.equals("-S")) {
					message.putString(value);
				} else {
					return null;
				}
			}

			return message;
		}
		return null;
	}

	private static void sendMessage(Message message, OutputStream outputStream) {
		List<Byte> buf = new ArrayList<>();

		byte[] data = message.getData();
		for (int i = 0; i < data.length; i++) {
			buf.add(data[i]);
		}

		// 加入协议头
		short type = message.getType();
		for (int i = 1; i >= 0; i--) {
			byte b = (byte) ((type >> (i * 8)) & 0xff);
			buf.add(0, b);
		}
		// 加入协议长度
		int size = buf.size();
		for (int i = 3; i >= 0; i--) {
			byte b = (byte) ((size >> (i * 8)) & 0xff);
			buf.add(0, b);
		}

		byte[] b1 = new byte[buf.size()];
		for (int i = 0; i < buf.size(); i++) {
			b1[i] = buf.get(i);
		}

		try {
			outputStream.write(b1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Message receiveMessage(InputStream inputStream) {
		List<Byte> list = new ArrayList<>();
		byte[] b = new byte[1024];
		int len = 0;
		try {
			while ((len = inputStream.read(b)) > 0) {
				for (int i = 0; i < len; i++) {
					list.add(b[i]);
				}
				int messagelen = getMessageLen(list);
				if (messagelen == (list.size() - 4)) {
					break;
				}
			}
			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int i = 0;
		byte[] lenByte = new byte[4];
		for (int j = 0; i < 4; j++, i++) {
			lenByte[3 - j] = list.get(i);
		}
		int msglen = 0;
		msglen = msglen << 0 | lenByte[0];
		msglen = msglen << 8 | lenByte[1];
		msglen = msglen << 16 | lenByte[2];
		msglen = msglen << 24 | lenByte[3];

		if (msglen != (list.size() - 4)) {
			return null;
		}

		byte[] typeByte = new byte[2];
		for (int j = 0; i < 6; j++, i++) {
			typeByte[1 - j] = list.get(i);
		}

		int typeInt = 0;
		typeInt = (typeInt << 0) | (typeByte[0] & 0xff);
		typeInt = (typeInt << 8) | (typeByte[1] & 0xff);
		short typeShort = (short) typeInt;

		byte[] dataByte = new byte[list.size() - 6];
		for (int j = 0; i < list.size(); j++, i++) {
			dataByte[j] = list.get(i);
		}

		Message message = new Message();
		message.setType(typeShort);
		message.setData(dataByte);

		return message;

	}

	private static int getMessageLen(List<Byte> list) {
		int i = 0;
		byte[] lenByte = new byte[4];
		for (int j = 0; i < 4; j++, i++) {
			lenByte[3 - j] = list.get(i);
		}
		int msglen = 0;
		msglen = msglen << 0 | lenByte[0];
		msglen = msglen << 8 | lenByte[1];
		msglen = msglen << 16 | lenByte[2];
		msglen = msglen << 24 | lenByte[3];
		return msglen;
	}

	private static void printMessage(Message message) {
		System.out.println("=================" + message.getType() + "=====================");
		while (!message.isEnd()) {
			int dataType = message.readByte();
			switch (dataType) {
			case 1:
				System.out.println("int:" + message.readInt());
				break;
			case 2:
				System.out.println("string:" + message.readString());
				break;
			case 3:
				System.out.println("short:" + message.readShort());
				break;
			case 4:
				System.out.println("long:" + message.readLong());
				break;
			case 5:
				System.out.println("boolean:" + message.readBoolean());
				break;
			case 6:
				System.out.println("byte:" + message.readByte());
				break;
			case 8:
				System.out.println("double:" + message.readDouble());
				break;
			}

		}
		System.out.println("===========================================");
	}
}
