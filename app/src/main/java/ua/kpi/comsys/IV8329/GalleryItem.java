package ua.kpi.comsys.IV8329;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GalleryItem extends Fragment {

    private static RecyclerView recyclerView;
    private static GalleryView adapter;
    private static int screenWidth;
    private final String ImgRequest = "https://pixabay.com/api/?key=19193969-87191e5db266905fe8936d565&q=hot+summer&image_type=photo&per_page=24";
    private static DatabaseHelper dbHelper;
    private static SQLiteDatabase db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) requireContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;

        dbHelper = new DatabaseHelper(this.getActivity().getApplicationContext(), DatabaseHelper.DB_Name, null, 1);
        db = dbHelper.getWritableDatabase();

        recyclerView = root.findViewById(R.id.gallery);
        if (adapter == null) {
            adapter = new GalleryView(this.getActivity().getApplicationContext(), screenWidth);
            new JSONTask().execute(ImgRequest);
        } else {
            new JSONTask().execute(ImgRequest);
            adapter.setMaxWidth(screenWidth);
            adapter.notifyDataSetChanged();
        }
        recyclerView.setAdapter(adapter);

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
            if (result.length() > 0) {
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject row = result.getJSONObject(i);
                        String imgURL = row.getString("webformatURL");
                        String imgID = Integer.valueOf(row.getInt("id")).toString();
                        adapter.addImg(imgURL, imgID);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Cursor cursor = db.query(dbHelper.Img_Table,
                        new String[] {dbHelper.Img_Location, dbHelper.Img_Id},
                        null, null, null, null, null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        adapter.addImg(cursor.getString(0), cursor.getString(1));
                        cursor.moveToNext();
                    }
                }
            }
        }
    }

}
