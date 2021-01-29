package com.androidwave.recyclerviewpagination;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.List;

public class PostRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {
  private static final int VIEW_TYPE_LOADING = 0;
  private static final int VIEW_TYPE_NORMAL = 1;
  private boolean isLoaderVisible = false;

  Context context;
  List<User> userList;

  public PostRecyclerAdapter(Context context, List<User> userList) {
    this.userList = userList;
    this.context = context;
  }

  public void AddAll(List<User> newUsers)
  {
    int initSize = newUsers.size();
    userList.addAll(newUsers);
    notifyItemRangeChanged(initSize, newUsers.size());
  }

  public String getLastItemId(){
    return userList.get(userList.size()-1).getId();
  }

  @NonNull @Override
  public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    switch (viewType) {
      case VIEW_TYPE_NORMAL:
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false));
      case VIEW_TYPE_LOADING:
        return new ProgressHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false));
      default:
        return null;
    }
  }

  @Override
  public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
    holder.onBind(position);
  }

  @Override
  public int getItemViewType(int position) {
    if (isLoaderVisible) {
      return position == userList.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
    } else {
      return VIEW_TYPE_NORMAL;
    }
  }


  @Override
  public int getItemCount() {
    return userList == null ? 0 : userList.size();
  }



  public void addItems(List<User> userItems) {
    userItems.addAll(userItems);
    notifyDataSetChanged();
  }

  public void addLoading() {
    isLoaderVisible = true;
    userList.add(new User());
    notifyItemInserted(userList.size() - 1);
  }

  public void removeLoading() {
    isLoaderVisible = false;
    int position = userList.size() - 1;
    User item = getItem(position);
    if (item != null) {
      userList.remove(position);
      notifyItemRemoved(position);
    }
  }

  public void clear() {
    userList.clear();
    notifyDataSetChanged();
  }

  User getItem(int position) {
    return userList.get(position);
  }

  public class ViewHolder extends BaseViewHolder {
    @BindView(R.id.textName)
    TextView textViewName;
    @BindView(R.id.textEmail)
    TextView textViewEmail;

    ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    protected void clear() {

    }

    public void onBind(int position) {
      super.onBind(position);
      User item = userList.get(position);

      textViewName.setText(item.getName());
      textViewEmail.setText(item.getEmail());
    }
  }

  public class ProgressHolder extends BaseViewHolder {
    ProgressHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override
    protected void clear() {
    }
  }
}
