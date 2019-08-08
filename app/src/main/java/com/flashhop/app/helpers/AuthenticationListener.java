package com.flashhop.app.helpers;

public interface AuthenticationListener {
    void onTokenReceived(String auth_token);
}