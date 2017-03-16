package backend;

/**
 * Created by Duarte on 16-Mar-17.
 */
public class MDBListener implements Runnable{
    private static MDBListener mdbListener = null;


    private MDBListener() {
    }

    public static MDBListener getInstance(){
        if (mdbListener==null){
            mdbListener= new MDBListener();
        }
        return mdbListener;
    }

    @Override
    public void run() {

    }
}
