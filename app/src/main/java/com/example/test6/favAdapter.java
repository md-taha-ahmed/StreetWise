package com.example.test6;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class favAdapter  extends  RecyclerView.Adapter<favAdapter.favListViewHolder>{
    FirebaseFirestore userdb;
    String user,hawker;

    private ArrayList<favObject> reviewlist;
    public favAdapter(ArrayList <favObject> reviewlist,String user){
        this.reviewlist = reviewlist;
        this.user = user;
        userdb= FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public favListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutview = LayoutInflater.from(parent.getContext()).inflate(R.layout.fav_item, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutview.setLayoutParams(lp);
        favAdapter.favListViewHolder rev = new favAdapter.favListViewHolder(layoutview);
        return rev;
    }

    @Override
    public void onBindViewHolder(@NonNull favListViewHolder holder,int position) {
        holder.fUsername.setText(reviewlist.get(position).getUsername());
        holder.fName.setText(reviewlist.get(position).getName());
        holder.fRating.setRating(reviewlist.get(position).getRating());
        holder.fCategory.setText("Category: " + reviewlist.get(position).getCategory());
        Uri uri = reviewlist.get(position).getImg();
        Picasso.get().load(uri).into(holder.fImage);
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userdb.collection("CUSTOMER").document(user).collection("favourites").document(reviewlist.get(position).getUsername())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(view.getContext(), "Vendor removed", Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewlist.size();
    }

    public class favListViewHolder extends RecyclerView.ViewHolder{
        public TextView fUsername, fName,fCategory;
        public CircleImageView fImage;
        public RatingBar fRating;
        public ImageButton delete;

        public favListViewHolder(@NonNull View itemView) {
            super(itemView);
            fUsername = itemView.findViewById(R.id.fav_tv_username);
            fName = itemView.findViewById(R.id.fav_tv_shopname);
            fImage = itemView.findViewById(R.id.fav_profile_image);
            fRating = itemView.findViewById(R.id.fav_tv_rating);
            fCategory = itemView.findViewById(R.id.fav_tv_category);
            delete = itemView.findViewById(R.id.fav_ib_del);

        }
    }
}

