package com.gepardec.mega.service.api;


import com.google.api.services.gmail.model.WatchResponse;

public interface GmailService {

    WatchResponse watchInbox();

    Void stopWatchingInbox();
}
