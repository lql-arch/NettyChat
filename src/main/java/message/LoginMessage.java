package message;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Scanner;

public class LoginMessage extends Message {

    private static final int type = LoginMessage;
    private String uid;
    private String pass;
    private String gander;
    private String build_time;//Date

    private boolean isLogin;

    private LoginMessage(String uid, String pass,boolean isLogin) {
        this.uid = uid;
        this.pass = pass;
        this.isLogin = isLogin;
    }

    private LoginMessage(String pass) {//注册用
        this.pass = pass;
        this.isLogin = false;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setBuild_time(String build_time) {
        this.build_time = build_time;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public String getUid() {
        return uid;
    }

    public String getPass() {
        return pass;
    }

    public boolean getIsLogin(){
        return isLogin;
    }

    public String getGander() {
        return gander;
    }

    public Date getTime() {
        return Date.valueOf(build_time);
    }

    public void setGander(String gander) {
        this.gander = gander;
    }

    public void setTime(Date build_time) {
        this.build_time = build_time.toString();
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public static LoginMessage LoginUser(){
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入uid:");
        String uid = sc.nextLine();
        System.out.println("请输入密码:");
        String pass = sc.nextLine();

        return new LoginMessage(uid,pass,true);
    }

    public int getLength(){
        return getUid().length()+getPass().length();
    }

    public static LoginMessage register() throws SQLException {

        Scanner sc = new Scanner(System.in);
        //System.out.println("请输入uid：");
        //String uid = sc.nextLine();
        System.out.println("请输入密码：(输入’exit‘退出)");
        String password ;
        while(true) {
            password = sc.nextLine();
            if(password.compareTo("exit") == 0){
                System.out.println("注册已取消");
                break;
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

    public String toString(){
        return getUid()+","+getPass();
    }

    @Override
    public int getMessageType() {
        return LoginMessage;
    }
}
