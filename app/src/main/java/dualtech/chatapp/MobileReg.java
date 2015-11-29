package dualtech.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
* Created by Jesz on 18-Jul-15.
*/
public class MobileReg extends Activity implements View.OnClickListener {

    private static final String TAG = "MobileReg";
    static String phnNo, user_Nm, verificationCode;
    EditText mobileNum, name, code;
    Button reg_next;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        Bundle bundle = getIntent().getExtras();
        verificationCode = bundle.getString("verificationCode");
        mobileNum = (EditText) findViewById(R.id.phone_num);
        name = (EditText) findViewById(R.id.user_name);
        mobileNum.setText(VerifyNumber.getNO());
        reg_next = (Button) findViewById(R.id.bt_reg);
        reg_next.setOnClickListener(this);
        code = (EditText) findViewById(R.id.act_code);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_reg:
                phnNo = String.valueOf(mobileNum.getText());
                user_Nm = String.valueOf(name.getText());

                ApplicationInit.setMobile_number(phnNo);
                ApplicationInit.setUser(user_Nm);

                /*Intent in = new Intent(this, VerifyMobile.class);
                in.putExtra("app_id", "79717e0a828b47c9bbb7156");
                in.putExtra("access_token","c944c1de46a398e7d305341b52f1027714ed6945");
                in.putExtra("mobile", phnNo);

                startActivityForResult(in, VerifyMobile.REQUEST_CODE);*/
                Log.d(TAG, phnNo);
                /*Intent i = new Intent().setClass(getApplicationContext(), BroadcastActivity.class);
                startActivity(i);*/
                if(verificationCode.equals(code.getText().toString())){
                    Intent i = new Intent().setClass(getApplicationContext(), BroadcastActivity.class);
                    startActivity(i);
                }else{
                    Toast.makeText(getApplicationContext(), "Activation Code is Wrong", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, VerifyNumber.class);
                    startActivity(i);
                }

        }
    }

    /*protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        // TODO Auto-generated method stub
        super.onActivityResult(arg0, arg1, arg2);

        if (arg0 == VerifyMobile.REQUEST_CODE) {
            String message = arg2.getStringExtra("message");
            int result = arg2.getIntExtra("result", 0);

            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
                    .show();
            Toast.makeText(getApplicationContext(), Integer.toString(result), Toast.LENGTH_SHORT)
                    .show();
            Intent i = new Intent().setClass(getApplicationContext(), BroadcastActivity.class);
                startActivity(i);
        }
    }*/

    @Override
    public void onBackPressed() {
    }

}