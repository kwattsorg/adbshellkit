package net.kwatts.android.droidcommandpro.data;

import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// https://firebase.googleblog.com/2017/12/using-android-architecture-components.html
// https://firebase.googleblog.com/2017/12/using-android-architecture-components_20.html\
// https://android.jlelse.eu/android-architecture-components-with-firebase-907b7699f6a0


public class FirebaseQueryLiveData extends LiveData<DataSnapshot> {
    private static final String LOG_TAG = "FirebaseQueryLiveData";

    private final Query query;
    private final ValueEventListener valueListener = new mValueEventListener();
    //private final ChildEventListener childListener = new MyEventListener();
    private final Handler handler = new Handler();
    private List<Command> mQueryValuesList = new ArrayList<>();
    private boolean listenerRemovePending = false;
    private final Runnable removeListener = new Runnable() {
        @Override
        public void run() {
            query.removeEventListener(valueListener);
            listenerRemovePending = false;
        }
    };

    public FirebaseQueryLiveData(Query query) {
        this.query = query;
    }

    public FirebaseQueryLiveData(DatabaseReference dbReference) {
        this.query = dbReference;
    }

    @Override
    protected void onActive() {
        if (listenerRemovePending) {
            handler.removeCallbacks(removeListener);
        } else {
            query.addValueEventListener(valueListener);
        }
        listenerRemovePending = false;
    }

    @Override
    protected void onInactive() {
        // Listener removal is schedule on a two second delay

        handler.postDelayed(removeListener, 2000);
        listenerRemovePending = true;
    }


    private class mValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            setValue(dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(LOG_TAG, "Cannot listen to query " + query, databaseError.toException());
        }
    }


    private class MyEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (dataSnapshot != null) {
                Log.d(LOG_TAG, "onChildAdded(): previous child name = " + s);
                setValue(dataSnapshot);
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Command msg = snap.getValue(Command.class);
                    mQueryValuesList.add(msg);
                }
            } else {
                Log.w(LOG_TAG, "onChildAdded(): data snapshot is NULL");
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(LOG_TAG, "Cannot listen to query " + query, databaseError.toException());
        }

    }

}
