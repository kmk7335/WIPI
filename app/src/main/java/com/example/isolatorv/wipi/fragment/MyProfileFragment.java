package com.example.isolatorv.wipi.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.isolatorv.wipi.CircleImageView;
import com.example.isolatorv.wipi.R;
import com.example.isolatorv.wipi.adapter.ListViewAdapter;
import com.example.isolatorv.wipi.login.Constants;
import com.example.isolatorv.wipi.login.JoinActivity;
import com.example.isolatorv.wipi.login.RegisterPet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import jp.wasabeef.glide.transformations.CropCircleTransformation;


/**
 * Created by tlsdm on 2017-09-06.
 */

public class MyProfileFragment extends Fragment {

    private SharedPreferences pref;
    private static final String TAG = "option_example";
    TextView viewName;
    TextView viewEmail;
    TextView viewSno;
    TextView viewPetName;
    TextView viewPetType;
    TextView viewPetAge;
    private String userName;
    private String userEmail;
    private String petName;
    private String petType;
    private String petAge;
    private String petImage;
    private int sno;
    private String unique_id;
    private ListView listView;
    private ListViewAdapter adapter;
    ImageView viewPetImage;
    private Button logoutBtn;

    String mJsonString;
    //플래그먼트가 액티비티에 붙을때 호출
    /*onAttach*************************************************************************************/
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Bundle extra = getArguments();
        userName = extra.getString("name");
        userEmail = extra.getString("email");
        unique_id = extra.getString("unique_id");
        sno = extra.getInt("sno");


        GetData task = new GetData();
        task.execute("http://13.229.34.115/getPetInfo.php");

    }
    /*onAttach*************************************************************************************/
    public static MyProfileFragment newInstance() {
        return new MyProfileFragment();
    }
    //onCreate와 같은 매서드
    /*onCreateView*********************************************************************************/
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_my_profile,container,false);


        logoutBtn = (Button) layout.findViewById(R.id.logoutBtn);

        viewPetImage = (ImageView) layout.findViewById(R.id.pet_image);



        listView = (ListView)layout.findViewById(R.id.listview1);


        pref= getActivity().getSharedPreferences("WIPI",0);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });


        return layout;
    }

    /*onCreateView*********************************************************************************/
    private class GetData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString =null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(),"Please Wait",null,true,true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            Log.d(Constants.TAG, "response  - " +  result);

            if (result == null){

                Log.d(Constants.TAG,errorString);
            }
            else {
                mJsonString = result;
                showResult();
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(Constants.TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {
                Log.d(Constants.TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }

        private void showResult(){
            try{
                JSONObject jsonobject = new JSONObject(mJsonString);
                JSONArray jsonarray = jsonobject.getJSONArray("result");

                for(int i = 0; i <jsonarray.length(); i++){
                    JSONObject item = jsonarray.getJSONObject(i);

                    if(item.getInt("sno")==sno){
                        petName = item.getString("pet_name");
                        petType = item.getString("pet_type");
                        petAge = item.getString("pet_age");
                        petImage = item.getString("pet_image");

                        adapter = new ListViewAdapter(getActivity());


                        adapter.addItem(petImage,petName, petType,"male",petAge, "small");
                        listView.setAdapter(adapter);
                        Log.d("TAG", petName);
                        Log.d("TAG", petType);
                        Log.d("TAG", petAge);






                        break;
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }
    private void logout() {

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constants.IS_LOGGED_IN,false);
        editor.putString(Constants.EMAIL,"");
        editor.putString(Constants.NAME,"");
        editor.putString(Constants.UNIQUE_ID,"");
        editor.apply();
        Intent intent = new Intent(getActivity(), JoinActivity.class);
        startActivity(intent);
        getActivity().finish();

    }

}
