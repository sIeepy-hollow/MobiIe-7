package ua.kpi.comsys.IV8329;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class BookActivity extends AppCompatActivity {

    private static Book book;
    private final String apiURL = "https://api.itbook.store/1.0/books/";

    private static ImageButton back_button;

    private static ImageView cover;
    private static TextView title;
    private static TextView subtitle;
    private static TextView isbn;
    private static TextView price;

    private static TextView authors;
    private static TextView publisher;
    private static TextView year;
    private static TextView pages;
    private static TextView rating;
    private static TextView description;

    private static DatabaseHelper dbHelper;
    private static SQLiteDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.dbHelper = new DatabaseHelper(this.getApplicationContext(), DatabaseHelper.DB_Name, null, 1);
        this.db = dbHelper.getWritableDatabase();
        Bundle bundle = getIntent().getExtras();
        book = (Book) bundle.getParcelable("book");
        setContentView(R.layout.activity_book);

        back_button = findViewById(R.id.back_btn);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String tmp;
        if (book.getImage() != null && !book.getImage().isEmpty()) {
            cover = findViewById(R.id.book_cover);
            findViewById(R.id.cover_text).setVisibility(View.INVISIBLE);
            tmp = book.getImage();
            if (tmp.contains("http")) {
                Glide.with(this)
                        .load(tmp)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_action_load)
                        .thumbnail(Glide.with(this).load(R.raw.spinner_icon))
                        .dontAnimate()
                        .into(cover);
            } else {
                Uri imgUri = Uri.fromFile(new File(tmp));
                Glide.with(this)
                        .load(imgUri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_action_load)
                        .thumbnail(Glide.with(this).load(R.raw.spinner_icon))
                        .dontAnimate()
                        .into(cover);
            }

        }
        if (book.getTitle() != null && !book.getTitle().isEmpty()) {
            title = findViewById(R.id.book_title);
            title.setText(book.getTitle());
            title.setTextColor(getResources().getColor(R.color.black));
        }
        if (book.getSubtitle() != null && !book.getSubtitle().isEmpty()) {
            subtitle = findViewById(R.id.book_subtitle);
            subtitle.setText(book.getSubtitle());
            subtitle.setTextColor(getResources().getColor(R.color.black));
        }
        if (book.getPrice() != null && !book.getPrice().isEmpty()) {
            price = findViewById(R.id.book_price);
            price.setText(book.getPrice());
            price.setTextColor(getResources().getColor(R.color.black));
        }
        if (book.getIsbn13() != null && !book.getIsbn13().isEmpty()) {
            tmp = getResources().getString(R.string.isbn) + book.getIsbn13();
            isbn = findViewById(R.id.book_isbn);
            isbn.setText(tmp);
            isbn.setTextColor(getResources().getColor(R.color.black));
            authors = findViewById(R.id.book_authors);
            publisher = findViewById(R.id.book_publisher);
            year = findViewById(R.id.book_year);
            pages = findViewById(R.id.book_pages);
            rating = findViewById(R.id.book_rating);
            description = findViewById(R.id.book_description);

            tmp = apiURL + book.getIsbn13();
            new JSONTask().execute(tmp);
        }

    }

    public class JSONTask extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            HttpsURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                }
                String res = buffer.toString();
                return new JSONObject(res);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (!result.isNull("authors")) {
                try {
                    book.setAuthors(result.getString("authors"));
                    book.setPublisher(result.getString("publisher"));
                    book.setYear(result.getString("year"));
                    book.setPages(result.getString("pages"));
                    book.setRating(result.getString("rating"));
                    book.setDescription(result.getString("desc"));
                    ContentValues values = new ContentValues();
                    values.put(dbHelper.Book_Authors, book.getAuthors());
                    values.put(dbHelper.Book_Publisher, book.getPublisher());
                    values.put(dbHelper.Book_Year, book.getYear());
                    values.put(dbHelper.Book_Pages, book.getPages());
                    values.put(dbHelper.Book_Rating, book.getRating());
                    values.put(dbHelper.Book_Description, book.getDescription());
                    db.update(dbHelper.Book_Table, values, dbHelper.Book_Isbn13 + " = " + book.getIsbn13(), null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Cursor cursor = db.query(dbHelper.Book_Table,
                        new String[] {dbHelper.Book_Authors, dbHelper.Book_Publisher, dbHelper.Book_Year, dbHelper.Book_Pages, dbHelper.Book_Rating, dbHelper.Book_Description},
                        dbHelper.Book_Isbn13 + " = " + book.getIsbn13(),
                        null, null, null, null);
                if (cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    if (cursor.getString(0) != null) {
                        book.setAuthors(cursor.getString(0));
                    }
                    if (cursor.getString(1) != null) {
                        book.setPublisher(cursor.getString(1));
                    }
                    if (cursor.getString(2) != null) {
                        book.setYear(cursor.getString(2));
                    }
                    if (cursor.getString(3) != null) {
                        book.setPages(cursor.getString(3));
                    }
                    if (cursor.getString(4) != null) {
                        book.setRating(cursor.getString(4));
                    }
                    if (cursor.getString(5) != null) {
                        book.setDescription(cursor.getString(5));
                    }
                }
            }
            if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
                authors.setText(book.getAuthors());
                authors.setTextColor(getResources().getColor(R.color.black));
            }
            if (book.getPublisher() != null && !book.getPublisher().isEmpty()) {
                publisher.setText(book.getPublisher());
                publisher.setTextColor(getResources().getColor(R.color.black));
            }
            if (book.getYear() != null && !book.getYear().isEmpty()) {
                year.setText(book.getYear());
                year.setTextColor(getResources().getColor(R.color.black));
            }
            if (book.getPages() != null && !book.getPages().isEmpty()) {
                pages.setText(book.getPages());
                pages.setTextColor(getResources().getColor(R.color.black));
            }
            if (book.getRating() != null && !book.getRating().isEmpty()) {
                String tmp = book.getRating() + getResources().getString(R.string.out_of_five);
                rating.setText(tmp);
                rating.setTextColor(getResources().getColor(R.color.black));
            }
            if (book.getDescription() != null && !book.getDescription().isEmpty()) {
                description.setText(book.getDescription());
                description.setTextColor(getResources().getColor(R.color.black));
            }
        }
    }
}
