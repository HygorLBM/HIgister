package com.iaz.higister.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iaz.higister.R;
import com.iaz.higister.data.model.ListItem;
import com.iaz.higister.data.model.UserList;
import com.iaz.higister.ui.viewItem.ViewItemActivity;
import com.iaz.higister.ui.viewItem.ViewItemFragment;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by alksander on 05/03/2018.
 */

public class MyListsFragment extends Fragment implements MyListsMvpView {

    @Inject
    MyListsPresenter mListsPresenter;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    MainActivity activity;

    public UserListsAdapter mListAdapter;
    String type;

    public static MyListsFragment newInstance(String type) {
        MyListsFragment myListsFragment = new MyListsFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        myListsFragment.setArguments(args);
        return myListsFragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getString("type");
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        activity = (MainActivity) getActivity();
        activity.activityComponent().inject(this);

        mListsPresenter.attachView(this);


        if (type.equals("created")) {
            mListsPresenter.receiveLists(new MyListsPresenter.OnUpdateLists() {
                @Override
                public void onSuccess(ArrayList<UserList> userLists) {
                    updateData(userLists);
                }

                @Override
                public void onFailed(Exception e) {
                    Log.e("receiveMyLists: ", "failed", e);
                }
            });
        }
        if (type.equals("favorited")) {
            mListsPresenter.receiveFavorites(new MyListsPresenter.OnUpdateLists() {
                @Override
                public void onSuccess(ArrayList<UserList> userLists) {
                    updateData(userLists);
                }

                @Override
                public void onFailed(Exception e) {
                    Log.e("receiveMyLists: ", "failed", e);
                }
            });
        }
        if (type.equals("feed")) {
            mListsPresenter.receiveFeed(new MyListsPresenter.OnUpdateLists() {
                @Override
                public void onSuccess(ArrayList<UserList> userLists) {
                    updateData(userLists);
                }

                @Override
                public void onFailed(Exception e) {
                    Log.e("receiveMyLists: ", "failed", e);
                }
            });
        }
    }

    @Override
    public MyListsFragment getFragment() {
        return this;
    }

    @Override
    public void updateData(ArrayList<UserList> lists) {

        if (mListAdapter == null) {
            mListAdapter = new UserListsAdapter(this, lists, type);
        }
        else {
            mListAdapter.setLists(lists);
            mListAdapter.notifyDataSetChanged();
        }

        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}