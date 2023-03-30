package com.example.test6;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class AboutActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    // For navigation drawer
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageButton about_menubtn;
    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    sqliteDB sdb;
    ///
    ClipboardManager clipboard;
    HorizontalScrollView about_hsv;
    private StorageReference mStorage;
    FirebaseFirestore userdb;
    String username;

    ImageButton about_lscroll, about_rscroll;
    TextView about_guide_no, about_glen_no, about_hatim_no, about_habban_no, about_guide_mail, about_glen_mail, about_hatim_mail, about_habban_mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        sdb = new sqliteDB(this);
        username = sdb.getDoc();
        mFirebaseAuth = FirebaseAuth.getInstance();
        /////for firestorage
        mStorage = FirebaseStorage.getInstance().getReference();
        userdb= FirebaseFirestore.getInstance();
        fillNavigation();
        about_guide_no = findViewById(R.id.guide_num);
        about_glen_no = findViewById(R.id.glen_num);
        about_hatim_no = findViewById(R.id.hatim_num);
        about_habban_no = findViewById(R.id.habban_num);
        about_guide_mail = findViewById(R.id.guide_mail);
        about_glen_mail = findViewById(R.id.glen_mail);
        about_hatim_mail = findViewById(R.id.hatim_num);
        about_habban_mail = findViewById(R.id.habban_mail);
        about_lscroll = findViewById(R.id.about_lscroll);
        about_rscroll = findViewById(R.id.about_rscroll);
        /////for firestorage
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);


        // For navigation drawer inside on create
        drawerLayout = findViewById(R.id.map_draw);


        //added for rounded drawer
        MaterialShapeDrawable navViewBackground = (MaterialShapeDrawable) navigationView.getBackground();
        navViewBackground.setShapeAppearanceModel(
                navViewBackground.getShapeAppearanceModel()
                        .toBuilder()
                        .setTopRightCorner(CornerFamily.ROUNDED,100)
                        .setBottomRightCorner(CornerFamily.ROUNDED,100)
                        .build());
        //added for transparent status bar
        Utility.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        about_menubtn = findViewById(R.id.about_menubtn);
        navigationView.bringToFront();
        about_menubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////////////////

        about_hsv = findViewById(R.id.about_hsv);
        about_hsv.setSmoothScrollingEnabled(true);
        about_rscroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about_hsv.smoothScrollBy(1000,0);
            }
        });
        about_lscroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about_hsv.smoothScrollBy(-1000,0);
            }
        });
        copyToClipboard();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_home:
                Intent intent = new  Intent(getApplicationContext(), HomeActivity.class);
//                intent.putExtra("user",username);
                startActivity(intent);
                finish();
                break;
            case R.id.nav_prof:
                Intent intent1 = new Intent(getApplicationContext(), ProfileActivity.class);
//                intent1.putExtra("user",username);
                startActivity(intent1);
                finish();
                break;
            case R.id.nav_sign_out:
                signout();
                break;
            case R.id.nav_about:
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signout() {
        mFirebaseAuth.addAuthStateListener(authStateListener);
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            for (UserInfo userInfo : firebaseUser.getProviderData()) {
                if (userInfo.getProviderId().equals("facebook.com")) {
                    LoginManager.getInstance().logOut();
                    Toast.makeText(getApplicationContext(), "Logged out of facebook", Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent2);
                    finish();
                }
                else if (userInfo.getProviderId().equals("google.com")){
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(getApplicationContext(), "Logged out of Google", Toast.LENGTH_SHORT).show();
                    mFirebaseAuth.removeAuthStateListener(authStateListener);
                    Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent2);
                    finish();
                }

            }

        }
        sdb = new sqliteDB(this);
        String doc = sdb.getDoc();
        sdb.deletedoc(doc);
        Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent2);
        finish();
    }

    public void copyToClipboard(){
        about_guide_no.setOnClickListener(v -> {
            ClipData clip = ClipData.newPlainText("Mobile No.", about_guide_no.getText().toString()) ;
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Mobile No. copied", Toast.LENGTH_SHORT).show();
        });
        about_glen_no.setOnClickListener(v -> {
            ClipData clip = ClipData.newPlainText("Mobile No.", about_glen_no.getText().toString()) ;
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Mobile No. copied", Toast.LENGTH_SHORT).show();
        });
        about_hatim_no.setOnClickListener(v -> {
            ClipData clip = ClipData.newPlainText("Mobile No.", about_hatim_no.getText().toString()) ;
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Mobile No. copied", Toast.LENGTH_SHORT).show();
        });
        about_habban_no.setOnClickListener(v -> {
            ClipData clip = ClipData.newPlainText("Mobile No.", about_habban_no.getText().toString()) ;
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Mobile No. copied", Toast.LENGTH_SHORT).show();
        });
        about_guide_mail.setOnClickListener(v -> {
            ClipData clip = ClipData.newPlainText("Mail", about_guide_mail.getText().toString()) ;
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Email-ID copied", Toast.LENGTH_SHORT).show();
        });
        about_glen_mail.setOnClickListener(v -> {
            ClipData clip = ClipData.newPlainText("Mail", about_glen_mail.getText().toString()) ;
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Email-ID copied", Toast.LENGTH_SHORT).show();
        });
        about_hatim_mail.setOnClickListener(v -> {
            ClipData clip = ClipData.newPlainText("Mail", about_hatim_mail.getText().toString()) ;
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Email-ID copied", Toast.LENGTH_SHORT).show();
        });
        about_habban_mail.setOnClickListener(v -> {
            ClipData clip = ClipData.newPlainText("Mail", about_habban_mail.getText().toString()) ;
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Email-ID copied", Toast.LENGTH_SHORT).show();
        });
    }
    private void fillNavigation() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_about);
        View headerView = navigationView.getHeaderView(0);
        TextView nav_user = (TextView) headerView.findViewById(R.id.nav_user);
        TextView nav_mail = (TextView) headerView.findViewById(R.id.nav_email);
        ImageView dp = (ImageView) headerView.findViewById(R.id.display_picture);
        dp.setClipToOutline(true); //added to set image clipped to border of imageview - Glen

        ///////profile picture
        StorageReference filepath = mStorage.child("USERS").child("CUSTOMER").child(username).child("PROFILE");
        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(dp);
            }
        });
        //////////////////////////////////////////////////

        DocumentReference docRef = userdb.collection("CUSTOMER").document(username);
        FirebaseFirestore.getInstance()
                .collection("CUSTOMER")
                .document(username)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        nav_user.setText(documentSnapshot.getString("Username"));
                        nav_mail.setText(documentSnapshot.getString("Email"));
                        //Toast.makeText(getApplicationContext(), " "+  documentSnapshot.getString("Username"), Toast.LENGTH_SHORT).show();
//                        Toast.makeText(getApplicationContext(), "values filled", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

}

