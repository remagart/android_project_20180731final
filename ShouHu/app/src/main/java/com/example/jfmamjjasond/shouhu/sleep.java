package com.example.jfmamjjasond.shouhu;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import static java.util.Calendar.HOUR_OF_DAY;

public class sleep extends android.support.v4.app.Fragment {

    Context thisactivity;
    Button btn_sleep,btn_wake;
    TextView txt_sleep,txt_wake,txt_reminder;
    //資料庫的橋接器
    ShouHou_DBAdapter myadapter;
    //為了要取得日期
    Calendar myCalendar;
    //這是測試用的name，之後會將BMI資料傳過來
    String user_name;
    String ShouHu_user_name;
    ListView sleep_record;
    SimpleCursorAdapter mysimplecursoradapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_sleep, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        thisactivity = getContext();

        //初始化UI和java
        init();
        //初始化資料庫橋接器
        myadapter = new ShouHou_DBAdapter(thisactivity);//需要知道是哪位使用者
        get_from_activity();

        //取得欲搜尋姓名的cursor
        Cursor mycursor = myadapter.querybyname_from_user_table(user_name);
        //要做if判斷有無cursor值，因為沒有值會有error
        //這邊是要做頁面載入會顯示時間和提醒
        if(mycursor.getCount() != 0){
            txt_sleep.setText(mycursor.getString(2));
            txt_wake.setText(mycursor.getString(3));
            //若是睡眠時間或醒來時間是null，跑到reminder會exception
            if(mycursor.getString(2) != null && mycursor.getString(3) != null){
                //若是睡眠時間或醒來時間是""，跑到reminder會exception
                if(!mycursor.getString(2).equals("") && !mycursor.getString(3).equals("")) {
                    reminder(mycursor);
                }
            }
        }

        loading_during_record();

        //按鈕事件處理
        btn_sleep.setOnClickListener(myclickevent);
        btn_wake.setOnClickListener(myclickevent);

        mydisplaylistview();

    }

    void init(){
        btn_sleep = (Button)getView().findViewById(R.id.sleepxml_btn_sleep);
        btn_wake = (Button)getView().findViewById(R.id.sleepxml_btn_wake);
        txt_sleep = (TextView)getView().findViewById(R.id.sleepxml_txt_sleep);
        txt_wake = (TextView)getView().findViewById(R.id.sleepxml_txt_wake);
        txt_reminder = (TextView)getView().findViewById(R.id.sleepxml_txt_reminder);
        sleep_record = (ListView)getView().findViewById(R.id.sleepxml_list_record);
    }

    View.OnClickListener myclickevent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String nowtime;
            String nowday;
            Cursor mycursor;
            switch(v.getId()){
                case R.id.sleepxml_btn_sleep:
                    mycursor = myadapter.querybyname_from_user_table(user_name);
                    nowtime = gettime();
                    nowday = getdate();
                    if(mycursor.getCount() == 0){
                        //若無使用者資料則新增資料到資料庫
                        add_sleep_time(nowtime);
                    }
                    else{
                        //有使用者資料就修改資料庫資料
                        modify_sleep_time(nowtime,mycursor);
                   }

                    // 做sleep紀錄的
                    sleep_record(nowday,nowtime);

                    //要更新紀錄，所以重新載入activity
                    reload_thispage();

                    Toast.makeText(thisactivity, "你今晚睡眠時間為 "+nowtime, Toast.LENGTH_SHORT).show();

                    break;
                case R.id.sleepxml_btn_wake:
                    mycursor = myadapter.querybyname_from_user_table(user_name);
                    nowtime = gettime();
                    nowday = getdate();
                    if(mycursor.getCount() == 0){
                        //若無使用者資料則新增資料到資料庫
                        add_wake_time(nowtime);
                    }
                    else{
                        //有使用者資料就修改資料庫資料
                        modify_wake_time(nowtime,mycursor);
                    }
                    //在做一次搜尋是因為資料庫資料有變
                    mycursor = myadapter.querybyname_from_user_table(user_name);
                    txt_sleep.setText(mycursor.getString(2));
                    txt_wake.setText(mycursor.getString(3));
                    if(mycursor.getString(2) != null && mycursor.getString(3) != null){
                        //若是睡眠時間或醒來時間是""，跑到reminder會exception
                        if(!mycursor.getString(2).equals("") && !mycursor.getString(3).equals("")) {
                            reminder(mycursor);
                        }
                    }

                    // 做wake紀錄的
                    wake_record(nowday,nowtime);

                    //要更新紀錄，所以重新載入activity
                    reload_thispage();

                    Toast.makeText(thisactivity, "早安阿!祝你有美好的一天^ ^ ", Toast.LENGTH_SHORT).show();
                    break;
                default:

            }
        }
    };

    //取得現在時間的方法
    String gettime(){
        String t;
        myCalendar = Calendar.getInstance();
        t = String.valueOf(myCalendar.get(HOUR_OF_DAY))+":"
                + String.valueOf(myCalendar.get(Calendar.MINUTE));
        return t;
    }
    String getdate(){
        String t;
        myCalendar = Calendar.getInstance();
        t = String.valueOf(myCalendar.get(Calendar.MONTH)+1)+"/"
                + String.valueOf(myCalendar.get(Calendar.DAY_OF_MONTH));
        return t;
    }
    void add_sleep_time(String t){
        myadapter.add_to_sleep_table(user_name,t,"");
    }
    void add_wake_time(String t){
        myadapter.add_to_sleep_table(user_name,"",t);
    }

    void modify_sleep_time(String t,Cursor c){
        myadapter.modify_sleep_table(user_name,t,c.getString(3));
    }
    void modify_wake_time(String t,Cursor c){
        myadapter.modify_sleep_table(user_name,c.getString(2),t);
    }
    void sleep_record(String t,String t2){
        // t是指日期,t2指時間
        Cursor mycursor;
        //因為是要記錄前一晚的睡覺時間
        String[] temp;
        int temp_month,temp_day;

        temp = t.split("/");
        temp_month = Integer.valueOf(temp[0]);
        temp_day = Integer.valueOf(temp[1]);
        //因為會加到下一天的睡著欄，所以日期要加一天
        //二月以28天計
        if(temp_month == 2 && temp_day == 28){
            temp_month = temp_month + 1;
            temp_day = 1;
        }
        //四,六,九,十一月為30天
        else if(temp_month == 4 || temp_month == 6 || temp_month == 9 || temp_month == 11){
            if(temp_day == 30){
                temp_month = temp_month + 1;
                temp_day = 1;
            }
            else {
                temp_day = temp_day + 1;
            }
        }
        //其餘月份為31天
        else{
            if(temp_day == 31 && temp_month != 12){
                temp_month = temp_month + 1;
                temp_day = 1;
            }
            //十二月三十一日要分開處理
            else if(temp_day == 31){
                temp_month = 1;
                temp_day = 1;
            }
            else {
                temp_day = temp_day + 1;
            }
        }

        t = String.valueOf(temp_month) + "/" + String.valueOf(temp_day);

        mycursor = myadapter.checkcursor(user_name,t);
        if(mycursor.getCount() == 0){
            myadapter.add_sleep(user_name,t,t2);
        }
        else{
            myadapter.modify_sleep(user_name,t,t2);
        }

    }
    void wake_record(String t,String t2){
        Cursor mycursor;
        mycursor = myadapter.checkcursor(user_name,t);
        if(mycursor.getCount() == 0){
            myadapter.add_wake(user_name,t,t2);
        }
        else{
            myadapter.modify_wake(user_name,t,t2);
        }
    }



    //睡眠守護提醒
    void reminder(Cursor c){
        // 陣列0 為 小時,陣列1 為 分鐘
        int[] during_time = new int[2];

        during_time = calculate_during_time(c,3,2);

        //針對不同區隔做提醒
        if(during_time[0] > 8){
            txt_reminder.setText("你好像昨晚有點睡過量囉!!");
        }
        else if(during_time[0] < 6){
            txt_reminder.setText("歐歐~~你昨晚睡不太夠欸!!");
        }
        else{
            txt_reminder.setText("太棒了!!你昨晚睡得剛剛好喔!!");
        }

    }

    void loading_during_record(){
        Cursor mycursor = myadapter.allcursor(user_name);
        int i;
        for(i = 1;i <= mycursor.getCount();i++){
            check_during(i);
        }
    }

    // 計算during和判別是否null
    void check_during(int id){
        Cursor mycursor_rec = myadapter.search_byID(id);
        if(mycursor_rec.getCount() != 0){
            if(mycursor_rec.getString(3) != null
                    && mycursor_rec.getString(4) != null){
                if(!mycursor_rec.getString(3).equals("")
                        && !mycursor_rec.getString(4).equals("")){
                    for_record_during_time(mycursor_rec,id);
                }

            }
        }
    }

    //for歷史紀錄的during time
    void for_record_during_time(Cursor c,int id){
        int[] during_time = new int[2];
        during_time = calculate_during_time(c,3,4);
        myadapter.modify_during(user_name,id,during_time);
    }

    int[] calculate_during_time(Cursor c,int col_wake,int col_sleep){
        // 陣列0 為 小時,陣列1 為 分鐘
        int[] during_time = new int[2];
        String[] start,end;
        int[] start_num = new int[2];
        int[] end_num = new int[2];

        //以:區隔取得小時和分鐘
        start = c.getString(col_sleep).split(":");
        end = c.getString(col_wake).split(":");

        //將資料從字串轉成數字
        start_num[0] = Integer.valueOf(start[0]);
        start_num[1] = Integer.valueOf(start[1]);
        end_num[0] = Integer.valueOf(end[0]);
        end_num[1] = Integer.valueOf(end[1]);

        //針對過凌晨去做調整取得小時間隔時間
        if(end_num[0] < start_num[0]){
            during_time[0] = 24 - start_num[0];
            during_time[0] = during_time[0] + end_num[0];
        }
        else if(end_num[0] > start_num[0]){
            during_time[0] = end_num[0] - start_num[0];
        }
        else{
            during_time[0] = 0;
        }
        //針對過60分去做調整取得分鐘間隔時間
        if(end_num[1] < start_num[1]){
            during_time[1] = 60 - start_num[1];
            during_time[1] = during_time[1] + end_num[1];
        }
        else if(end_num[1] > start_num[1]){
            during_time[1] = end_num[1] - start_num[1];
        }
        else{
            during_time[1] = 0;
        }

        return during_time;
    }

    //從MainActivity取得使用者名稱
    void get_from_activity(){
        Bundle mybundle = getArguments();
        ShouHu_user_name = mybundle.getString("user_name");
        user_name = ShouHu_user_name;
    }

    //要更新紀錄，所以重新載入activity
    void reload_thispage() {
        Intent i = new Intent(thisactivity, MainActivity.class);
        Bundle mybundle = new Bundle();
        mybundle.putString("user_name", user_name);
        mybundle.putBoolean("Is_from_sleep",true);
        i.putExtras(mybundle);
        startActivity(i);
    }

    void mydisplaylistview(){
        Cursor mycursor = myadapter.allcursor(user_name);
        String from[] = new String[]{
          myadapter.KEY_DATE,
          myadapter.KEY_WAKE_TIME,
          myadapter.KEY_YESTERDAY_SLEEP,
          myadapter.KEY_DURING
        };
        int[] to = new int[]{
            R.id.listviewdetail_date,
            R.id.listviewdetail_wake,
            R.id.listviewdetail_sleep,
            R.id.listviewdetail_duration
        };
        mysimplecursoradapter = new SimpleCursorAdapter(thisactivity,R.layout.listview_detail_for_sleep,mycursor,from,to,0);
        sleep_record.setAdapter(mysimplecursoradapter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myadapter.close();
    }
}
