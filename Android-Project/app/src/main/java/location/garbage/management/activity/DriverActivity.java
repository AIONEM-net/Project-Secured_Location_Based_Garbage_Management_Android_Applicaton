package location.garbage.management.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import location.garbage.management.R;
import location.garbage.management.adapter.GarbageAdapter;
import location.garbage.management.model.Driver;
import location.garbage.management.model.Garbage;
import location.garbage.management.storage.UserSharedPreferences;


public class DriverActivity extends AppCompatActivity {

    public static Driver driver = new Driver();

    boolean isPicked = false;

    ArrayList<Garbage> listGarbage = new ArrayList<>();

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();;

        if(firebaseUser == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
            return;
        }

        setSupportActionBar(findViewById(R.id.toolbar));

        TextView txtDriverInfo = (TextView) findViewById(R.id.txtDriverInfo);
        Button btnPending = (Button) findViewById(R.id.btnPending);
        Button btnCompleted = (Button) findViewById(R.id.btnCompleted);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        GarbageAdapter garbageAdapter = new GarbageAdapter(this, listGarbage);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(garbageAdapter);

        Query databaseReference = FirebaseDatabase.getInstance().getReference("Garbage").orderByChild("isPicked");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot list) {

                listGarbage.clear();

                for(DataSnapshot item : list.getChildren()) {

                    try {
                        Garbage garbage = item.getValue(Garbage.class);

                        if(garbage != null && garbage.isPicked == isPicked) {

                            if(TextUtils.isEmpty(driver.district) || TextUtils.isEmpty(garbage.district) || driver.district.contains(garbage.district)) {
                                listGarbage.add(garbage);

                                if(!garbage.isPicked) {

                                    long timeInterval = System.currentTimeMillis() - garbage.time;

                                    if(timeInterval < 1*60*1000) {

                                        sendNotification(DriverActivity.this,
                                                (garbage.uid + garbage.phone).hashCode(),
                                                "New "+ garbage.packages +" package"+(!garbage.packages.equals("1") ? "s" : "")+" to pick",
                                                garbage.houseNO + ", " + garbage.district + " - " + garbage.phone + ""
                                        );

                                    }

                                }

                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                garbageAdapter.setListGarbage(listGarbage);
                garbageAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        FirebaseDatabase.getInstance().getReference("Drivers").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                driver = snapshot.getValue(Driver.class);

                if(driver == null) {
                    driver = new Driver();
                }

                driver.uid = snapshot.getKey();

                databaseReference.removeEventListener(valueEventListener);

                if(driver.isApproved) {

                    progressBar.setVisibility(View.VISIBLE);

                    databaseReference.addValueEventListener(valueEventListener);

                    txtDriverInfo.setText(driver.district);
                    txtDriverInfo.setBackgroundColor(Color.parseColor("#00574B"));

                }else {
                    txtDriverInfo.setText("Not Approved");
                    txtDriverInfo.setBackgroundColor(Color.parseColor("#B10202"));

                    progressBar.setVisibility(View.GONE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnPending.setBackgroundResource(R.drawable.button_active);
        btnPending.setPaintFlags(btnPending.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        btnPending.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        btnPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                databaseReference.removeEventListener(valueEventListener);

                listGarbage.clear();
                garbageAdapter.setListGarbage(listGarbage);
                garbageAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.VISIBLE);

                isPicked = false;

                databaseReference.addValueEventListener(valueEventListener);

                btnPending.setBackgroundResource(R.drawable.button_active);
                btnPending.setPaintFlags(btnPending.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                btnPending.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                btnCompleted.setBackgroundResource(R.drawable.button);
                btnCompleted.setPaintFlags(0);
                btnCompleted.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));

            }
        });

        btnCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                databaseReference.removeEventListener(valueEventListener);

                listGarbage.clear();
                garbageAdapter.setListGarbage(listGarbage);
                garbageAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.VISIBLE);

                isPicked = true;

                databaseReference.addValueEventListener(valueEventListener);

                btnCompleted.setBackgroundResource(R.drawable.button_active);
                btnCompleted.setPaintFlags(btnCompleted.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                btnCompleted.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                btnPending.setBackgroundResource(R.drawable.button);
                btnPending.setPaintFlags(0);
                btnPending.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_driver, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_logout) {
            logout();
            return false;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        UserSharedPreferences share = new UserSharedPreferences(DriverActivity.this);
        share.removeUser();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(DriverActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    private static final String CHANNEL_ID = "Secured Garbage Management";
    private static final String CHANNEL_NAME = "Secured Garbage Management";
    private static final String CHANNEL_DESC = "Secured Garbage Management";

    public static void sendNotification(Context context, int id, String title, String message) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(CHANNEL_DESC);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(soundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(id, builder.build());
    }

}
