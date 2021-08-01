package com.xing.mita.movie.dao.option;

import com.xing.mita.movie.dao.GreenDaoManager;
import com.xing.mita.movie.dao.bean.UserDao;
import com.xing.mita.movie.entity.User;

import java.util.List;

/**
 * @author Mita
 * @date 2019/1/9
 * @Description
 */
public class UserOption {

    public static void saveUser(User user) {
        UserDao dao = getDao();
        dao.save(user);
    }

    public static User getUser() {
        UserDao dao = getDao();
        List<User> list = dao.loadAll();
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    public static void update(User user) {
        getDao().update(user);
    }

    private static UserDao getDao() {
        return GreenDaoManager.getInstance().getSession().getUserDao();
    }
}
