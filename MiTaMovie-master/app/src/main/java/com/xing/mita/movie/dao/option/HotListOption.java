package com.xing.mita.movie.dao.option;

import android.text.TextUtils;

import com.xing.mita.movie.dao.GreenDaoManager;
import com.xing.mita.movie.dao.bean.HotListDao;
import com.xing.mita.movie.entity.HotList;

import java.util.List;

/**
 * @author Mita
 * @date 2018/10/24
 * @Description
 */
public class HotListOption {

    /**
     * 保存热门推荐
     *
     * @param list List<HotList>
     */
    public static void saveHotList(List<HotList> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        HotListDao dao = getDao();
        HotList hot = list.get(0);
        String source = hot.getSource();
        if (!TextUtils.isEmpty(source)) {
            List<HotList> oldList = dao.queryBuilder().where(
                    HotListDao.Properties.Source.eq(hot.getSource())).list();
            if (oldList != null && oldList.size() > 0) {
                dao.deleteInTx(oldList);
            }
        }
        dao.saveInTx(list);
    }

    /**
     * 获取保存的热门推荐
     *
     * @return List<HotList>
     */
    public static List<HotList> getHotList(String source) {
        if (TextUtils.isEmpty(source)) {
            return null;
        }
        HotListDao dao = getDao();
        return dao.queryBuilder().where(HotListDao.Properties.Source.eq(source)).list();
    }

    private static HotListDao getDao() {
        return GreenDaoManager.getInstance().getSession().getHotListDao();
    }
}
