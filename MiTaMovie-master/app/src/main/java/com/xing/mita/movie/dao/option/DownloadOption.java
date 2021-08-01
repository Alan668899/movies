package com.xing.mita.movie.dao.option;

import android.text.TextUtils;

import com.xing.mita.movie.dao.GreenDaoManager;
import com.xing.mita.movie.dao.bean.DownloadDao;
import com.xing.mita.movie.entity.Download;
import com.xing.mita.movie.utils.Constant;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Mita
 * @date 2019/2/18
 * @Description
 */
public class DownloadOption {

    /**
     * 保存一条缓存信息
     *
     * @param download Download
     */
    public static void save(Download download) {
        getDao().save(download);
    }

    /**
     * 加载某条缓存信息
     *
     * @param url String
     * @return Download
     */
    public static Download load(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        DownloadDao dao = getDao();
        return dao.queryBuilder().where(DownloadDao.Properties.Url.eq(url)).unique();
    }

    /**
     * 查询首先下载
     *
     * @return Download
     */
    public static Download loadFirstDown() {
        DownloadDao dao = getDao();
        Download down = dao.queryBuilder()
                .where(DownloadDao.Properties.Priority.eq(Constant.DOWNLOAD_FIRST_PRIORITY))
                .unique();
        if (down != null) {
            return down;
        }
        List<Download> list = loadAll();
        if (list == null || list.size() == 0) {
            return null;
        }
        Collections.sort(list, new Comparator<Download>() {
            @Override
            public int compare(Download o1, Download o2) {
                // 返回值为int类型，大于0表示正序，小于0表示逆序
                return o2.getPriority() - o1.getPriority();
            }
        });
        return list.get(0);
    }

    /**
     * 获取全部缓存信息
     *
     * @return List<Download>
     */
    public static List<Download> loadAll() {
        return getDao().loadAll();
    }

    /**
     * 删除某条缓存信息
     *
     * @param url String
     */
    public static void delete(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        DownloadDao dao = getDao();
        Download download = load(url);
        if (download != null) {
            dao.delete(download);
        }
    }

    /**
     * 更新
     *
     * @param down Download
     */
    public static void update(Download down) {
        getDao().update(down);
    }

    private static DownloadDao getDao() {
        return GreenDaoManager.getInstance().getSession().getDownloadDao();
    }
}
