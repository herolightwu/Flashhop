package com.flashhop.app.models;

import java.util.ArrayList;
import java.util.List;

public class MsgModel {
    public String dbKey;
    public String uId;
    public String uName;
    public String uPhoto;
    public long lTime;
    public long nType;
    public String sMsg;
    public List<String> photos = new ArrayList<>();
    public List<String> likes = new ArrayList<>();//users list
    public List<CommentModel> comments = new ArrayList<>();
    public boolean bLike;
    public boolean bComment;
    public boolean bOnline;
}
