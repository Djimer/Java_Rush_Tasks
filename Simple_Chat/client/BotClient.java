package com.javarush.task.task30.task3008.client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class BotClient extends Client{
    public class BotSocketThread extends SocketThread{
        @Override
        protected void processIncomingMessage(String message) {
            System.out.println(message);
            if (message == null) return;

            String[] parts = message.split(": ");
            if (parts.length != 2) return;

            HashMap<String, String> map = new HashMap<>();
            map.put("дата", "d.MM.YYYY");
            map.put("день", "d");
            map.put("месяц", "MMMM");
            map.put("год", "YYYY");
            map.put("время", "H:mm:ss");
            map.put("час", "H");
            map.put("минуты", "m");
            map.put("секунды", "s");

            String pattern = map.get(parts[1]);
            if (pattern == null) return;

            String answer = new SimpleDateFormat(pattern).format(Calendar.getInstance().getTime());
            sendTextMessage(String.format("Информация для %s: %s", parts[0], answer));
        }

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {

        return "date_bot_" + (int)(Math.random()*100);
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
