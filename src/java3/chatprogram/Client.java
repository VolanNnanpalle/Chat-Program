package java3.chatprogram;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import static java3.chatprogram.Client.onlineUserArea;
import static java3.chatprogram.Client.onlineUserList;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


/**
 * This is my Client class
 * @author Volan Nnanpalle
 */
public class Client extends Application
{

    //attributes
    public static TextField sendArea=new TextField();
    public static TextArea messageArea=new TextArea();
    public static TextArea onlineUserArea=new TextArea();
    public static final TextField usernameField=new TextField();
    private static final Button sendButton=new Button("SEND");
    private static final Button createButton=new Button("CREATE");
    static Image refreshImage
        =new Image("file:refresh-button-icon-63755.png", 20, 20,
            false, false);
    public static final Button refreshButton=new Button("",
        new ImageView(refreshImage));
    public static Circle circle;
    static Socket socket;
    public static HashMap<String, Socket> onlineUserList=new HashMap<>();
    static InetAddress localIpAddress;

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        GridPane root=new GridPane();
        //labels for the text fields
        Label usernameFieldLabel=new Label("Username");
        Label onlineAreaLabel=new Label("Online");
        Label messageAreaLabel=new Label("Messages");
        //prompting the user
        usernameField.setPromptText("Enter username");
        sendArea.setPromptText("SendMessage");
        //initialize the object of the circle 
        circle=new Circle();
        //creates a image
        Image circleImage=new Image("file:profile .jpeg");
        circleImage.isPreserveRatio();

        circle.setFill(new ImagePattern(circleImage)); //fills the circle image
        refreshImage.isPreserveRatio();
        //sets the radius of the circle 
        circle.setRadius(40);

        //sets the color of the buttons 
        sendButton.setStyle("-fx-font: 15 arial; -fx-base: #1E90FF;");
        createButton.setStyle("-fx-font: 15 arial; -fx-base: brown;");
        refreshButton.setStyle("-fx-font: 15 arial; -fx-base: green;");

        //create field color 
        usernameField.setStyle("-fx-text-inner-color: red;");
        //message color 
        messageArea.setStyle("-fx-text-inner-color: #4169E1;");
        //online user color 
        onlineUserArea.setStyle(
            "-fx-text-inner-color: green; -fx-font-size: 16;");
        //send text field color 
        sendArea.setStyle("-fx-text-inner-color: #1E90FF;");


        //makes sure the newUser is not able to edit the message box and online 
        //box
        messageArea.setEditable(false);
        onlineUserArea.setEditable(false);

        //sets the spacing of the component on the scene 
        root.setVgap(5);
        root.setHgap(15);

        //setting the shape of the refresh button 
        double r=15;
        refreshButton.setShape(new Circle(r));
        refreshButton.setMinSize(2*r, 2*r);
        refreshButton.setMaxSize(2*r, 2*r);

        //set the sizes of the text fields
        messageArea.setPrefColumnCount(20);
        sendArea.setPrefColumnCount(7);
        onlineUserArea.setPrefColumnCount(10);
        usernameField.setPrefColumnCount(7);

        //sets the position of the labels 
        usernameFieldLabel.setTranslateY(0);
        onlineAreaLabel.setTranslateX(425);
        onlineAreaLabel.setTranslateY(170);
        messageAreaLabel.setTranslateX(100);
        messageAreaLabel.setTranslateY(170);

        //add everything to the scene 
        root.add(circle, 5, 2);
        root.getChildren().add(usernameFieldLabel);
        root.getChildren().add(onlineAreaLabel);
        root.getChildren().add(messageAreaLabel);
        root.setStyle("-fx-background:black;"); //sets the color of the pane
        root.add(usernameField, 2, 0);
        root.add(messageArea, 2, 20);
        root.add(sendArea, 2, 30);
        root.add(sendButton, 4, 30);
        root.add(createButton, 4, 0);
        root.add(onlineUserArea, 4, 20);
        root.add(refreshButton, 5, 20);

        /* creates a new usernameField if the newUser clicks the createButton
         * button */
        createUsernameAction();
        sendAction(); //sends the messageArea to all users online

        //to close everything with the red x is clicked 
        primaryStage.setOnCloseRequest((WindowEvent t)->
        {
            Platform.exit();
            System.exit(0);
        });

        Scene scene=new Scene(root, 700, 500);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Client");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * This is my main method which launches my application
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException
    {

        localIpAddress=InetAddress.getLocalHost();//gets the localhost
        socket=new Socket(localIpAddress, 4444); //the socket
        ReadThread readThread=new ReadThread(); //new thread for the newUser who gets online
        readThread.start();
        messageArea.appendText(
            "To specifiy who the message is going to type\nname of who you "+
            "want to send the message\nto/the message\n");
        messageArea.appendText("To exit send end and then click the red x\n");
        launch(args);
    }

    /**
     * This method allows the newUser to click the createButton button and
     * createButton a new usernameField to chat
     * @throws IOException
     */
    public void createUsernameAction() throws IOException
    {
        usernameField.setOnKeyPressed((KeyEvent k)->
        {
            if(k.getCode().equals(KeyCode.ENTER))
            {
                try
                {
                    PrintWriter pw=new PrintWriter(socket.getOutputStream(),
                        true);
                    String newUser=usernameField.getText(); //gets the users name

                    //sends the newUser name to the server to be stored
                    pw.println(newUser);

                    //prevents the user from changing thier name
                    Client.createButton.setDisable(true);
                    Client.usernameField.setDisable(true);

                }catch(IOException|NoSuchElementException ex)
                {
                }
            }
        });

        createButton.setOnAction((ActionEvent e)->
        {
            try
            {
                PrintWriter pw=new PrintWriter(socket.getOutputStream(),
                    true);
                String newUser=usernameField.getText(); //gets the users name

                //sends the newUser name to the server to be stored
                pw.println(newUser);

                //prevents the user from changing thier name
                Client.createButton.setDisable(true);
                Client.usernameField.setDisable(true);

            }catch(IOException|NoSuchElementException ex)
            {
            }

        });

    }

    /**
     * This method allows the newUser to click the sendButton button and
     * sendButton the message to all users
     */
    public void sendAction()
    {

        sendArea.setOnKeyPressed((KeyEvent k)->
        {
            if(k.getCode().equals(KeyCode.ENTER))

            {
                try
                {
                    PrintWriter pw=new PrintWriter(socket.getOutputStream(),
                        true);
                    //gets the message the newUser wants to sendButton
                    String messageToServer=sendArea.getText();
                    pw.println(messageToServer); //sends the message to the server
                    sendArea.clear(); //clears the message box
                }catch(IOException ex)
                {
                }
            }
        });

        sendButton.setOnAction((ActionEvent e)->
        {

            try
            {
                PrintWriter pw=new PrintWriter(socket.getOutputStream(),
                    true);
                //gets the message the newUser wants to sendButton
                String messageToServer=sendArea.getText();
                pw.println(messageToServer); //sends the message to the server
                sendArea.clear(); //clears the message box
            }catch(IOException ex)
            {
            }

        });
    }
}

/**
 * This is my Read thread which reads what the server is sending
 * @author Volan Nnanpalle
 */
class ReadThread extends Thread
{

    //attribute
    String userRemove;

    @Override
    public void run()
    {
        try
        {

            Scanner in=new Scanner(Client.socket.getInputStream());
            String messageFromServer="";

            while(true)
            {
                //reads what the thread is sending
                messageFromServer=in.nextLine();

                System.out.println("****************");
                //sends the message to all users online
                if(messageFromServer.equals("data")==true)
                {
                    messageFromServer=in.nextLine(); //reads the message
                    System.out.
                        println("server wrote -->"+messageFromServer+"\n");
                    //appends the message on the message box
                    Client.messageArea.appendText(messageFromServer+"\n");

                }
                //if the newUser want to sendButton to an individual 
                if(messageFromServer.equals("/")==true)
                {
                    messageFromServer=in.nextLine();
                    System.out.
                        println("server wrote -->"+messageFromServer+"\n");
                    //appends the message on the message box
                    Client.messageArea.appendText(messageFromServer+"\n");
                }
                //if the user wants to quit
                if(messageFromServer.equals("end"))
                {
                    userRemove=in.nextLine();
                    System.out.
                        println("server wrote -->"+messageFromServer+"\n");
                    Client.messageArea.appendText("**"+userRemove+
                        " has logged off."+
                        " Refresh online users**");
                    Client.onlineUserList.remove(userRemove);
                    /* gives the newUser the ability to constantly refreshButton
                     * whos online */
                    Client.refreshButton.setOnAction((ActionEvent e)->
                    {
                        Client.onlineUserArea.clear();
                        //to print out all the newUser names in the map
                        Collection<String> allKeys=onlineUserList.keySet();
                        allKeys.forEach((userOnline)->
                        {
                            //sets newUser online
                            onlineUserArea.appendText(userOnline+"\n");
                        });
                    });

                }else
                {
                    //if a new newUser is created 
                    if(messageFromServer.equals("newuser")==true)
                    {
                        messageFromServer=in.nextLine(); //reads the users name
                        System.out.println("NEW MEMBER LOGGED IN :: "+
                            messageFromServer+
                            "\n");
                        //lets all users know that a new newUser is online
                        Client.messageArea.appendText("***new user "+
                            messageFromServer+
                            " has logged in***"+
                            "\n");
                        //add the new newUser to the onlineUserList
                        Client.onlineUserList.put(messageFromServer,
                            Client.socket);
                        //clears the online text area
                        Client.onlineUserArea.clear();
                        //to print out all the newUser names in the map 
                        Collection<String> allKeys=Client.onlineUserList.
                            keySet();
                        allKeys.forEach((userOnline)->
                        {
                            //sets newUser online 
                            Client.onlineUserArea.appendText(userOnline+"\n");
                        });

                    }else
                    {
                        if(messageFromServer.equals("allusers")==true)
                        {
                            messageFromServer=in.nextLine();
                            System.out.println("NEW MEMBER LOGGED IN :: "+
                                messageFromServer+"\n");
                            //add the new newUser to the onlineUserList
                            Client.onlineUserList.put(messageFromServer,
                                Client.socket);
                            //clears the online text area
                            Client.onlineUserArea.clear();
                            //to print out all the newUser names in the map 
                            Collection<String> allKeys=Client.onlineUserList.
                                keySet();
                            allKeys.forEach((userOnline)->
                            {
                                //sets newUser online 
                                Client.onlineUserArea.
                                    appendText(userOnline+"\n");
                            });
                        }
                    }
                }
            }
        }catch(IOException|NoSuchElementException e)
        {
        }
    }
}
