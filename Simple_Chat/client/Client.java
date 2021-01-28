package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;


public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    protected String getServerAddress(){
        System.out.println("Введите адрес сервера");
        return ConsoleHelper.readString();
    }

    protected int getServerPort(){
        System.out.println("Введите порт");
        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        System.out.println("Введите имя пользователя");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT,text));
        } catch (IOException e) {
            e.printStackTrace();
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread{
        protected void processIncomingMessage(String message){
            System.out.println(message);
        }

        protected void informAboutAddingNewUser(String userName){
            System.out.println("Участник "+ userName + "присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName){
            System.out.println("Участник "+ userName + "покинул чат");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true) {
                Message message = connection.receive();
                if(message.getType() == MessageType.NAME_REQUEST){
                    connection.send(new Message(MessageType.USER_NAME,getUserName()));
                }
                else if(message.getType() == MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    return;
                }
                else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT){
                    processIncomingMessage(message.getData());
                }
                else if (message.getType() == MessageType.USER_ADDED){
                    informAboutAddingNewUser(message.getData());
                }
                else if (message.getType() == MessageType.USER_REMOVED){
                    informAboutDeletingNewUser(message.getData());
                }
                else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        public void run(){
            String serverAddress = getServerAddress();
            int serverPort = getServerPort();
            try {
                Socket socket = new Socket(serverAddress,serverPort);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (Exception e) {
                notifyConnectionStatusChanged(false);
            }
        }

    }

    public void run(){
        Thread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.out.println("Ошибка");
                return;
            }
        }

        if(clientConnected){
            System.out.println("Соединение установлено. Для выхода наберите команду 'exit'.");
        }
        else {
            System.out.println("Произошла ошибка во время работы клиента.");
        }

        while (clientConnected){
            String text = ConsoleHelper.readString();
            if(text.equals("exit")){
                break;
            }
            if(shouldSendTextFromConsole()){
                sendTextMessage(text);
            }
        }


    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
