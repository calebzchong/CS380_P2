import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

public class PhysLayerClient {
	
	private static HashMap<String, Integer> _4B5BTable = new HashMap<>();

	public static void main(String[] args) {
		initialize4B5BTable();
		try (Socket socket = new Socket("18.221.102.182", 38002)) {
			System.out.println("Connected to server.");

			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			
			int[] preambleBytes = new int[64];
			for ( int i = 0; i < preambleBytes.length; i++){
				preambleBytes[i] = is.read();
			}
			double baseline = getBaseline( preambleBytes );
			System.out.printf("Baseline established from preamble: %.2f%n" ,baseline);
			
			int[] recvSignals = new int[320];
			for ( int i = 0; i < recvSignals.length; i++){
				recvSignals[i] = is.read();
			}
//			System.out.println("Recv signals: " + Arrays.toString(recvSignals));
			int[] recvBits = signalsToBits(recvSignals, baseline);
//			System.out.println("Recv bits   : " + Arrays.toString(recvBits));
			int[] decodedNRZI = decodeNRZI( recvBits );
//			System.out.println("NRZI decoded: " + Arrays.toString(decodedNRZI));
			byte[] recvMsg = convert4B5B( decodedNRZI );
			System.out.printf("Received %d bytes: ", recvMsg.length );
			printBytes(recvMsg);
			
			os.write(recvMsg);
			if ( is.read() == 1 ){
				System.out.println("\nResponse good.");
			} else {
				System.out.println("\nResponse bad.");
			}
			
		} catch ( Exception e ){
			e.printStackTrace();
		} finally {
			System.out.println("Disconnected from Server.");
		}
		
	}
	
	private static int[] decodeNRZI(int[] recvBits) {
		int[] bytes = new int[recvBits.length];
		int prev = 0;
		for ( int i = 0; i < bytes.length; i++){
			bytes[i] = prev ^ recvBits[i];
			prev = recvBits[i];
		}
		return bytes;
	}

	private static void printBytes(byte[] recvMsg) {
		for ( byte b : recvMsg ){
			System.out.printf("%02X", b);;
		}
	}

	private static void initialize4B5BTable() {
		_4B5BTable.put( "11110", 0b0000 );
		_4B5BTable.put( "01001", 0b0001 );
		_4B5BTable.put( "10100", 0b0010 );
		_4B5BTable.put( "10101", 0b0011 );
		_4B5BTable.put( "01010", 0b0100 );
		_4B5BTable.put( "01011", 0b0101 );
		_4B5BTable.put( "01110", 0b0110 );
		_4B5BTable.put( "01111", 0b0111 );
		_4B5BTable.put( "10010", 0b1000 );
		_4B5BTable.put( "10011", 0b1001 );
		_4B5BTable.put( "10110", 0b1010 );
		_4B5BTable.put( "10111", 0b1011 );
		_4B5BTable.put( "11010", 0b1100 );
		_4B5BTable.put( "11011", 0b1101 );
		_4B5BTable.put( "11100", 0b1110 );
		_4B5BTable.put( "11101", 0b1111 );
	}

	private static byte[] convert4B5B( int[] recvMsg ) {
		byte[] bytes = new byte[recvMsg.length / 10];
		for ( int i = 0; i < bytes.length; i++ ){
			String byteString = "";
			for ( int j = i*10; j < i*10+10; j++ ){
				byteString += recvMsg[j];
			}
			bytes[i] = convert4B5BByte( byteString );
		}
		return bytes;
	}
	
	private static byte convert4B5BByte(String byteString) {
//		System.out.println(byteString);
//		System.out.println(byteString.length());
		int x = _4B5BTable.get(byteString.substring(0, 5));
		x = x * 0b10000;
		x = x ^ _4B5BTable.get(byteString.substring(5, 10));
		return (byte)x;
	}

	private static int[] signalsToBits( int[] recvSignals, double baseline ){
		int[] bits = new int[recvSignals.length];
		for ( int i = 0; i < recvSignals.length; i++ ){
			if ( recvSignals[i] > baseline ){
				bits[i] = 1;
			} else {
				bits[i] = 0;
			}
		}
		return bits;
	}

	private static double getBaseline(int[] preambleBytes) {
		int sum = 0;
		for ( int b : preambleBytes ){
			sum += b;
		}
		return (double)sum / preambleBytes.length;
	}

	private static int byteToUnsignedInt(byte b){
		return b & 0xFF;
	}

}
