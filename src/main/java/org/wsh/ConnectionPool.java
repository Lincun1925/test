package org.wsh;

import java.sql.Connection;

/**
 * 连接池接口
 */
public interface ConnectionPool{
    //获取连接
    public Connection getConnection();
    //释放连接
    public void releaseConnection(Connection connection);
}