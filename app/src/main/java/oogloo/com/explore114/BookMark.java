package oogloo.com.explore114;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static oogloo.com.explore114.R.id.delete_bookmark;

public class BookMark extends AppCompatActivity {
    ArrayList<String> verse_id, surah_id, verse_text;
    ListView lv;
    CustomAdapter c_adap;
    Cursor c;
    int top, index;
    int stat, topp;
    ShareDialog shareDialog;
    Context context = this;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        facebookSDKInitialize();
        setContentView(R.layout.activity_book_mark);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        shareDialog = new ShareDialog(BookMark.this);
        lv = (ListView) findViewById(R.id.lv_bookmark);
        //get last state of lv from shared prefrences and show that position item in listview
        SharedPreferences settings = getSharedPreferences("lv_state", 0);
        stat = settings.getInt("index", 0);
        topp = settings.getInt("top", 0);
        showData();

        isPermissionGranted();

    }

    //show all data from sqlite
    private void showData() {
        SQLiteDatabase db = openOrCreateDatabase("mydatabase", Context.MODE_PRIVATE, null);
        c = db.rawQuery("select * from bookmarks ", null);
        verse_id = new ArrayList<>();
        surah_id = new ArrayList<>();
        verse_text = new ArrayList<>();

        try {
            while (c.moveToNext()) {
                verse_id.add(c.getString(0));
                surah_id.add(c.getString(1));
                verse_text.add(c.getString(2));
                c_adap = new CustomAdapter(BookMark.this, R.layout.cstm_ayah, R.id.tv_ayahText, verse_text);
                lv.setAdapter(c_adap);
                if (stat != 0) {
                    lv.setSelectionFromTop(stat, topp);
                }
            }
        } finally {
            c.close();
            if (verse_id.size() == 0) {
                lv.setVisibility(View.GONE);
                Toast.makeText(this, "No Bookmarks", Toast.LENGTH_SHORT).show();
            }
            String title = "   BOOKMARKS";
            String size = String.valueOf(verse_id.size()) + title;
            SpannableString s = new SpannableString(size);
            s.setSpan(new TypefaceSpan(this, "ALGER.TTF"), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            setTitle(s);
        }
    }

    // custom adapter class for custom list view
    class CustomAdapter extends ArrayAdapter<String> {

        CustomAdapter(Context context, int resource, int textViewResourceId, ArrayList<String> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.cstm_ayah, parent, false);
            Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Bariol_Regular.otf");
            final RelativeLayout top = (RelativeLayout) v.findViewById(R.id.rel_top);
            final LinearLayout lv_icon = (LinearLayout) v.findViewById(R.id.lv_icon);
            final ImageView options = (ImageView) v.findViewById(R.id.options);
            final TextView tv_ayah_text = (TextView) v.findViewById(R.id.tv_ayahText);
            final TextView tv_ayahTR = (TextView) v.findViewById(R.id.tv_ayahTR);
            tv_ayahTR.setTypeface(custom_font);
            final ImageView bookmark = (ImageView) v.findViewById(delete_bookmark);
            final ImageView mbook=(ImageView) v.findViewById(R.id.bookmark);
            bookmark.setVisibility(View.VISIBLE);
            try {
                tv_ayah_text.setText(verse_text.get(position));
                tv_ayahTR.setText("Surah : " + surah_id.get(position) + "   " + "Verse : " + verse_id.get(position));
            } catch (Exception e) {
                notifyDataSetChanged();
            }
            options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    try {
                        switch (v.getId()) {
                            case R.id.options:

                                PopupMenu popup = new PopupMenu(BookMark.this, v);
                                popup.getMenuInflater().inflate(R.menu.popupmenu,
                                        popup.getMenu());
                                popup.show();
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {

                                        switch (item.getItemId()) {
                                            case R.id.popupdetail:

                                                try {
                                                    Intent i = new Intent(BookMark.this, VerseDetail_Search.class);
                                                    i.putExtra("VerseTR", verse_text.get(position));
                                                    i.putExtra("VerseID", verse_id.get(position));
                                                    i.putExtra("SurahID", surah_id.get(position));
                                                    startActivity(i);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                break;
                                            case R.id.fb:
                                                try {
                                                    lv_icon.setVisibility(View.VISIBLE);
                                                    Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.backgr);
                                                    options.setVisibility(View.GONE);
                                                    bookmark.setVisibility(View.GONE);
                                                    mbook.setVisibility(View.GONE);
                                                    Bitmap bitmap = generateBitmap(top);
                                                    Bitmap finalpic = makepakpic(background, bitmap);
                                                    lv_icon.setVisibility(View.GONE);
                                                    ShareDialog(finalpic);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case R.id.whatsapp:
                                                try {
                                                    options.setVisibility(View.GONE);
                                                    bookmark.setVisibility(View.GONE);
                                                    lv_icon.setVisibility(View.VISIBLE);
                                                    Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.backgr);
                                                    Bitmap bitmap = generateBitmap(top);
                                                    Bitmap finalpic = makepakpic(background, bitmap);
                                                    lv_icon.setVisibility(View.GONE);

                                                    Uri uri = getImageUri(v.getContext(), finalpic);
                                                    String Text = "Get this Quran app for your Android phone from Playstore:" + "\n" + "https://play.google.com/store/apps/details?id=oogloo.com.explore114";

                                                    Intent shareIntent = new Intent();
                                                    shareIntent.setAction(Intent.ACTION_SEND);
                                                    //Target whatsapp:
                                                    shareIntent.setPackage("com.whatsapp");
                                                    //Add text and then Image URI
                                                    shareIntent.putExtra(Intent.EXTRA_TEXT, Text);
                                                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                                    shareIntent.setType("image/jpeg");
                                                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                                    startActivity(shareIntent);

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case R.id.others:
                                                try {
                                                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                                    sharingIntent.setType("text/plain");
                                                    String shareBody = tv_ayah_text.getText().toString() + "\n" + tv_ayahTR.getText().toString() + "\n"
                                                            + "Surah No : " + surah_id.get(position) + " Verse No : " + verse_id.get(position)
                                                            + "\n\n" + "Explore 114" + "\n" + "OOGLOO Web and Beyond";
                                                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "EXPLORE 114");
                                                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                                                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            default:
                                                break;
                                        }
                                        return true;
                                    }
                                });
                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(BookMark.this, R.style.mydialog);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        alert.setMessage(Html.fromHtml("<b>"+"DO YOU WANT TO DELETE BOOKMARK ? "+"</b>", Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        alert.setMessage(Html.fromHtml("<b>"+"DO YOU WANT TO DELETE BOOKMARK ? "+"</b>"));
                    }
                    alert.setCancelable(true);
                    alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SQLiteDatabase db = openOrCreateDatabase("mydatabase", Context.MODE_PRIVATE, null);
                            db.execSQL("DELETE FROM bookmarks WHERE verse_id = '" + verse_id.get(position) + "' AND surah_id = '" + surah_id.get(position) + "';");
                            db.close();
                            c_adap.notifyDataSetChanged();
                            showData();
                        }
                    });
                    alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    AlertDialog alertDialog = alert.create();
                    alertDialog.show();
                    Button b_pos;
                    b_pos = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (b_pos != null) {
                        b_pos.setTextColor(getResources().getColor(R.color.colorAccent));
                    }
                    Button b_neg;
                    b_neg = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    if (b_neg != null) {
                        b_neg.setTextColor(getResources().getColor(R.color.colorAccent));
                    }
                }
            });
            return v;
        }
    }

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted");
                return true;
            } else {

                Log.v("TAG", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG", "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(BookMark.this, "Now you cant share on Whatsapp", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private Bitmap makepakpic(Bitmap firstImage, Bitmap secondImage) {

        int width = secondImage.getWidth();
        int height = secondImage.getHeight();
        firstImage = Bitmap.createScaledBitmap(
                firstImage, width, height, false);
        Bitmap result = Bitmap.createBitmap(firstImage.getWidth(), firstImage.getHeight(), firstImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(firstImage, 0, 0, null);
        canvas.drawBitmap(secondImage, 0, 0, null);
        return result;
    }

    protected void facebookSDKInitialize() {

        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();
    }

    public void ShareDialog(Bitmap imagePath) {

        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(imagePath)
                .setCaption("EXPLORE 114")
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        shareDialog.show(content);
    }

    private Bitmap generateBitmap(RelativeLayout v) {

        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(),
                v.getHeight()
                , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    //save the last state of list view in shared prefrence
    @Override
    public void onBackPressed() {
        index = lv.getFirstVisiblePosition();
        View v = lv.getChildAt(0);
        top = (v == null) ? 0 : (v.getTop() - lv.getPaddingTop());
        SharedPreferences settings = getSharedPreferences("lv_state", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("index", index);
        editor.putInt("top", top);
        editor.commit();
        super.onBackPressed();
    }
}