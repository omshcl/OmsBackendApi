//import org.apache.commons.net.telnet.TelnetClient;
//import java.io.InputStream;
//import java.io.PrintStream;
//
//public class Telnet {
//	private TelnetClient telnet = new TelnetClient();
//	private InputStream in;
//	private PrintStream out;
//	private String prompt = "#";
//
//	public Telnet(String message) {
//		try {
//			// Connect to the specified server
//			telnet.connect("localhost", 5554);
//			// Get input and output stream references
//			in = telnet.getInputStream();
//			out = new PrintStream(telnet.getOutputStream());
//			// read till ok
//			readUntil("OK");
//			write("auth abc");
//			readUntil("OK");
//			write(message);
//			readUntil("OK");
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public String readUntil(String pattern) {
////		try {
////			char lastChar = pattern.charAt(pattern.length() � 1);
////			StringBuffer sb = new StringBuffer();
////			boolean found = false;
////			char ch = (char) in.read();
////			while (true) {
////				System.out.print(ch);
////				sb.append(ch);
////				if (ch == lastChar) {
////					if (sb.toString().endsWith(pattern)) {
////						return sb.toString();
////					}
////				}
////				ch = (char) in.read();
////			}
////		}
////		catch (Exception e) {
////			e.printStackTrace();
////		}
////		return null;
//	}
//	public void write(String value) {
//		try {
//			out.println(value);
//			out.flush();
//			System.out.println(value);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	public String sendCommand(String command) {
//		try {
//			write(command);
//			return readUntil(prompt + " ");
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
////	public void disconnect() {
////		try {
////			telnet.disconnect();
////		}
////		catch (Exception e) {
////			e.printStackTrace();
////		}
////	}
//} 