package me.avelar.donee.controller;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

import me.avelar.donee.dao.CollectionDAO;
import me.avelar.donee.model.Collection;
import me.avelar.donee.model.Session;
import me.avelar.donee.util.IntentFactory;
import me.avelar.donee.web.DoneeService.RequestStatus;
import me.avelar.donee.web.SenderIntentService;

public class CollectionLogic {

    public static final String EXTRA_CONTENT = "CONTENT";
    public static final String EXTRA_FOCUSED = "FOCUSED";
    public static final String EXTRA_COLLECTION = "COLLECTION";

    // destinations
    public static final int DRAFTS = 0, OUTBOX = 1;

    public static void storeCollection(Context context, Collection collection, int destination) {
        Session session = SessionManager.getLastSession(context);
        if (session == null) {
            sendBroadcastToActivity(context, RequestStatus.UNKNOWN_ERROR, destination);
        } else {
            if (destination == OUTBOX) collection.setSubmitted(true);
            if (CollectionDAO.insert(context, collection, session.getUser())) {
                sendBroadcastToActivity(context, RequestStatus.SUCCEEDED, destination);
            } else {
                sendBroadcastToActivity(context, RequestStatus.UNKNOWN_ERROR, destination);
            }
        }
    }

    public static void sendOutbox(Context context) {
        Intent senderIntent = new Intent(context,SenderIntentService.class);
        SessionManager.validateCurrent(context);
        context.startService(senderIntent);
    }

    private static void sendBroadcastToActivity(Context c, RequestStatus status, int destination) {
        Intent intent;
        switch (status) {
            case SUCCEEDED:
                intent = IntentFactory.create(IntentFactory.Type.COLLECTION_STORED);
                break;
            default: // so far no action
                intent = IntentFactory.create(IntentFactory.Type.COLLECTION_STORED_ERROR);
                break;
        }
        intent.putExtra(IntentFactory.EXTRA_DETAIL, destination);
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
    }

    private static void sendBroadcastToFragment(Context c, ArrayList<Collection> collections) {
        Intent intent = IntentFactory.create(IntentFactory.Type.COLLECTION_LOADED);
        intent.putExtra(IntentFactory.EXTRA_DATA, collections);
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
    }

    public static void getCollections(Context context, int collectionType) {
        Session session = SessionManager.getLastSession(context);
        if (session == null) {
            sendBroadcastToFragment(context, null);
            return;
        }

        ArrayList<Collection> collections = null;
        switch (collectionType) {
            case DRAFTS:
                collections = CollectionDAO.findDrafts(context, session.getUser());
                break;
            case OUTBOX:
                collections = CollectionDAO.findOutbox(context, session.getUser());
                break;
        }
        sendBroadcastToFragment(context, collections);
    }

    public static void deleteAll(Context context, int contentType) {
        CollectionDAO.delete(context, contentType);
        Intent intent = IntentFactory.create(IntentFactory.Type.COLLECTION_LOADED);
        intent.putExtra(IntentFactory.EXTRA_DETAIL, true);
        intent.putExtra(IntentFactory.EXTRA_DATA,   new ArrayList<Collection>());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
