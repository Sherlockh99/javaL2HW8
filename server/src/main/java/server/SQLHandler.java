package server;

import java.sql.*;

public class SQLHandler {
    private static Connection connection; //для подключения к БД
    private static PreparedStatement psGetNickName;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psChangeNick;

    public static boolean connect(){
        try{
            Class.forName("org.sqlite.JDBC"); //позволяет по имени класса загружать его в память
            connection = DriverManager.getConnection("jdbc:sqLite:MagicChat.db"); //url или путь в зависимости от типа базы
            prepareAllStatements();
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private static void prepareAllStatements() throws SQLException{
        psGetNickName = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?;");
        psRegistration = connection.prepareStatement("INSERT INTO users (login, password, nickname) VALUES ( ? , ? , ?);");
        psChangeNick = connection.prepareStatement("UPDATE users SET nickname = ? WHERE nickname = ?;");
    }

    public static String getNickNameByLoginAndPassword(String login, String password){
        String nick = null;
        try{
            psGetNickName.setString(1,login);
            psGetNickName.setString(2,password);
            ResultSet rs = psGetNickName.executeQuery();
            if(rs.next()){
                nick = rs.getString(1);
            }
            rs.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return nick;
    }

    public static boolean registration(String login, String password, String nickname){
        try{
            psRegistration.setString(1, login);
            psRegistration.setString(2, password);
            psRegistration.setString(3, nickname);
            psRegistration.executeUpdate();
            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean changeNick(String oldNickname, String newNickName){
        try{
            psChangeNick.setString(1,newNickName);
            psChangeNick.setString(2,oldNickname);
            psChangeNick.executeUpdate();
            return true;
        }catch (SQLException e){
            return false;
        }
    }

    public static void disconnect(){
        try {
            psRegistration.close();
            psGetNickName.close();
            psChangeNick.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
        try{
            connection.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
