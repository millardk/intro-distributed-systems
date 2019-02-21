package cs455.overlay.transport;

import cs455.overlay.node.Node;
import cs455.overlay.util.Logger;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class SocketContainer {

    private Socket socket;
    private TCPReceiverThread receiver;
    private TCPSenderThread sender;
    private String externalIpAddress;

    public void sendData(byte[] data){
        sender.addToQueue(data);
    }

    public SocketContainer(Socket socket) throws IOException{
        this.socket = socket;
        this.socket.setSendBufferSize(2048);

        this.externalIpAddress = socket.getInetAddress().getCanonicalHostName();

        receiver = new TCPReceiverThread();
        sender = new TCPSenderThread();

        Thread receiverThread = new Thread(receiver);
        Thread senderThread = new Thread(sender);
        receiverThread.start();
        senderThread.start();
    }

    class TCPReceiverThread implements Runnable {

        private DataInputStream din;
        private EventProcessor processor;

        public TCPReceiverThread() throws IOException {
            din = new DataInputStream(socket.getInputStream());
            processor = new EventProcessor();
            Thread processorThread = new Thread(processor);
            processorThread.start();
        }

        @Override
        public void run() {
            while(socket != null){
                try {
                    int dataLength = din.readInt();
                    byte[] data = new byte[dataLength];
                    din.readFully(data, 0, dataLength);
                    processor.addEvent(data);

                } catch (IOException se) {
                    se.printStackTrace();
                }
            }
        }

        class EventProcessor implements Runnable {

            private LinkedList<byte[]> processQueue = new LinkedList<>();

            synchronized void addEvent(byte[] data){
                processQueue.add(data);
                this.notify();
            }

            synchronized byte[] getDataBlocking() throws InterruptedException {
                while (processQueue.isEmpty())
                    this.wait();
                return processQueue.pollFirst();
            }

            @Override
            public void run() {
                while(true){
                    try {
                        byte[] data = getDataBlocking();
                        Event event = EventFactory.createEvent(data);
                        Node.theInstance.onEvent(event, SocketContainer.this);

                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

        }

    }

    class TCPSenderThread implements Runnable{

        private DataOutputStream dout;
        LinkedList<byte[]> sendQueue = new LinkedList<>();

        public TCPSenderThread() throws IOException {
            dout = new DataOutputStream(socket.getOutputStream());
        }

        public synchronized void addToQueue(byte[] data){
            sendQueue.add(data);
            this.notify();
        }

        private synchronized byte[] getDataFromQueue() throws InterruptedException {
            while (sendQueue.isEmpty())
                this.wait();
            return sendQueue.pollFirst();
        }

        private void sendData(byte[] data) throws IOException {
            Logger.log("Sending data haha");
            dout.writeInt(data.length);
            Logger.log("sent length "+data.length);
            dout.write(data);
            dout.flush();
        }

        @Override
        public void run() {
            while(true){
                try {
                    byte[] data = getDataFromQueue();
                    sendData(data);

                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

    }

    public boolean matchesIp(String ip){
        if(this.externalIpAddress.equals("localhost")){
            return Node.theInstance.myIpAddress.equals(ip);
        }

        return this.externalIpAddress.equals(ip);
    }

}
