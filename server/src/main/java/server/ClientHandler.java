package server;

import service.ServiceMessages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean authenticated;
    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    socket.setSoTimeout(120000);
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            sendMsg("/end");
                            break;
                        }
//                        if (str.startsWith("/auth")) {
                        if (str.startsWith(ServiceMessages.AUTH)) {
                            String[] token = str.split(" ", 3);
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            login = token[1];
                            if (newNick != null) {
                                if (!server.isLoginAuthenticated(login)) {
                                    authenticated = true;
                                    nickname = newNick;
                                    sendMsg(ServiceMessages.AUTH_OK + " " + nickname);
                                    server.subscribe(this);
                                    System.out.println("Client: " + nickname + " authenticated");
                                    break;
                                } else {
                                    sendMsg("С этим логином уже зашли в чат");
                                }
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                        if (str.startsWith("/reg")) {
                            String[] token = str.split(" ", 4);
                            if (token.length < 4) {
                                continue;
                            }
                            if (server.getAuthService()
                                    .registration(token[1], token[2], token[3])) {
                                sendMsg("/reg_ok");
                            } else {
                                sendMsg("/reg_no");
                            }
                        }
                    }
                    if(authenticated){
                        socket.setSoTimeout(0);
                    }
                    //цикл работы
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }
                            if (str.startsWith("/w")) {
                                String[] token = str.split(" ", 3);
                                if (token.length < 3) {
                                    continue;
                                }
                                server.privateMsg(this, token[1], token[2]);
                            }
                            if(str.startsWith(ServiceMessages.CHANGE_NICKNAME)){
                                /*String[] token = str.split(" ", 3);
                                if (token.length < 3) {
                                    sendMsg(ServiceMessages.CHANGE_NICKNAME_NO);
                                    continue;
                                }
                                */

                                String[] token = str.split(" ", 2);
                                if (token.length < 2) {
                                    sendMsg(ServiceMessages.CHANGE_NICKNAME_NO);
                                    continue;
                                }
                                if (token[1].contains(" ")) {
                                    sendMsg(ServiceMessages.CHANGE_NICKNAME_NO + "; Ник не может содержать пробелов");
                                    continue;
                                }
                                if(server.getAuthService().changeNick(this.nickname,token[1])){
                                    //sendMsg("Ваш ник изменен на " + token[1]);
                                    this.nickname = token[1];
                                    sendMsg(ServiceMessages.CHANGE_NICKNAME_OK + "; Ваш ник изменен на " + token[1]);
                                    server.broadcastClientList();
                                }else{
                                    sendMsg(ServiceMessages.CHANGE_NICKNAME_NO + "; Не удалось изменить ник. Ник " + token[1] + " уже существует");
                                }
                            }
                        } else {
                            server.broadcastMsg(this, str);
                        }
                    }

                }catch (SocketTimeoutException e){
                    sendMsg("/end");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Client disconnect!");
                    server.unsubscribe(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
