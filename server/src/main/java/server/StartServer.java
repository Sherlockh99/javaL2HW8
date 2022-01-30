package server;

public class StartServer {
    private static AuthService authService1;
    public static void main(String[] args) {
        new Server();
        /*
        authService1 = new MySQLAuthService();
        authService1.registration("Bob4", "75", "Bob4");
        authService1.disconnect();
         */
    }
}
