import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.KeyStore;

public class Server {

    private static final int ssl_port_number = 1234;
    private static final int tcp_port_number = 213;

    private static final String keystore = "server/server.keystore";
    private static final String keystore_passwd = "Gulnisa";



    public static void main(String[] args) throws Exception {
        try {
            new Thread(() -> {
                try {
                    start_ssl();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();

            new Thread(() -> {
                try {
                    start_tcp();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static void start_ssl() throws Exception {

        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(keystore), keystore_passwd.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, keystore_passwd.toCharArray());

            SSLContext context = SSLContext.getInstance("SSL");
            context.init(kmf.getKeyManagers(), null, null);

            SSLServerSocketFactory factory = context.getServerSocketFactory();

            try (SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(ssl_port_number)) {

                System.out.println("SSL Server started on port " + ssl_port_number);

                while (true) {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();

                    new Thread(() -> {
                        try {
                            handleClient(clientSocket, true);
                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }).start();
                }

            }
        }
        catch(Exception ex){
                ex.printStackTrace();
        }

    }

    private static void start_tcp() throws Exception {

        try(ServerSocket serverSocket = new ServerSocket(tcp_port_number)){
            System.out.println("TCP Server started on port " + tcp_port_number);

            while(true){
                Socket clientSocket = serverSocket.accept();

                new Thread(() -> {
                    try {
                        handleClient(clientSocket, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket, boolean ssl) throws Exception {

        System.out.printf("Client connected: " + clientSocket.getRemoteSocketAddress()+ " with %s", ssl? "SSL \n":"TCP \n");

        try(BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)){

            String request = in.readLine();


            if( "ALL FILES".equalsIgnoreCase(request)){
                File dir = new File("server/Files");
                for (File f : dir.listFiles()) {

                        long start_time = System.nanoTime();

                        out.println("File start: " + f.getName());

                        try(BufferedReader f_reader = new BufferedReader(new FileReader(f))){

                            String line;
                            while((line = f_reader.readLine()) != null){
                                out.println(line);
                            }

                        }

                        out.println("File end");

                        long end_time = System.nanoTime();
                        double elapsed = (end_time - start_time) / 1_000_000.0; //in milliseconds



                        System.out.printf("%s | Size: %d bytes | Time: %.2f ms | Protocol: %s\n", f.getName(), f.length(), elapsed, ssl? "SSL":"TCP");
                }

            } else if (request.equalsIgnoreCase("QUIT")) {
                System.out.println("Client left: " + clientSocket.getRemoteSocketAddress());
                out.printf("Disconnected from %s",ssl? "SSL":"TCP"+" server.");

            } else{

                File f = new File("server/Files/"+request);

                if(f.exists() && f.isFile()){

                    long start_time = System.nanoTime();

                    try(BufferedReader f_reader = new BufferedReader(new FileReader(f))){
                        String line;

                        while((line= f_reader.readLine()) != null){
                            out.println(line);
                        }
                    }

                    long end_time = System.nanoTime();
                    double elapsed = (end_time - start_time) / 1_000_000.0;

                    System.out.printf("%s | Size: %d bytes | Time: %.2f ms | Protocol: %s\n", f.getName(), f.length(), elapsed, ssl? "SSL":"TCP");

                }
                else{

                    out.println("File not found");
                }
            }


        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }


}
