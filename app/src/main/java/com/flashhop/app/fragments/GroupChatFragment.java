package com.flashhop.app.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.adapters.ChatAdapter;
import com.flashhop.app.models.AddressModel;
import com.flashhop.app.models.CommentModel;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.LastMsgModel;
import com.flashhop.app.models.MsgDBModel;
import com.flashhop.app.models.MsgModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.ImageFilePath;
import com.flashhop.app.utils.KeyboardUtil;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.flashhop.app.utils.Const.APP_TAG;


public class GroupChatFragment extends DialogFragment {

    public static final int PERMISSION_RECORD_AUDIO = 5678;

    Context context;
    HomeActivity parent;

    View rView;
    RelativeLayout rl_outside;
    ImageView iv_back, iv_menu, iv_attach, iv_voice;
    CircleImageView[] civ_avatar = new CircleImageView[10];
    RelativeLayout rl_avatars;
    Button btn_where, btn_send, btn_invite;
    TextView tv_title, tv_weekday, tv_date, tv_remain;
    EditText et_msg, et_search;
    LinearLayout ll_search;
    NestedScrollView nsv_chat;
    List<String> up_Photos = new ArrayList<>();
    RecyclerView msg_recycler;
    ChatAdapter chatAdapter;
    List<MsgModel> msg_list = new ArrayList<>();
    List<String> online_users = new ArrayList<>();
    public EventModel group_event = new EventModel();

    String sMembers;
    int pre_hometype;
    boolean brecording = false;
    private MediaRecorder mRecorder;
    private File mVoiceFile;

    DatabaseReference database;
    StorageReference storage;

    public GroupChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rView = inflater.inflate(R.layout.fragment_group_chat, container, false);
        context = getContext();
        parent = (HomeActivity)getActivity();
        database = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference();
        pre_hometype = MyApp.home_type;
        MyApp.home_type = Const.HOME_MSG;
        parent.setNavViewVisible(false);

        makeMemberString();
        initLayout();
        loadChatData();
        return rView;
    }

    private void initLayout(){
        rl_outside = rView.findViewById(R.id.group_chat_frag_outside);
        rl_outside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });
        iv_back = rView.findViewById(R.id.group_chat_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRecorder != null && brecording) {
                    stopRecording(false);
                }
                hideKeyboard(view);
                MyApp.home_type = pre_hometype;
                parent.setNavViewVisible(true);
                dismiss();
            }
        });
        iv_menu = rView.findViewById(R.id.group_chat_frag_iv_menu);
        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                /*if(MyApp.selAddr.length() > 0){
                    et_msg.setText("My Address : " + MyApp.selAddr);
                    sendMessage();
                } else*/
                showConfirmDlg();
            }
        });
        //registerForContextMenu(iv_menu);
        iv_voice = rView.findViewById(R.id.group_chat_frag_iv_rec);
        iv_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!brecording){
                    brecording = true;
                    iv_voice.setImageResource(R.drawable.ic_mic_red);
                    voicelength = 0;
                    if(parent.checkRecordPermission())
                        startRecording();
                } else{
                    stopRecording(true);
                    brecording = false;
                    iv_voice.setImageResource(R.drawable.ic_mic);
                    if(voicelength > 0 && mVoiceFile != null){
                        uploadVoice();
                    }
                }
            }
        });

        civ_avatar[0] = rView.findViewById(R.id.group_chat_frag_civ_avatar0);
        civ_avatar[1] = rView.findViewById(R.id.group_chat_frag_civ_avatar1);
        civ_avatar[2] = rView.findViewById(R.id.group_chat_frag_civ_avatar2);
        civ_avatar[3] = rView.findViewById(R.id.group_chat_frag_civ_avatar3);
        civ_avatar[4] = rView.findViewById(R.id.group_chat_frag_civ_avatar4);
        civ_avatar[5] = rView.findViewById(R.id.group_chat_frag_civ_avatar5);
        civ_avatar[6] = rView.findViewById(R.id.group_chat_frag_civ_avatar6);
        civ_avatar[7] = rView.findViewById(R.id.group_chat_frag_civ_avatar7);
        civ_avatar[8] = rView.findViewById(R.id.group_chat_frag_civ_avatar8);
        civ_avatar[9] = rView.findViewById(R.id.group_chat_frag_civ_avatar9);

        rl_avatars = rView.findViewById(R.id.group_chat_frag_rl_avatar);
        rl_avatars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                FragmentManager fragmentManager = parent.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out);
                GroupInfoFragment group_frag = new GroupInfoFragment();
                group_frag.group_event = group_event;
                fragmentTransaction.add(android.R.id.content, group_frag, Const.FRAG_GROUP_INFO).addToBackStack(Const.FRAG_GROUP_INFO).commit();
            }
        });

        ll_search = rView.findViewById(R.id.group_chat_frag_ll_search);
        et_search = rView.findViewById(R.id.group_chat_frag_et_search);
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                chatAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btn_invite = rView.findViewById(R.id.group_chat_frag_btn_invite);
        btn_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.showInviteFriendsFrag(group_event);
                hideKeyboard(view);
            }
        });

        btn_where = rView.findViewById(R.id.group_chat_frag_btn_where);
        btn_where.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                dismiss();
                showHoppersMap();
            }
        });

        iv_attach = rView.findViewById(R.id.group_chat_frag_iv_attach);
        iv_attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                parent.editProData.images.clear();
                parent.choosePhotoFromGallery();
            }
        });

        tv_title = rView.findViewById(R.id.group_chat_frag_tv_title);
        tv_date = rView.findViewById(R.id.group_chat_frag_tv_date);
        tv_weekday = rView.findViewById(R.id.group_chat_frag_tv_weekday);
        tv_remain = rView.findViewById(R.id.group_chat_frag_tv_remain);
        et_msg = rView.findViewById(R.id.group_chat_frag_et_msg);

        btn_send = rView.findViewById(R.id.group_chat_frag_btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                hideKeyboard(view);
            }
        });

        nsv_chat = rView.findViewById(R.id.group_chat_frag_nsv);

        msg_recycler = rView.findViewById(R.id.group_chat_frag_msg_recycler);
        LinearLayoutManager linearMgr = new LinearLayoutManager(context);
        msg_recycler.setLayoutManager(linearMgr);
        chatAdapter = new ChatAdapter(msg_list, context, database, group_event.id);
        msg_recycler.setAdapter(chatAdapter);

        chatAdapter.setOnItemPhotoListener(new ChatAdapter.OnItemPhotoListener() {
            @Override
            public int onPhotoClick(int pos, int index) {
                MsgModel msg = msg_list.get(pos);
                String sPath = msg.photos.get(index);
                previewPhoto(sPath, msg.uName);
                return 0;
            }
        });

        chatAdapter.setOnItemAvatarListener(new ChatAdapter.OnItemAvatarListener() {
            @Override
            public int onAvatarClick(int pos) {
                MsgModel msg = msg_list.get(pos);
                if(MyApp.curUser.uid.equals(msg.uId)){
                    gotoMyProfile();
                    return 0;
                }
                UserModel other = findUser(msg.uId);
                if(other!= null){
                    gotoUserProfile(other);
                }
                return 0;
            }
        });

        tv_title.setText(group_event.title);
        try{
            SimpleDateFormat sDf = new SimpleDateFormat("MMM dd yyyy");
            Date dt = sDf.parse(group_event.date);
            SimpleDateFormat eDf = new SimpleDateFormat("EEE");
            String weekday = eDf.format(dt);
            tv_weekday.setText(weekday);
        } catch (ParseException ex){
            ex.printStackTrace();
        }
        String[] subStr = group_event.date.split(" ");
        if(subStr.length > 1)
            tv_date.setText(subStr[0] + "\n" + subStr[1]);

        Picasso.get()
                .load(group_event.creator.photo_url)
                .into(civ_avatar[0]);

        for(int i = 1; i < 10; i ++){
            civ_avatar[i].setVisibility(View.GONE);
        }
        for(int i = 0; i < group_event.members.size(); i++){
            civ_avatar[i+1].setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(group_event.members.get(i).photo_url)
                    .into(civ_avatar[i+1]);
        }
    }

    private UserModel findUser(String uID){
        if(group_event.creator != null && group_event.creator.uid.equals(uID)){
            return group_event.creator;
        }
        for(UserModel inUser : group_event.members){
            if(inUser.uid.equals(uID)){
                return inUser;
            }
        }
        return null;
    }

    private void sendMessage(){
        if(et_msg.getText().toString().trim().length() > 0){
            long tsLong = System.currentTimeMillis()/1000;
            final String msg = et_msg.getText().toString().trim();
            String key = database.child("chat_history").child(group_event.id).push().getKey();
            MsgDBModel one = new MsgDBModel();
            one.uid = MyApp.curUser.uid;
            one.username = MyApp.curUser.first_name;
            one.avatar = MyApp.curUser.photo_url;
            one.timestamp = tsLong;
            one.type = Const.MSG_TYPE_TEXT;
            one.value = msg;
            one.reads.add(MyApp.curUser.uid);
            database.child("chat_history").child(group_event.id).child(key).setValue(one);
            //last msg register
            LastMsgModel lastMsg = new LastMsgModel();
            lastMsg.uId = MyApp.curUser.uid;
            lastMsg.msg = MyApp.curUser.first_name + " : " + msg;
            lastMsg.uName = MyApp.curUser.first_name;
            lastMsg.lTime = tsLong;
            lastMsg.likes = MyApp.curUser.uid + ",";
            database.child("last_history").child(group_event.id).setValue(lastMsg);
            //send notification
            String sub_msg = msg.length() > 30 ? msg.substring(0,29) + "..." : msg;
            String ct_str = one.username + " : " + sub_msg;
            sendChatNotification(ct_str);
        } else{
            WindowUtils.animateView(context, btn_send);
        }

        et_msg.setText("");
    }

    private void sendPhotos(){
        long tsLong = System.currentTimeMillis()/1000;
        String key = database.child("chat_history").child(group_event.id).push().getKey();
        MsgDBModel one = new MsgDBModel();
        one.uid = MyApp.curUser.uid;
        one.username = MyApp.curUser.first_name;
        one.avatar = MyApp.curUser.photo_url;
        one.timestamp = tsLong;
        one.type = Const.MSG_TYPE_PHOTO;
        one.value = "Posted " + up_Photos.size() + " Photos";
        one.photos.addAll(up_Photos);
        one.reads.add(MyApp.curUser.uid);
        database.child("chat_history").child(group_event.id).child(key).setValue(one);
        //last msg register
        LastMsgModel lastMsg = new LastMsgModel();
        lastMsg.uId = MyApp.curUser.uid;
        lastMsg.msg = MyApp.curUser.first_name + " : Posted " + up_Photos.size() + " Photos";
        lastMsg.uName = MyApp.curUser.first_name;
        lastMsg.lTime = tsLong;
        lastMsg.likes = MyApp.curUser.uid + ",";
        database.child("last_history").child(group_event.id).setValue(lastMsg);
        //send notification
        String ct_str = one.username + " : " + one.value;
        sendChatNotification(ct_str);
    }

    private void sendVoice(String voice_url, String len_str){
        long tsLong = System.currentTimeMillis()/1000;
        String key = database.child("chat_history").child(group_event.id).push().getKey();
        MsgDBModel one = new MsgDBModel();
        one.uid = MyApp.curUser.uid;
        one.username = MyApp.curUser.first_name;
        one.avatar = MyApp.curUser.photo_url;
        one.timestamp = tsLong;
        one.type = Const.MSG_TYPE_VOICE;
        one.value = "Sent Voice : " + len_str;
        one.photos.add(voice_url);
        one.reads.add(MyApp.curUser.uid);
        database.child("chat_history").child(group_event.id).child(key).setValue(one);
        //last msg register
        LastMsgModel lastMsg = new LastMsgModel();
        lastMsg.uId = MyApp.curUser.uid;
        lastMsg.msg = MyApp.curUser.first_name + " : Sent Voice : " + len_str;
        lastMsg.uName = MyApp.curUser.first_name;
        lastMsg.lTime = tsLong;
        lastMsg.likes = MyApp.curUser.uid + ",";
        database.child("last_history").child(group_event.id).setValue(lastMsg);
        //send notification
        String ct_str = lastMsg.msg;
        sendChatNotification(ct_str);
    }

    private void loadChatData(){
        database.child("chat_history").child(group_event.id).orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() != null){
                    MsgModel oneMsg = new MsgModel();
                    oneMsg.dbKey = dataSnapshot.getKey();
                    MsgDBModel other = dataSnapshot.getValue(MsgDBModel.class);
                    int ind = -1;
                    for(int i = 0; i < msg_list.size(); i++){
                        if(msg_list.get(i).dbKey.equals(oneMsg.dbKey)){
                            ind = i;
                            break;
                        }
                    }

                    oneMsg.uId = other.uid;
                    oneMsg.uName = other.username;
                    oneMsg.uPhoto = other.avatar;
                    oneMsg.lTime = other.timestamp;
                    oneMsg.likes.addAll(other.likes);
                    oneMsg.comments.addAll(other.comments);
                    oneMsg.photos.addAll(other.photos);
                    oneMsg.nType = other.type;
                    oneMsg.sMsg = other.value;
                    for(String sval : other.likes){
                        if(sval.equals(MyApp.curUser.uid)){
                            oneMsg.bLike = true;
                        }
                    }
                    for(CommentModel com : other.comments){
                        if(com.uId.equals(MyApp.curUser.uid)){
                            oneMsg.bComment = true;
                        }
                    }
                    if(ind == -1){
                        msg_list.add(oneMsg);
                    } else{
                        msg_list.set(ind, oneMsg);
                    }

                    boolean bRead = false;
                    for(String val : other.reads){
                        if(val.equals(MyApp.curUser.uid)){
                            bRead = true;
                            break;
                        }
                    }
                    if(!bRead){
                        database.child("chat_history").child(group_event.id).child(oneMsg.dbKey).child("read").child(other.reads.size()+"").setValue(MyApp.curUser.uid);
                    }
                    chatAdapter.setDataList(msg_list);
                    nsv_chat.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            nsv_chat.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    },200);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue() != null) {
                    MsgModel oneMsg = new MsgModel();
                    oneMsg.dbKey = dataSnapshot.getKey();
                    MsgDBModel other = dataSnapshot.getValue(MsgDBModel.class);
                    int ind = -1;
                    for (int i = 0; i < msg_list.size(); i++) {
                        if (msg_list.get(i).dbKey.equals(oneMsg.dbKey)) {
                            ind = i;
                            break;
                        }
                    }
                    oneMsg.uId = other.uid;
                    oneMsg.uName = other.username;
                    oneMsg.uPhoto = other.avatar;
                    oneMsg.lTime = other.timestamp;
                    oneMsg.likes.addAll(other.likes);
                    oneMsg.photos.addAll(other.photos);
                    oneMsg.comments.addAll(other.comments);
                    oneMsg.nType = other.type;
                    oneMsg.sMsg = other.value;
                    for (String sval : other.likes) {
                        if (sval.equals(MyApp.curUser.uid)) {
                            oneMsg.bLike = true;
                        }
                    }
                    for (CommentModel com : other.comments) {
                        if (com.uId.equals(MyApp.curUser.uid)) {
                            oneMsg.bComment = true;
                        }
                    }
                    if (ind == -1) {
                        msg_list.add(oneMsg);
                    } else {
                        msg_list.set(ind, oneMsg);
                    }

                    boolean bRead = false;
                    for (String val : other.reads) {
                        if (val.equals(MyApp.curUser.uid)) {
                            bRead = true;
                            break;
                        }
                    }
                    if (!bRead) {
                        database.child("chat_history").child(group_event.id).child(oneMsg.dbKey).child("read").child(other.reads.size() + "").setValue(MyApp.curUser.uid);
                    }

                    chatAdapter.setDataList(msg_list);
                    nsv_chat.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            nsv_chat.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    }, 200);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    String sKey = dataSnapshot.getKey();
                    MsgDBModel other = dataSnapshot.getValue(MsgDBModel.class);
                    for (int i = 0; i < msg_list.size(); i++) {
                        if (msg_list.get(i).dbKey.equals(sKey)) {
                            //send notification
                            //String ct_str = msg_list.get(i).uName + " : Remove a message";
                            //sendChatNotification(ct_str);
                            msg_list.remove(i);
                            chatAdapter.setDataList(msg_list);
                            return;
                        }
                    }
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        database.child("last_history").child(group_event.id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LastMsgModel one = dataSnapshot.getValue(LastMsgModel.class);
                if(one != null && !one.likes.contains(MyApp.curUser.uid)){
                    one.likes = one.likes + MyApp.curUser.uid + ",";
                    database.child("last_history").child(group_event.id).setValue(one);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        database.child("online").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String kid = dataSnapshot.getKey();
                long val = (long)dataSnapshot.getValue();
                if(val == 1){
                    online_users.add(kid);
                }
                refreshOnline();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String kid = dataSnapshot.getKey();
                long val = (long)dataSnapshot.getValue();
                if(val == 1){
                    online_users.add(kid);
                } else{
                    online_users.remove(kid);
                }
                refreshOnline();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void refreshOnline(){
        for(int i = 0; i < msg_list.size(); i++){
            msg_list.get(i).bOnline = false;
            for(int j = 0 ; j < online_users.size(); j++){
                if(msg_list.get(i).uId.equals(online_users.get(j))){
                    msg_list.get(i).bOnline = true;
                    break;
                }
            }
        }
        chatAdapter.setDataList(msg_list);
    }

    public void uploadVoice(){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Uploading...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        //Uri voice_uri = Uri.parse("file://"+mVoiceFile.getAbsolutePath());
        //File file = new File(voice_uri.toString());
        if(mVoiceFile == null) return;

        AndroidNetworking.upload(Const.HOST_URL + Const.UPLOAD_FILE_URL)
                //.addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addMultipartFile("file", mVoiceFile)
                .setTag(APP_TAG)
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {

                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        // do anything with response
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1) {
                                String urlstr = response.getString("data");
                                //This is your image url do whatever you want with it.
                                int minutes = (int) (voicelength / 60000);
                                int seconds = (int) (voicelength / 1000) % 60;
                                String len_str = seconds + "\"";
                                if(minutes > 0){
                                    len_str = minutes + "\' " + seconds + "\"";
                                }
                                sendVoice(urlstr, len_str);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                    }
                });

        /*StorageReference ref = storage.child("voices").child(voice_uri.getLastPathSegment());
        ref.putFile(voice_uri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    hud.dismiss();
                                    String urlstr = uri.toString();
                                    //This is your image url do whatever you want with it.
                                    int minutes = (int) (voicelength / 60000);
                                    int seconds = (int) (voicelength / 1000) % 60;
                                    String len_str = seconds + "\"";
                                    if(minutes > 0){
                                        len_str = minutes + "\' " + seconds + "\"";
                                    }
                                    sendVoice(urlstr, len_str);
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hud.dismiss();
                    }
                });*/
    }

    public void uploadPhotos(){
        List<String> images = parent.editProData.images;
        if(images.size() == 0) return;
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Uploading...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        up_Photos.clear();

        Uri[] uri=new Uri[images.size()];
        for (int i =0 ; i < images.size(); i++) {
            File file = new File(images.get(i));
            if(file != null){
                long fileSizeInMB = file.length()/ (1024*1024);
                if(fileSizeInMB >= 2){
                    File file1 = ImageFilePath.saveBitmapToFile(file);
                }
            }
            uri[i] = Uri.parse("file://"+images.get(i));
            StorageReference ref = storage.child("photos").child(uri[i].getLastPathSegment());
            ref.putFile(uri[i])
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String urlstr = uri.toString();
                                    //This is your image url do whatever you want with it.
                                    up_Photos.add(urlstr);
                                    if(up_Photos.size() == images.size()){
                                        hud.dismiss();
                                        sendPhotos();
                                    }
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hud.dismiss();
                    }
                });

        }
    }

    private void sendChatNotification(String content){
        AndroidNetworking.post(Const.HOST_URL + Const.SEND_CHAT_PUSH)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("event_id", group_event.id)
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

        refreshLayout();
    }

    private void refreshLayout(){
        //remain time
        long diff = TxtUtils.getDifferenceChatTime(group_event.date, group_event.time);
        long l_tt = diff/(60 * 24);
        if(l_tt == 0){
            l_tt = diff/60;
            if(l_tt == 1){
                l_tt = diff;
                String str = String.format(getString(R.string.chat_group_remain), l_tt + " minutes");
                tv_remain.setText(str);
            } else{
                String str = String.format(getString(R.string.chat_group_remain), l_tt + " hours");
                tv_remain.setText(str);
            }
        } else{
            String str = String.format(getString(R.string.chat_group_remain), l_tt + " days");
            tv_remain.setText(str);
        }

        if(pre_hometype == Const.HOME_EVENT){
            //compare endtime
            String[] timeStr = group_event.time.split("-");
            if(timeStr.length == 1){
                diff = TxtUtils.getDifferenceTime(group_event.date, timeStr[0]);
                diff += 120;//add 2hours
            } else{
                diff = TxtUtils.getDifferenceTime(group_event.date, timeStr[1]);
            }
            if(diff > 0){
                btn_where.setVisibility(View.VISIBLE);
                ll_search.setVisibility(View.GONE);
            } else{
                btn_where.setVisibility(View.GONE);
                ll_search.setVisibility(View.GONE);
            }
        } else{
            btn_where.setVisibility(View.GONE);
            ll_search.setVisibility(View.VISIBLE);
        }

        diff = TxtUtils.getDifferenceTime(group_event.date, group_event.time);
        if(diff < 0){
            btn_invite.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
    }

    private void makeMemberString(){
        sMembers = group_event.creator.uid;
        for(int i = 0; i < group_event.members.size(); i++){
            sMembers = sMembers + "," + group_event.members.get(i).uid;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId()==R.id.group_chat_frag_iv_menu){
            parent.getMenuInflater().inflate(R.menu.group_chat_menu,menu);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.group_chat_show_users_photos:
                Toast.makeText(parent.getApplicationContext(),"Show user profile",Toast.LENGTH_LONG).show();
                return true;
            case R.id.group_chat_show_online:
                return true;
            case R.id.group_chat_tracking_host:
                return true;
            case R.id.group_chat_search:
                return true;
            case R.id.group_chat_mute_notification:
                return true;
            case R.id.group_chat_exit:
                return true;
            case R.id.group_chat_report:
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void gotoMyProfile(){
        parent.setNavViewVisible(true);
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ProfileFragment profileFrag = new ProfileFragment();
        fragmentTransaction.add(R.id.home_frame, profileFrag, Const.FRAG_PROFILE_TAG).addToBackStack(Const.FRAG_PROFILE_TAG).commit();
    }

    public void gotoUserProfile(UserModel other){
        parent.setNavViewVisible(true);
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        UserProfileFragment userFrag = new UserProfileFragment();
        userFrag.other = other;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_slide_in_up, R.anim.ani_slide_in_down, R.anim.ani_slide_in_up, R.anim.ani_slide_in_down);
        transaction.add(R.id.home_frame, userFrag, "USER_FRAG").addToBackStack("USER_FRAG").commit();
    }

    private void showHoppersMap(){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        HoppersFragment hoppersFrag = new HoppersFragment();
        hoppersFrag.ev = group_event;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.replace(R.id.home_frame, hoppersFrag, Const.FRAG_HOPPERS_TAG).addToBackStack(Const.FRAG_HOPPERS_TAG).commit();
    }

    public void previewPhoto(String strFile, String uname){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        PreviewPostedFrag previewDlg = new PreviewPostedFrag();
        previewDlg.image_path = strFile;
        previewDlg.user_name = uname;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(android.R.id.content, previewDlg, "PREVIEW_DLG").addToBackStack("PREVIEW_DLG").commit();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (mRecorder != null) {
            stopRecording(false);
        }
    }

    private void showConfirmDlg(){
        TextView tv_desc;
        Button btn_cancel, btn_sure;
        final Dialog backDlg = new Dialog(context, R.style.FullHeightDialog);
        backDlg.setContentView(R.layout.custom_confirm_dlg);
        View dview = backDlg.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc  = backDlg.findViewById(R.id.custom_confirm_tv_desc);
        btn_cancel = backDlg.findViewById(R.id.custom_confirm_btn_cancel);
        btn_sure = backDlg.findViewById(R.id.custom_confirm_btn_sure);

        tv_desc.setText(R.string.chat_send_current_location);
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
                getAddress(MyApp.curLoc.getLatitude(), MyApp.curLoc.getLongitude());
                if(MyApp.myAddr.length() > 0){
                    et_msg.setText("My Address : " + MyApp.myAddr);
                    sendMessage();
                }
                backDlg.dismiss();
            }
        });
    }

    //recording voice
    private long mStartTime = 0;
    private long voicelength = 0;

    //private int[] amplitudes = new int[100];
    //private int i = 0;

    /*private Handler mHandler = new Handler();
    private Runnable mTickExecutor = new Runnable() {
        @Override
        public void run() {
            //tick();
            mHandler.postDelayed(mTickExecutor,100);
        }
    };*/

    public void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
            mRecorder.setAudioEncodingBitRate(48000);
        } else {
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setAudioEncodingBitRate(64000);
        }
        mRecorder.setAudioSamplingRate(16000);
        mVoiceFile = getOutputFile();
        mVoiceFile.getParentFile().mkdirs();
        mRecorder.setOutputFile(mVoiceFile.getAbsolutePath());

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartTime = SystemClock.elapsedRealtime();
            //mHandler.postDelayed(mTickExecutor, 100);
            Log.d("Voice Recorder","started recording to "+mVoiceFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e("Voice Recorder", "prepare() failed "+e.getMessage());
        }
    }

    protected  void stopRecording(boolean saveFile) {
        if(mRecorder != null){
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            voicelength = SystemClock.elapsedRealtime() - mStartTime;
            mStartTime = 0;
            //mHandler.removeCallbacks(mTickExecutor);
            if (!saveFile && mVoiceFile != null) {
                mVoiceFile.delete();
            }
        }
    }

    private File getOutputFile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath().toString()
                + "/Flashhop/RECORDING_"
                + dateFormat.format(new Date())
                + ".m4a");
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)(parent.getSystemService(Activity.INPUT_METHOD_SERVICE));
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private String getAddress(double lat, double lng) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                for(int j = 0; j < addresses.size(); j++){
                    Address addr = addresses.get(j);
                    AddressModel one = new AddressModel();
                    String addr_str = addr.getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    //String city = addr.getLocality();
                    //String state = addr.getAdminArea();
                    //String country = addr.getCountryName();
                    //String postalCode = addr.getPostalCode();
                    String knownName = addr.getFeatureName(); // Only if available else return NULL
                    one.localAddr = knownName;
                    MyApp.myAddr = addr_str;
                }

            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }

    /*private void tick() {
        long time = (mStartTime < 0) ? 0 : (SystemClock.elapsedRealtime() - mStartTime);
        int minutes = (int) (time / 60000);
        int seconds = (int) (time / 1000) % 60;
        int milliseconds = (int) (time / 100) % 10;
        //mTimerTextView.setText(minutes+":"+(seconds < 10 ? "0"+seconds : seconds)+"."+milliseconds);
        if (mRecorder != null) {
            amplitudes[i] = mRecorder.getMaxAmplitude();
            //Log.d("Voice Recorder","amplitude: "+(amplitudes[i] * 100 / 32767));
            if (i >= amplitudes.length -1) {
                i = 0;
            } else {
                ++i;
            }
        }
    }*/

}
