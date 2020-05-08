package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class WakeOnLan {
	
	/*
	 * Wake On Lan magic packet is generally sent to port 0, 7, 9, 2304
	 */
    public static void wol(String MAC, int port) throws NetworkException {
        final String HEX = "0123456789ABCDEF";
        byte bMAC[] = new byte[6];
        int m = 0; // string index
        int i = 0; // MAC byte array index
        int h = 0; // last (high) hex digit
        MAC = MAC.toUpperCase();
        while (m < MAC.length() && i < 2 * 6) {
            int n = HEX.indexOf(MAC.charAt(m));
            if (n >= 0) {
                if (i % 2 == 0) {
                    h = n;
                } else {
                    bMAC[i / 2] = (byte) (h * 16 + n);
                }
                i++;
            }
            m++;
        }
        if (m < MAC.length()) {
            throw new NetworkException("MAC Address must be 12 Hex digits exactly");
        }
        wol(bMAC, port);
    }

    private static void wol(byte[] MAC, int port) throws NetworkException {
        if (MAC == null || MAC.length != 6) {
            throw new NetworkException("MAC array must be present and 6 bytes long");
        }

        // Assemble Magic Packet
        int packetLength = 102;
        byte packetData[] = new byte[packetLength];
        int m = 0;

        // Start off with six 0xFF values
        packetData[m++] = (byte) 255;
        packetData[m++] = (byte) 255;
        packetData[m++] = (byte) 255;
        packetData[m++] = (byte) 255;
        packetData[m++] = (byte) 255;
        packetData[m++] = (byte) 255;

        // Append sixteen copies of MAC address
        for (int i = 0; i < 16 * 6; i++) {
            packetData[m] = MAC[m % 6];
            m++;
        }

        DatagramSocket socket = null;
        try {
        	socket = new DatagramSocket();
            InetSocketAddress address = new InetSocketAddress("255.255.255.255", port);
            DatagramPacket datagram = new DatagramPacket(packetData, packetLength, address);
            socket.setBroadcast(true);
            socket.send(datagram);
        } catch (IOException e) {
        	e.printStackTrace();
			throw new NetworkException(e.getMessage());
		} finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
