package org.wsh;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPoolImpl implements ConnectionPool {
    //空闲连接池
    private List<Connection> freeConnection = Collections.synchronizedList(new ArrayList<Connection>());
    //活动连接池
    private List<ConnectionInfo> activeConnection = Collections.synchronizedList(new ArrayList<ConnectionInfo>());
    //当前连接数
    private AtomicInteger countConn = new AtomicInteger(0);

    //初始化
    public ConnectionPoolImpl() {
        //初始连接数大于0，则把空闲连接放入池中
        if (DBConfig.initConnections > 0) {
            for (int i = 0; i < DBConfig.initConnections; i++) {
                addOneToFree();
            }
        }
        //开启定时检查
        check();
    }

    //加入空闲连接池
    private void addOneToFree() {
        //新建连接
        Connection conn = newConnection();
        if (conn == null) {
            //新建失败，自旋
            addOneToFree();
        } else {
            //加入空闲连接池
            freeConnection.add(conn);
        }
    }

    //新建连接
    private Connection newConnection() {
        try {
//            Class.forName(DBConfig.driverName);
            Connection conn = DriverManager.getConnection(DBConfig.url, DBConfig.username, DBConfig.password);
            System.out.println("创建一个新连接：" + conn);
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取连接
    public synchronized Connection getConnection() {
        Connection connection = null;
        try {
            //当前连接数量小于最大活动连接数
            if (countConn.get() < DBConfig.maxActiveConnections) {
                //空闲连接池中有空闲连接
                if (freeConnection.size() > 0) {
                    connection = freeConnection.remove(0);
                } else {
                    //没空闲则创建活动连接
                    connection = newConnection();
                }

                if (connection != null && !connection.isClosed()) {
                    //将连接对象添加时间信息，加入活动连接池
                    activeConnection.add(new ConnectionInfo(connection, System.currentTimeMillis()));
                    //总连接加一
                    countConn.getAndIncrement();
                    System.out.println(Thread.currentThread().getName() + "获取连接：" + connection
                            + "空闲连接数：" + freeConnection.size() + "活动连接数：" + activeConnection.size() + "总连接数：" + countConn.get());
                } else {
                    connection = getConnection();
                }

            } else {
                System.out.println("连接池最大连接数已满：" + countConn.get());

                wait(DBConfig.connectionTimeOut);
                connection = getConnection();
            }
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //释放连接
    public synchronized void releaseConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                //从活动连接池中移除该连接
                for (int i = 0; i < activeConnection.size(); i++) {
                    if (activeConnection.get(i).getConn() == connection) {
                        activeConnection.remove(i);
                    }
                }
                //判断空闲连接池容量是否达到最大连接数
                if (freeConnection.size() < DBConfig.maxConnections) {
                    //小于最大连接，则加入到空闲连接池
                    freeConnection.add(connection);
                } else {
                    //否则，直接关闭该连接
                    connection.close();
                    //更改连接总数
                    countConn.getAndDecrement();
                }
            }
            System.out.println("释放了一个活动连接，当前空闲连接为：" + freeConnection.size() +
                    "活动连接为：" + activeConnection.size() + "总连接为：" + countConn.get());
            notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void check() {
        TimeCheck timeCheck = new TimeCheck();
        //开启定时任务，延迟过首次最大连接时间connectionTimeOut，检查周期为checkTime
        new Timer().schedule(timeCheck,DBConfig.connectionTimeOut,DBConfig.checkTime);
    }

    class TimeCheck extends TimerTask {
        @Override
        public void run() {
            System.out.println("定时检查连接池");
            for (int i = 0; i < activeConnection.size(); i++) {
                ConnectionInfo connectionInfo = activeConnection.get(i);
                //连接创建时间
                long userTime = connectionInfo.getUseStartTime();
                //当前时间
                long currentTime = System.currentTimeMillis();
                //若连接超时
                if ((currentTime - userTime) > DBConfig.connectionTimeOut) {
                    Connection conn = connectionInfo.getConn();
                    try {
                        //关闭连接
                        conn.close();
                        //从活动连接池中移除
                        activeConnection.remove(i);
                        //连接总数减一
                        countConn.getAndDecrement();
                        System.out.println("存在超时连接：" + conn + "已释放：目前总连接数为：" + countConn.get());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
