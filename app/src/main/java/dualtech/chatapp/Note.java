 /**
 * Created by tunde_000 on 31/08/2015.
public class LoadContacts extends Activity {
    private static final String TAG = "LOADCONTACTS";
    static ArrayList<String> numbers;
    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_contact);
        spinner=(ProgressBar)findViewById(R.id.pb1);
        spinner.setVisibility(View.VISIBLE);
        prefs = this.getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        read_contact();
        numbers = new ArrayList<>();
        gcm = GoogleCloudMessaging.getInstance(this);
    }

    private void getContactsFromServer(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;

                Gson gson = new Gson();
                final String jsonPhoneList = gson.toJson(numbers);

                StringRequest postRequest = new StringRequest(Request.Method.POST, ApplicationInit.SERVER_ADDRESS,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "Response: " + response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, error.toString());
                        Toast.makeText(getApplicationContext(), "Server failed to receive the Contacts", Toast.LENGTH_SHORT).show();
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        // the POST parameters:
                        params.put("Contacts", "getContacts");
                        params.put("ContactList", jsonPhoneList);
                        params.put("UserOwner", ApplicationInit.getREGISTRATION_KEY());
                        return params;
                    }
                };

                int socketTimeOut = 60000;//60sec
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeOut,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                postRequest.setRetryPolicy(policy);

                Volley.newRequestQueue(LoadContacts.this).add(postRequest);

                msg = "Sent Contact";
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(LoadContacts.this, msg, Toast.LENGTH_LONG).show();
                Intent openMain = new Intent("dualtech.chatapp.MAINACTIVITY");
                openMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(openMain);
            }
        }.execute(null, null, null);
    }

    public void read_contact() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Log.d(TAG, "doInBack");
                ContentResolver CR = LoadContacts.this.getContentResolver();
                Cursor contact_details = CR.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1", null,
                        "UPPER (" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");

                if (contact_details.moveToFirst()) {
                    do {
                        String contactName = contact_details.getString(contact_details.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String phoneNumber;

                        if (Integer.parseInt(contact_details.getString(contact_details.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            //Get all associated numbers
                            phoneNumber = contact_details.getString(contact_details.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            numbers.add(phoneNumber);
                        }
                    } while (contact_details.moveToNext());
                }
                contact_details.close();
                Log.d(TAG, "Done");
                return "DONE";
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d(TAG, "Done list");
                getContactsFromServer();
            }
        }.execute();
    }
}


**/
