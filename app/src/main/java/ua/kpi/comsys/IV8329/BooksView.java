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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.List;

public class BooksView extends RecyclerView.Adapter<BooksView.ViewHolder> {

    private final LayoutInflater inflater;
    private List<Book> books;
    private BookListener bookListener;
    private static DatabaseHelper dbHelper;
    private static SQLiteDatabase db;
    private final String ImgStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MP_Labs/books/";
    private final File storageDir = new File(ImgStoragePath);

    public BooksView(Context context, BookListener bookListener) {
        books = new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
        this.bookListener = bookListener;
        this.dbHelper = new DatabaseHelper(context, DatabaseHelper.DB_Name, null, 1);
        this.db = dbHelper.getWritableDatabase();
    }

    @Override
    public BooksView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.book_entry, parent, false);
        view.getResources();
        return new ViewHolder(view, bookListener);
    }

    @Override
    public void onBindViewHolder(BooksView.ViewHolder holder, int position) {
        Book book = books.get(position);
        ContentValues values = new ContentValues();
        values.put(dbHelper.Book_Title, book.getTitle());
        values.put(dbHelper.Book_Subtitle, book.getSubtitle());
        values.put(dbHelper.Book_Isbn13, book.getIsbn13());
        values.put(dbHelper.Book_Price, book.getPrice());
        db.insert(dbHelper.Book_Table, null, values);
        String tmp = book.getImage();
        if (tmp != null) {
            holder.coverView.setVisibility(View.VISIBLE);
            if (tmp.contains("http")) {
                String bookIsbn = book.getIsbn13();
                String fileName = bookIsbn + ".png";
                String filePath = storageDir + fileName;
                Glide.with(holder.coverView.getContext())
                        .load(tmp)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_action_load)
                        .thumbnail(Glide.with(holder.coverView.getContext()).load(R.raw.spinner_icon))
                        .dontAnimate()
                        .into(holder.coverView);
                Cursor cursor = db.query(dbHelper.Book_Table, null, dbHelper.Book_Cover + " = \"" + filePath + "\"", null, null, null, null);
                if (cursor.getCount() == 0) {
                    Glide.with(holder.coverView.getContext())
                            .load(tmp)
                            .into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                    saveIMGtoStorage(fileName, bitmap);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                }
                            });
                }
            } else {
                Uri imgUri = Uri.fromFile(new File(tmp));
                Glide.with(holder.coverView.getContext())
                        .load(imgUri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_action_load)
                        .thumbnail(Glide.with(holder.coverView.getContext()).load(R.raw.spinner_icon))
                        .dontAnimate()
                        .into(holder.coverView);
            }
        } else {
            holder.coverView.setVisibility(View.INVISIBLE);
        }
        if (!book.getTitle().isEmpty()) {
            holder.titleView.setText(book.getTitle());
            holder.titleView.setTextColor(holder.titleView.getResources().getColor(R.color.black));
        } else {
            holder.titleView.setText(holder.titleView.getContext().getResources().getString(R.string.place_title));
            holder.titleView.setTextColor(holder.titleView.getResources().getColor(R.color.pale_grey));
        }
        if (!book.getSubtitle().isEmpty()) {
            holder.subtitleView.setText(book.getSubtitle());
            holder.subtitleView.setTextColor(holder.titleView.getResources().getColor(R.color.black));
        } else {
            holder.subtitleView.setText(holder.titleView.getContext().getResources().getString(R.string.place_subtitle));
            holder.subtitleView.setTextColor(holder.titleView.getResources().getColor(R.color.pale_grey));
        }
        if (!book.getIsbn13().isEmpty()) {
            tmp = holder.isbnView.getResources().getString(R.string.isbn) + book.getIsbn13();
            holder.isbnView.setText(tmp);
            holder.isbnView.setTextColor(holder.titleView.getResources().getColor(R.color.black));
        } else {
            holder.isbnView.setText(holder.titleView.getContext().getResources().getString(R.string.place_isbn));
            holder.isbnView.setTextColor(holder.titleView.getResources().getColor(R.color.pale_grey));
        }
        if (!book.getPrice().isEmpty()) {
            holder.priceView.setText(book.getPrice());
            holder.priceView.setTextColor(holder.titleView.getResources().getColor(R.color.black));
        } else {
            holder.priceView.setText(holder.titleView.getContext().getResources().getString(R.string.place_price));
            holder.priceView.setTextColor(holder.titleView.getResources().getColor(R.color.pale_grey));
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView coverView;
        final TextView titleView, subtitleView, isbnView, priceView;
        BookListener bookListener;

        ViewHolder(View view, BookListener bookListener) {
            super(view);
            coverView = view.findViewById(R.id.cover);
            titleView = view.findViewById(R.id.title);
            subtitleView = view.findViewById(R.id.subtitle);
            isbnView = view.findViewById(R.id.isbn);
            priceView = view.findViewById(R.id.price);
            this.bookListener = bookListener;
            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            bookListener.onBookClick(getAdapterPosition());
        }

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public interface BookListener {
        void onBookClick(int position);
    }

    public void addBook(Book book) {
        this.books.add(book);
        this.notifyDataSetChanged();
    }

    public Book getBook(int position) {
        return this.books.get(position);
    }

    public void clear() {
        this.books = new ArrayList<>();
        this.notifyDataSetChanged();
    }

    public void saveIMGtoStorage(String fileName, Bitmap bitmap) {
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
                values.put(dbHelper.Book_Cover, savedImagePath);
                db.update(dbHelper.Book_Table, values, dbHelper.Book_Isbn13 + " = " + fileName.substring(0, fileName.indexOf(".")), null);
            } else {
                try {
                    imageFile.createNewFile();
                    OutputStream fOut = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.close();
                    ContentValues values = new ContentValues();
                    values.put(dbHelper.Book_Cover, savedImagePath);
                    db.update(dbHelper.Book_Table, values, dbHelper.Book_Isbn13 + " = " + fileName.substring(0, fileName.indexOf(".")), null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}