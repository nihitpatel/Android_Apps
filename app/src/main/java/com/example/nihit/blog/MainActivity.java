package com.example.nihit.blog;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mBlogList;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;

    private FirebaseAuth mAuth = null;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() == null){

                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }

            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mDatabase.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        mBlogList = (RecyclerView) findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onStart() {
        super.onStart();

        checkUserExist();

        mAuth.addAuthStateListener(mAuthListener);



        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(

                Blog.class,
                R.layout.blog_row,
                BlogViewHolder.class,
                mDatabase

        ) {

            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(), model.getImage());

            }
        };

        mBlogList.setAdapter(firebaseRecyclerAdapter);
   }

    private void checkUserExist() {

        if(mAuth.getCurrentUser() != null) {
            final String uid = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(uid)) {
                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    public static  class BlogViewHolder extends RecyclerView.ViewHolder {

            View mView;

        public BlogViewHolder(View itemView) {
            super(itemView);

            mView =  itemView;
        }

        public void setTitle(String title)
        {
            TextView post_title = (TextView) mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }

        public void setDesc(String desc)
        {
            TextView post_desc = (TextView) mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }

        public void setImage(Context ctx, String image)
        {
            ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(image).into(post_image);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.action_add){
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        }

        if(item.getItemId()==R.id.action_logout){

            mAuth.signOut();

        }

        if(item.getItemId()==R.id.action_profile)
        {
            Toast.makeText(MainActivity.this,"Under Construction",Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }





}
