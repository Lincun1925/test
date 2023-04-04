package org.wsh;

import java.sql.Connection;

/**
 * 活动连接，记录连接状态
 */
public class ConnectionInfo {
    private Connection conn;
    //开始使用时间
    private long useStartTime;

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public long getUseStartTime() {
        return useStartTime;
    }

    public void setUseStartTime(long useStartTime) {
        this.useStartTime = useStartTime;
    }
    //构造函数
    public ConnectionInfo(Connection conn, long useStartTime){
        super();
        this.conn = conn;
        this.useStartTime = useStartTime;
    }
}
