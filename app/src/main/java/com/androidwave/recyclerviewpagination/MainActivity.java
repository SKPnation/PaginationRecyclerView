package com.androidwave.recyclerviewpagination;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.ArrayList;

import static com.androidwave.recyclerviewpagination.PaginationListener.PAGE_START;

public class MainActivity extends AppCompatActivity
    implements SwipeRefreshLayout.OnRefreshListener {

  private static final String TAG = "MainActivity";

  @BindView(R.id.recyclerView)
  RecyclerView mRecyclerView;
  @BindView(R.id.swipeRefresh)
  SwipeRefreshLayout swipeRefresh;
  private PostRecyclerAdapter adapter;
  private int currentPage = PAGE_START;
  private boolean isLastPage = false;
  private int totalPage = 10;
  private boolean isLoading = false;
  int itemCount = 0;

  DatabaseReference usersDb;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

    swipeRefresh.setOnRefreshListener(this);

    mRecyclerView.setHasFixedSize(true);
    // use a linear layout manager

    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setStackFromEnd(true);
    layoutManager.setReverseLayout(true);
    mRecyclerView.setLayoutManager(layoutManager);

    adapter = new PostRecyclerAdapter(new ArrayList<>());
    mRecyclerView.setAdapter(adapter);
    getUsers();

    /**
     * add scroll listener while user reach in bottom load more will call
     */
    mRecyclerView.addOnScrollListener(new PaginationListener(layoutManager) {
      @Override
      protected void loadMoreItems() {
        isLoading = true;
        currentPage++;
        getUsers();
      }

      @Override
      public boolean isLastPage() {
        return isLastPage;
      }

      @Override
      public boolean isLoading() {
        return isLoading;
      }
    });
  }

  /**
   * do api call here to fetch data from server
   * In example i'm adding data manually
   */
  private void getUsers() {
    final ArrayList<User> items = new ArrayList<>();
    new Handler().postDelayed(new Runnable() {

      @Override
      public void run() {
        usersDb.addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot ds: dataSnapshot.getChildren())
            {
              User user = ds.getValue(User.class);

              items.add(user);
            }

            /**
             * manage progress view
             */
            if (currentPage != PAGE_START) adapter.removeLoading();
            adapter.addItems(items);
            swipeRefresh.setRefreshing(false);

            // check weather is last page or not
            if (currentPage < totalPage) {
              adapter.addLoading();
            } else {
              isLastPage = true;
            }
            isLoading = false;

          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {
            //..
          }
        });
//        for (int i = 0; i < 10; i++) {
//          itemCount++;
//          PostItem postItem = new PostItem();
//          postItem.setTitle(getString(R.string.text_title) + itemCount);
//          postItem.setDescription(getString(R.string.text_description));
//          items.add(postItem);
//        }
        // do this all stuff on Success of APIs response

      }
    }, 1500);
  }

  @Override
  public void onRefresh() {
    itemCount = 0;
    currentPage = PAGE_START;
    isLastPage = false;
    adapter.clear();
    getUsers();
  }
}
