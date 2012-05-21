package universe.constellation.orion.viewer.db;

/*
 * Orion Viewer is a pdf and djvu viewer for android devices
 *
 * Copyright (C) 2011-2012  Michael Bogdanov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import universe.constellation.orion.viewer.R;

import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;

/**
 * User: mike
 * Date: 05.02.12
 * Time: 18:09
 */
public class BookmarkAccessor extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String BOOKMARKS_TABLE_NAME = "bookmarks";

    public static final String BOOKS_TABLE_NAME = "books";

    private static final String BOOK_REF = "book_id";

    public static final String BOOKMARK_PAGE = "page";

    public static final String BOOKMARK_TEXT = "bookmark_text";

    public static final String BOOKMARK_ID = "_id";

    public static final String BOOK_NAME = "name";

    public static final String BOOK_FILE_SIZE = "file_size";

    public static final String BOOK_ID = "_id";

    public static final String OFFSET_X = "OFFSET_X";

    public static final String OFFSET_Y = "OFFSET_Y";

    private Bookmark GOTO;

    private static final String BOOKMARKS_TABLE_CREATE =
                "CREATE TABLE " + BOOKMARKS_TABLE_NAME + " (" +
                BOOKMARK_ID + " INTEGER primary key autoincrement, " +
                BOOK_REF + " INTEGER not null, " +
                BOOKMARK_PAGE + " INTEGER not null, " +
                BOOKMARK_TEXT + " TEXT not null, " +
                OFFSET_X + " INTEGER not null," +
                OFFSET_Y + " INTEGER not null);";

    private static final String BOOKS_TABLE_CREATE =
                "CREATE TABLE " + BOOKS_TABLE_NAME + " (" +
                BOOK_ID + " INTEGER primary key autoincrement, " +
                BOOK_NAME + " TEXT not null, " +
                BOOK_FILE_SIZE + " INTEGER not null);";



    public BookmarkAccessor(Context context) {
        super(context, "orion", null, DATABASE_VERSION);
        GOTO = new Bookmark();
        GOTO.page = -1;
        GOTO.id = -1;
        GOTO.text = context.getString(R.string.menu_goto_text);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(BOOKS_TABLE_CREATE);
        db.execSQL("create unique index if not exists name_and_size on " + BOOKS_TABLE_NAME + "(" + BOOK_NAME + ", " + BOOK_FILE_SIZE + ");");
        db.execSQL(BOOKMARKS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public List selectBookmarks(long bookId) {
        SQLiteDatabase dataBase = getReadableDatabase();
        ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();

        bookmarks.add(GOTO);
        Cursor cursor = null;
        try {
            if (bookId != -1) {
                cursor = dataBase.rawQuery("select " + BOOKMARK_ID + ", " + BOOKMARK_TEXT + "," + BOOKMARK_PAGE + " from " + BOOKMARKS_TABLE_NAME + " where " + BOOK_REF + " = " + bookId + " order by " + BOOKMARK_PAGE, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int idIndex = cursor.getColumnIndex(BOOKMARK_ID);
                    int textIndex = cursor.getColumnIndex(BOOKMARK_TEXT);
                    int pageIndex = cursor.getColumnIndex(BOOKMARK_PAGE);
                    do {
                        Bookmark bookmark = new Bookmark();
                        bookmark.id = cursor.getInt(idIndex);
                        bookmark.text = cursor.getString(textIndex);
                        bookmark.page = cursor.getInt(pageIndex);
                        bookmarks.add(bookmark);
                    } while (cursor.moveToNext());
                }

            }
            return bookmarks;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //pageNum is zero based!
    public String selectExistingBookmark(long bookId, int pageNum) {
        SQLiteDatabase dataBase = getReadableDatabase();
        Cursor c = null;
        try {
            if (bookId != -1 ) {
                c = dataBase.rawQuery("select " + BOOKMARK_TEXT + " from " + BOOKMARKS_TABLE_NAME + " where " + BOOK_REF + " = " + bookId + " and " + BOOKMARK_PAGE + " = " + pageNum, null);
                if (c != null && c.moveToFirst()) {
                    return c.getString(c.getColumnIndex(BOOKMARK_TEXT));
                }
            }
            return "Page " + (pageNum + 1);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }


    public long selectBookId(String name, long size) {
        SQLiteDatabase dataBase = getReadableDatabase();
        Cursor c = null;
        long id = -1;
        try {
            c = dataBase.rawQuery("select " + BOOK_ID + " from " + BOOKS_TABLE_NAME + " where " + BOOK_NAME + " = \"" + name + "\" and " + BOOK_FILE_SIZE + " = " + size, null);
            if (c != null && c.moveToFirst()) {
                id = c.getLong(c.getColumnIndex(BOOK_ID));
            }

            System.out.println("selectBookId " + id);
            return id;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }


    public long insertOrUpdate(String name, long size) {
        SQLiteDatabase dataBase = getReadableDatabase();
        long id = -1;

        ContentValues cv = new ContentValues();
        cv.put(BOOK_NAME, name);
        cv.put(BOOK_FILE_SIZE, size);
        id = dataBase.insert(BOOKS_TABLE_NAME, null, cv);
        System.out.println("insertOrUpdate " + id);
        return id;
    }


    public long insertOrUpdateBookmark(long bookId, int page, String text) {
        System.out.println("Book ID " + bookId);
        SQLiteDatabase dataBase = getReadableDatabase();
        Cursor c = null;
        long bookmarkId = -1;

        try {
            if (bookId != -1 ) {
                c = dataBase.rawQuery("select " + BOOKMARK_ID + " from " + BOOKMARKS_TABLE_NAME + " where " + BOOK_REF + " = " + bookId + " and " + BOOKMARK_PAGE + " = " + page, null);
                if (c != null && c.moveToFirst()) {
                    bookmarkId = c.getLong(c.getColumnIndex(BOOKMARK_ID));
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        ContentValues cv = new ContentValues();
        cv.put(BOOK_REF, bookId);
        cv.put(BOOKMARK_PAGE, page);
        cv.put(BOOKMARK_TEXT, text);
        cv.put(OFFSET_X, 0);
        cv.put(OFFSET_Y, 0);
        if (bookmarkId != -1) {
            cv.put(BOOKMARK_ID, bookmarkId);
            dataBase.update(BOOKMARKS_TABLE_NAME, cv, BOOKMARK_ID + " = " + bookmarkId, null);
        } else {
            bookmarkId = dataBase.insert(BOOKMARKS_TABLE_NAME, null, cv);
        }

        System.out.println("bookmarkdId " + bookmarkId);
        return bookmarkId;

    }


    public Cursor getExportedBookCursor(long bookId) {
        SQLiteDatabase dataBase = getReadableDatabase();
        Cursor c = dataBase.rawQuery("select " + BOOKS_TABLE_NAME + "." + BOOK_ID + "," + BOOK_NAME + "," + BOOK_FILE_SIZE + ", " + BOOKMARK_PAGE + ", " + BOOKMARK_TEXT + " from " + BOOKS_TABLE_NAME + " INNER JOIN " + BOOKMARKS_TABLE_NAME + " ON " +  BOOKS_TABLE_NAME + "." + BOOK_ID + "="  + BOOKMARKS_TABLE_NAME + "." + BOOK_REF + (bookId != -1 ? " and " + BOOK_REF + " = " + bookId : "") + "  group by " + BOOKMARKS_TABLE_NAME + "." + BOOK_ID, null);
        return c;
    }

}