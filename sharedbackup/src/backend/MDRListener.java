package backend;

/**
 * Created by Duarte on 16-Mar-17.
 */
public class MDRListener implements Runnable {
    private static MDRListener mdrListener = null;


    private MDRListener() {
    }

    public static MDRListener getInstance(){
        if (mdrListener==null){
            mdrListener= new MDRListener();
        }
        return mdrListener;
    }

    @Override
    public void run() {

    }
}
