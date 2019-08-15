package com.flashhop.app.models;

import java.util.ArrayList;
import java.util.List;

public class MsgDBModel {
    public String uid;
    public String username;
    public String avatar;
    public long timestamp;
    public long type;
    public String value;
    public List<String> photos = new ArrayList<>();
    public List<String> likes = new ArrayList<>();//users list
    public List<CommentModel> comments = new ArrayList<>();
    public List<String> reads = new ArrayList<>();
}
