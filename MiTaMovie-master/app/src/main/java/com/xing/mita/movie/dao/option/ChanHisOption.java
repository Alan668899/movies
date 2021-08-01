package com.xing.mita.movie.dao.option;

import android.util.Log;

import com.xing.mita.movie.dao.GreenDaoManager;
import com.xing.mita.movie.dao.bean.ChannelHistoryDao;
import com.xing.mita.movie.entity.ChannelHistory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Mita
 * @date 2019/2/14
 * @Description
 */
public class ChanHisOption {

    public static void save(ChannelHistory history) {
        if (history == null) {
            return;
        }
        ChannelHistoryDao dao = getDao();
        ChannelHistory chanHis = dao.queryBuilder()
                .where(ChannelHistoryDao.Properties.Name.eq(history.getName()))
                .unique();
        if (chanHis == null) {
            dao.save(history);
        } else {
            chanHis.setUpdateTime(System.currentTimeMillis());
            dao.update(chanHis);
        }
    }

    public static List<ChannelHistory> loadAll() {
        ChannelHistoryDao dao = getDao();
        List<ChannelHistory> list = dao.loadAll();
        if (list == null || list.size() == 0) {
            return null;
        }
        Collections.sort(list, new Comparator<ChannelHistory>() {
            @Override
            public int compare(ChannelHistory o1, ChannelHistory o2) {
                return (int) (o2.getUpdateTime() - o1.getUpdateTime());
            }
        });
        if (list.size() > 6) {
            return list.subList(0, 6);
        }
        return list;
    }

    public static ChannelHistoryDao getDao() {
        return GreenDaoManager.getInstance().getSession().getChannelHistoryDao();
    }
}
