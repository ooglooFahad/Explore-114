package oogloo.com.explore114;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.twotoasters.jazzylistview.JazzyListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static com.facebook.FacebookSdk.getApplicationContext;

public class SurahActivity extends Fragment {
    JazzyListView lv;
    ProgressDialog pd;
    CustomAdapter c;
    ArrayList<Surah> ar_surah;
    ArrayList<SurahEN> ar_surahsEN;
    TextToSpeech TTS;

    public SurahActivity() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_surah, container, false);
        //show progress dialog
        pd = new ProgressDialog(getActivity(), R.style.pdtheme);
        pd.setCancelable(false);
        pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        pd.show();
        //initialization
        lv = (JazzyListView) v.findViewById(R.id.lvsurah);
        ar_surah = new ArrayList<>();
        ar_surahsEN = new ArrayList<>();
        //show surah
        ShowSurah();
//on item click in listview pass data through intent
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Surah surah = ar_surah.get(position);
                SurahEN surahEn = ar_surahsEN.get(position);
                Intent i = new Intent(getActivity(), ShowSurahDetail.class);
                i.putExtra("toppp", "");
                i.putExtra("statt", "");
                i.putExtra("PS_ID", surah.surah_id);
                i.putExtra("PS_Name", surah.surah_name + "  :  " + surahEn.surah_name_en);
                startActivity(i);
            }
        });

        return v;
    }

    // show surah name
    private void ShowSurah() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String RecievedString = "";
                HashMap<String, String> params = new HashMap<String, String>();
                Network network = new Network("showSurah.php", params);

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
                        String str_surahID = positionHashmap.get("surah_id");
                        String str_surahNAME = positionHashmap.get("surah_name");
                        String str_langID = positionHashmap.get("language_id");

                        Surah surah = new Surah();
                        surah.surah_id = str_surahID;
                        if (str_langID.equals("1")) {
                            surah.surah_name = str_surahNAME;
                            ar_surah.add(surah);
                        }
                        if (str_langID.equals("3")) {
                            SurahEN surahen = new SurahEN();
                            surahen.surah_name_en = str_surahNAME;
                            ar_surahsEN.add(surahen);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (getActivity() == null)
                        return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            c = new CustomAdapter(getActivity(), R.layout.cstm_parah, R.id.tv_parahNAME, ar_surah);
                            lv.setAdapter(c);
                            pd.dismiss();
                        }
                    });
                }
            }
        }).start();
    }

    //custom class to show surah name in custom listview
    class CustomAdapter extends ArrayAdapter<Surah> {
        public CustomAdapter(Context context, int resource, int textViewResourceId, ArrayList<Surah> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.cstm_parah, parent, false);
            Typeface custom_font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Bariol_Regular.otf");
            TextView tv_pparahName = (TextView) v.findViewById(R.id.tv_parahNAME);
            tv_pparahName.setTypeface(custom_font);
            tv_pparahName.setSelected(true);
            TextView tv_parahNameEN = (TextView) v.findViewById(R.id.tv_parahNAME_EN);
            tv_parahNameEN.setTypeface(custom_font);
            tv_parahNameEN.setSelected(true);

            Surah surah = ar_surah.get(position);
            tv_pparahName.setText(surah.surah_id + " : " + surah.surah_name);
            SurahEN surahen = ar_surahsEN.get(position);
            tv_parahNameEN.setText(surahen.surah_name_en);
//            String words = tv_parahNameEN.getText().toString();
//            StartSpeak(words);

            return v;
        }
    }

    //array class for surah name
    public class Surah {
        String surah_id;
        String surah_name;
    }

    //array class for surah name english
    public class SurahEN {
        String surah_name_en;
    }

    //clear progress dialog on destroy
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pd != null) {
            pd.dismiss();
            pd = null;
        }
    }

    private void StartSpeak(final String data) {

        TTS=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int initStatus) {
                if (initStatus == TextToSpeech.SUCCESS) {
                    if(TTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                        TTS.setLanguage(Locale.US);
                    TTS.setPitch(1.3f);
                    TTS.setSpeechRate(0.7f);
                    // start speak
                    speakWords(data);
                }
                else if (initStatus == TextToSpeech.ERROR) {
                    Toast.makeText(getApplicationContext(), "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
                }
            }


        });
    }
    private void speakWords(String speech) {
        TTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }
}