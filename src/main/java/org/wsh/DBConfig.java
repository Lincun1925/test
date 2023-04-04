package org.wsh;

/**
 * 数据库连接池基础配置
 */
public class DBConfig {
    //jdbc驱动，对jdbc标准接口的实现，操作对应数据库
    public static final String driverName = "com.mysql.jdbc.Driver";
    //数据库连接url
    public static final String url = "jdbc:mysql://localhost:3306/db2";
    //账号密码
    public static final String username = "root";
    public static final String password = "1234";
    //最大连接数
    public static final int maxConnections = 10;
    //初始化连接数
    public static final int initConnections = 5;
    //重复获取连接频率
    public static final long checkTime = 1000;//1000 * 60
    //最大允许的连接数，数据库
    public static final int maxActiveConnections = 100;
    //连接超时时间，设置20分钟
    public static final long connectionTimeOut = 10000;//1000 * 60 * 20
}
