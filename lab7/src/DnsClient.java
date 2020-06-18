import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class DnsClient 
{
    private static final int DNS_SERVER_PORT = 53;
    private String ipAddress;
    private String domain;
    
    public DnsClient(String domain, String ipAddress) 
    {
    	this.domain = domain;
    	this.ipAddress = ipAddress;
    }
    
    //preluare adresa IP
    public String getIpAddress() throws IOException {
        InetAddress ipAddr = InetAddress.getByName(ipAddress);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        //ID field
        dos.writeShort(0x1234);

        // Write Query Flags
        dos.writeShort(0x0000);

        // Question Count: Specifies the number of questions in the Question section of the message.
        dos.writeShort(0x0001);

        // Answer Record Count: Specifies the number of resource records in the Answer section of the message.
        dos.writeShort(0x0000);

        // Authority Record Count: Specifies the number of resource records in the Authority section of
        // the message. (“NS” stands for “name server”)
        dos.writeShort(0x0000);

        // Additional Record Count: Specifies the number of resource records in the Additional section of the message.
        dos.writeShort(0x0000);

        String[] _domainParts = domain.split("\\.");

        for (int i = 0; i<_domainParts.length; i++) {
            byte[] _domainBytes = _domainParts[i].getBytes("UTF-8");
            dos.writeByte(_domainBytes. length);
            dos.write(_domainBytes);
        }

        // No more parts
        dos.writeByte(0x00);

        // Type 0x01 = A (Host Request)
        dos.writeShort(0x0001);

        // Class 0x01 = IN
        dos.writeShort(0x0001);

        byte[] dnsFrame = baos.toByteArray();

        // Trimitere cerere
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, ipAddr, DNS_SERVER_PORT);
        socket.send(dnsReqPacket);

        // Primire raspuns de la serverul DNS
        byte[] buf = new byte[512];// buffer 512 bytes
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);


        DataInputStream din = new DataInputStream(new ByteArrayInputStream(buf));
        short id = din.readShort();//2 bytes
        short flags = din.readShort();
        short questions = din.readShort();
        short answers = din.readShort();
        short authority = din.readShort();
        short additional = din.readShort();
        
        System.out.println("Response from server:");
        System.out.println("Id: "+ String.format("%x",id));
        System.out.println("Flags: " + String.format("%x",flags));
        System.out.println("Question record count: " + String.format("%x",questions));
        System.out.println("Answer record count: " + String.format("%x",answers));
        System.out.println("Authority record count: " + String.format("%x",authority)); 
        
        System.out.println("Additional record count: "+String.format("%x",additional));
        
        int recLen = 0;
        while ((recLen = din.readByte()) > 0) {
            byte[] record = new byte[recLen];

            for (int i = 0; i < recLen; i++) {
                record[i] = din.readByte();
                
            }
            System.out.println("Record: "+ new String(record, "UTF-8"));
        }

        short recordType = din.readShort();
        short clas = din.readShort();
        short fields = din.readShort();
        short type = din.readShort();
        short clas2 = din.readShort();
        int ttl = din.readInt();
        short addrLen = din.readShort();
  
        System.out.println("Type: 0x"+ String.format("%x",type));
        System.out.println("Class: 0x"+ String.format("%x",clas));
        System.out.println("Len: 0x"+ String.format("%x",addrLen));
        System.out.println("TTL: "+ String.format("%d",ttl));
        
        StringBuilder ipAddres = new StringBuilder();

        for (int i = addrLen - 4; i < addrLen; i++ ) {
            ipAddres.append(String.format("%d", (din.readByte() & 0xFF)));
            if (i != addrLen - 1) {
                ipAddres.append('.');
            }
        }
        socket.close();
        System.out.println("Address: "+ ipAddres.toString());
        return ipAddres.toString();
    }
    public static void main(String[] args) throws IOException 
    {
    	//DnsClient dnsClient = new DnsClient("www.tuiasi.ro", "81.180.223.1");
    	//DnsClient dnsClient = new DnsClient("www.riweb.tibeica.com", "81.180.223.1");
    	//dnsClient.getIpAddress();
    }
}
