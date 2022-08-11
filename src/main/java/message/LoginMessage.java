package message;

import client.Start;
import io.netty.channel.ChannelHandlerContext;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Scanner;

public class LoginMessage extends Message {

    private static final int type = LoginMessage;
    private String uid;
    private String pass;
    private String gander;
    private String build_time;//Date

    private boolean logOut;
    private boolean Login;
    public LoginMessage(){}

    private LoginMessage(String uid, String pass, boolean isLogin) {
        this.uid = uid;
        this.pass = pass;
        this.Login = isLogin;
    }

    private LoginMessage(String pass) {//注册用
        this.pass = pass;
        this.Login = false;
    }

    public boolean isLogOut() {
        return logOut;
    }

    public LoginMessage setLogOut(boolean logOut) {
        this.logOut = logOut;
        return this;
    }

    public LoginMessage setPass(String pass) {
        this.pass = pass;
        return this;
    }

    public LoginMessage setBuild_time(String build_time) {
        this.build_time = build_time;
        return this;
    }

    public boolean isLogin() {
        return Login;
    }

    public LoginMessage setLogin(boolean login) {
        Login = login;
        return this;
    }

    public String getUid() {
        return uid;
    }

    public String getPass() {
        return pass;
    }

    public boolean getIsLogin(){
        return Login;
    }

    public String getGander() {
        return gander;
    }

    public Date getTime() {
        return Date.valueOf(build_time);
    }

    public LoginMessage setGander(String gander) {
        this.gander = gander;
        return this;
    }

    public LoginMessage setTime(Date build_time) {
        this.build_time = build_time.toString();
        return this;
    }

    public LoginMessage setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public static LoginMessage LoginUser(){
        String uid,pass;
        while(true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("请输入uid:");
            uid = sc.nextLine();
            System.out.println("请输入密码:");
//            EraserThread eraserThread = new EraserThread();
//            eraserThread.start();
            pass = sc.nextLine();
//            eraserThread.setActive(false);
            if (pass.length() > 25) {
                System.err.println("密码字符长度不得超过25");
            }else{
                break;
            }
        }

        return new LoginMessage(uid,pass,true);
    }

    public int getLength(){
        return getUid().length()+getPass().length();
    }

    public static LoginMessage register() throws SQLException {

        Scanner sc = new Scanner(System.in);
        System.out.println("请输入密码：(输入’exit‘退出)");
        String password ;
        while(true) {
            password = sc.nextLine();
            if(password.length() > 25){
                System.err.println("密码字符长度不得超过25");
                continue;
            }
            if(password.compareTo("exit") == 0){
                System.out.println("注册已取消");
                return null;
            }
            System.out.println("请再次确认密码：");
            String _password = sc.nextLine();
            if (password.compareToIgnoreCase(_password) == 0) {
                break;
            }
            System.out.println("密码错误,请输入密码：(输入’exit‘退出)");
        }

        return new LoginMessage(password);
    }

    public static boolean logOut(ChannelHandlerContext ctx) throws InterruptedException {
        System.out.println("你确认要注销吗？(yes/no)");
        String result = new Scanner(System.in).nextLine();
        if(result.compareToIgnoreCase("yes") == 0 || result.compareToIgnoreCase("y") == 0){
            ctx.channel().writeAndFlush(new LoginMessage().setUid(Start.uid).setLogOut(true));
            Start.semaphore.acquire();
            return true;
        }
        return false;
    }

    public String toString(){
        return getUid()+","+getPass();
    }

    @Override
    public int getMessageType() {
        return LoginMessage;
    }

    static class EraserThread extends Thread {
        private boolean active;
        private String mask;

        public EraserThread() {
            this('*');
        }

        public EraserThread(char maskChar) {
            active = true;
            mask = "\010" + maskChar;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }

        @Override
        public void run() {
            while (isActive()) {
                System.out.print(mask);
            }
        }
    }
}
