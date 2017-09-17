package oogloo.com.explore114;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.twotoasters.jazzylistview.JazzyListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class ParahActivity extends Fragment {
    JazzyListView lv;
    ProgressDialog pd;
    CustomAdapter c;
    ArrayList<Parah> ar_parahs;
    ArrayList<ParahEN> ar_parahsEN;

    public ParahActivity() {
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_parah, container, false);
        //progress dialog
        pd = new ProgressDialog(getActivity(), R.style.pdtheme);
        pd.setCancelable(false);
        pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        pd.show();
        //initiallize
        lv = (JazzyListView) v.findViewById(R.id.lvparah);
        ar_parahs = new ArrayList<>();
        ar_parahsEN = new ArrayList<>();
        if (!isNetworkAvailable()) {
            show_alert();
        } else {
            ShowParah();
        }
        //pass data in intent
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Parah parah = ar_parahs.get(position);
                ParahEN parahEn = ar_parahsEN.get(position);
                Intent i = new Intent(getActivity(), ShowParahDetail.class);
                i.putExtra("toppp", "");
                i.putExtra("statt", "");
                i.putExtra("PS_ID", parah.parah_id);
                i.putExtra("PS_Name", parah.parah_name + "  :  " + parahEn.parah_name_en);
                startActivity(i);
            }
        });
        return v;
    }

    //show parah name
    private void ShowParah() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String RecievedString = "";
                HashMap<String, String> params = new HashMap<String, String>();
                Network network = new Network("showParah.php", params);

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
                        String str_parahID = positionHashmap.get("parah_id");
                        String str_parahNAME = positionHashmap.get("parah_name");
                        String str_langID = positionHashmap.get("language_id");
                        Parah parah = new Parah();
                        parah.parah_id = str_parahID;
                        if (str_langID.equals("1")) {
                            parah.parah_name = str_parahNAME;
                            ar_parahs.add(parah);
                            Log.d("run: ", str_parahNAME);
                        }
                        if (str_langID.equals("3")) {
                            ParahEN parahen = new ParahEN();
                            parahen.parah_name_en = str_parahNAME;
                            ar_parahsEN.add(parahen);
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
                            c = new CustomAdapter(getActivity(), R.layout.cstm_parah, R.id.tv_parahNAME, ar_parahs);
                            lv.setAdapter(c);
                            pd.dismiss();
                        }
                    });
                }
            }
        }).start();
    }

    //custom class for parah name
    class CustomAdapter extends ArrayAdapter<Parah> {
        public CustomAdapter(Context context, int resource, int textViewResourceId, ArrayList<Parah> objects) {
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

            Parah parah = ar_parahs.get(position);
            tv_pparahName.setText(parah.parah_id + " : " + parah.parah_name);
            ParahEN parahen = ar_parahsEN.get(position);
            tv_parahNameEN.setText(parahen.parah_name_en);

            return v;
        }
    }

    //parah array
    public class Parah {
        String parah_id;
        String parah_name;
    }

    // parah name englisg
    public class ParahEN {
        String parah_name_en;
    }

    //clear dialog when app destroy
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pd != null) {
            pd.dismiss();
            pd = null;
        }
    }

    //show alert when internet not connected
    private void show_alert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.mydialog);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            alert.setMessage(Html.fromHtml("<b>"+"PLEASE CONNECT TO INTERNET!"+"</b>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            alert.setMessage(Html.fromHtml("<b>"+"PLEASE CONNECT TO INTERNET!"+"</b>"));
        }
        alert.setCancelable(false);
        alert.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
                startActivity(getActivity().getIntent());
            }
        });
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
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

    //check internet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}