package com.androidwave.recyclerviewpagination;

import android.os.Bundle;
import android.os.Handler;
import android.widget.AbsListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidwave.recyclerviewpagination.Utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.ArrayList;
import java.util.List;

import static com.androidwave.recyclerviewpagination.Utils.Utils.DataCache;

public class MainActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

  private static final String TAG = "MainActivity";

  public static final int PAGE_START = 1;


  @BindView(R.id.recyclerView)
  RecyclerView mRecyclerView;
  @BindView(R.id.swipeRefresh)
  SwipeRefreshLayout swipeRefresh;
  private PostRecyclerAdapter adapter;
  private int currentPage = PAGE_START;
  private boolean isLastPage = false;
  private boolean isLoading = false;
  int itemCount = 0;

  List<User> userList = new ArrayList<>();
  //private RecyclerView recyclerView;
  private LinearLayoutManager layoutManager;    //for linear layout
  private int ITEMS_PER_PAGE= 10;
  private Boolean isScrolling = false;
  private int currentItems,totalItems,scrolledOutItems;
  private Boolean reachedTheEnd=false;

  DatabaseReference usersDb;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    mRecyclerView = findViewById(R.id.recyclerView);
    //progressBar = findViewById(R.id.progressBar);
    //tnAdd = findViewById(R.id.addBtn);

//    progressBar.setVisibility(View.VISIBLE);
//    progressBar.setIndeterminate(true);

    usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
    //usersCollection = FirebaseFirestore.getInstance().collection("Users");

    layoutManager = new LinearLayoutManager(this);
    //layoutManager.setStackFromEnd(true);
    layoutManager.setReverseLayout(true);

    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), layoutManager.getOrientation());
    mRecyclerView.addItemDecoration(dividerItemDecoration);

    adapter = new PostRecyclerAdapter(this, userList);
    mRecyclerView.setAdapter(adapter);
    mRecyclerView.setLayoutManager(layoutManager);

//    btnAdd.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View view) {
//        //add new User
//        addNewUser();
//      }
//    });

    loadPaginated();



  }

  /**
   * do api call here to fetch data from server
   */
  private void getUsers(String nodeId) {
    //final ArrayList<User> items = new ArrayList<>();
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        Query query;

        if (nodeId==null)
        {
          query = usersDb
                  .orderByKey()
                  .limitToFirst(ITEMS_PER_PAGE);
        }
        else
        {
          query = usersDb
                  .orderByKey()
                  .startAt(nodeId)
                  .limitToFirst(ITEMS_PER_PAGE);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            List<User> users = new ArrayList<>();
            if (dataSnapshot != null && dataSnapshot.exists()){
              for (DataSnapshot ds: dataSnapshot.getChildren()){
                if (ds.getChildrenCount() > 0){
                  User user = ds.getValue(User.class);
                  user.setId(ds.getKey());
                  if (Utils.userExists(ds.getKey())) {
                    reachedTheEnd=true;
                  }else{
                    reachedTheEnd=false;
                    DataCache.add(user);
                    users.add(user);
                    new Handler().postDelayed(new Runnable() {
                      @Override
                      public void run() {
                        mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount()-1);
                      }
                    },1500);

                    swipeRefresh.setRefreshing(false);

                  }
                }else{
                  Utils.show(MainActivity.this,"DataSnapshot count is 0");
                }
              }

//              /**
//               * manage progress view
//               */
//              if (currentPage != PAGE_START) adapter.removeLoading();
//              adapter.addItems(items);
//              swipeRefresh.setRefreshing(false);

            }else {
              Utils.show(MainActivity.this, "DataSnapshot Doesn't Exist or is Null");
            }
            if (!reachedTheEnd){
              adapter.AddAll(users);
              swipeRefresh.setRefreshing(false);
            }else{
              swipeRefresh.setRefreshing(false);
            }
            //progressBar.setVisibility(GONE);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {
            Utils.show(MainActivity.this,databaseError.getMessage());
          }
        });

      }
    }, 1500);
  }


  private void loadPaginated(){
    DataCache = new ArrayList<>();
    mRecyclerView.setAdapter(adapter);

    getUsers(null);

    /**
     * add scroll listener while user reach in bottom load more will call
     */
    mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        //Check for Scroll State
        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
          isScrolling = true;
        }
      }

      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        currentItems = layoutManager.getChildCount();
        totalItems = layoutManager.getItemCount();
        scrolledOutItems = ((LinearLayoutManager) (
                recyclerView.getLayoutManager()))
                .findFirstVisibleItemPosition();

        if (isScrolling && (currentItems + scrolledOutItems == totalItems)){
          isScrolling = false;

          if (dy < 0){
            //Scrollin Down
            if (!reachedTheEnd){
              getUsers(adapter.getLastItemId());

            }else {
              Utils.show(MainActivity.this,"No Items founds");
            }
          }else {
            //Scrolling Up
          }
        }
      }
    });
  }


  @Override
  public void onRefresh() {
    itemCount = 0;
    currentPage = PAGE_START;
    isLastPage = false;
    adapter.clear();
    loadPaginated();
  }
}
