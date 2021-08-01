package com.xing.mita.movie.dao.option;

import android.text.TextUtils;

import com.xing.mita.movie.dao.GreenDaoManager;
import com.xing.mita.movie.dao.bean.ConnectionDao;
import com.xing.mita.movie.entity.Connection;

import java.util.List;

/**
 * @author Mita
 * @date 2018/10/31
 * @Description
 */
public class CollectionOption {

    /**
     * 收藏
     *
     * @param connection Connection
     */
    public static void saveCollection(Connection connection) {
        getDao().save(connection);
    }

    /**
     * 判断是否收藏了
     *
     * @param link 链接
     * @return boolean
     */
    public static boolean hasCollection(String link) {
        if (TextUtils.isEmpty(link)) {
            return false;
        }
        ConnectionDao dao = getDao();
        Connection connection = dao.queryBuilder().where(ConnectionDao.Properties.Link.eq(link)).unique();
        return connection != null;
    }

    /**
     * 取消收藏
     *
     * @param link 链接
     */
    public static void cancelCollection(String link) {
        ConnectionDao dao = getDao();
        Connection connection = dao.queryBuilder().where(ConnectionDao.Properties.Link.eq(link)).unique();
        if (connection != null) {
            dao.delete(connection);
        }
    }

    /**
     * 获取所以收藏
     *
     * @return List<Connection>
     */
    public static List<Connection> getCollections() {
        return getDao().loadAll();
    }

    public static ConnectionDao getDao() {
        return GreenDaoManager.getInstance().getNewSession().getConnectionDao();
    }
}
