
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class allinone {

	private static String serverIp = "127.0.0.1";
	private static int serverPort = 6666;
	private static long numberOfMessages = 1000000;
	private static int msgLength = 1000;
	private static boolean logEnabled = false;
	
	public static void main(String[] args) {
		
		if(args.length != 2 && args.length != 6) {
			System.out.println("usage: -s [server port address]\n"+
			"-c [server ip address] [server port address] [number of messages] [message length] [integrity enabled]");
			return;
		}
		
		if(args.length==2){
			serverPort = Integer.parseInt(args[1]);
			
			try {
				long counter = 0;
				
				ServerSocket ss = new ServerSocket(serverPort);
				Socket s = ss.accept();// establishes connection
				s.setSoTimeout(0);
				DataInputStream dis = new DataInputStream(s.getInputStream());
				numberOfMessages = dis.readLong();
				msgLength = dis.readInt();
				logEnabled = dis.readBoolean();
				
				byte[] msg = new byte[msgLength];
				
				long startTime = 0 ,endTime = 0;
				long maxInterTime=0;
				long meanInterTime = 0;

				if(!logEnabled){
					startTime = System.currentTimeMillis();
					for (long i = 0; i < numberOfMessages; i++) {
						dis.readFully(msg);
						counter++;
					}
					endTime = System.currentTimeMillis();
				}
				else{
					long recCurrTime,interTime,recPrecTime = 0;
					for (long i = 0; i < numberOfMessages; i++) {
						dis.readFully(msg);
						recCurrTime=System.nanoTime();
						if(i!=0){
							interTime = recCurrTime-recPrecTime;
							meanInterTime = (meanInterTime*(i-1)+interTime)/i;
							if(interTime > maxInterTime){
								maxInterTime=interTime;
							}
						}
						recPrecTime = recCurrTime;
						counter++;
						if(counter%100==0)
							System.out.println("ricevuti " + counter + " messaggi");
					}
				
				}
				
				
				s.close();
				ss.close();
				
				System.out.println("SERVER: ricevuti " + counter + " messaggi di " + msgLength + " byte");
				if(!logEnabled){
					float bandwidth = (counter*msgLength)*8/(1000*(endTime-startTime));
					System.out.println("SERVER: impiegato " + (endTime - startTime) + " ms\n"+
					"velocità "+bandwidth+" Mbit/s\n");
				}
				
				else{
					System.out.println("SERVER: medio delay tra due messaggi "+meanInterTime+" ns\n");
					System.out.println("SERVER: massimo delay tra due messaggi "+maxInterTime/1000000L+" ms\n");
				}
				
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		else if(args.length==6){
			serverIp = args[1];
			serverPort = Integer.parseInt(args[2]);
			numberOfMessages = Integer.parseInt(args[3]);
			msgLength = Integer.parseInt(args[4]);
			logEnabled = Boolean.parseBoolean(args[5]);

			try {
				byte[] msg = new byte[msgLength];
				for(int i=0; i<msgLength; i++) {
					msg[i] = 0x12;
				}
				
				Socket s = new Socket(serverIp, serverPort);
				s.setSoTimeout(0);
				DataOutputStream dout = new DataOutputStream(s.getOutputStream());
				
				dout.writeLong(numberOfMessages);
				dout.writeInt(msgLength);
				dout.writeBoolean(logEnabled);
				dout.flush();
				
				long startTime=0,endTime=0;
				long maxInterTime = 0;
				long meanInterTime = 0;
				if(!logEnabled){
					startTime = System.currentTimeMillis();
					for (long i = 0; i < numberOfMessages; i++) {
						dout.write(msg);
						dout.flush();
					}

					endTime = System.currentTimeMillis();
				}
				else{
					long sentCurrTime, interTime, sentPrecTime = 0;
					for (long i = 0; i < numberOfMessages; i++) {
						dout.write(msg);
						dout.flush();
						sentCurrTime=System.nanoTime();
						if(i!=0){
							interTime = sentCurrTime-sentPrecTime;
							meanInterTime = (meanInterTime*(i-1)+interTime)/i;
							if(interTime > maxInterTime){
								maxInterTime=interTime;
							}
						}
						sentPrecTime = sentCurrTime;
						if(i%100==0)
							System.out.println("inviati " + i + " messaggi");
					}
				}
				
				
				
				dout.close();
				s.close();
				
				
				if(!logEnabled){
					float bandwidth = (numberOfMessages*msgLength)*8/(1000*(endTime-startTime));
					System.out.println("impiegato " + (endTime - startTime) + " ms\n"+
				"velocità "+bandwidth+" Mbit/s\n");
					
				}
				else{
					System.out.println("medio delay tra due messaggi: "+meanInterTime+" ns\n");
					System.out.println("massimo delay tra due messaggi: "+maxInterTime/1000000L+" ms\n");
				}
			} catch (IOException ex){
				ex.printStackTrace();
			}
		}
	}
}
