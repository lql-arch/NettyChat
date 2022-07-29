package Server.processLogin;

import config.DbUtil;
import message.LoginMessage;

import java.sql.*;
import java.time.LocalDateTime;

public class processLogin {
    static int uid = 100000;
    public static boolean find(String uid , String password) throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        if(con.isClosed()){
            return false;
        }
        PreparedStatement ps1 = con.prepareStatement("use members");
        ps1.execute();
        PreparedStatement ps2 = con.prepareStatement("select password FROM members.user where uid = ?");
        ps2.setObject(1,uid);
        try(ResultSet rs = ps2.executeQuery()){
            while(rs.next()){
                String pass = rs.getString("password");
                if(password.compareToIgnoreCase(pass) == 0){
                    return  true;
                }
            }
        }

        return false;
    }

    public static boolean register(LoginMessage msg) throws SQLException {
        String password = msg.getPass();
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        if(con.isClosed()){
            return false;
        }
        PreparedStatement ps1 = con.prepareStatement("use members");
        ps1.execute();

        //生成uid
        String uid = getUid();
        msg.setUid(uid);

        Timestamp date = Timestamp.valueOf(LocalDateTime.now());

        PreparedStatement ps3 = con.prepareStatement("insert into members.user(uid,name,password,age,gander,build_time) values (?,?,?,?,?,?)");
        ps3.setObject(1,uid);
        ps3.setObject(2,uid);
        ps3.setObject(3,password);
        ps3.setObject(4,0);
        ps3.setObject(5,"b");
        ps3.setObject(6,date);

        return !ps3.execute();
    }

    public static boolean login(LoginMessage msg) throws SQLException {
        boolean isLogin = msg.getIsLogin();
        if(isLogin){
            isLogin = find(msg.getUid(),msg.getPass());
        }else{
            isLogin = register(msg);
        }
        return isLogin;
    }

    public static String getUid() throws SQLException {
        DbUtil db = DbUtil.getDb();
        Connection con = db.getConn();
        if(con.isClosed()){
            throw new SQLException();
        }
        PreparedStatement ps1 = con.prepareStatement("use members");
        ps1.execute();
        ps1 = con.prepareStatement("select uid from members.user order by uid desc limit 1");
        ResultSet rs = ps1.executeQuery();
        rs.next();
        String max = rs.getString("uid");
        if(max == null){
            return String.valueOf(processLogin.uid);
        }

        return String.valueOf(Integer.getInteger(max)+1);
    }

}
