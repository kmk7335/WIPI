package com.example.isolatorv.wipi.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by tlsdm on 2017-09-06.
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.isolatorv.wipi.R;
import com.example.isolatorv.wipi.MapData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

import static android.content.Context.LOCATION_SERVICE;

/* 작성자 : 나신의
 * 최초 작성일자 :17/08/14
 * 마지막 수정일자 : 17/10/24
 * 플래그 먼트 클래스 맵을 띄우고 동물병원을 찾아 맵에 찍음
 */

public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        PlacesListener {

    private GoogleApiClient mGoogleApiClient = null;
    private GoogleMap mGoogleMap = null;
    private MapView mapView = null;
    private Marker currentMarker = null;

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초


    boolean askPermissionOnceAgain = false;
    boolean mRequestingLocationUpdates = false;
    boolean mMoveMapByUser = true;
    boolean mMoveMapByAPI = true;
    Location mCurrentLocation;

    private boolean currentPositionOn = true;
    LatLng currentPosition;
    List<Marker> previous_marker = null;

    LocationRequest locationRequest = new LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


    private boolean hostpitalreday = true;
    private boolean petstoreready = false;


    List<MapData> hospitalList = null;
    List<MapData> petShopList = null;
    List<MapData> hostpitalholiday=null;
    List<MapData> cafeList = null;

    private boolean hospitalOn=false;
    private boolean petstoreOn=false;
    private boolean hospitalWeekendOn=false;
    private boolean coffieShopOn=false;


    private OnFragmentInteractionListener listener;

    //floatingActionButton 정의
    FloatingActionButton fab;
    FloatingActionButton fab1;
    FloatingActionButton fab2;
    FloatingActionButton fab3;
    FloatingActionButton fab4;

    //Save the FAB's active status
    //false -> fab = close
    //true -> fab = open
    private boolean FAB_Status = false;

    //LinearLayout
    LinearLayout Llayout_1;
    LinearLayout Llayout_2;
    LinearLayout Llayout_3;
    LinearLayout Llayout_4;

    //TextView
    TextView textView_1;
    TextView textView_2;
    TextView textView_3;
    TextView textView_4;

    //Animations
    Animation show_fab_1;
    Animation hide_fab_1;
    Animation show_fab_2;
    Animation hide_fab_2;
    Animation show_fab_3;
    Animation hide_fab_3;
    Animation show_fab_4;
    Animation hide_fab_4;

    private static final String TAG_JSON="result";
    private static final String TAG_INDEX="Index";
    private static final String TAG_RATITUDE = "Lat";
    private static final String TAG_RONGTITUDE = "Long";
    private static final String TAG_ADDRESS ="Address";
    private static final String TAG_POST = "Post";
    private static final String TAG_NAME = "Name";


    String mJsonString;
    String mJsonString2;
    //플래그먼트가 액티비티에 붙을때 호출
    /*onAttach*************************************************************************************/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


        if (context instanceof FeedFragment.OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }


    }
    public static MapFragment newInstance() {
        return new MapFragment();
    }
    /*onAttach*************************************************************************************/

    //생성될때 초기화 UI는 안됨
    /*onCreate*************************************************************************************/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    /*onCreate*************************************************************************************/


    public interface OnFragmentInteractionListener {

        void onClicked();

    }
    //플래그먼트가 액티비티에 떨어질 때 호출
    /*onDetach*************************************************************************************/
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
    /*onDetach*************************************************************************************/

    //onCreate와 같은 매서드
    /*onCreateView*********************************************************************************/
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.map, container, false);
        previous_marker = new ArrayList<Marker>();

        if(hospitalList==null)hospitalList = new ArrayList<MapData>();
        if(petShopList==null)petShopList = new ArrayList<MapData>();
        if(hostpitalholiday==null)hostpitalholiday = new ArrayList<MapData>();
        if(cafeList==null)cafeList = new ArrayList<MapData>();

        Log.d(TAG, "onCreateView");
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        mapView = (MapView) layout.findViewById(R.id.map);
        mapView.getMapAsync(this);

        //하단 큰버튼 작은 버튼 정의
        fab = (FloatingActionButton) layout.findViewById(R.id.fab);
        fab1 = (FloatingActionButton) layout.findViewById(R.id.fab_1);
        fab2 = (FloatingActionButton) layout.findViewById(R.id.fab_2);
        fab3 = (FloatingActionButton) layout.findViewById(R.id.fab_3);
        fab4 = (FloatingActionButton) layout.findViewById(R.id.fab_4);

        //애니메이션 정의
        show_fab_1 = AnimationUtils.loadAnimation(getActivity(), R.anim.fab1_show);
        hide_fab_1 = AnimationUtils.loadAnimation(getActivity(), R.anim.fab1_hide);
        show_fab_2 = AnimationUtils.loadAnimation(getActivity(), R.anim.fab2_show);
        hide_fab_2 = AnimationUtils.loadAnimation(getActivity(), R.anim.fab2_hide);
        show_fab_3 = AnimationUtils.loadAnimation(getActivity(), R.anim.fab3_show);
        hide_fab_3 = AnimationUtils.loadAnimation(getActivity(), R.anim.fab3_hide);
        show_fab_4 = AnimationUtils.loadAnimation(getActivity(), R.anim.fab4_show);
        hide_fab_4 = AnimationUtils.loadAnimation(getActivity(), R.anim.fab4_hide);

        //LinearLayout
        Llayout_1 = (LinearLayout)layout.findViewById(R.id.linearyLayout1);
        Llayout_2 = (LinearLayout)layout.findViewById(R.id.linearyLayout2);
        Llayout_3 = (LinearLayout)layout.findViewById(R.id.linearyLayout3);
        Llayout_4 = (LinearLayout)layout.findViewById(R.id.linearyLayout4);

        //TextView
        textView_1 = (TextView)layout.findViewById(R.id.text_1);
        textView_2 = (TextView)layout.findViewById(R.id.text_2);
        textView_3 = (TextView)layout.findViewById(R.id.text_3);
        textView_4 = (TextView)layout.findViewById(R.id.text_4);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (FAB_Status == false) {
                    //Display FAB menu
                    expandFAB();
                    FAB_Status = true;
                } else {
                    //Close FAB menu
                    hideFAB();
                    FAB_Status = false;
                }
            }
        });

        //하단 버튼 이벤트
        /*fab event*******************************************************************************/
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hospitalOn=!hospitalOn;
                createMarker();
                if (FAB_Status == false) {
                    //Display FAB menu
                    expandFAB();
                    FAB_Status = true;
                } else {
                    //Close FAB menu
                    hideFAB();
                    FAB_Status = false;
                }
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                petstoreOn =!petstoreOn;
                createMarker();
                if (FAB_Status == false) {
                    //Display FAB menu
                    expandFAB();
                    FAB_Status = true;
                } else {
                    //Close FAB menu
                    hideFAB();
                    FAB_Status = false;
                }
            }
        });

        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hospitalWeekendOn = !hospitalWeekendOn;
                createMarker();
                if (FAB_Status == false) {
                    //Display FAB menu
                    expandFAB();
                    FAB_Status = true;
                } else {
                    //Close FAB menu
                    hideFAB();
                    FAB_Status = false;
                }
            }
        });

        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coffieShopOn = !coffieShopOn;
                createMarker();
                if (FAB_Status == false) {
                    //Display FAB menu
                    expandFAB();
                    FAB_Status = true;
                } else {
                    //Close FAB menu
                    hideFAB();
                    FAB_Status = false;
                }
            }
        });

        GetData task = new GetData();
        task.execute("http://13.229.34.115/AndroidPHP.php");

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.map_title);


        return layout;
    }
    /*onCreateView*********************************************************************************/

    //OncreateView다음에 호출 view를 변경하는 작업이 가능
    /*onActivityCreated****************************************************************************/
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //액티비티가 처음 생성될 때 실행되는 함수
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() == false) {

            Log.d(TAG, "onActivityCreated: mGoogleApiClient connect");
        mGoogleApiClient.connect();
    }

        if (mapView != null) {
        mapView.onCreate(savedInstanceState);
    }
    }
    /*onActivityCreated****************************************************************************/

    //유저에게 플래그먼트가 보이도록 하는 매서드
    /*onStart**************************************************************************************/
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }
    /*onStart**************************************************************************************/

    //유저와 상호작용이 가능하게 되는 매서드
    /*onResume*************************************************************************************/
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        if (mGoogleApiClient.isConnected()) {

            Log.d(TAG, "onResume : call startLocationUpdates");
            if (!mRequestingLocationUpdates) startLocationUpdates();
        }


        //앱 정보에서 퍼미션을 허가했는지를 다시 검사해봐야 한다.
        if (askPermissionOnceAgain) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askPermissionOnceAgain = false;

                checkPermissions();
            }
        }
    }
    /*onResume*************************************************************************************/

    //부모액티비티가 아닌 다른 액티비가 forground로 나오게 되면 호출
    /*onPause**************************************************************************************/
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        FAB_Status = false;
    }
    /*onPause**************************************************************************************/

    //다른 액티비가 화면을 완전히 가리게 되면 호출
    /*onStop***************************************************************************************/
    @Override
    public void onStop() {
        if (mRequestingLocationUpdates) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            stopLocationUpdates();
        }

        if (mGoogleApiClient.isConnected()) {

            Log.d(TAG, "onStop : mGoogleApiClient disconnect");
            mGoogleApiClient.disconnect();
        }

        mMoveMapByAPI = true;
        currentPositionOn=true;
        FAB_Status = false;
        super.onStop();
        mapView.onStop();
    }
    /*onStop***************************************************************************************/

    //플래그먼트가 사라질때 호출 상태를 번들에 저장
    /*onSaveInstanceState**************************************************************************/
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    /*onSaveInstanceState**************************************************************************/

    //메모리에서 내려올때 호출
    /*onLowMemory**********************************************************************************/
    @Override
    public void onLowMemory() {
        mMoveMapByAPI = true;
        currentPositionOn=true;
        FAB_Status = false;

        super.onLowMemory();
        mapView.onLowMemory();
    }
    /*onLowMemory**********************************************************************************/

    //플래그먼트가 제거될때 호출
    /*onDestroy************************************************************************************/
    @Override
    public void onDestroy() {
        mMoveMapByAPI = true;
        currentPositionOn=true;
        FAB_Status = false;
        super.onDestroy();
        mapView.onLowMemory();
        /*if(mOnMyListener !=null){
            mOnMyListener.onReceivedData("menu");
        }*/
    }
    /*onDestroy************************************************************************************/


    //구글맵 사용이 가능할때 콜백하는 매서드
    /*onMapReady***********************************************************************************/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");

        mGoogleMap = googleMap;
        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {

            @Override
            public boolean onMyLocationButtonClick() {

                Log.d(TAG, "onMyLocationButtonClick : 위치에 따른 카메라 이동 활성화");
                mMoveMapByAPI = true;
                return true;
            }
        });
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d(TAG, "onMapClick :");

            }
        });

        mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {

            @Override
            public void onCameraMoveStarted(int i) {

                if (mMoveMapByUser == true && mRequestingLocationUpdates) {

                    Log.d(TAG, "onCameraMove : 위치에 따른 카메라 이동 비활성화");
                    mMoveMapByAPI = false;
                }

                mMoveMapByUser = true;

            }
        });


        mGoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {

            @Override
            public void onCameraMove() {


            }
        });


    }
    /*onMapReady***********************************************************************************/

    //구글맵 연결에 성공했을때 콜백하는 매서드
    /*onConnected**********************************************************************************/
    @Override
    public void onConnected(@Nullable Bundle bundle) {
       try{
           if (mRequestingLocationUpdates == false) {

               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                   int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                           Manifest.permission.ACCESS_FINE_LOCATION);

                   if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {

                       ActivityCompat.requestPermissions(getActivity(),
                               new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                               PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                   } else {

                       Log.d(TAG, "onConnected : 퍼미션 가지고 있음");
                       Log.d(TAG, "onConnected : call startLocationUpdates");
                       startLocationUpdates();
                       mGoogleMap.setMyLocationEnabled(true);
                   }

               } else {

                   Log.d(TAG, "onConnected : call startLocationUpdates");
                   startLocationUpdates();
                   mGoogleMap.setMyLocationEnabled(true);
               }
           }
       }catch (Exception e){
           e.getMessage();
       }

    }
    /*onConnected**********************************************************************************/

    //구글맵연결에 실패 했을때 콜백 매서드
    /*onConnectionFailed***************************************************************************/
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
        setDefaultLocation();
    }
    /*onConnectionFailed***************************************************************************/

    //구글맵연결에 문제 발생시 콜백 매서드
    /*onConnectionSuspended************************************************************************/
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended");
        if (cause == CAUSE_NETWORK_LOST)
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG, "onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");
    }
    /*onConnectionSuspended************************************************************************/

    //사용자 위치를 받아오는 매서드(반복호출)
    /*onLocationChanged****************************************************************************/
    @Override
    public void onLocationChanged(Location location) {


        Log.d(TAG, "onLocationChanged : ");


        setCurrentLocation(location);

        mCurrentLocation = location;

        currentPosition
                = new LatLng(location.getLatitude(), location.getLongitude());

        if(hostpitalreday){showPlaceInformation(currentPosition);}
        if(petstoreready){ showPlaceInformation(currentPosition);}
        if(currentPositionOn){createMarker(); currentPositionOn=false;}

    }
    /*onLocationChanged****************************************************************************/

    //GPS와 NETWORK 서비스 사용여부 반환 매서드
    /*checkLocationServicesStatus*******************************************************************/
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    /*checkLocationServicesStatus*******************************************************************/

    //처음 시작시 퍼미션 체크후 위치를 잡아오는 매서드
    /*startLocationUpdates*************************************************************************/
    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }
            Log.d(TAG, "startLocationUpdates : call FusedLocationApi.requestLocationUpdates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            mRequestingLocationUpdates = true;

        }

    }
    /*startLocationUpdates*************************************************************************/

    //위치 서비스 중지 매서드
    /*stopLocationUpdates**************************************************************************/
    private void stopLocationUpdates() {

        Log.d(TAG, "stopLocationUpdates : LocationServices.FusedLocationApi.removeLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }
    /*stopLocationUpdates**************************************************************************/

    //현재 위치를 잡아서 구글맵에 찍어주는 매서드
    /*setCurrentLocation***************************************************************************/
    public void setCurrentLocation(Location location) {

        mMoveMapByUser = false;


        if (currentMarker != null) currentMarker.remove();


        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (mMoveMapByAPI) {

            Log.d(TAG, "setCurrentLocation :  mGoogleMap moveCamera "
                    + location.getLatitude() + " " + location.getLongitude());
            // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }
    /*setCurrentLocation***************************************************************************/

    //기본 위치를 설정 매서드
    /*setDefaultLocation***************************************************************************/
    public void setDefaultLocation() {

        mMoveMapByUser = false;

        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);

        if (currentMarker != null) currentMarker.remove();


        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);

    }
    /*setDefaultLocation***************************************************************************/


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    //권한 체크 매서드
    /*checkPermissions*****************************************************************************/
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        boolean fineLocationRationale = ActivityCompat
                .shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager
                .PERMISSION_DENIED && fineLocationRationale)
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");

        else if (hasFineLocationPermission
                == PackageManager.PERMISSION_DENIED && !fineLocationRationale) {
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " +
                    "체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");
        } else if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");
            if (mGoogleApiClient.isConnected() == false) {

                Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");
                mGoogleApiClient.connect();
            }
        }
    }
    /*checkPermissions*****************************************************************************/

    //권한을 콜백 하는 매서드
    /*onRequestPermissionsResult********************************************************************/
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (permsRequestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0) {

            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (permissionAccepted) {
                if (mGoogleApiClient.isConnected() == false) {

                    Log.d(TAG, "onRequestPermissionsResult : mGoogleApiClient connect");
                    mGoogleApiClient.connect();
                }
            } else {

                checkPermissions();
            }
        }
    }
    /*onRequestPermissionsResult********************************************************************/


    //권한 체크시 띄우는 매서드
    /*showDialogForPermission***********************************************************************/
    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                getActivity().finish();
            }
        });
        builder.create().show();
    }
    /*showDialogForPermission***********************************************************************/

    //권한 체크시 다이얼로그를 띄움
    /*showDialogForPermissionSetting****************************************************************/
    private void showDialogForPermissionSetting(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                askPermissionOnceAgain = true;

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getActivity().getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                getActivity().finish();
            }
        });
        builder.create().show();
    }
    /*showDialogForPermissionSetting****************************************************************/

    //여기부터는 GPS 활성화를 위한 메소드들
    //GPS창을 띄움
    /*showDialogForLocationServiceSetting***********************************************************/
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
    /*showDialogForLocationServiceSetting***********************************************************/

    //액티비가 켜지고 결과를 반환 하는 매서드
    /*onActivityResult*****************************************************************************/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : 퍼미션 가지고 있음");


                        if (mGoogleApiClient.isConnected() == false) {

                            Log.d(TAG, "onActivityResult : mGoogleApiClient connect ");
                            mGoogleApiClient.connect();
                        }
                        return;
                    }
                }

                break;
        }
    }
    /*onActivityResult*****************************************************************************/

    //안쓰는 매서드

    /**********************************************************************************************/
    @Override
    public void onPlacesFailure(PlacesException e) {

    }
    @Override
    public void onPlacesStart() {

    }

    /**********************************************************************************************/

    //구글맵에 장소들을 받아오는데 성공하면 실행 매서드
    /*onPlacesSuccess******************************************************************************/
    @Override
    public void onPlacesSuccess(final List<Place> places) {
        try {


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    for (noman.googleplaces.Place place : places) {
                        if (hostpitalreday) {
                            hospitalList.add(new MapData(place.getName(), place.getLatitude(), place.getLongitude(), place.getVicinity()));
                        }
                        if (petstoreready) {
                            petShopList.add(new MapData(place.getName(), place.getLatitude(), place.getLongitude(), place.getVicinity()));
                        }
                    }
                }
            });

        } catch (RuntimeException e) {
            e.printStackTrace();
        }


    }
    /*onPlacesSuccess******************************************************************************/

    //구글맵에 장소를 찍는 매서드
    /*showPlaceInformation*************************************************************************/
    public void showPlaceInformation(LatLng location) {

        if(petstoreready){
            new NRPlaces.Builder()
                    .listener(this)
                    .key("AIzaSyCWZjDQSkXD5jzLn4QGOPBmBix9gztod68")
                    .latlng(location.latitude, location.longitude)//현재 위치
                    .radius(3000) //3K미터 내에서 검색
                    .type(PlaceType.PET_STORE) //펫샵
                    .build()
                    .execute();
            Log.d(TAG,"showPlaceInformation: petstore");
        }

        if (hostpitalreday) {
            new NRPlaces.Builder()
                    .listener(this)
                    .key("AIzaSyCWZjDQSkXD5jzLn4QGOPBmBix9gztod68")
                    .latlng(location.latitude, location.longitude)//현재 위치
                    .radius(3000) //3K미터 내에서 검색
                    .type(PlaceType.VETERINARY_CARE) //동물병원
                    .build()
                    .execute();
            Log.d(TAG,"showPlaceInformation: hostpital");
        }

    }
    /*showPlaceInformation*************************************************************************/

    //구글 장소가 끝난뒤
    /*onPlacesFinished*****************************************************************************/
    @Override
    public void onPlacesFinished() {
        if(hostpitalreday&&hospitalList!=null){
            hostpitalreday=false;
            Log.d(TAG,"onPlacesFinhished-hospitalListSize : "+hospitalList.size());
            petstoreready =true;

        }
        if(petstoreready&&petShopList.size()>0){
            Log.d(TAG,"onPlacesFinhished-petShopListSize : "+petShopList.size());
            petstoreready =false;
        }
        Log.d(TAG,"onPlacesFinhished-hostpitalholidayListSize : "+hostpitalholiday.size());
        Log.d(TAG,"onPlacesFinhished-CafeListSize : "+cafeList.size());
    }
    /*onPlacesFinished*****************************************************************************/

    //마크 찍기
    /*createMarker*********************************************************************************/
    public void createMarker() {
        mGoogleMap.clear();
        Log.d(TAG,"createMarker : hostpitalListsize : "+hospitalList.size());
        Log.d(TAG,"createMarker : petShopListsize : "+petShopList.size());
        Log.d(TAG,"createMarker : hostpitalholidayListsize : "+hostpitalholiday.size());
        if (hospitalList.size()>0&& hospitalOn ) {
            Log.d(TAG,"hospitalListMarker");
            Log.d(TAG, "hospitalListListsize : " + hospitalList.size());
            for (int i = 0; i < hospitalList.size(); i++) {
                LatLng latLng
                        = new LatLng(hospitalList.get(i).getLatitude()
                        , hospitalList.get(i).getLongtitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(hospitalList.get(i).getName());
                markerOptions.snippet(hospitalList.get(i).getsinpat());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                mGoogleMap.addMarker(markerOptions);
            }
        }
        if (petShopList.size()>0&& petstoreOn) {
            Log.d(TAG,"petShopListCreateMarker");
            Log.d(TAG, "petShopList :" + petShopList.size());
            for (int i = 0; i < petShopList.size(); i++) {
                LatLng latLng
                        = new LatLng(petShopList.get(i).getLatitude()
                        , petShopList.get(i).getLongtitude());

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(petShopList.get(i).getName());
                markerOptions.snippet(petShopList.get(i).getsinpat());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                mGoogleMap.addMarker(markerOptions);
            }
        }

        if (hostpitalholiday.size() > 0&&hospitalWeekendOn) {
            Log.d(TAG, "hostpitalholidayCreateMarker");
            Log.d(TAG, "hostpitalholiday :" + petShopList.size());
            for (int i = 0; i < hostpitalholiday.size(); i++) {
                LatLng latLng
                        = new LatLng(hostpitalholiday.get(i).getLatitude()
                        , hostpitalholiday.get(i).getLongtitude());

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(hostpitalholiday.get(i).getName());
                markerOptions.snippet(hostpitalholiday.get(i).getsinpat());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                mGoogleMap.addMarker(markerOptions);
            }
        }

        if (cafeList.size() > 0&& coffieShopOn) {
            Log.d(TAG, "cafeListCreateMarker");
            Log.d(TAG, " cafeList :" + cafeList.size());
            for (int i = 0; i < cafeList.size(); i++) {
                LatLng latLng
                        = new LatLng(cafeList.get(i).getLatitude()
                        , cafeList.get(i).getLongtitude());

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(cafeList.get(i).getName());
                markerOptions.snippet(cafeList.get(i).getsinpat());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                mGoogleMap.addMarker(markerOptions);
            }
        }
    }
    /*createMarker*********************************************************************************/

    //맵 하단 오른쪽 버튼 나타내기 매서드
    /*expandFAB************************************************************************************/
    private void expandFAB() {

        //Floating Action Button 1
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) Llayout_1.getLayoutParams();
        layoutParams.rightMargin += (int)(Llayout_1.getWidth()*0.2);
        layoutParams.bottomMargin += (int)(Llayout_1.getHeight()*1.5);
        Llayout_1.setLayoutParams(layoutParams);
        Llayout_1.startAnimation(show_fab_1);
        fab1.setClickable(true);

        //Floating Action Button 2
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) Llayout_2.getLayoutParams();
        layoutParams2.rightMargin += (int) (Llayout_2.getWidth() * 0.2);
        layoutParams2.bottomMargin += (int) (Llayout_2.getHeight() * 2.7);
        Llayout_2.setLayoutParams(layoutParams2);
        Llayout_2.startAnimation(show_fab_2);
        fab2.setClickable(true);

        //Floating Action Button 3
        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) Llayout_3.getLayoutParams();
        layoutParams3.rightMargin += (int) (Llayout_3.getWidth() * 0.2);
        layoutParams3.bottomMargin += (int) (Llayout_3.getHeight() * 3.9);
        Llayout_3.setLayoutParams(layoutParams3);
        Llayout_3.startAnimation(show_fab_3);
        fab3.setClickable(true);

        //Floating Action Button 4
        FrameLayout.LayoutParams layoutParams4 = (FrameLayout.LayoutParams) Llayout_4.getLayoutParams();
        layoutParams4.rightMargin += (int) (Llayout_4.getWidth() * 0.2);
        layoutParams4.bottomMargin += (int) (Llayout_4.getHeight() * 5.1);
        Llayout_4.setLayoutParams(layoutParams4);
        Llayout_4.startAnimation(show_fab_4);
        fab4.setClickable(true);

        textView_1.setVisibility(View.VISIBLE);
        textView_2.setVisibility(View.VISIBLE);
        textView_3.setVisibility(View.VISIBLE);
        textView_4.setVisibility(View.VISIBLE);
    }
    /*expandFAB************************************************************************************/

    //맵 하단 오른쪽 버튼 숨기기 매서드
    /*hideFAB***************************************************************************************/
    private void hideFAB() {

        textView_1.setVisibility(View.INVISIBLE);
        textView_2.setVisibility(View.INVISIBLE);
        textView_3.setVisibility(View.INVISIBLE);
        textView_4.setVisibility(View.INVISIBLE);

        //Floating Action Button 1
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) Llayout_1.getLayoutParams();
        layoutParams.rightMargin -= (int)(Llayout_1.getWidth()*0.2);
        layoutParams.bottomMargin -= (int)(Llayout_1.getHeight()*1.5);
        Llayout_1.setLayoutParams(layoutParams);
        Llayout_1.startAnimation(hide_fab_1);
        fab1.setClickable(false);

        //Floating Action Button 2
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) Llayout_2.getLayoutParams();
        layoutParams2.rightMargin -= (int) (Llayout_2.getWidth() * 0.2);
        layoutParams2.bottomMargin -= (int) (Llayout_2.getHeight() * 2.7);
        Llayout_2.setLayoutParams(layoutParams2);
        Llayout_2.startAnimation(hide_fab_2);
        fab2.setClickable(false);

        //Floating Action Button 3
        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) Llayout_3.getLayoutParams();
        layoutParams3.rightMargin -= (int) (Llayout_3.getWidth() * 0.2);
        layoutParams3.bottomMargin -= (int) (Llayout_3.getHeight() * 3.9);
        Llayout_3.setLayoutParams(layoutParams3);
        Llayout_3.startAnimation(hide_fab_3);
        fab3.setClickable(false);

        //Floating Action Button 4
        FrameLayout.LayoutParams layoutParams4 = (FrameLayout.LayoutParams) Llayout_4.getLayoutParams();
        layoutParams4.rightMargin -= (int) (Llayout_4.getWidth() * 0.2);
        layoutParams4.bottomMargin -= (int) (Llayout_4.getHeight() * 5.1);
        Llayout_4.setLayoutParams(layoutParams4);
        Llayout_4.startAnimation(hide_fab_4);
        fab4.setClickable(false);
    }
    /*hideFAB***************************************************************************************/

    //PHP접속하는 이너 클래스
    //24시 동물병원,동물을데리고 들어갈수 있는 커피숍을 가져온다
    /*GetData***************************************************************************************/
    private class GetData extends AsyncTask<String,Void,String> {
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

            Log.d(TAG, "response  - " + result);

            if (result == null){

                Log.d(TAG,errorString);
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
                Log.d(TAG, "response code - " + responseStatusCode);

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

                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }

    }
    private void showResult() {
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject item = jsonArray.getJSONObject(i);

                String index = item.getString(TAG_INDEX);
                String name = item.getString(TAG_NAME);
                String latitude1 = item.getString(TAG_RATITUDE);
                String longtitude1 = item.getString(TAG_RONGTITUDE);
                String address = item.getString(TAG_ADDRESS);
                String post1 = item.getString(TAG_POST);

                int index_i = Integer.parseInt(index);
                double latitude_d = Double.parseDouble(latitude1);
                double logntitude_d = Double.parseDouble(longtitude1);
                int post_i = Integer.parseInt(post1);

                if(index_i==1) hostpitalholiday.add(new MapData(name, latitude_d, logntitude_d, address, post_i));
                else if(index_i==2) cafeList.add(new MapData(name,latitude_d,logntitude_d,address,post_i));
            }
        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }
    }
    /*GetData***************************************************************************************/


}