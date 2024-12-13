import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class Client {

    private static final String server_address = "localhost";
    private static final int tcp_port = 213;
    private static final int ssl_port = 1234;
    private static final String trust_store = "client/client.truststore";
    private static final String trust_store_passwd = "Gulnisa";

    public static void main(String[] args) {

        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));) {

            System.out.println("Choose one of the protocols (TCP/SSL): ");
            String protocol = consoleReader.readLine().toUpperCase();

            System.out.println("Enter the file name or type ALL FILES to get all files: ");
            String fileName = consoleReader.readLine();

            if ("TCP".equals(protocol)) {
                establish_conn_TCP(fileName);
            } else if ("SSL".equals(protocol)) {
                establish_conn_SSL(fileName);
            } else {
                System.out.println("Invalid protocol selection.");
            }





        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void establish_conn_TCP(String fileName) {
        try (Socket socket = new Socket(server_address, tcp_port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(fileName);


            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void establish_conn_SSL(String fileName) {
        try {
            System.setProperty("javax.net.ssl.trustStore", trust_store);
            System.setProperty("javax.net.ssl.trustStorePassword", trust_store_passwd);

            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

            try (SSLSocket socket = (SSLSocket) factory.createSocket(server_address, ssl_port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println(fileName);

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

