package dualtech.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.matesnetwork.Cognalys.VerifyMobile;

/**
* Created by Jesz on 18-Jul-15.
*/
public class MobileReg extends Activity implements View.OnClickListener {

    private static final String TAG = "MobileReg";
    static String phnNo, user_Nm;
    EditText mobileNum, name;
    Button reg_next;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        mobileNum = (EditText) findViewById(R.id.phone_num);
        name = (EditText) findViewById(R.id.user_name);
        mobileNum.setText(VerifyNumber.getNO());
        reg_next = (Button) findViewById(R.id.bt_reg);
        reg_next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_reg:
                phnNo = String.valueOf(mobileNum.getText());
                user_Nm = String.valueOf(name.getText());

                ApplicationInit.setMobile_number(phnNo);
                ApplicationInit.setUser(user_Nm);

                Intent in = new Intent(this, VerifyMobile.class);
                in.putExtra("app_id", "79717e0a828b47c9bbb7156");
                in.putExtra("access_token","c944c1de46a398e7d305341b52f1027714ed6945");
                in.putExtra("mobile", phnNo);

                startActivityForResult(in, VerifyMobile.REQUEST_CODE);
                Log.d(TAG, phnNo);
                /*Intent i = new Intent().setClass(getApplicationContext(), BroadcastActivity.class);
                startActivity(i);*/
        }
    }

    @Override
    public void onBackPressed() {
    }

}