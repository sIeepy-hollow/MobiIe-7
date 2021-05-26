package ua.kpi.comsys.IV8329;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class GalleryView extends RecyclerView.Adapter<GalleryView.ViewHolder> {

    private final LayoutInflater inflater;
    private ArrayList<ArrayList<String>> pictures = new ArrayList<>();
    private ArrayList<ArrayList<String>> ids = new ArrayList<>();
    private int maxWidth;
    private final double coef;
    private static int curr_img = 0;
    private static Context context;
    private static DatabaseHelper dbHelper;
    private static SQLiteDatabase db;
    private final String ImgStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MP_Labs/";
    private final File storageDir = new File(ImgStoragePath);

    public GalleryView(Context context, int width) {
        this.inflater = LayoutInflater.from(context);
        this.coef = ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        this.maxWidth = (int) (width - 20*coef);
        this.context = context;
        this.dbHelper = new DatabaseHelper(context, DatabaseHelper.DB_Name, null, 1);
        this.db = dbHelper.getWritableDatabase();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.gallery_section, parent, false);
        view.getResources();
        return new GalleryView.ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView[] imgs = new ImageView[8];
        ViewHolder(View view){
            super(view);
            imgs[0] = view.findViewById(R.id.img_1);
            imgs[1] = view.findViewById(R.id.img_2);
            imgs[2] = view.findViewById(R.id.img_3);
            imgs[3] = view.findViewById(R.id.img_4);
            imgs[4] = view.findViewById(R.id.img_5);
            imgs[5] = view.findViewById(R.id.img_6);
            imgs[6] = view.findViewById(R.id.img_7);
            imgs[7] = view.findViewById(R.id.img_8);
        }
    }

    @Override
    public void onBindViewHolder(GalleryView.ViewHolder holder, int position) {
        ArrayList<String> section_pics = pictures.get(position);
        ArrayList<String> section_ids = ids.get(position);
        for (int i=0; i<section_pics.size(); i++) {
            String fileName = section_ids.get(i) + ".png";
            if (section_pics.get(i).contains("http")) {
                Glide.with(holder.imgs[i].getContext())
                        .load(section_pics.get(i))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_action_load)
                        .thumbnail(Glide.with(holder.imgs[i].getContext()).load(R.raw.spinner_icon))
                        .dontAnimate()
                        .into(holder.imgs[i]);
                Cursor cursor = db.query(dbHelper.Img_Table, null, dbHelper.Img_Id + " = " + section_ids.get(i), null, null, null, null);
                if (cursor.getCount() == 0) {
                    Glide.with(holder.imgs[i].getContext())
                            .load(section_pics.get(i))
                            .into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                    saveIMGtoStorage(fileName, bitmap);
                                }
                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) { }
                            });
                }
            } else {
                Uri imgUri = Uri.fromFile(new File(section_pics.get(i)));
                Glide.with(holder.imgs[i].getContext())
                        .load(imgUri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_action_load)
                        .thumbnail(Glide.with(holder.imgs[i].getContext()).load(R.raw.spinner_icon))
                        .dontAnimate()
                        .into(holder.imgs[i]);
            }
            holder.imgs[i].getLayoutParams().width = maxWidth/4;
            holder.imgs[i].getLayoutParams().height = maxWidth/4;
        }
        if (section_pics.size() > 1) {
            holder.imgs[1].getLayoutParams().width = 3*maxWidth/4;
            holder.imgs[1].getLayoutParams().height = 3*maxWidth/4;
        }
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void addImg(String location, String id) {
        int i = -1;
        int j = -1;
        for (int ind = 0; ind<ids.size(); ind++) {
            if (ids.get(ind).contains(id)) {
                i = ind;
                j = ids.get(ind).indexOf(id);
            }
        }
        if (i >= 0) {
            pictures.get(i).remove(pictures.get(i).get(j));
            if (j == pictures.get(i).size()) {
                pictures.get(i).add(location);
            } else {
                pictures.get(i).add(j, location);
            }
        } else {
            if (curr_img == 8 || pictures.size()==0) {
                pictures.add(new ArrayList<>());
                ids.add(new ArrayList<>());
                curr_img = 0;
            }
            pictures.get(pictures.size()-1).add(location);
            ids.get(ids.size()-1).add(id);
            curr_img++;
        }
        this.notifyDataSetChanged();
    }


    public void setMaxWidth(int maxWidth) {
        this.maxWidth = (int) (maxWidth - 20*coef);
    }

    public boolean isEmpty() {
        return this.pictures.size() == 0;
    }

    public void saveIMGtoStorage (String fileName, Bitmap bitmap) {
        boolean successDirCreated = false;
        if (!storageDir.exists()) {
            successDirCreated = storageDir.mkdirs();
        } else {
            successDirCreated = true;
        }
        if (successDirCreated) {
            File imageFile = new File(storageDir, fileName);
            String savedImagePath = imageFile.getAbsolutePath();
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(dbHelper.Img_Id, fileName.substring(0, fileName.indexOf(".")));
                values.put(dbHelper.Img_Location, savedImagePath);
                db.insert(dbHelper.Img_Table, null, values);
            } else {
                try {
                    imageFile.createNewFile();
                    OutputStream fOut = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.close();
                    ContentValues values = new ContentValues();
                    values.put(dbHelper.Img_Id, fileName.substring(0, fileName.indexOf(".")));
                    values.put(dbHelper.Img_Location, savedImagePath);
                    db.insert(dbHelper.Img_Table, null, values);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
