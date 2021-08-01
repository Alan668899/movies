package com.xing.mita.movie.dao.option;

import com.xing.mita.movie.dao.GreenDaoManager;
import com.xing.mita.movie.dao.bean.ProgramDao;
import com.xing.mita.movie.entity.Program;

import java.util.List;

/**
 * @author Mita
 * @date 2019/1/23
 * @Description
 */
public class ProgramOption {

    public static void save(List<Program> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        ProgramDao dao = getDao();
        dao.deleteAll();
        dao.saveInTx(list);
    }

    public static List<Program> getAll() {
        return getDao().loadAll();
    }

    public static Program getById(Long id) {
        ProgramDao dao = getDao();
        return dao.load(id);
    }

    private static ProgramDao getDao() {
        return GreenDaoManager.getInstance().getSession().getProgramDao();
    }
}
