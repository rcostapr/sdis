package protocols;

import java.sql.Timestamp;
import java.util.Date;

import backend.ConfigManager;

public class SharedClock {

	private static final int FIVE_MINS = 5 * 60 * 1000;

    private long mSharedTime = 0;
    private long mEndSyncTime = 0;
    private boolean isSynced = false;
    private Date mDate;

    private static SharedClock sInstance = null;

    private SharedClock() {
        mDate = new Date();
    }

    public static SharedClock getInstance() {
        if (sInstance == null) {
            sInstance = new SharedClock();
        }

        return sInstance;
    }

    public long getTime() throws NotSyncedException {
        if (!isSynced) {
            throw new NotSyncedException();
        }

        long now = mDate.getTime();

        return mSharedTime + now - mEndSyncTime;

    }

    public String getTimeString() throws NotSyncedException {
        return new Timestamp(getTime()).toString();
    }

    public void startSync() {
        new Thread(new SharedTimeUpdater()).start();
    }

    private class SharedTimeUpdater implements Runnable {

        @Override
        public void run() {
            while (ConfigManager.getConfigManager().isAppRunning()) {
                try {
                    MasterServices masterRef = MasterPeer.getInstance().getMasterStub();
                    if (masterRef == null) {
                        System.err.println("Error getting the master reference.");
                        try {
                            Thread.sleep(FIVE_MINS);
                        } catch (InterruptedException e) {
                            System.err.println("Thread interrupted. Exiting...");
                            System.exit(1);
                        }
                        continue;
                    }
                    long receivedTime = MasterPeer.getInstance().getMasterStub().getMasterClock();
                    long startSyncTime = mDate.getTime();

                    mEndSyncTime = mDate.getTime();
                    mSharedTime = receivedTime + mEndSyncTime - startSyncTime;

                    System.out.println("Clock synchronized from " + mDate.toString() + " to " + new Date(mSharedTime).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(FIVE_MINS);
                } catch (InterruptedException e) {
                    System.err.println("Thread interrupted. Exiting...");
                    System.exit(1);
                }
            }
        }
    }

    public static class NotSyncedException extends Exception {
    }
}
