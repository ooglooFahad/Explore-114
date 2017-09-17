package oogloo.com.explore114;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.twotoasters.jazzylistview.JazzyListView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchWordUR extends AppCompatActivity {
    ProgressDialog pd;
    CustomAdapter c;
    JazzyListView lv_SW;
    ArrayList<WordTR> ar_wordTR;
    String word;
    String size="Explore 114";
    ShareDialog shareDialog;
    Context context = this;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        facebookSDKInitialize();
        setContentView(R.layout.activity_search_word_result);
        shareDialog = new ShareDialog(SearchWordUR.this);
        SpannableString s = new SpannableString(size);
        s.setSpan(new TypefaceSpan(this, "ALGER.TTF"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);
        ar_wordTR = new ArrayList<>();
        word = getIntent().getExtras().getString("word");
        pd = new ProgressDialog(SearchWordUR.this, R.style.pdtheme);
        pd.setCancelable(false);
        pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        pd.show();
        lv_SW = (JazzyListView) findViewById(R.id.lv_searchWord);

        if (!isNetworkAvailable()) {
            show_alert();
        } else {
            ShowWordEN();
        }
        isPermissionGranted();
    }

    // show translation in english
    private void ShowWordEN() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String RecievedString = "";
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("word", word);
                Network network = new Network("search_wordUR.php", params);
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
                        String str_surahID = positionHashmap.get("surah_id");


                        WordTR wordTR = new WordTR();
                        wordTR.verse_id = str_verseID;
                        wordTR.verse_textTR = str_verseText;
                        wordTR.surah_id = str_surahID;
                        ar_wordTR.add(wordTR);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            c = new CustomAdapter(SearchWordUR.this, R.layout.cstm_ayah, R.id.tv_ayahText, ar_wordTR);
                            lv_SW.setAdapter(c);
                            String size=String.valueOf(ar_wordTR.size()) + " Records Found";
                            SpannableString s = new SpannableString(size);
                            s.setSpan(new TypefaceSpan(SearchWordUR.this, "ALGER.TTF"), 0, s.length(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            setTitle(s);
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

    //custom class to show data in listview
    class CustomAdapter extends ArrayAdapter<WordTR> {
        public CustomAdapter(Context context, int resource, int textViewResourceId, ArrayList<WordTR> objects) {
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

            final TextView ayahTextTR = (TextView) v.findViewById(R.id.tv_ayahText);
            ayahTextTR.setTypeface(custom_font);
            final TextView surah_parah = (TextView) v.findViewById(R.id.tv_ayahTR);
            surah_parah.setTypeface(custom_font);

            final ImageView bookmark = (ImageView) v.findViewById(R.id.bookmark);
            final ImageView delete_bookmark = (ImageView) v.findViewById(R.id.delete_bookmark);
            try {
                WordTR search_verse = ar_wordTR.get(position);
                ayahTextTR.setText(search_verse.verse_textTR);
                surah_parah.setText("Surah : " + search_verse.surah_id + "   " + "Aayat : " + search_verse.verse_id );
                SQLiteDatabase db = openOrCreateDatabase("mydatabase", Context.MODE_PRIVATE, null);
                Cursor c = db.rawQuery("select * from bookmarks ", null);
                try {
                    while (c.moveToNext()) {
                        String verseid = c.getString(0);
                        String surah_id = c.getString(1);
                        if (verseid.equals(search_verse.verse_id) && surah_id.equals(search_verse.surah_id)) {
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

                                PopupMenu popup = new PopupMenu(SearchWordUR.this, v);
                                popup.getMenuInflater().inflate(R.menu.popupmenu,
                                        popup.getMenu());
                                popup.show();
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {

                                        switch (item.getItemId()) {
                                            case R.id.popupdetail:

                                                try {
                                                    WordTR search_verse = ar_wordTR.get(position);
                                                    Intent i = new Intent(SearchWordUR.this, VerseDetail_Search.class);
                                                    i.putExtra("VerseTR", search_verse.verse_textTR);
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
                                                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                                    sharingIntent.setType("text/plain");
                                                    String shareBody = ayahTextTR.getText().toString() + "\n" + surah_parah.getText().toString()
                                                            +"\n\n"+"Explore 114"+"\n"+"OOGLOO Web and Beyond";
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
                    WordTR ayah_textAR = ar_wordTR.get(position);
                    final SQLiteDatabase db = openOrCreateDatabase("mydatabase", Context.MODE_PRIVATE, null);
                    db.execSQL("create table if not exists bookmarks(verse_id INTEGER,surah_id INTEGER,verse_textAR TEXT);");
                    //database insert
                    db.execSQL("insert into bookmarks values('" + ayah_textAR.verse_id + "', '" + ayah_textAR.surah_id + "'," +
                            "'" + ayah_textAR.verse_textTR + "');");
                    bookmark.setVisibility(View.GONE);
                    delete_bookmark.setVisibility(View.VISIBLE);
                    Toast.makeText(SearchWordUR.this, "BOOKMARK SAVED", Toast.LENGTH_SHORT).show();
                }
            });

            delete_bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WordTR ayah_textAR = ar_wordTR.get(position);
                    SQLiteDatabase db = openOrCreateDatabase("mydatabase", Context.MODE_PRIVATE, null);
                    db.execSQL("DELETE FROM bookmarks WHERE verse_id = '" + ayah_textAR.verse_id + "' AND surah_id = '" + ayah_textAR.surah_id + "';");
                    db.close();
                    bookmark.setVisibility(View.VISIBLE);
                    delete_bookmark.setVisibility(View.GONE);
                    Toast.makeText(SearchWordUR.this, "BOOKMARK REMOVED", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SearchWordUR.this, "Now you cant share on Whatsapp", Toast.LENGTH_SHORT).show();
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
    // array class for translation
    public class WordTR {
        String verse_id;
        String verse_textTR;
        String surah_id;
    }

    //show alert if internet not connected
    private void show_alert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(SearchWordUR.this, R.style.mydialog);
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
}