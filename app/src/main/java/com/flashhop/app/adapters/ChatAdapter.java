package com.flashhop.app.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.fragments.SettingsFragment;
import com.flashhop.app.models.CommentModel;
import com.flashhop.app.models.MsgModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.flashhop.app.utils.Const.APP_TAG;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private List<MsgModel> mDataSet;
    private List<MsgModel> filteredList;
    private ChatFilter chatFilter;

    private final Context mContext;

    private String eId;
    private boolean isPLAYING = false;
    private int nPlay = -1;
    MediaPlayer mp;
    DatabaseReference database;

    private OnItemPhotoListener onItemPhotoListener;
    private OnItemAvatarListener onItemAvatarListener;

    public void setOnItemAvatarListener(OnItemAvatarListener mListener){
        onItemAvatarListener = mListener;
    }

    public void setOnItemPhotoListener(OnItemPhotoListener mListener){
        onItemPhotoListener = mListener;
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public ChatAdapter(List<MsgModel> dataSet, Context context, DatabaseReference ref, String eid) {
        mDataSet = dataSet;
        filteredList = dataSet;
        mContext = context;
        database = ref;
        eId = eid;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v;
        switch (viewType){
            case Const.MSG_TYPE_PHOTO:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_chat_image, viewGroup, false);
                return new ItemChatPhotoHolder(v);
            case Const.MSG_TYPE_TEXT:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_chat_text, viewGroup, false);
                return new ItemChatTxtHolder(v);
            case Const.MSG_TYPE_VOICE:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_chat_voice, viewGroup, false);
                return new ItemChatVoiceHolder(v);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        MsgModel one = filteredList.get(position);
        final  String kkey = one.dbKey;

        LinearLayoutManager linearMgr = new LinearLayoutManager(mContext);
        CommentAdapter commentAdapter = new CommentAdapter(one.comments, mContext);

        if(viewHolder instanceof ItemChatTxtHolder){
            Picasso.get()
                    .load(one.uPhoto)
                    .into(((ItemChatTxtHolder)viewHolder).civ_avatar);
            ((ItemChatTxtHolder)viewHolder).civ_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onItemAvatarListener != null){
                        onItemAvatarListener.onAvatarClick(position);
                    }
                }
            });
            ((ItemChatTxtHolder)viewHolder).tv_name.setText(one.uName);
            ((ItemChatTxtHolder)viewHolder).tv_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onItemAvatarListener != null){
                        onItemAvatarListener.onAvatarClick(position);
                    }
                }
            });
            ((ItemChatTxtHolder)viewHolder).tv_content.setText(one.sMsg);
            ((ItemChatTxtHolder)viewHolder).tv_time.setText(getTime(one.lTime * 1000L).toUpperCase());
            if(one.likes.size() > 0){
                ((ItemChatTxtHolder)viewHolder).tv_likes.setText(one.likes.size() + " Likes");
            } else{
                ((ItemChatTxtHolder)viewHolder).tv_likes.setText("Like");
            }
            if(one.bLike){
                ((ItemChatTxtHolder)viewHolder).iv_like.setImageResource(R.drawable.ic_liked);
            } else{
                ((ItemChatTxtHolder)viewHolder).iv_like.setImageResource(R.drawable.ic_like);
            }
            ((ItemChatTxtHolder)viewHolder).ll_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String cKey = one.likes.size() + "";
                    if(!one.bLike){
                        database.child("chat_history").child(eId).child(kkey).child("likes").child(cKey).setValue(MyApp.curUser.uid);
                    } else{
                        for(int k = 0; k < one.likes.size(); k++){
                            if(one.likes.get(k).equals(MyApp.curUser.uid)){
                                one.likes.remove(k);
                                break;
                            }
                        }
                        database.child("chat_history").child(eId).child(kkey).child("likes").setValue(one.likes);
                    }
                }
            });

            if(one.bComment){
                ((ItemChatTxtHolder)viewHolder).iv_comment.setImageResource(R.drawable.ic_comment_d);
            } else{
                ((ItemChatTxtHolder)viewHolder).iv_comment.setImageResource(R.drawable.ic_comment);
            }
            if(one.comments.size() > 0){
                ((ItemChatTxtHolder)viewHolder).tv_comments.setText(one.comments.size() + " Comments");
                //((ItemChatTxtHolder)viewHolder).commentRecycler.setVisibility(View.VISIBLE);
            } else{
                ((ItemChatTxtHolder)viewHolder).tv_comments.setText("Comment");
                //((ItemChatTxtHolder)viewHolder).commentRecycler.setVisibility(View.GONE);
            }
            ((ItemChatTxtHolder)viewHolder).ll_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //((ItemChatTxtHolder)viewHolder).et_comment.setVisibility(View.VISIBLE);
                    if(((ItemChatTxtHolder)viewHolder).commentRecycler.getVisibility() == View.GONE){
                        ((ItemChatTxtHolder)viewHolder).commentRecycler.setVisibility(View.VISIBLE);
                        ((ItemChatTxtHolder)viewHolder).et_comment.setVisibility(View.VISIBLE);
                    } else{
                        ((ItemChatTxtHolder)viewHolder).commentRecycler.setVisibility(View.GONE);
                        ((ItemChatTxtHolder)viewHolder).et_comment.setVisibility(View.GONE);
                    }
                    //notifyItemChanged(position);
                }
            });

            ((ItemChatTxtHolder)viewHolder).et_comment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if(i == EditorInfo.IME_ACTION_SEND){
                        String sComment = ((ItemChatTxtHolder)viewHolder).et_comment.getText().toString().trim();
                        if(sComment.length() > 0){
                            long tsLong = System.currentTimeMillis()/1000;
                            String cKey = one.comments.size() + "";//database.child("chat_history").child(eId).child(kkey).child("comment").push().getKey();
                            CommentModel com = new CommentModel();
                            com.uId = MyApp.curUser.uid;
                            com.sMsg = sComment;
                            com.uPhoto = MyApp.curUser.photo_url;
                            com.lTime = tsLong;
                            com.uName = MyApp.curUser.first_name;
                            database.child("chat_history").child(eId).child(kkey).child("comments").child(cKey).setValue(com);
                            ((ItemChatTxtHolder)viewHolder).et_comment.setText("");
                            ((ItemChatTxtHolder)viewHolder).et_comment.setVisibility(View.GONE);
                            one.comments.add(com);
                            one.bComment = true;
                            //notifyItemChanged(position);
                            String sNoti = MyApp.curUser.first_name + " left a comment.";
                            sendCommentNotification(sNoti);
                        } else{
                            WindowUtils.animateView(mContext, ((ItemChatTxtHolder)viewHolder).et_comment);
                        }
                    }
                    hideKeyboard(textView);
                    return false;
                }
            });
            ((ItemChatTxtHolder)viewHolder).commentRecycler.setLayoutManager(linearMgr);
            ((ItemChatTxtHolder)viewHolder).commentRecycler.setAdapter(commentAdapter);
            /*((ItemChatTxtHolder)viewHolder).tv_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(one.nType == Const.MSG_TYPE_VOICE){
                        if (!isPLAYING) {
                            if(one.photos.get(0).length() > 0){
                                isPLAYING = true;
                                mp = new MediaPlayer();
                                try {

                                    mp.setDataSource(one.photos.get(0));
                                    mp.prepare();
                                    mp.start();
                                } catch (IOException e) {
                                    Log.e("VOICE PLAY", "prepare() failed");
                                }
                            }
                        } else {
                            isPLAYING = false;
                            stopPlaying();
                        }
                    }
                }
            });*/

            if(one.uId.equals(MyApp.curUser.uid)){
                ((ItemChatTxtHolder)viewHolder).tv_delete.setVisibility(View.VISIBLE);
            } else{
                ((ItemChatTxtHolder)viewHolder).tv_delete.setVisibility(View.GONE);
            }
            ((ItemChatTxtHolder)viewHolder).tv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDelConfirmDlg(kkey);
                }
            });
            if(one.bOnline){
                ((ItemChatTxtHolder)viewHolder).civ_avatar.setBorderWidth(2);
            } else{
                ((ItemChatTxtHolder)viewHolder).civ_avatar.setBorderWidth(0);
            }

        } else if(viewHolder instanceof ItemChatVoiceHolder){
            Picasso.get()
                    .load(one.uPhoto)
                    .into(((ItemChatVoiceHolder)viewHolder).civ_avatar);
            ((ItemChatVoiceHolder)viewHolder).civ_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onItemAvatarListener != null){
                        onItemAvatarListener.onAvatarClick(position);
                    }
                }
            });
            ((ItemChatVoiceHolder)viewHolder).tv_name.setText(one.uName);
            ((ItemChatVoiceHolder)viewHolder).tv_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onItemAvatarListener != null){
                        onItemAvatarListener.onAvatarClick(position);
                    }
                }
            });
            ((ItemChatVoiceHolder)viewHolder).tv_content.setText(one.sMsg);
            ((ItemChatVoiceHolder)viewHolder).tv_time.setText(getTime(one.lTime * 1000L).toUpperCase());
            if(one.likes.size() > 0){
                ((ItemChatVoiceHolder)viewHolder).tv_likes.setText(one.likes.size() + " Likes");
            } else{
                ((ItemChatVoiceHolder)viewHolder).tv_likes.setText("Like");
            }
            if(one.bLike){
                ((ItemChatVoiceHolder)viewHolder).iv_like.setImageResource(R.drawable.ic_liked);
            } else{
                ((ItemChatVoiceHolder)viewHolder).iv_like.setImageResource(R.drawable.ic_like);
            }
            ((ItemChatVoiceHolder)viewHolder).ll_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String cKey = one.likes.size() + "";
                    if(!one.bLike){
                        database.child("chat_history").child(eId).child(kkey).child("likes").child(cKey).setValue(MyApp.curUser.uid);
                    } else{
                        for(int k = 0; k < one.likes.size(); k++){
                            if(one.likes.get(k).equals(MyApp.curUser.uid)){
                                one.likes.remove(k);
                                break;
                            }
                        }
                        database.child("chat_history").child(eId).child(kkey).child("likes").setValue(one.likes);
                    }
                }
            });

            if(one.bComment){
                ((ItemChatVoiceHolder)viewHolder).iv_comment.setImageResource(R.drawable.ic_comment_d);
            } else{
                ((ItemChatVoiceHolder)viewHolder).iv_comment.setImageResource(R.drawable.ic_comment);
            }
            if(one.comments.size() > 0){
                ((ItemChatVoiceHolder)viewHolder).tv_comments.setText(one.comments.size() + " Comments");
                //((ItemChatVoiceHolder)viewHolder).commentRecycler.setVisibility(View.VISIBLE);
            } else{
                ((ItemChatVoiceHolder)viewHolder).tv_comments.setText("Comment");
                //((ItemChatVoiceHolder)viewHolder).commentRecycler.setVisibility(View.GONE);
            }
            ((ItemChatVoiceHolder)viewHolder).ll_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(((ItemChatVoiceHolder)viewHolder).commentRecycler.getVisibility() == View.GONE){
                        ((ItemChatVoiceHolder)viewHolder).commentRecycler.setVisibility(View.VISIBLE);
                        ((ItemChatVoiceHolder)viewHolder).et_comment.setVisibility(View.VISIBLE);
                    } else{
                        ((ItemChatVoiceHolder)viewHolder).commentRecycler.setVisibility(View.GONE);
                        ((ItemChatVoiceHolder)viewHolder).et_comment.setVisibility(View.GONE);
                    }
                    //notifyItemChanged(position);
                }
            });

            ((ItemChatVoiceHolder)viewHolder).et_comment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if(i == EditorInfo.IME_ACTION_SEND){
                        String sComment = ((ItemChatVoiceHolder)viewHolder).et_comment.getText().toString().trim();
                        if(sComment.length() > 0){
                            long tsLong = System.currentTimeMillis()/1000;
                            String cKey = one.comments.size() + "";//database.child("chat_history").child(eId).child(kkey).child("comment").push().getKey();
                            CommentModel com = new CommentModel();
                            com.uId = MyApp.curUser.uid;
                            com.sMsg = sComment;
                            com.uPhoto = MyApp.curUser.photo_url;
                            com.lTime = tsLong;
                            com.uName = MyApp.curUser.first_name;
                            database.child("chat_history").child(eId).child(kkey).child("comments").child(cKey).setValue(com);
                            ((ItemChatVoiceHolder)viewHolder).et_comment.setText("");
                            ((ItemChatVoiceHolder)viewHolder).et_comment.setVisibility(View.GONE);
                            one.comments.add(com);
                            one.bComment = true;
                            //notifyItemChanged(position);
                            String sNoti = MyApp.curUser.first_name + " left a comment.";
                            sendCommentNotification(sNoti);
                        } else{
                            WindowUtils.animateView(mContext, ((ItemChatVoiceHolder)viewHolder).et_comment);
                        }
                    }
                    hideKeyboard(textView);
                    return false;
                }
            });
            ((ItemChatVoiceHolder)viewHolder).commentRecycler.setLayoutManager(linearMgr);
            ((ItemChatVoiceHolder)viewHolder).commentRecycler.setAdapter(commentAdapter);
            ((ItemChatVoiceHolder)viewHolder).tv_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isPLAYING) {
                        if(one.photos.get(0).length() > 0){
                            isPLAYING = true;
                            mp = new MediaPlayer();
                            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    mp.stop();
                                    if (mp != null) {
                                        mp.release();
                                        mp = null;
                                    }
                                    isPLAYING = false;
                                    nPlay = -1;
                                    notifyItemChanged(position);
                                }
                            });
                            try {
                                mp.setDataSource(one.photos.get(0));
                                mp.prepare();
                                mp.start();
                                nPlay = position;
                            } catch (IOException e) {
                                Log.e("VOICE PLAY", "prepare() failed");
                            }
                        }
                    } else {
                        isPLAYING = false;
                        nPlay = -1;
                        stopPlaying();
                    }
                    notifyItemChanged(position);
                }
            });
            if(position == nPlay){
                ((ItemChatVoiceHolder)viewHolder).iv_speaker.setVisibility(View.VISIBLE);
            } else{
                ((ItemChatVoiceHolder)viewHolder).iv_speaker.setVisibility(View.GONE);
            }

            if(one.uId.equals(MyApp.curUser.uid)){
                ((ItemChatVoiceHolder)viewHolder).tv_delete.setVisibility(View.VISIBLE);
            } else{
                ((ItemChatVoiceHolder)viewHolder).tv_delete.setVisibility(View.GONE);
            }
            ((ItemChatVoiceHolder)viewHolder).tv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDelConfirmDlg(kkey);
                }
            });

            if(one.bOnline){
                ((ItemChatVoiceHolder)viewHolder).civ_avatar.setBorderWidth(2);
            } else{
                ((ItemChatVoiceHolder)viewHolder).civ_avatar.setBorderWidth(0);
            }
        } else if(viewHolder instanceof ItemChatPhotoHolder){
            Picasso.get()
                    .load(one.uPhoto)
                    .into(((ItemChatPhotoHolder)viewHolder).civ_avatar);
            ((ItemChatPhotoHolder)viewHolder).civ_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onItemAvatarListener != null){
                        onItemAvatarListener.onAvatarClick(position);
                    }
                }
            });
            ((ItemChatPhotoHolder)viewHolder).tv_name.setText(one.uName);
            ((ItemChatPhotoHolder)viewHolder).tv_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onItemAvatarListener != null){
                        onItemAvatarListener.onAvatarClick(position);
                    }
                }
            });
            ((ItemChatPhotoHolder)viewHolder).tv_time.setText(getTime(one.lTime * 1000L).toUpperCase());
            ((ItemChatPhotoHolder)viewHolder).tv_content.setText(one.sMsg);
            if(one.likes.size() > 0){
                ((ItemChatPhotoHolder)viewHolder).tv_likes.setText(one.likes.size() + " Likes");
            } else{
                ((ItemChatPhotoHolder)viewHolder).tv_likes.setText("Like");
            }
            if(one.bLike){
                ((ItemChatPhotoHolder)viewHolder).iv_like.setImageResource(R.drawable.ic_liked);
            } else{
                ((ItemChatPhotoHolder)viewHolder).iv_like.setImageResource(R.drawable.ic_like);
            }
            ((ItemChatPhotoHolder)viewHolder).ll_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String cKey = one.likes.size() + "";
                    if(!one.bLike){
                        database.child("chat_history").child(eId).child(kkey).child("likes").child(cKey).setValue(MyApp.curUser.uid);
                    } else{
                        database.child("chat_history").child(eId).child(kkey).child("likes").child(cKey).setValue(null);
                    }
                }
            });

            if(one.comments.size() > 0){
                ((ItemChatPhotoHolder)viewHolder).tv_comments.setText(one.comments.size() + " Comments");
                //((ItemChatPhotoHolder)viewHolder).commentRecycler.setVisibility(View.VISIBLE);
            } else{
                ((ItemChatPhotoHolder)viewHolder).tv_comments.setText("Comment");
                //((ItemChatPhotoHolder)viewHolder).commentRecycler.setVisibility(View.GONE);
            }
            if(one.bComment){
                ((ItemChatPhotoHolder)viewHolder).iv_comment.setImageResource(R.drawable.ic_comment_d);
            } else{
                ((ItemChatPhotoHolder)viewHolder).iv_comment.setImageResource(R.drawable.ic_comment);
            }
            ((ItemChatPhotoHolder)viewHolder).ll_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(((ItemChatPhotoHolder)viewHolder).commentRecycler.getVisibility() == View.GONE){
                        ((ItemChatPhotoHolder)viewHolder).commentRecycler.setVisibility(View.VISIBLE);
                        ((ItemChatPhotoHolder)viewHolder).et_comment.setVisibility(View.VISIBLE);
                    } else{
                        ((ItemChatPhotoHolder)viewHolder).commentRecycler.setVisibility(View.GONE);
                        ((ItemChatPhotoHolder)viewHolder).et_comment.setVisibility(View.GONE);
                    }
                    //notifyItemChanged(position);
                }
            });

            ((ItemChatPhotoHolder)viewHolder).et_comment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if(i == EditorInfo.IME_ACTION_SEND){
                        String sComment = ((ItemChatPhotoHolder)viewHolder).et_comment.getText().toString().trim();
                        if(sComment.length() > 0){
                            long tsLong = System.currentTimeMillis()/1000;
                            String cKey = one.comments.size() + "";//database.child("chat_history").child(eId).child(kkey).child("comment").push().getKey();
                            CommentModel com = new CommentModel();
                            com.uId = MyApp.curUser.uid;
                            com.sMsg = sComment;
                            com.uPhoto = MyApp.curUser.photo_url;
                            com.lTime = tsLong;
                            com.uName = MyApp.curUser.first_name;
                            database.child("chat_history").child(eId).child(kkey).child("comments").child(cKey).setValue(com);
                            ((ItemChatPhotoHolder)viewHolder).et_comment.setText("");
                            ((ItemChatPhotoHolder)viewHolder).et_comment.setVisibility(View.GONE);
                            one.comments.add(com);
                            one.bComment = true;
                            //notifyDataSetChanged();
                            notifyItemChanged(position);
                            String sNoti = MyApp.curUser.first_name + " left a comment.";
                            sendCommentNotification(sNoti);
                        } else{
                            WindowUtils.animateView(mContext, ((ItemChatPhotoHolder)viewHolder).et_comment);
                        }
                    }
                    hideKeyboard(textView);
                    return false;
                }
            });
            ((ItemChatPhotoHolder)viewHolder).commentRecycler.setLayoutManager(linearMgr);
            ((ItemChatPhotoHolder)viewHolder).commentRecycler.setAdapter(commentAdapter);
            LinearLayoutManager photoMgr = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            ChatPhotoAdapter photoAdapter = new ChatPhotoAdapter(one.photos, mContext);
            ((ItemChatPhotoHolder)viewHolder).photoRecycler.setLayoutManager(photoMgr);
            ((ItemChatPhotoHolder)viewHolder).photoRecycler.setAdapter(photoAdapter);
            photoAdapter.setOnItemClickListener(new ChatPhotoAdapter.OnItemClickListener() {
                @Override
                public int onItemClick(int pos) {
                    if(onItemPhotoListener != null){
                        onItemPhotoListener.onPhotoClick(position, pos);
                    }
                    return 0;
                }
            });
            if(one.uId.equals(MyApp.curUser.uid)){
                ((ItemChatPhotoHolder)viewHolder).tv_delete.setVisibility(View.VISIBLE);
            } else{
                ((ItemChatPhotoHolder)viewHolder).tv_delete.setVisibility(View.GONE);
            }
            ((ItemChatPhotoHolder)viewHolder).tv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDelConfirmDlg(kkey);
                }
            });

            if(one.bOnline){
                ((ItemChatPhotoHolder)viewHolder).civ_avatar.setBorderWidth(2);
            } else{
                ((ItemChatPhotoHolder)viewHolder).civ_avatar.setBorderWidth(0);
            }
        }
    }

    private void stopPlaying() {
        mp.release();
        mp = null;
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void setDataList(List<MsgModel> dataSet){
        mDataSet = dataSet;
        filteredList = dataSet;
        notifyDataSetChanged();
    }

    //return view type
    @Override
    public int getItemViewType(int postion){
        MsgModel one = mDataSet.get(postion);
        return (int)one.nType;
    }

    private String getTime(long timeStamp){

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }

    private void sendCommentNotification(String content){
        AndroidNetworking.post(Const.HOST_URL + Const.SEND_CHAT_PUSH)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("event_id", eId)
                .addBodyParameter("content", content)
                .addBodyParameter("sender_id", MyApp.curUser.uid)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int bSuccess = response.getInt("success");
                            if (bSuccess == 1) {
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                    }
                });
    }

    public interface OnItemAvatarListener {
        public int onAvatarClick(int pos);
    }

    public interface OnItemCommentListener {
        public int onItemClick(int pos);
    }

    public interface OnItemPhotoListener{
        public int onPhotoClick(int pos, int index);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)(mContext.getSystemService(Activity.INPUT_METHOD_SERVICE));
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showDelConfirmDlg(final String subkey){
        TextView tv_desc;
        Button btn_cancel, btn_sure;
        final Dialog backDlg = new Dialog(mContext, R.style.FullHeightDialog);
        backDlg.setContentView(R.layout.custom_confirm_dlg);
        View dview = backDlg.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc = backDlg.findViewById(R.id.custom_confirm_tv_desc);
        btn_cancel = backDlg.findViewById(R.id.custom_confirm_btn_cancel);
        btn_sure = backDlg.findViewById(R.id.custom_confirm_btn_sure);

        tv_desc.setText(R.string.chat_delete_confirm_desc);
        btn_sure.setText(R.string.btn_yes);
        btn_cancel.setText(R.string.btn_cancel);
        backDlg.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                backDlg.dismiss();
            }
        });

        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.child("chat_history").child(eId).child(subkey).setValue(null);
                backDlg.dismiss();
            }
        });
    }

    /**
     * Get custom filter
     * @return filter
     */
    @Override
    public Filter getFilter() {
        if (chatFilter == null) {
            chatFilter = new ChatFilter();
        }
        return chatFilter;
    }

    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    private class ChatFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                List<MsgModel> tempList = new ArrayList<MsgModel>();
                // search content in friend list
                for (MsgModel one : mDataSet) {
                    if (one.uName.toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            one.sMsg.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(one);
                    } else{
                        for(CommentModel com : one.comments){
                            if (com.uName.toLowerCase().contains(constraint.toString().toLowerCase()) ||
                                    com.sMsg.toLowerCase().contains(constraint.toString().toLowerCase())) {
                                tempList.add(one);
                                break;
                            }
                        }
                    }

                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = mDataSet.size();
                filterResults.values = mDataSet;
            }

            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<MsgModel>) results.values;
            notifyDataSetChanged();
        }
    }
}

class ItemChatTxtHolder extends RecyclerView.ViewHolder{
    public CircleImageView civ_avatar;
    public TextView tv_name, tv_time, tv_content, tv_likes, tv_comments, tv_delete;
    public ImageView iv_like, iv_comment;
    public EditText et_comment;
    public RecyclerView commentRecycler;
    public LinearLayout ll_like, ll_comment;

    public ItemChatTxtHolder(View itemV){
        super(itemV);
        civ_avatar = itemV.findViewById(R.id.chat_text_item_civ_avatar);
        tv_content = itemV.findViewById(R.id.chat_text_item_tv_msg);
        tv_name = itemV.findViewById(R.id.chat_text_item_tv_name);
        tv_time = itemV.findViewById(R.id.chat_text_item_tv_time);
        tv_likes = itemV.findViewById(R.id.chat_text_item_tv_like);
        tv_comments = itemV.findViewById(R.id.chat_text_item_tv_comment);
        iv_like = itemV.findViewById(R.id.chat_text_item_iv_like);
        iv_comment = itemV.findViewById(R.id.chat_text_item_iv_comment);
        commentRecycler = itemV.findViewById(R.id.chat_text_item_recycler);
        et_comment = itemV.findViewById(R.id.chat_text_item_et_comment);
        tv_delete = itemV.findViewById(R.id.chat_text_item_tv_delete);
        ll_comment = itemV.findViewById(R.id.chat_text_item_ll_comment);
        ll_like = itemV.findViewById(R.id.chat_text_item_ll_like);
    }
}

class ItemChatPhotoHolder extends RecyclerView.ViewHolder{
    public CircleImageView civ_avatar;
    public TextView tv_name, tv_time, tv_content, tv_likes, tv_comments, tv_delete;
    public ImageView iv_like, iv_comment;
    public EditText et_comment;
    public RecyclerView commentRecycler, photoRecycler;
    public LinearLayout ll_like, ll_comment;

    public ItemChatPhotoHolder(View itemV){
        super(itemV);
        civ_avatar = itemV.findViewById(R.id.chat_image_item_civ_avatar);
        tv_content = itemV.findViewById(R.id.chat_image_item_tv_count);
        tv_name = itemV.findViewById(R.id.chat_image_item_tv_name);
        tv_time = itemV.findViewById(R.id.chat_image_item_tv_time);
        tv_likes = itemV.findViewById(R.id.chat_image_item_tv_like);
        tv_comments = itemV.findViewById(R.id.chat_image_item_tv_comment);
        iv_like = itemV.findViewById(R.id.chat_image_item_iv_like);
        iv_comment = itemV.findViewById(R.id.chat_image_item_iv_comment);
        photoRecycler = itemV.findViewById(R.id.chat_image_item_recycler_photos);
        commentRecycler = itemV.findViewById(R.id.chat_image_item_recycler);
        et_comment = itemV.findViewById(R.id.chat_image_item_et_comment);
        tv_delete = itemV.findViewById(R.id.chat_image_item_tv_delete);
        ll_comment = itemV.findViewById(R.id.chat_image_item_ll_comment);
        ll_like = itemV.findViewById(R.id.chat_image_item_ll_like);
    }
}

class ItemChatVoiceHolder extends RecyclerView.ViewHolder{
    public CircleImageView civ_avatar;
    public TextView tv_name, tv_time, tv_content, tv_likes, tv_comments, tv_delete;
    public ImageView iv_like, iv_comment, iv_speaker;
    public EditText et_comment;
    public RecyclerView commentRecycler;
    public LinearLayout ll_like, ll_comment;

    public ItemChatVoiceHolder(View itemV){
        super(itemV);
        civ_avatar = itemV.findViewById(R.id.chat_voice_item_civ_avatar);
        tv_content = itemV.findViewById(R.id.chat_voice_item_tv_msg);
        tv_name = itemV.findViewById(R.id.chat_voice_item_tv_name);
        tv_time = itemV.findViewById(R.id.chat_voice_item_tv_time);
        tv_likes = itemV.findViewById(R.id.chat_voice_item_tv_like);
        tv_comments = itemV.findViewById(R.id.chat_voice_item_tv_comment);
        iv_like = itemV.findViewById(R.id.chat_voice_item_iv_like);
        iv_comment = itemV.findViewById(R.id.chat_voice_item_iv_comment);
        commentRecycler = itemV.findViewById(R.id.chat_voice_item_recycler);
        et_comment = itemV.findViewById(R.id.chat_voice_item_et_comment);
        iv_speaker = itemV.findViewById(R.id.chat_voice_item_iv_speaker);
        tv_delete = itemV.findViewById(R.id.chat_voice_item_tv_delete);
        ll_comment = itemV.findViewById(R.id.chat_voice_item_ll_comment);
        ll_like = itemV.findViewById(R.id.chat_voice_item_ll_like);
    }
}
