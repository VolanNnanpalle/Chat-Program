package java3.chatprogram;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * This is my Server Class
 * @author Volan Nnanpalle
 */
public class Server
{

    //attributes 
    public static Socket socket;
    public static ServerSocket serverSocket;
    //stores all the clients threads in the vector
    public static Vector<ClientThread> vectorForClientThreads
        =new Vector<ClientThread>();
    static HashMap<String, Socket> onlineUserList=new HashMap<>();

    public static void main(String[] a) throws IOException
    {
        try
        {
            serverSocket=new ServerSocket(4444);
            while(true)
            {
                //waits for client to connect 
                socket=serverSocket.accept();
                Scanner in=new Scanner(socket.getInputStream());
                ClientThread ct=new ClientThread();

                String messageFromClient=in.nextLine();
                //sets the name of the thread as the user'socket name
                ct.setName(messageFromClient);
                //add the users threads to the Vector 
                vectorForClientThreads.add(ct);
                ct.sendUserName(); //send users thread to all users online
            }

        }catch(IOException|NoSuchElementException e)
        {
        }
    }

    /**
     * This my client thread that sends all the information to the client
     */
    private static class ClientThread extends Thread
    {

        //attributes 
        Socket socket;
        ServerSocket serverSocket;
        String messageFromClient;
        String removeUser;

        //defualt constructor 
        public ClientThread()
        {
            start(); //start the thread 
        }

        /**
         * This is my run method that is all was reading input from the client
         */

        @Override
        public void run()
        {
            socket=Server.socket; //gets the socket
            serverSocket=Server.serverSocket; //get the server socket
            try
            {
                Scanner in=new Scanner(socket.getInputStream());
                messageFromClient="";
                while(true)
                {
                    //Thread.sleep(250);
                    messageFromClient=in.nextLine(); //gets what the user sent

                    sendData(); //sends what the user sent to all users online
                    System.out.println("Server sent: "+messageFromClient);

                }


            }catch(IOException e)
            {

            }catch(Exception e)
            {

            }
        }



        /**
         * This method sends all the messages to the current thread
         * @throws IOException
         */
        synchronized void sendData() throws IOException
        {
            /* enumerate the elements in the vector:i.e get one at time sends
             * all teh messages to the current thread */
            Enumeration vEnum=Server.vectorForClientThreads.elements();
            /* if there is more cleint threads in the vector this loop keeps
             * going */
            if(messageFromClient.equals("end"))
            {
                removeUser=this.getName();
                Server.onlineUserList.remove(removeUser);
                //removes the client thread from the vector 
                Server.vectorForClientThreads.remove(this);
                while(vEnum.hasMoreElements())
                {
                    // to enumerate the data in the thread
                    ClientThread ct=(ClientThread) vEnum.nextElement();

                    PrintWriter pw=new PrintWriter(ct.socket.getOutputStream(),
                        true);
                    //sends data to the client so the client knows its a message 
                    pw.println("end");
                    //send the users name to remove from the onlineUserList 
                    pw.println(this.getName());
                }
            }
            if(messageFromClient.contains("/"))
            {
                StringTokenizer messageTokenizer=new StringTokenizer(
                    messageFromClient, "/");
                //tokenize the message from the user
                String name=messageTokenizer.nextToken();
                String message=messageTokenizer.nextToken();
                Socket directUserSocket=onlineUserList.get(name);

                PrintWriter pw=new PrintWriter(directUserSocket.
                    getOutputStream(),
                    true);
                //sends data to indicate it'socket going to a specific client
                pw.println("/");
                //send the users name and the message attaced to it 
                pw.println(this.getName()+"-->"+message);
            }else
            {
                while(vEnum.hasMoreElements())
                {
                    // to enumerate the data in the thread
                    ClientThread ct=(ClientThread) vEnum.nextElement();
                    PrintWriter pw=new PrintWriter(ct.socket.getOutputStream(),
                        true);
                    //sends data to the client so the client knows its a message 
                    pw.println("data");
                    //send the users name and the message attaced to it 
                    pw.println(this.getName()+"-->"+messageFromClient);

                }
            }

        }

        /**
         * This method send all users onlineUserList to current thread online
         * @throws IOException
         */
        synchronized void sendUserName() throws IOException
        {
            //enumerate the elements in the vector
            Enumeration vEnum=Server.vectorForClientThreads.elements();
            /* if there is more usernames, this will keep sending the usernames
             * to the different threads */
            while(vEnum.hasMoreElements())
            {
                ClientThread ct=(ClientThread) vEnum.nextElement();
                //if the thread is the current thread go to the next
                if(ct==this)
                {
                    continue; //skips to the next iteration 
                }
                Server.onlineUserList.put(ct.getName(), ct.socket);
                PrintWriter pw=new PrintWriter(socket.getOutputStream(), true);
                //this lets the user know that the username is going to be sent
                pw.println("allusers");
                pw.println(ct.getName()); //sends the username
            }

            //send this user as new login to all threads
            vEnum=Server.vectorForClientThreads.elements();
            while(vEnum.hasMoreElements())
            {
                ClientThread ns=(ClientThread) vEnum.nextElement();
                PrintWriter pw
                    =new PrintWriter(ns.socket.getOutputStream(), true);
                Server.onlineUserList.put(ns.getName(), ns.socket);
                pw.println("newuser");
                pw.println(this.getName());
            }

        }


    }



}
