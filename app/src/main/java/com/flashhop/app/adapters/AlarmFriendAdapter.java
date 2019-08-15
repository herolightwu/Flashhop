package com.flashhop.app.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.fragments.UserProfileFragment;
import com.flashhop.app.models.AlarmModel;
import com.flashhop.app.models.UserModel;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AlarmFriendAdapter extends RecyclerView.Adapter<AlarmFriendAdapter.ViewHolder> {

    private List<AlarmModel> mDataSet;

    private OnItemClickListener mOnItemClickListener;

    private final Context mContext;
    private final HomeActivity parent;

    public void setOnItemClickListener (OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom {@link RecyclerView.ViewHolder}).
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tv_content;
        public final CircleImageView civ_photo;
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            civ_photo = v.findViewById(R.id.alarm_friends_item_civ_avatar);
            tv_content = v.findViewById(R.id.alarm_friends_item_tv_content);

            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public AlarmFriendAdapter(List<AlarmModel> dataSet, Context context, HomeActivity act) {
        mDataSet = dataSet;
        mContext = context;
        this.parent = act;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_alarm_friends_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        AlarmModel one = mDataSet.get(position);
        if(one.uPhoto.length() > 0){
            Picasso.get()
                    .load(one.uPhoto)
                    .into(viewHolder.civ_photo);
        }
        viewHolder.tv_content.setText(fromHtml("<html>" + one.sDesc + "</html>"));
        viewHolder.civ_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUserProfile(one.user);
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });
    }

    private void gotoUserProfile(UserModel other){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        UserProfileFragment userFrag = new UserProfileFragment();
        userFrag.other = other;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_slide_in_up, R.anim.ani_slide_in_down, R.anim.ani_slide_in_up, R.anim.ani_slide_in_down);
        transaction.add(R.id.home_frame, userFrag, "USER_FRAG").addToBackStack("USER_FRAG").commit();
    }

    @SuppressWarnings("deprecation")
    public Spanned fromHtml(String html){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void setDataList(List<AlarmModel> dataSet){
        mDataSet = dataSet;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        public int onItemClick(int pos);
    }
}
