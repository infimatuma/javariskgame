package lv.dium.riskserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MultiplayerAcceptConnection implements Runnable {
    private Socket socket;

    public MultiplayerAcceptConnection(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            StringBuilder fiscalData = new StringBuilder();

            try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                String text;

                boolean started = false;
                boolean ip_next = false;
                boolean lgn_next = false;
                boolean psw_next = false;

                writer.println("--RISK--");

                String commandType = null;

                do {
                    text = reader.readLine();

                    if(commandType != null){
                        System.out.println("Command [a] with body: " + text);
                        writer.println("--COMMAND--");
                        commandType = null;
                    }
                    else {
                        if (text.equals("a")) { // attack handler
                            commandType = "a";
                        } else if (text.equals("g")) { // greeting handler
                            commandType = "b";
                        }
                    }
                    System.out.println("Got: " + text);

                } while (!text.equals("--BYEBYE--"));

            } finally {
                socket.close();
            }


            System.out.println("Receipt data sent.");
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}