package com.example.old_man.Reminder;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.old_man.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainPage extends AppCompatActivity {

    private FloatingActionButton add;
    private Dialog dialog;
    private AppDatabase appDatabase;
    private RecyclerView recyclerView;
    private AdapterReminders adapter;
    private List<Reminders> temp;
    private TextView empty;
    private ArrayList<Integer> parallelArrayOf_Ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        Toolbar toolbar1 = findViewById(R.id.ReminderToolbar1);
        setSupportActionBar(toolbar1);

        appDatabase = AppDatabase.geAppdatabase(MainPage.this);

        add = findViewById(R.id.floatingButton);
        empty = findViewById(R.id.empty);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReminder();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainPage.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        setItemsInRecyclerView();

    }

    public void addReminder(){

        dialog = new Dialog(MainPage.this);
        dialog.setContentView(R.layout.floating_popup);

        final TextView textView = dialog.findViewById(R.id.date);
        Button select,add,cancel;
        select = dialog.findViewById(R.id.selectDate);
        add = dialog.findViewById(R.id.addButton);
        cancel = dialog.findViewById(R.id.cancelButton);
        final EditText message = dialog.findViewById(R.id.reminderMessage);


        final Calendar newCalender = Calendar.getInstance();
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(MainPage.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {

                        final Calendar newDate = Calendar.getInstance();
                        Calendar newTime = Calendar.getInstance();
                        TimePickerDialog time = new TimePickerDialog(MainPage.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                newDate.set(year,month,dayOfMonth,hourOfDay,minute,0);
                                Calendar tem = Calendar.getInstance();
                                Log.w("TIME",System.currentTimeMillis()+"");
                                if(newDate.getTimeInMillis()-tem.getTimeInMillis()>0)
                                    textView.setText(newDate.getTime().toString());
                                else
                                    Toast.makeText(MainPage.this,"Invalid time",Toast.LENGTH_SHORT).show();

                            }
                        },newTime.get(Calendar.HOUR_OF_DAY),newTime.get(Calendar.MINUTE),true);
                        time.show();

                    }
                },newCalender.get(Calendar.YEAR),newCalender.get(Calendar.MONTH),newCalender.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMinDate(System.currentTimeMillis());
                dialog.show();

            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ScheduleExactAlarm")
            @Override
            public void onClick(View v) {
                RoomDAO roomDAO = appDatabase.getRoomDAO();
                Reminders reminders = new Reminders();
                reminders.setMessage(message.getText().toString().trim());

                Date remind;
                try {
                    SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                    remind = format.parse(textView.getText().toString().trim());
                } catch (ParseException pe) {
                    Toast.makeText(MainPage.this, "mainPage error line 133", Toast.LENGTH_SHORT).show();
                    throw new IllegalArgumentException(pe);
                }

                reminders.setRemindDate(remind);
                roomDAO.Insert(reminders);
                List<Reminders> l = roomDAO.getAll();
                reminders = l.get(l.size() - 1);
                Log.e("ID chahiye", reminders.getId() + "");

                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
                calendar.setTime(remind);
                calendar.set(Calendar.SECOND, 0);
                Intent intent = new Intent(MainPage.this, NotifierAlarm.class);
                intent.putExtra("Message", reminders.getMessage());
                intent.putExtra("RemindDate", reminders.getRemindDate().toString());
                intent.putExtra("id", reminders.getId());

                // Update this line to include FLAG_IMMUTABLE
                PendingIntent intent1 = PendingIntent.getBroadcast(MainPage.this, reminders.getId(), intent, PendingIntent.FLAG_MUTABLE);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


                // Add a check for Android 12 and above
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    try {
                        // Check if the app is allowed to schedule exact alarms
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent1);
                        } else {
                            Toast.makeText(MainPage.this, "Cannot schedule exact alarms.", Toast.LENGTH_SHORT).show();
                            // Handle the case where the app cannot schedule exact alarms
                        }
                    } catch (SecurityException e) {
                        Toast.makeText(MainPage.this, "Cannot schedule exact alarms. SecurityException.", Toast.LENGTH_SHORT).show();
                        // Handle the SecurityException
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent1);
                }

                Toast.makeText(MainPage.this, "Inserted Successfully", Toast.LENGTH_SHORT).show();
                setItemsInRecyclerView();
                AppDatabase.destroyInstance();
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    public void setItemsInRecyclerView(){

        RoomDAO dao = appDatabase.getRoomDAO();
        temp = dao.orderThetable();
        if(temp.size()>0) {
            empty.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        parallelArrayOf_Ids = new ArrayList<>();
        for(int i = 0; i < temp.size();i++)
            parallelArrayOf_Ids.add(temp.get(i).getId());

        adapter = new AdapterReminders(temp);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new AdapterReminders.onItemClickListener() {
            @Override
            public void onDeleteClick(int position) {

                //removing alarm
                alarmRemover(parallelArrayOf_Ids.get(position));
                //removing from data base
                AppDatabase appDatabase2 = AppDatabase.geAppdatabase(MainPage.this);
                final RoomDAO dao1 = appDatabase2.getRoomDAO();
                Reminders reminder = dao1.getObjectUsingID(parallelArrayOf_Ids.get(position));
                dao1.Delete(reminder);
                AppDatabase.destroyInstance();
                Toast.makeText(MainPage.this, "Deleted", Toast.LENGTH_SHORT).show();
                setItemsInRecyclerView();
            }
        });


    }

    public void DeleteAll(View v){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete all reminders?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppDatabase appDatabase2 = AppDatabase.geAppdatabase(MainPage.this);
                        final RoomDAO dao1 = appDatabase2.getRoomDAO();

                        List<Integer> ids = dao1.getAllIds();
                        for(int i = 0; i<ids.size();i++) {
                            alarmRemover(ids.get(i));
                            //Log.d("ReminderId", ids.get(i)+ "\n");
                        }

//                for(int i = 0;i< parallelArrayOf_Ids.size();i++)
//                       Log.d("ReminderId","parallel id" +parallelArrayOf_Ids.get(i));

                        dao1.DeleteAll();//iss line ko last me he rahne de
                        AppDatabase.destroyInstance();
                        Toast.makeText(MainPage.this, "All deleted", Toast.LENGTH_SHORT).show();
                        setItemsInRecyclerView();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
    public void alarmRemover(int id){
        Intent intent = new Intent(this,NotifierAlarm.class);
        PendingIntent intent1 = PendingIntent.getBroadcast(this,id,intent,PendingIntent.FLAG_MUTABLE);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.cancel(intent1);
    }
}