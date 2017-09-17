package oogloo.com.explore114;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchResult extends AppCompatActivity {
    ProgressDialog pd;
    CustomAdapter c;
    ListView lv_S;
    ArrayList<Translation> ar_translation;
    String verse_id, surah_id;
    ArrayList<Search_Verse> ar_searchVerse;
    String size="Explore 114";
    ShareDialog shareDialog;
    Context context = this;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        facebookSDKInitialize();
        setContentView(R.layout.activity_search_result);
        shareDialog = new ShareDialog(SearchResult.this);
        SpannableString s = new SpannableString(size);
        s.setSpan(new TypefaceSpan(this, "ALGER.TTF"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);
        //initialize
        ar_translation = new ArrayList<>();
        ar_searchVerse = new ArrayList<>();
        lv_S = (ListView) findViewById(R.id.lv_SearchResult);
        //progress dialog
        pd = new ProgressDialog(SearchResult.this, R.style.pdtheme);
        pd.setCancelable(false);
        pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        pd.show();
        //get intent data
        verse_id = getIntent().getExtras().getString("verseID");
        surah_id = getIntent().getExtras().getString("surahID");
        //get language from shared prefrence
        SharedPreferences settings = getSharedPreferences("language", 0);
        String lang = settings.getString("lang", "urdu"); //0 is the default value
        if (!isNetworkAvailable()) {
            show_alert();
        } else {
            if (lang.equals("ENGLISH")) {
                ShowVerseAR();
                ShowTranslationEN();
            } else {
                ShowVerseAR();
                ShowTranslationUR();
            }
        }
        isPermissionGranted();
    }

    //show on create option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    //show language option
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.englishTR) {
            ar_translation.clear();
            ar_searchVerse.clear();
            ShowVerseAR();
            ShowTranslationEN();

        }
        if (id == R.id.urduTR) {
            ar_translation.clear();
            ar_searchVerse.clear();
            ShowVerseAR();
            ShowTranslationUR();
        }
        return super.onOptionsItemSelected(item);
    }

    //show verse in arabic
    private void ShowVerseAR() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String RecievedString = "";
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("verseID", verse_id);
                params.put("surah_name", surah_id);
                Network network = new Network("search_verse.php", params);
                try {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    RecievedString = network.ToRecieveDataFromWeb();
                    JsonParsing jsonparsing = new JsonParsing(RecievedString);
                    ArrayList<HashMap<String, String>> convertedarraydata = jsonparsing.ParsejsonArray(RecievedString);
                    for (int i = 0; i < convertedarraydata.size(); i++) {
                        HashMap<String, String> positionHashmap;
                        positionHashmap = convertedarraydata.get(i);
                        String str_verseText = positionHashmap.get("verseText");
                        String str_verseID = positionHashmap.get("verse_id");

                        Search_Verse search_verse = new Search_Verse();
                        search_verse.verse_id = str_verseID;
                        search_verse.verse_textAR = str_verseText;
                        ar_searchVerse.add(search_verse);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            c = new CustomAdapter(SearchResult.this, R.layout.cstm_ayah, R.id.tv_ayahText, ar_searchVerse);
                            lv_S.setAdapter(c);
                            if (ar_searchVerse.size() == 0) {
                                lv_S.setVisibility(View.GONE);
                                Toast.makeText(SearchResult.this, "Verse No " + verse_id + " Not Found", Toast.LENGTH_SHORT).show();
                            }
                            if (pd != null) {
                                pd.dismiss();
                                pd = null;
                            }
                        }
                    });
                }
            }
        }).start();
    }

    //show verse translation in urdu
    private void ShowTranslationUR() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String RecievedString = "";
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("surahid", surah_id);
                params.put("verseid", verse_id);
                Network network = new Network("showVerseTranslationUR.php", params);

                try {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    RecievedString = network.ToRecieveDataFromWeb();
                    JsonParsing jsonparsing = new JsonParsing(RecievedString);
                    ArrayList<HashMap<String, String>> convertedarraydata = jsonparsing.ParsejsonArray(RecievedString);
                    for (int i = 0; i < convertedarraydata.size(); i++) {
                        HashMap<String, String> positionHashmap;
                        positionHashmap = convertedarraydata.get(i);

                        String str_ayahText = positionHashmap.get("verseText");
                        String str_verseID = positionHashmap.get("verse_id");
                        String str_surahID = positionHashmap.get("surah_id");


                        Translation ayahTR = new Translation();
                        ayahTR.ayah_textTR = str_ayahText;
                        ayahTR.verse_id = str_verseID;
                        ayahTR.surah_id = str_surahID;
                        ar_translation.add(ayahTR);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //show verse translation in english
    private void ShowTranslationEN() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String RecievedString = "";
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("surahid", surah_id);
                params.put("verseid", verse_id);
                Network network = new Network("showVerseTranslationEN.php", params);

                try {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    RecievedString = network.ToRecieveDataFromWeb();
                    JsonParsing jsonparsing = new JsonParsing(RecievedString);
                    ArrayList<HashMap<String, String>> convertedarraydata = jsonparsing.ParsejsonArray(RecievedString);
                    for (int i = 0; i < convertedarraydata.size(); i++) {
                        HashMap<String, String> positionHashmap;
                        positionHashmap = convertedarraydata.get(i);

                        String str_ayahText = positionHashmap.get("verseText");
                        String str_verseID = positionHashmap.get("verse_id");
                        String str_surahID = positionHashmap.get("surah_id");


                        Translation ayahTR = new Translation();
                        ayahTR.ayah_textTR = str_ayahText;
                        ayahTR.verse_id = str_verseID;
                        ayahTR.surah_id = str_surahID;
                        ar_translation.add(ayahTR);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //custom class to show data in custom list view
    class CustomAdapter extends ArrayAdapter<Search_Verse> {
        public CustomAdapter(Context context, int resource, int textViewResourceId, ArrayList<Search_Verse> objects) {
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
            tv_ayah_text.setTypeface(custom_font);
            final TextView tv_ayahTR = (TextView) v.findViewById(R.id.tv_ayahTR);
            tv_ayahTR.setTypeface(custom_font);
            final ImageView bookmark = (ImageView) v.findViewById(R.id.bookmark);
            final ImageView delete_bookmark = (ImageView) v.findViewById(R.id.delete_bookmark);
            try {
                Search_Verse search_verse = ar_searchVerse.get(position);
                tv_ayah_text.setText(search_verse.verse_id + " : " + search_verse.verse_textAR);
                Translation translation = ar_translation.get(position);
                tv_ayahTR.setText(translation.ayah_textTR);
                SQLiteDatabase db = openOrCreateDatabase("mydatabase", MODE_WORLD_WRITEABLE, null);
                Cursor c = db.rawQuery("select * from bookmarks ", null);
                try {
                    while (c.moveToNext()) {
                        String verseid = c.getString(0);
                        String surah_id = c.getString(1);
                        if (verseid.equals(search_verse.verse_id) && surah_id.equals(translation.surah_id)) {
                            bookmark.setVisibility(View.GONE);
                            delete_bookmark.setVisibility(View.VISIBLE);
                        }
                    }
                } finally {
                    c.close();
                }
            } catch (Exception e) {
                notifyDataSetChanged();
            }
            options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    try {
                        switch (v.getId()) {
                            case R.id.options:

                                PopupMenu popup = new PopupMenu(SearchResult.this, v);
                                popup.getMenuInflater().inflate(R.menu.popupmenu,
                                        popup.getMenu());
                                popup.show();
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {

                                        switch (item.getItemId()) {
                                            case R.id.popupdetail:

                                                try {
                                                    Translation search_verse = ar_translation.get(position);
                                                    Intent i = new Intent(SearchResult.this, VerseDetail_Search.class);
                                                    i.putExtra("VerseTR", search_verse.ayah_textTR);
                                                    i.putExtra("VerseID", search_verse.verse_id);
                                                    i.putExtra("SurahID", search_verse.surah_id);
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
                                                    delete_bookmark.setVisibility(View.GONE);
                                                    Bitmap bitmap = generateBitmap(top);
                                                    lv_icon.setVisibility(View.GONE);
                                                    Bitmap finalpic = makepakpic(background, bitmap);
                                                    ShareDialog(finalpic);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case R.id.whatsapp:
                                                try {
                                                    options.setVisibility(View.GONE);
                                                    bookmark.setVisibility(View.GONE);
                                                    delete_bookmark.setVisibility(View.GONE);
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
                                                    Search_Verse ayah = ar_searchVerse.get(position);
                                                    Translation ayah2 = ar_translation.get(position);
                                                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                                    sharingIntent.setType("text/plain");
                                                    String shareBody = tv_ayah_text.getText().toString() + "\n" + tv_ayahTR.getText().toString() + "\n"
                                                            +"Surah No : " + ayah2.surah_id + " Verse No : " + ayah2.verse_id
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
                    Search_Verse ayah_textAR = ar_searchVerse.get(position);
                    Translation ayah_textTR = ar_translation.get(position);
                    final SQLiteDatabase db = openOrCreateDatabase("mydatabase", Context.MODE_PRIVATE, null);
                    db.execSQL("create table if not exists bookmarks(verse_id INTEGER,surah_id INTEGER,verse_textAR TEXT);");
                    //database insert
                    db.execSQL("insert into bookmarks values('" + ayah_textTR.verse_id + "', '" + ayah_textTR.surah_id + "'," +
                            "'" + ayah_textAR.verse_textAR + "');");
                    bookmark.setVisibility(View.GONE);
                    delete_bookmark.setVisibility(View.VISIBLE);
                    Toast.makeText(SearchResult.this, "BOOKMARK SAVED", Toast.LENGTH_SHORT).show();
                }
            });
            delete_bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Translation ayah_textTR = ar_translation.get(position);
                    SQLiteDatabase db = openOrCreateDatabase("mydatabase", Context.MODE_PRIVATE, null);
                    db.execSQL("DELETE FROM bookmarks WHERE verse_id = '" + ayah_textTR.verse_id + "' AND surah_id = '" + ayah_textTR.surah_id + "';");
                    db.close();
                    bookmark.setVisibility(View.VISIBLE);
                    delete_bookmark.setVisibility(View.GONE);
                    Toast.makeText(SearchResult.this, "BOOKMARK REMOVED", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SearchResult.this, "Now you cant share on Whatsapp", Toast.LENGTH_SHORT).show();
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

    //array for arabic
    public class Search_Verse {
        String verse_id;
        String verse_textAR;
    }

    //array class for translation
    public class Translation {
        String verse_id;
        String ayah_textTR;
        String surah_id;
    }

    //show alert when internet is not conneted
    private void show_alert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(SearchResult.this, R.style.mydialog);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            alert.setMessage(Html.fromHtml("<b>"+"PLEASE CONNECT TO INTERNET!"+"</b>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            alert.setMessage(Html.fromHtml("<b>"+"PLEASE CONNECT TO INTERNET!"+"</b>"));
        }
        alert.setCancelable(false);
        alert.setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                startActivity(getIntent());
            }
        });
        alert.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
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

    //check internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

