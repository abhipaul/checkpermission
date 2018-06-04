package payment.card.io.checkpermission;

/**
 * Created by KIT912 on 1/18/2018.
 */


import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.konylabs.android.KonyMain;
import com.konylabs.vm.Function;
import java.io.PrintStream;

public class KonyBridgeFFI
        extends Activity
        implements ICaptureCall {
    private static Function cb;

    public static void checkPermission(Function callback) {
        cb = callback;
        KonyBridgeFFI d = new KonyBridgeFFI();
        d.callMainActivity();
    }

    public void callMainActivity() {
        MainActivity.registerResponseCallback(this);
        Intent in = new Intent(KonyMain.getActContext(), MainActivity.class);
        KonyMain.getActContext().startActivity(in);
    }

    @Override
    public void getResponse(int i) {
        try {
            Log.d("StandardLib", "data binding " + i);
            cb.execute(new Object[]{i + ""});
            System.out.println(">>>>>>>>>>>>>>>>>>SCAN_RESULT is " + i);
            finish();
        } catch (Exception e) {
            System.out.println(">>>>>>>>>>>>>>>>>>JSCallback invocation failed");
            e.printStackTrace();
        }
    }
}

