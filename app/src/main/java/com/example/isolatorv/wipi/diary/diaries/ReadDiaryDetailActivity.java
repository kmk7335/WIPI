package com.example.isolatorv.wipi.diary.diaries;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.isolatorv.wipi.R;
import com.example.isolatorv.wipi.diary.Utils.BitmapUtils;
import com.example.isolatorv.wipi.diary.Utils.CommonUtils;
import com.example.isolatorv.wipi.diary.Utils.DateUtils;
import com.example.isolatorv.wipi.diary.Utils.DialogUtils;
import com.example.isolatorv.wipi.diary.Utils.EasyDiaryUtils;
import com.example.isolatorv.wipi.diary.Utils.FontUtils;
import com.example.isolatorv.wipi.diary.helper.EasyDiaryActivity;
import com.example.isolatorv.wipi.diary.photo.PhotoViewPagerActivity;

import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ReadDiaryDetailActivity extends EasyDiaryActivity {

    private long mCurrentTimeMillis;

    @BindView(R.id.container)
    ViewPager mViewPager;

    @BindView(R.id.edit)
    ImageView mEdit;


    @BindView(R.id.delete)
    ImageView mDelete;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);

        final int startPageIndex;
        // init viewpager
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), getIntent().getStringExtra("query"));
        if (savedInstanceState == null) {
            startPageIndex = mSectionsPagerAdapter.sequenceToPageIndex(getIntent().getIntExtra("sequence", -1));
        } else {
            startPageIndex = mSectionsPagerAdapter.sequenceToPageIndex(savedInstanceState.getInt("sequence", -1));
        }

        setContentView(R.layout.activity_read_diary_detail);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.read_diary_detail_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FontUtils.setToolbarTypeface(toolbar, Typeface.DEFAULT);

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                PlaceholderFragment fragment = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem());
                Log.i("determine", String.valueOf(fragment.getActivity()));
                if (fragment.getActivity() != null) {
                    fragment.setDiaryTypeface();
                    fragment.setDiaryFontSize();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(startPageIndex, false);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        PlaceholderFragment fragment = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem());
        outState.putInt("sequence", fragment.mSequence);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick({R.id.delete, R.id.edit})
    public void onClick(View view) {

//        ViewGroup viewPagerRootView = (ViewGroup) mViewPager.getChildAt(0);
//        mViewPager.setCurrentItem(2);
//        float fontSize = ((TextView) viewPagerRootView.findViewById(R.id.title)).getTextSize();
        final PlaceholderFragment fragment = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem());
        float fontSize = fragment.mTitle.getTextSize();

        switch(view.getId()) {
            case R.id.delete:
                DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DiaryDao.deleteDiary(fragment.mSequence);

                        finish();
                    }
                };
                DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                };
                DialogUtils.showAlertDialog(ReadDiaryDetailActivity.this, getString(R.string.delete_confirm), positiveListener, negativeListener);
                break;
            case R.id.edit:
                Intent updateDiaryIntent = new Intent(ReadDiaryDetailActivity.this, UpdateDiaryActivity.class);
                updateDiaryIntent.putExtra("sequence", fragment.mSequence);
                startActivity(updateDiaryIntent);
                finish();
                break;
        }
    }

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String DIARY_SEQUENCE = "diary_sequence";
        private static final String DIARY_SEARCH_QUERY = "diary_search_query";
        private int mSequence;

        @BindView(R.id.contents)
        TextView mContents;

        @BindView(R.id.title)
        TextView mTitle;

        @BindView(R.id.date)
        TextView mDate;

        @BindView(R.id.weather)
        ImageView mWeather;

        @BindView(R.id.photoContainer)
        ViewGroup mPhotoContainer;

        @BindView(R.id.photoContainerScrollView)
        HorizontalScrollView mHorizontalScrollView;

        public PlaceholderFragment() {}

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sequence, String query) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(DIARY_SEQUENCE, sequence);
            args.putString(DIARY_SEARCH_QUERY, query);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            // bind view
            View rootView = inflater.inflate(R.layout.fragment_read_diary_detail, container, false);
            mContents = (TextView) rootView.findViewById(R.id.contents);
            mTitle = (TextView) rootView.findViewById(R.id.title);
            mDate = (TextView) rootView.findViewById(R.id.date);
            mWeather = (ImageView) rootView.findViewById(R.id.weather);
            ViewGroup mPhotoContainer = (ViewGroup) rootView.findViewById(R.id.photoContainer);
            HorizontalScrollView mHorizontalScrollView = (HorizontalScrollView) rootView.findViewById(R.id.photoContainerScrollView);

            mSequence = getArguments().getInt(DIARY_SEQUENCE);
            DiaryDto diaryDto = DiaryDao.readDiaryBy(mSequence);
            if (StringUtils.isEmpty(diaryDto.getTitle())) {
                mTitle.setVisibility(View.GONE);
            }
            mTitle.setText(diaryDto.getTitle());
            mContents.setText(diaryDto.getContents());
            mDate.setText(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));

            String query = getArguments().getString(DIARY_SEARCH_QUERY);
            if (StringUtils.isNotEmpty(query)) {
                EasyDiaryUtils.highlightString(mTitle, query);
                EasyDiaryUtils.highlightString(mContents, query);
            }

            int weather = diaryDto.getWeather();
            EasyDiaryUtils.initWeatherView(mWeather, weather);

            if (diaryDto.getPhotoUris() != null && diaryDto.getPhotoUris().size() > 0) {
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent photoViewPager = new Intent(getContext(), PhotoViewPagerActivity.class);
                        photoViewPager.putExtra("sequence", mSequence);
                        startActivity(photoViewPager);
                    }
                };

                for (PhotoUriDto dto : diaryDto.getPhotoUris()) {
                    Uri uri = Uri.parse(dto.getPhotoUri());
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapUtils.decodeUri(getContext(), uri, CommonUtils.dpToPixel(getContext(), 70, 1), CommonUtils.dpToPixel(getContext(), 60, 1), CommonUtils.dpToPixel(getContext(), 40, 1));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.question_mark_4);
                    }
                    ImageView imageView = new ImageView(getContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(CommonUtils.dpToPixel(getContext(), 70, 1), CommonUtils.dpToPixel(getContext(), 50, 1));
                    layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(getContext(), 3, 1), 0);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setBackgroundResource(R.drawable.bg_card_01);
                    imageView.setImageBitmap(bitmap);
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    mPhotoContainer.addView(imageView);
                    imageView.setOnClickListener(onClickListener);
                }
            } else {
                mHorizontalScrollView.setVisibility(View.GONE);
            }
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            setDiaryTypeface();
            setDiaryFontSize();
        }

        private void setDiaryTypeface() {
            FontUtils.setTypeface(getContext(), getActivity().getAssets(), mTitle);
            FontUtils.setTypeface(getContext(), getActivity().getAssets(), mDate);
            FontUtils.setTypeface(getContext(), getActivity().getAssets(), mContents);
        }

        private void setDiaryFontSize() {
            float fontSize = CommonUtils.loadFloatPreference(getContext(), "font_size", 0);
            if (fontSize > 0) {
                mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
                mDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
                mContents.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            }
        }

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private List<DiaryDto> mDiaryList;
        private List<PlaceholderFragment> mFragmentList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm, String query) {
            super(fm);
            this.mDiaryList = DiaryDao.readDiary(query);
            for (DiaryDto diaryDto : mDiaryList) {
                mFragmentList.add(PlaceholderFragment.newInstance(diaryDto.getSequence(), query));
            }
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        public PlaceholderFragment getFragment(int position) {
            return mFragmentList.get(position);
        }

        public int sequenceToPageIndex(int sequence) {
            int pageIndex = 0;
            if (sequence > -1) {
                for (int i = 0; i < mDiaryList.size(); i++) {
                    if (mDiaryList.get(i).getSequence() == sequence) {
                        pageIndex = i;
                        break;
                    }
                }
            }
            return pageIndex;
        }

        @Override
        public int getCount() {
            return mDiaryList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

}