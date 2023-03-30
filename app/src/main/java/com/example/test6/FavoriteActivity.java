package com.example.test6;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class FavoriteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private favAdapter fadapter;
    ArrayList <favObject> reviewList;

    // For navigation drawer
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageButton fav_menubtn, fav_audio;
    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    ///
    RecyclerView fav_recylerview;
    private RecyclerView.LayoutManager layoutManager;
    EditText fav_ed1;
    sqliteDB sdb;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    FirebaseFirestore userdb;
    String username,username_hawker,string_uri,name_hawker,category;
    float rate;
    Uri prof_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        reviewList = new ArrayList<>();
        fav_recylerview = findViewById(R.id.fav_recyclerview);
        fav_recylerview.setNestedScrollingEnabled(false);
        fav_recylerview.setHasFixedSize(false);
        fav_recylerview.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        layoutManager = new LinearLayoutManager(FavoriteActivity.this, RecyclerView.VERTICAL, false);
        fav_recylerview.setLayoutManager(layoutManager);
        sdb = new sqliteDB(this);
        username=sdb.getDoc();
        userdb= FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        fav_ed1 = findViewById(R.id.fav_ed1);
        fav_audio = findViewById(R.id.fav_audio);
        fav_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

                try {
                    startActivityForResult(intent, 139);
                }
                catch (Exception e) {
                    Toast.makeText(FavoriteActivity.this, " " + e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });


        // For navigation drawer inside on create
        drawerLayout = findViewById(R.id.map_draw);
        navigationView = findViewById(R.id.nav_view);
        fillNavigation();
        //added for rounded drawer
        MaterialShapeDrawable navViewBackground = (MaterialShapeDrawable) navigationView.getBackground();
        navViewBackground.setShapeAppearanceModel(
                navViewBackground.getShapeAppearanceModel()
                        .toBuilder()
                        .setTopRightCorner(CornerFamily.ROUNDED,100)
                        .setBottomRightCorner(CornerFamily.ROUNDED,100)
                        .build());
        //added for transparent status bar
        com.example.test6.Utility.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        //end of rounded drawer

        fav_menubtn = findViewById(R.id.menu_img);
        navigationView.bringToFront();
        fav_menubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////////////////


        fadapter = new favAdapter(reviewList,username);
        fav_recylerview.setAdapter(fadapter);
        updateData();
        fadapter.notifyDataSetChanged();

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
            case R.id.nav_map:
                Intent intent2 = new  Intent(getApplicationContext(), com.example.test6.MapsActivity.class);
                intent2.putExtra("type","All");
                startActivity(intent2);
                finish();
                break;
            case R.id.nav_sign_out:
                signout();
                break;
            case R.id.nav_fav:
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_about:
                Intent intent3 = new  Intent(getApplicationContext(),AboutActivity.class);
                startActivity(intent3);
                finish();
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
                    Intent intent2 = new Intent(getApplicationContext(), com.example.test6.MainActivity.class);
                    startActivity(intent2);
                    finish();
                }
                else if (userInfo.getProviderId().equals("google.com")){
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(getApplicationContext(), "Logged out of Google", Toast.LENGTH_SHORT).show();
                    mFirebaseAuth.removeAuthStateListener(authStateListener);
                    Intent intent2 = new Intent(getApplicationContext(), com.example.test6.MainActivity.class);
                    startActivity(intent2);
                    finish();
                }

            }

        }
        sdb = new sqliteDB(this);
        String doc = sdb.getDoc();
        sdb.deletedoc(doc);
        Intent intent2 = new Intent(getApplicationContext(), com.example.test6.MainActivity.class);
        startActivity(intent2);
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 139) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                fav_ed1.setText(Objects.requireNonNull(result).get(0));
            }
        }
    }
    private void fillNavigation() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
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
    public void updateData(){
        userdb.collection("CUSTOMER").document(username).collection("favourites").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                reviewList.clear();
                for (DocumentSnapshot doc : value) {
                    if (error != null) {
                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        username_hawker = doc.getId();
                        string_uri = doc.getString("profile");

                        DocumentReference docRef = userdb.collection("HAWKERS").document(username_hawker);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        name_hawker = document.getString("Name");
                                        category = document.getString("Type");

                                        rate = (float )3.5;

                                        if (string_uri!=null) {
                                            prof_uri = Uri.parse(string_uri);
                                            favObject fObject = new favObject("(" + name_hawker + ")", username_hawker, category, rate,prof_uri);
                                            reviewList.add(fObject);
                                        }
                                        else{
                                            //prof_uri = Uri.parse(string_uri);
                                            favObject fObject = new favObject(name_hawker, username_hawker, category, rate,null);
                                            reviewList.add(fObject);
                                        }

                                        fadapter.notifyDataSetChanged();

                                    } else {

                                    }
                                } else {

                                }
                            }
                        });

                        //rate = Float.parseFloat(doc.get("rating").toString());

                    }
                }

            }
        });
    }
}
