package game;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Message {
	private short type;
	private List<Byte> buf = new ArrayList<>();
	private static final Charset charset = Charset.forName("UTF-8");
	private int position = 0;

	public void setType(short type) {
		this.type = type;
	}

	private void writeByte(byte v) {
		buf.add(v);
	}

	public void putByte(byte v) {
		this.writeByte((byte) 6);
		this.writeByte(v);
	}

	public void putShort(short v) {
		this.writeByte((byte) 3);
		for (int i = 0; i < 2; i++) {
			byte b = (byte) ((v >> (i * 8)) & 0xff);
			this.writeByte(b);
		}
	}

	public void putInt(int v) {
		this.writeByte((byte) 1);
		for (int i = 0; i < 4; i++) {
			byte b = (byte) ((v >> (i * 8)) & 0xff);
			this.writeByte(b);
		}
	}

	public void putLong(long v) {
		this.writeByte((byte) 4);
		for (int i = 0; i < 8; i++) {
			byte b = (byte) ((v >> (i * 8)) & 0xff);
			this.writeByte(b);
		}
	}

	public void putFloat(float v) {
		int temp = Float.floatToRawIntBits(v);
		for (int i = 0; i < 4; i++) {
			byte b = (byte) ((temp >> (i * 8)) & 0xff);
			this.writeByte(b);
		}
	}

	public void putDouble(double v) {
		this.writeByte((byte) 8);
		long temp = Double.doubleToLongBits(v);
		for (int i = 0; i < 8; i++) {
			byte b = (byte) ((temp >> (i * 8)) & 0xff);
			this.writeByte(b);
		}
	}

	public void putBoolean(boolean v) {
		this.writeByte((byte) 5);
		byte b = (byte) (v ? 1 : 0);
		this.writeByte(b);
	}

	public void putString(String s) {
		this.writeByte((byte) 2);

		int size = s.length();
		for (int i = 0; i < 4; i++) {
			byte b = (byte) ((size >> (i * 8)) & 0xff);
			this.writeByte(b);
		}

		byte[] b = s.getBytes();
		for (int i = 0; i < b.length; i++) {
			this.writeByte(b[i]);
		}
	}

	public byte[] getData() {
		byte[] b = new byte[buf.size()];
		for (int i = 0; i < b.length; i++) {
			b[i] = buf.get(i);
		}
		return b;
	}

	public void setData(byte[] b) {
		this.buf.clear();
		for (byte b1 : b) {
			this.buf.add(b1);
		}
	}

	public short getType() {
		return type;
	}

	public byte getByte() {
		readByte();
		int b = readByte();
		return (byte) b;
	}

	public boolean getBoolean() {
		readByte();
		return this.readBoolean();
	}

	public short getShort() {
		readByte();

		return this.readShort();
	}

	public int getInt() {
		readByte();

		return this.readInt();

	}

	public long getLong() {
		this.readByte();

		return this.readLong();
	}

	public float getFloat() {
		this.readByte();
		return this.readFloat();
	}

	public double getDouble() {
		this.readByte();
		return this.readDouble();
	}

	public String getString() {
		this.readByte();
		String str = this.readString();
		return str;
	}

	private byte[] readBytes(int count) {
		byte[] b = new byte[count];
		for (int i = count - 1; i >= 0; i--) {
			int b1 = this.readByte();
			b[i] = (byte) b1;
		}

		return b;
	}

	public int readByte() {
		byte b = this.buf.get(position++);
		int tempInt = b & 0xff;
		return tempInt;
	}

	public boolean readBoolean() {
		int bool = this.readByte();
		if (bool == 1) {
			return true;
		}
		return false;
	}

	public short readShort() {
		byte[] src = this.readBytes(2);
		int value = 0;

		for (int i = 0; i < 2; i++) {
			value = value << 8 | this.revertByte(src[i]);
		}
		return (short) value;
	}

	public int readInt() {
		byte[] src = this.readBytes(4);
		int value = 0;
		for (int i = 0; i < 4; i++) {
			value = value << 8 | this.revertByte(src[i]);
		}
		return value;
	}

	public long readLong() {
		byte[] src = this.readBytes(8);
		long value = 0;
		for (int i = 0; i < 8; i++) {
			value = value << 8 | this.revertByte(src[i]);
		}
		return value;
	}

	public float readFloat() {
		byte[] src = this.readBytes(4);
		int bits = 0;
		for (int i = 0; i < 4; i++) {
			bits = bits << 8 | this.revertByte(src[i]);
		}
		float value = Float.intBitsToFloat(bits);
		return value;

	}

	public double readDouble() {
		long bits = 0;
		byte[] src = this.readBytes(8);
		for (int i = 0; i < 8; i++) {
			bits = bits << 8 | this.revertByte(src[i]);
		}
		double value = Double.longBitsToDouble(bits);
		return value;
	}

	public String readString() {
		int len = 0;
		byte[] src1 = this.readBytes(4);
		for (int i = 0; i < 4; i++) {
			len = len << 8 | this.revertByte(src1[i]);
		}

		byte[] data = new byte[len];
		for (int i = 0; i < len; i++) {
			int b = this.readByte();
			data[i] = (byte) b;
		}
		String str = new String(data, charset);
		return str;

	}

	public boolean isEnd() {
		if (position >= this.buf.size()) {
			return true;
		}
		return false;
	}

	private int revertByte(byte b) {
		int value = b & 0xff;
		return value;
	}

}
