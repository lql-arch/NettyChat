package config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class DbUtil {
    private final static Logger log = LogManager.getLogger(DbUtil.class);
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/members?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String USER;
    private static String PASS;
    private static Connection conn ;
    private  static DbUtil db ;
    private static boolean login = false;

    private  DbUtil(){}

    public DbUtil(String user,String pass){
        USER = user;
        PASS = pass;
    }

    public DbUtil start() throws ClassNotFoundException, SQLException {
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
//            new com.mysql.cj.jdbc.Driver();

            // 打开链接
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            System.out.println(" ...mySql已启动.");
        }catch (ClassNotFoundException ex){
            throw new ClassNotFoundException();
        }catch (SQLException ex){
            throw new SQLException();
        }
        return this;
    }

    public void close() throws SQLException {
        try {
            conn.close();
            System.out.println("Goodbye!");
        }catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public static DbUtil loginMysql(){
        if(!login) {
            login = true;
            Scanner sc = new Scanner(System.in);
            System.out.println("mysql用户名：");
            String name = sc.nextLine();
            System.out.println("mysql密码：");
            String pass = sc.nextLine();
            db = new DbUtil(name,pass);
            return db;
        }
        return DbUtil.getDb();
    }

    public static DbUtil getDb(){
        return db;
    }

    public Connection getConn(){
        return conn;
    }

}
