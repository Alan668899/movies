package com.xing.mita.movie.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.xing.mita.movie.dao.bean.DaoMaster;
import com.xing.mita.movie.dao.bean.HistoryDao;
import com.xing.mita.movie.dao.bean.HotListDao;
import com.xing.mita.movie.dao.bean.MovieHistoryDao;
import com.xing.mita.movie.dao.bean.SearchHistoryDao;
import com.xing.mita.movie.dao.bean.UpdateDao;
import com.xing.mita.movie.dao.bean.UserDao;

import org.greenrobot.greendao.database.Database;

/**
 * @author Mita
 * @date 2018/11/24
 * @Description
 */
public class MySqlLiteOpenHelper extends DaoMaster.OpenHelper {

    public MySqlLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {

                    @Override
                    public void onCreateAllTables(Database db, boolean ifNotExists) {
                        DaoMaster.createAllTables(db, ifNotExists);
                    }

                    @Override
                    public void onDropAllTables(Database db, boolean ifExists) {
                        DaoMaster.dropAllTables(db, ifExists);
                    }
                },
                HistoryDao.class,
                HotListDao.class,
                MovieHistoryDao.class,
                SearchHistoryDao.class,
                UpdateDao.class,
                UserDao.class
        );
    }
}
