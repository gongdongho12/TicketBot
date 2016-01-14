package com.dongholab.ticketbot;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dongholab.ticketbot.adapter.TicketAdapter;
import com.dongholab.ticketbot.api.TicketbotAPI;
import com.dongholab.ticketbot.data.Check;
import com.dongholab.ticketbot.data.Soldout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    GregorianCalendar m_calendar;
    int m_year, m_month, m_day, m_hour, m_min;
    EditText m_start, m_end;
    LinearLayout m_dateArea;

    TextView m_date;

    ListView m_tiketList;

    //UI Adapter
    TicketAdapter m_ticketAdapter;

    //api주소
    String m_server_php = "http://1.214.121.11:44444/";
    String m_server_node = "http://1.214.121.11:50005/";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    //GCM 버튼
    private Button mRegistrationButton; //-
    private ProgressBar mRegistrationProgressBar; //-
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private TextView mInformationTextView;

    public void getInstanceIdToken() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }


    public void registBroadcastReceiver() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(QuickstartPreferences.REGISTRATION_READY)) {
                    // 액션이 READY일 경우
                    mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                    mInformationTextView.setVisibility(View.GONE);
                } else if (action.equals(QuickstartPreferences.REGISTRATION_GENERATING)) {
                    // 액션이 GENERATING일 경우
                    mRegistrationProgressBar.setVisibility(ProgressBar.VISIBLE);
                    mInformationTextView.setVisibility(View.VISIBLE);
                    mInformationTextView.setText(getString(R.string.registering_message_generating));
                } else if (action.equals(QuickstartPreferences.REGISTRATION_COMPLETE)) {
                    // 액션이 COMPLETE일 경우
                    mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                    mRegistrationButton.setText(getString(R.string.registering_message_complete));
                    mRegistrationButton.setEnabled(false);
                    String token = intent.getStringExtra("token");
                    mInformationTextView.setText(token);
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        registBroadcastReceiver();

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.statusbar));
        }

        m_calendar = new GregorianCalendar();
        m_year = m_calendar.get(Calendar.YEAR);
        m_month = m_calendar.get(Calendar.MONTH);
        m_day = m_calendar.get(Calendar.DAY_OF_MONTH);
        m_hour = m_calendar.get(Calendar.HOUR_OF_DAY);
        m_min = m_calendar.get(Calendar.MINUTE);

        m_start = (EditText) findViewById(R.id.start);
        m_end = (EditText) findViewById(R.id.end);

        m_dateArea = (LinearLayout) findViewById(R.id.datearea);

        m_date = (TextView) findViewById(R.id.date);
        m_date.setText(String.format("%04d년 %02d월 %02d일 %02d시", m_year, m_month + 1, m_day, m_hour));

        m_tiketList = (ListView) findViewById(R.id.ticketlist);
        m_tiketList.setOnItemClickListener(this);

        m_dateArea.setOnClickListener(this);

        /*
        // 토큰을 보여줄 TextView를 정의
        mInformationTextView = (TextView) findViewById(R.id.informationTextView);
        mInformationTextView.setVisibility(View.GONE);
        // 토큰을 가져오는 동안 인디케이터를 보여줄 ProgressBar를 정의
        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
        // 토큰을 가져오는 Button을 정의
        mRegistrationButton = (Button) findViewById(R.id.registrationButton);
        mRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInstanceIdToken(); //토큰 가져오기
            }
        });

        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(MainActivity.this, dateSetListener, m_year, m_month, m_day).show();
            }
        });
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(QuickstartPreferences.REGISTRATION_READY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(QuickstartPreferences.REGISTRATION_GENERATING));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));

    }

    /**
     * 앱이 화면에서 사라지면 등록된 LocalBoardcast를 모두 삭제한다.
     */
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }


    /**
     * Google Play Service를 사용할 수 있는 환경이지를 체크한다.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.datearea:
                if (m_start.getText().length() == 0 || m_end.getText().length() == 0) {
                    Toast.makeText(getBaseContext(), "역을 입력해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    new DatePickerDialog(MainActivity.this, dateSetListener, m_year, m_month, m_day).show();
                }
                break;

            default:
                break;
        }
    }

    //날짜 선택
    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            m_year = year;
            m_month = monthOfYear;
            m_day = dayOfMonth;

            //m_date.setText(String.format("%04d년 %02d월 %02d일 %02d시", m_year, m_month + 1, m_day, m_hour));
            //String msg = String.format("%d / %d / %d", year, monthOfYear+1, dayOfMonth);
            //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            TimePickerDialog timeDlg = new TimePickerDialog(MainActivity.this, timeSetListener, m_hour, m_min, false);
            timeDlg.show();
        }
    };

    //시간 선택
    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            //String msg = String.format("%d / %d / %d", m_year, m_month + 1, m_day);
            //String date = String.format("%04d-%02d-%02d", m_year, m_month + 1, m_day);
            //Log.d("date", date);

            //현재 API가 시간 입력이 없으므로 생략합니다.
            SoldoutList_Task soldoutList_task = new SoldoutList_Task();
            soldoutList_task.execute(m_server_php, m_start.getText().toString(), m_end.getText().toString(), String.format("%04d-%02d-%02d", m_year, m_month + 1, m_day), String.format("%02d", hourOfDay));
            m_date.setText(String.format("%04d년 %02d월 %02d일 %02d시", m_year, m_month + 1, m_day, hourOfDay));
            //Toast.makeText(MainActivity.this, m_start.getText() + " / " + m_end.getText() + " / " + msg + " / " + hourOfDay, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        
    }

    //Json 파싱
    public class SoldoutList_Task extends AsyncTask<String, Void, List<Soldout>> {

        Gson gson;
        List<Soldout> soldoutList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("Async", "시작");
            gson = new GsonBuilder()/*.setDateFormat("yyyy-MM-dd")*/.create();
        }

        @Override
        protected List<Soldout> doInBackground(String... params) {
            //params 0 시작위치, 1 나중위치
            for(String d : params) {
                Log.d("prameter", d);
            }
            //retrofit 적용
            RestAdapter retrofit = new RestAdapter.Builder()
                    .setEndpoint(params[0])
                    .setConverter(new GsonConverter(gson))
                    .build();

            TicketbotAPI ticketbot_api = retrofit.create(TicketbotAPI.class);

            Map<String, String> query = new HashMap<String, String>();
            query.put("api", "list");
            query.put("departure", params[1]);
            query.put("arrive", params[2]);
            query.put("date", params[3]);
            query.put("hour", params[4]);
            try {
                soldoutList = ticketbot_api.getSoldoutList(query);
            }catch (Exception e){
                soldoutList = null;
            }

            return soldoutList;
        }

        @Override
        protected void onPostExecute(List<Soldout> data) {
            super.onPostExecute(data);
            if (data == null) {
                Toast.makeText(MainActivity.this, "매진 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                m_ticketAdapter = new TicketAdapter(getBaseContext(), R.layout.row, new ArrayList<Soldout>());
            }else{
                m_ticketAdapter = new TicketAdapter(getBaseContext(), R.layout.row, data);
            }
            m_tiketList.setAdapter(m_ticketAdapter);
        }
    }

    //Json 파싱
    public class Reservate_Task extends AsyncTask<String, Void, Check> {

        Gson gson;
        Check check;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("Async", "시작");
            gson = new GsonBuilder()/*.setDateFormat("yyyy-MM-dd")*/.create();
        }

        @Override
        protected Check doInBackground(String... params) {
            //params 0 시작위치, 1 나중위치
            for(String d : params) {
                Log.d("prameter", d);
            }
            //retrofit 적용
            RestAdapter retrofit = new RestAdapter.Builder()
                    .setEndpoint(params[0])
                    .setConverter(new GsonConverter(gson))
                    .build();

            TicketbotAPI ticketbot_api = retrofit.create(TicketbotAPI.class);

            Map<String, String> query = new HashMap<String, String>();
            query.put("gcm_key", params[1]);
            query.put("title", params[2]);
            query.put("message", params[3]);
            Check check = ticketbot_api.getReservate(query);

            return check;
        }

        @Override
        protected void onPostExecute(Check data) {
            super.onPostExecute(data);
            Toast.makeText(MainActivity.this, "예약 완료", Toast.LENGTH_SHORT).show();
        }
    }
}