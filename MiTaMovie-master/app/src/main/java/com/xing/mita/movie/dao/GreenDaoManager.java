package com.xing.mita.movie.dao;

import com.xing.mita.movie.app.SysApplication;
import com.xing.mita.movie.dao.bean.DaoMaster;
import com.xing.mita.movie.dao.bean.DaoSession;

/**
 * @author MiTa
 * @date 2017/12/1.
 */
public class GreenDaoManager {

    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private static GreenDaoManager mInstance;

    private GreenDaoManager() {
        if (mInstance == null) {
            DaoMaster.DevOpenHelper devOpenHelper = new
                    DaoMaster.DevOpenHelper(SysApplication.getContext(), "MiTaMovie.db", null);
            mDaoMaster = new DaoMaster(devOpenHelper.getWritableDatabase());

//            MySqlLiteOpenHelper helper = new MySqlLiteOpenHelper(SysApplication.getContext(),
//                    "MiTaMovie.db", null);
//            mDaoMaster = new DaoMaster(helper.getWritableDatabase());

            mDaoSession = mDaoMaster.newSession();
        }
    }

    public static GreenDaoManager getInstance() {
        if (mInstance == null) {
            //保证异步处理安全操作
            synchronized (GreenDaoManager.class) {

                if (mInstance == null) {
                    mInstance = new GreenDaoManager();
                }
            }
        }
        return mInstance;
    }

    public DaoMaster getMaster() {
        return mDaoMaster;
    }

    public DaoSession getSession() {
        return mDaoSession;
    }

    public DaoSession getNewSession() {
        mDaoSession = mDaoMaster.newSession();
        return mDaoSession;
    }
}
