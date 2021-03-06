package com.xing.mita.movie.dao.bean;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.xing.mita.movie.entity.Update;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "UPDATE".
*/
public class UpdateDao extends AbstractDao<Update, Long> {

    public static final String TABLENAME = "UPDATE";

    /**
     * Properties of entity Update.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property VersionCode = new Property(1, int.class, "versionCode", false, "VERSION_CODE");
        public final static Property VersionName = new Property(2, String.class, "versionName", false, "VERSION_NAME");
        public final static Property Content = new Property(3, String.class, "content", false, "CONTENT");
        public final static Property Url = new Property(4, String.class, "url", false, "URL");
        public final static Property HasDownload = new Property(5, boolean.class, "hasDownload", false, "HAS_DOWNLOAD");
    }


    public UpdateDao(DaoConfig config) {
        super(config);
    }
    
    public UpdateDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"UPDATE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"VERSION_CODE\" INTEGER NOT NULL ," + // 1: versionCode
                "\"VERSION_NAME\" TEXT," + // 2: versionName
                "\"CONTENT\" TEXT," + // 3: content
                "\"URL\" TEXT," + // 4: url
                "\"HAS_DOWNLOAD\" INTEGER NOT NULL );"); // 5: hasDownload
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"UPDATE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Update entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getVersionCode());
 
        String versionName = entity.getVersionName();
        if (versionName != null) {
            stmt.bindString(3, versionName);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(4, content);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(5, url);
        }
        stmt.bindLong(6, entity.getHasDownload() ? 1L: 0L);
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Update entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getVersionCode());
 
        String versionName = entity.getVersionName();
        if (versionName != null) {
            stmt.bindString(3, versionName);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(4, content);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(5, url);
        }
        stmt.bindLong(6, entity.getHasDownload() ? 1L: 0L);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Update readEntity(Cursor cursor, int offset) {
        Update entity = new Update( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // versionCode
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // versionName
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // content
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // url
            cursor.getShort(offset + 5) != 0 // hasDownload
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Update entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setVersionCode(cursor.getInt(offset + 1));
        entity.setVersionName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setContent(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setUrl(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setHasDownload(cursor.getShort(offset + 5) != 0);
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Update entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Update entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Update entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
