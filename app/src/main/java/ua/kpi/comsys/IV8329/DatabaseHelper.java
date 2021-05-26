package ua.kpi.comsys.IV8329;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_Name = "Lab_MD_DB.db";
    public static final String Book_Table = "BOOKS";
    public static final String Img_Table = "IMAGES";
    public static final String Book_Title = "BOOK_TITLE";
    public static final String Book_Subtitle = "BOOK_SUBTITLE";
    public static final String Book_Isbn13 = "BOOK_ISBN13";
    public static final String Book_Price = "BOOK_PRICE";
    public static final String Book_Cover = "BOOK_COVER";
    public static final String Book_Authors = "BOOK_AUTHORS";
    public static final String Book_Publisher = "BOOK_PUBLISHER";
    public static final String Book_Pages = "BOOK_PAGES";
    public static final String Book_Year = "BOOK_YEAR";
    public static final String Book_Rating = "BOOK_RATING";
    public static final String Book_Description = "BOOK_DESC";
    public static final String Img_Id = "IMG_ID";
    public static final String Img_Location = "IMG_LOCATION";

    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_table_books = "CREATE TABLE " + Book_Table +
                " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Book_Title + " TEXT, " +
                Book_Subtitle + " TEXT, " +
                Book_Isbn13 + " TEXT UNIQUE, " +
                Book_Price + " TEXT, " +
                Book_Cover + " TEXT, " +
                Book_Authors + " TEXT, " +
                Book_Publisher + " TEXT, " +
                Book_Pages + " TEXT, " +
                Book_Year + " TEXT, " +
                Book_Rating + " TEXT, " +
                Book_Description + " TEXT);";
        String create_table_imgs = "CREATE TABLE " + Img_Table +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Img_Id + " TEXT UNIQUE, " +
                Img_Location + " TEXT);";
        db.execSQL(create_table_books);
        db.execSQL(create_table_imgs);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void dropImgs(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + Img_Table + ";");
    }
}
