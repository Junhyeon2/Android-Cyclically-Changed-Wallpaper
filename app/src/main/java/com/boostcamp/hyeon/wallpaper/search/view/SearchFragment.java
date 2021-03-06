package com.boostcamp.hyeon.wallpaper.search.view;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.boostcamp.hyeon.wallpaper.R;
import com.boostcamp.hyeon.wallpaper.base.app.WallpaperApplication;
import com.boostcamp.hyeon.wallpaper.base.domain.ImageNaver;
import com.boostcamp.hyeon.wallpaper.base.listener.EndlessRecyclerViewScrollListener;
import com.boostcamp.hyeon.wallpaper.base.util.SharedPreferenceHelper;
import com.boostcamp.hyeon.wallpaper.detail.view.DetailActivity;
import com.boostcamp.hyeon.wallpaper.search.adapter.ImageNaverListAdapter;
import com.boostcamp.hyeon.wallpaper.search.presenter.ImageNaverListPresenter;
import com.boostcamp.hyeon.wallpaper.search.presenter.ImageNaverListPresenterImpl;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements ImageNaverListPresenter.View, TextView.OnEditorActionListener, RadioGroup.OnCheckedChangeListener{
    private static final String TAG = SearchFragment.class.getSimpleName();
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.tv_search_message) TextView mSearchMessageTextView;
    private EditText mSearchEditText;
    private ImageNaverListAdapter mImageNaverListAdapter;
    private ImageNaverListPresenterImpl mImageNaverListPresenter;
    private RadioGroup mSearchResultSaveRadioGroup;
    private String mSearchResultSaveValue;
    private String mQuery;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        //for use option menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);

        init();
        return view;
    }

    private void init(){
        // get EditText from Toolbar
        Toolbar toolbar = (Toolbar)(getActivity()).findViewById(R.id.toolbar);
        mSearchEditText = (EditText)toolbar.findViewById(R.id.et_search);
        mSearchEditText.setOnEditorActionListener(this);

        //init SharedPreferences
        if(SharedPreferenceHelper.getInstance().getString(SharedPreferenceHelper.Key.STRING_SEARCH_RESULT_SAVE, null) == null){
            SharedPreferenceHelper.getInstance().put(SharedPreferenceHelper.Key.STRING_SEARCH_RESULT_SAVE, getString(R.string.label_search_result_do_not_save));
        }else if(isSaveSearchResult()){
            mSearchEditText.setText(SharedPreferenceHelper.getInstance().getString(SharedPreferenceHelper.Key.STRING_LAST_SEARCH_QUERY, ""));
        }else if(!isSaveSearchResult()){
            deleteSearchResult();
        }

        //init adapter
        Realm realm = WallpaperApplication.getRealmInstance();
        realm.beginTransaction();
        RealmResults<ImageNaver>  imageNaverRealmResults = realm.where(ImageNaver.class).findAll();

        mImageNaverListAdapter = new ImageNaverListAdapter(
                imageNaverRealmResults,
                false
        );
        realm.commitTransaction();

        //init Image(Right) RecyclerView
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mImageNaverListAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                mImageNaverListPresenter.search(mQuery, totalItemsCount);
            }
        });

        //init presenter
        mImageNaverListPresenter = new ImageNaverListPresenterImpl(getContext());
        mImageNaverListPresenter.attachView(this);
        mImageNaverListPresenter.setListAdapterModel(mImageNaverListAdapter);
        mImageNaverListPresenter.setListAdapterView(mImageNaverListAdapter);

        //init initial value
        mSearchResultSaveValue = SharedPreferenceHelper.getInstance().getString(SharedPreferenceHelper.Key.STRING_SEARCH_RESULT_SAVE, null);

        if(mImageNaverListAdapter.getData().size() <= 0)
            mSearchMessageTextView.setVisibility(View.VISIBLE);
        else
            mSearchMessageTextView.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_option_setting:
                showOptionSettingDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(actionId == EditorInfo.IME_ACTION_DONE){
            deleteSearchResult();
            mQuery = v.getText().toString();
            mImageNaverListPresenter.search(mQuery, 1);
            SharedPreferenceHelper.getInstance().put(SharedPreferenceHelper.Key.STRING_LAST_SEARCH_QUERY, mQuery);
        }
        return false;
    }

    @Override
    public void moveToDetailActivity(Bundle bundle) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtras(bundle);

        getActivity().startActivity(intent);
    }

    private void showOptionSettingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.item_menu_option_alert);
        builder.setPositiveButton(getString(R.string.label_setting_completion), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                completeSearchOptionSettings();
            }
        });
        builder.setNegativeButton(getString(R.string.label_cancel), null);
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mSearchResultSaveRadioGroup = (RadioGroup)((Dialog)dialog).findViewById(R.id.rb_search_result_save);
                mSearchResultSaveRadioGroup.setOnCheckedChangeListener(SearchFragment.this);
                if(isSaveSearchResult()){
                    mSearchResultSaveRadioGroup.check(mSearchResultSaveRadioGroup.getChildAt(0).getId());
                }else{
                    mSearchResultSaveRadioGroup.check(mSearchResultSaveRadioGroup.getChildAt(1).getId());
                }
            }
        });
        alertDialog.show();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton radioButton = (RadioButton)group.findViewById(checkedId);
        if(radioButton == null)
            return;
        if(group.equals(mSearchResultSaveRadioGroup) && radioButton.isChecked()) {
            mSearchResultSaveValue = radioButton.getText().toString();
        }
    }

    private void completeSearchOptionSettings(){
        SharedPreferenceHelper.getInstance().put(SharedPreferenceHelper.Key.STRING_SEARCH_RESULT_SAVE, mSearchResultSaveValue);
        if(isSaveSearchResult()){
            SharedPreferenceHelper.getInstance().put(SharedPreferenceHelper.Key.STRING_LAST_SEARCH_QUERY, "");
        }
    }

    private boolean isSaveSearchResult(){
        return (SharedPreferenceHelper.getInstance().getString(SharedPreferenceHelper.Key.STRING_SEARCH_RESULT_SAVE, null).equals(getString(R.string.label_search_result_save)));
    }

    private void deleteSearchResult(){
        Realm realm = WallpaperApplication.getRealmInstance();
        realm.beginTransaction();
        realm.where(ImageNaver.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public void showFailureMessage() {
        Toast.makeText(getActivity(), getString(R.string.message_search_failure), Toast.LENGTH_SHORT).show();
    }
}
