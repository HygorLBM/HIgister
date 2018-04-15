package com.iaz.higister.ui.viewItem;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iaz.higister.data.DataManager;
import com.iaz.higister.data.model.ListItem;
import com.iaz.higister.data.model.UserList;
import com.iaz.higister.injection.ConfigPersistent;
import com.iaz.higister.ui.base.BasePresenter;
import com.iaz.higister.ui.viewList.ViewListActivity;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.iaz.higister.util.Constants.PERMISSION_WRITE_EXTERNAL;
import static com.iaz.higister.util.Constants.REQUEST_IMAGE_CAPTURE;
import static com.iaz.higister.util.Constants.REQUEST_IMAGE_CAPTURE_BANNER;
import static com.iaz.higister.util.Constants.SELECT_PICTURE;
import static com.iaz.higister.util.Constants.SELECT_PICTURE_BANNER;

@ConfigPersistent
public class ViewItemPresenter extends BasePresenter<ViewItemMvpView> {

    private final DataManager mDataManager;
    private Disposable mDisposable;
    private ViewItemActivity activity;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Inject
    public ViewItemPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(ViewItemMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        if (mDisposable != null) mDisposable.dispose();
    }

    public void setActivity(ViewItemActivity activity) {
        this.activity = activity;
    }

    public void openDialogWindow() {
        EasyImage.openGallery(activity, SELECT_PICTURE);
    }

    public void getPhoto() {
        EasyImage.openCamera(activity, REQUEST_IMAGE_CAPTURE);
    }

    public void openDialogWindowBanner() {
        EasyImage.openGallery(activity, SELECT_PICTURE_BANNER);
    }

    public void getPhotoBanner() {
        EasyImage.openCamera(activity, REQUEST_IMAGE_CAPTURE_BANNER);
    }

    public void activityResult(int requestCode, int resultCode, Intent data) {
        EasyImage.handleActivityResult(requestCode, resultCode, data, activity, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
                e.printStackTrace();
            }

            @Override
            public void onImagesPicked(@android.support.annotation.NonNull List<File> imageFiles, EasyImage.ImageSource source, int type) {
                Uri imageUri = Uri.fromFile(imageFiles.get(0));
                getMvpView().callGlide(imageUri);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
            }
        });

    }

    public void requestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your camera");
                    Uri imageUri = activity.getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    getMvpView().dismissDialog();
                } else {
                    getMvpView().showSnackBar("É necessário permitir o acesso à camera");
                }
            }
        }
    }

    public void checkIfExists(UserList list) {
        // Add a new document with a generated ID
        if (list.uid == null) {
            saveList(list);
        }
        else {

            db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("createdLists").document(list.uid).get().addOnCompleteListener(task -> {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            Log.e("listExists: ", "true");
                            saveItens(list, list.uid);

                        }else{
                            Log.e("listExists: ","false");
                            saveList(list);
                        }
                    });


        }
    }

    public void saveList(UserList list) {
        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("createdLists").add(list)
                .addOnSuccessListener(documentReference -> {
                    Log.d("updateProfile", "DocumentSnapshot successfully written!");

                    saveItens(list, documentReference.getId());
                })
                .addOnFailureListener(e ->
                        Log.w("updateProfile", "Error writing document", e));
    }

    public void saveItens(UserList list, String listUid) {

        for (ListItem item : list.listItems) {
            db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("createdLists").document(listUid).collection("listItems").add(item)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("updateProfile", "DocumentSnapshot successfully written!");
                        Intent intent = new Intent(getMvpView().getActivityFromView(), ViewListActivity.class);
                        intent.putExtra("list", list);
                        getMvpView().getActivityFromView().startActivity(intent);
                    })
                    .addOnFailureListener(e ->
                            Log.w("updateProfile", "Error writing document", e));
        }
    }

}