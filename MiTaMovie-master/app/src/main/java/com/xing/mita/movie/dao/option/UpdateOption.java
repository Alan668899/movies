package com.xing.mita.movie.dao.option;

import com.xing.mita.movie.dao.GreenDaoManager;
import com.xing.mita.movie.dao.bean.UpdateDao;
import com.xing.mita.movie.entity.Update;

import java.util.List;

/**
 * @author Mita
 * @date 2018/11/21
 * @Description 升级表操作
 */
public class UpdateOption {

    /**
     * 保存最新更新信息
     *
     * @param update Update
     */
    public static void saveUpdate(Update update) {
        UpdateDao dao = getDao();
        List<Update> list = dao.loadAll();
        if (list != null && list.size() > 0) {
            dao.deleteAll();
        }
        dao.save(update);
    }

    /**
     * 获取保存的升级信息
     *
     * @return Update
     */
    public static Update getUpdate() {
        UpdateDao dao = getDao();
        List<Update> list = dao.loadAll();
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 更新升级包已经下载完成
     */
    public static void updateHasDownload() {
        UpdateDao dao = getDao();
        List<Update> list = dao.loadAll();
        if (list != null && list.size() > 0) {
            Update update = list.get(0);
            update.setHasDownload(true);
            dao.update(update);
        }
    }

    /**
     * 清除升级信息
     */
    public static void clear() {
        getDao().deleteAll();
    }

    private static UpdateDao getDao() {
        return GreenDaoManager.getInstance().getSession().getUpdateDao();
    }
}
