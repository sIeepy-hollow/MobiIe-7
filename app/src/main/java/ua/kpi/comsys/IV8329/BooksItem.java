package ua.kpi.comsys.IV8329;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;


public class BooksItem extends Fragment implements BooksView.BookListener {

    private static RecyclerView recyclerView;
    private static BooksView adapter;
    private SearchView booksSearch;
    private static TextView noResults;
    private final String apiURL = "https://api.itbook.store/1.0/search/";
    private static DatabaseHelper dbHelper;
    private static SQLiteDatabase db;
    private static String origQuery;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_books, container, false);

        dbHelper = new DatabaseHelper(this.getActivity().getApplicationContext(), DatabaseHelper.DB_Name, null, 1);
        db = dbHelper.getWritableDatabase();

        recyclerView = root.findViewById(R.id.books_list);
        if (adapter == null) {
            adapter = new BooksView(this.getActivity().getApplicationContext(), this);
        }
        recyclerView.setAdapter(adapter);
        booksSearch = root.findViewById(R.id.books_search);
        noResults = root.findViewById(R.id.noRes);
        if (adapter.getItemCount() == 0) {
            noResults.setVisibility(View.VISIBLE);
        } else {
            noResults.setVisibility(View.INVISIBLE);
        }
        booksSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onQueryTextChange(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    try {
                        origQuery = newText;
                        String query = URLEncoder.encode(newText.toLowerCase(), "utf-8");
                        String JSONurl = apiURL + query;
                        new JSONTask().execute(JSONurl);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    adapter.clear();
                    noResults.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
        return root;
    }

    public class JSONTask extends AsyncTask<String, String, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... params) {
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
                return new JSONArray(res.substring(res.indexOf("["), res.indexOf("]")+1));
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
            return new JSONArray();
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            adapter.clear();
            if (result.length() > 0) {
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject row = result.getJSONObject(i);
                        String title = row.getString("title");
                        String subtitle = row.getString("subtitle");
                        String isbn13 = row.getString("isbn13");
                        String price = row.getString("price");
                        String image = row.getString("image");
                        adapter.addBook(new Book(title, subtitle, isbn13, price, image));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Cursor cursor = db.query(dbHelper.Book_Table,
                        new String[] {dbHelper.Book_Title, dbHelper.Book_Subtitle, dbHelper.Book_Isbn13, dbHelper.Book_Price, dbHelper.Book_Cover},
                        null, null, null, null, null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        if (cursor.getString(0).toLowerCase().contains(origQuery)) {
                            String img = cursor.getString(4);
                            if (img != null) {
                                adapter.addBook(new Book(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
                            } else {
                                adapter.addBook(new Book(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), ""));
                            }

                        }
                        cursor.moveToNext();
                    }
                }
            }
            if (adapter.getItemCount() == 0) {
                noResults.setVisibility(View.VISIBLE);
            } else {
                noResults.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onBookClick(int position) {
        Intent intent = new Intent(this.getContext(), BookActivity.class);
        intent.putExtra("book", adapter.getBook(position));
        startActivity(intent);
    }

}