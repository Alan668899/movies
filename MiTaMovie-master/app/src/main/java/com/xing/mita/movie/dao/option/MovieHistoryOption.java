package com.xing.mita.movie.dao.option;

import android.text.TextUtils;

import com.xing.mita.movie.dao.GreenDaoManager;
import com.xing.mita.movie.dao.bean.MovieHistoryDao;
import com.xing.mita.movie.entity.MovieHistory;

import java.util.List;

/**
 * @author Mita
 * @date 2018/11/1
 * @Description
 */
public class MovieHistoryOption {

    /**
     * 保存或更新今天已有记录
     *
     * @param history MovieHistory
     */
    public static void saveHistory(MovieHistory history) {
        MovieHistoryDao dao = getDao();
        List<MovieHistory> list = dao.queryBuilder()
                .where(MovieHistoryDao.Properties.Link.eq(history.getLink())).list();
        if (list != null && list.size() > 0) {
            dao.deleteInTx(list);
        }
        dao.save(history);
    }

    /**
     * 根据url查询观看记录
     *
     * @param url String
     * @return List<MovieHistory>
     */
    public static MovieHistory getHistoryByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        MovieHistoryDao dao = getDao();
        List<MovieHistory> list = dao.queryBuilder().where(MovieHistoryDao.Properties.Link.eq(url)).list();
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 查询某一天的观看记录
     *
     * @param date long
     * @return List<MovieHistory>
     */
    public static List<MovieHistory> getOneDayHistory(long date) {
        MovieHistoryDao dao = getDao();
        return dao.queryBuilder().where(MovieHistoryDao.Properties.Date.eq(date)).list();
    }

    /**
     * 查询两天前的观看记录
     *
     * @param date long
     * @return List<MovieHistory>
     */
    public static List<MovieHistory> getDaysAgoHistory(long date) {
        MovieHistoryDao dao = getDao();
        return dao.queryBuilder().where(MovieHistoryDao.Properties.Date.lt(date)).list();
    }

    /**
     * 根据id删除记录
     *
     * @param keys List<Long>
     */
    public static void deleteOneHistory(List<Long> keys) {
        getDao().deleteByKeyInTx(keys);
    }

    /**
     * 清空记录
     */
    public static void deleteAll() {
        getDao().deleteAll();
    }

    public static MovieHistoryDao getDao() {
        return GreenDaoManager.getInstance().getNewSession().getMovieHistoryDao();
    }
}
