package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message){
        connectionMap.forEach((name,connection)-> {
            try {
                connection.send(message);
            } catch (IOException e) {
                System.out.println("Сообщение не было отправлено");
            }
        });
    }

    private static class Handler extends Thread{
        private Socket socket;

        Handler(Socket socket){
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            Message message;

            while (true){
                connection.send(new Message(MessageType.NAME_REQUEST,"Введите имя"));
                message = connection.receive();
                String userName = message.getData();

                if(message.getType() == MessageType.USER_NAME &&
                        !userName.equals("") && !connectionMap.containsKey(userName)){

                    connectionMap.put(userName,connection);
                    break;
                }
            }
            connection.send(new Message(MessageType.NAME_ACCEPTED,"Вы добавлены в чат"));

            return message.getData();

        }

        private void notifyUsers(Connection connection, String userName) throws IOException{
            connectionMap.forEach((name, connection1)->{
                if(userName != name) {
                    try {
                        connection.send(new Message(MessageType.USER_ADDED, name));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException,
                ClassNotFoundException{

            while (true){
                Message message = connection.receive();

                if(message.getType() == MessageType.TEXT){
                    sendBroadcastMessage(new Message(MessageType.TEXT,userName +
                            ": " + message.getData()));
                }
                else{
                    ConsoleHelper.writeMessage("Ошибка! Введите текст");
                }
            }

        }

        @Override
        public void run() {
            System.out.println("Установено соединение с адресом: "+ socket.getRemoteSocketAddress());
            String userName = "";
            try {
                Connection connection = new Connection(socket);
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED,userName));
                notifyUsers(connection,userName);
                serverMainLoop(connection,userName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(!userName.equals("")){
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED,userName));
            }
        }
    }

    public static void main(String[] args){
        System.out.println("Введите порт сервера");
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())){
            System.out.println("Сервер запущен");
            while (true) {
                Handler handler = new Handler(serverSocket.accept());
                handler.start();
            }
        }
        catch (IOException e){
            System.out.println(e);
        }
    }


}
