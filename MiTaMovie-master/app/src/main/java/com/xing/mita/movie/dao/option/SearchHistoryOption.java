package com.xing.mita.movie.dao.option;

import android.text.TextUtils;

import com.xing.mita.movie.dao.GreenDaoManager;
import com.xing.mita.movie.dao.bean.SearchHistoryDao;
import com.xing.mita.movie.entity.SearchHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mita
 * @date 2018/10/24
 * @Description
 */
public class SearchHistoryOption {

    /**
     * 保存搜索关键词
     *
     * @param keyword String
     */
    public static void saveKeyword(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return;
        }
        SearchHistoryDao dao = getDao();
        SearchHistory history = dao.queryBuilder().where(SearchHistoryDao.Properties.Keyword.eq(keyword)).unique();
        if (history != null) {
            dao.delete(history);
        }
        history = new SearchHistory(keyword);
        dao.save(history);
    }

    /**
     * 获取所有保存的关键词
     *
     * @return List<String>
     */
    public static List<String> getAllKeyword() {
        List<SearchHistory> historyList = getDao().loadAll();
        List<String> list = new ArrayList<>();
        for (SearchHistory history : historyList) {
            list.add(history.getKeyword());
        }
        return list;
    }

    /**
     * 清除所有关键词
     */
    public static void clearHistory() {
        getDao().deleteAll();
    }

    private static SearchHistoryDao getDao() {
        return GreenDaoManager.getInstance().getSession().getSearchHistoryDao();
    }
}
