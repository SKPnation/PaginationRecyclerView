package com.androidwave.recyclerviewpagination;

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

  private List<User> mUserItems;

  public PostRecyclerAdapter(List<User> userItems) {
    this.mUserItems = userItems;
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
      return position == mUserItems.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
    } else {
      return VIEW_TYPE_NORMAL;
    }
  }

  @Override
  public int getItemCount() {
    return mUserItems == null ? 0 : mUserItems.size();
  }

  public void addItems(List<User> userItems) {
    mUserItems.addAll(userItems);
    notifyDataSetChanged();
  }

  public void addLoading() {
    isLoaderVisible = true;
    mUserItems.add(new User());
    notifyItemInserted(mUserItems.size() - 1);
  }

  public void removeLoading() {
    isLoaderVisible = false;
    int position = mUserItems.size() - 1;
    User item = getItem(position);
    if (item != null) {
      mUserItems.remove(position);
      notifyItemRemoved(position);
    }
  }

  public void clear() {
    mUserItems.clear();
    notifyDataSetChanged();
  }

  User getItem(int position) {
    return mUserItems.get(position);
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
      User item = mUserItems.get(position);

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
