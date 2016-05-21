package me.avelar.donee.view.fields;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.avelar.donee.R;
import me.avelar.donee.model.Field;
import me.avelar.donee.model.ValidationRule;
import me.avelar.donee.view.ActivityCollect;

@SuppressLint("ViewConstructor")
public class ImageGroup extends FieldGroup implements View.OnClickListener {

    private static final int MAX_IMAGE_DIMENSION = 800;
    private static final int THUMBNAIL_DIMENSION = 100;

    private Bitmap mImage;

    private View      mLlValue;
    private View      mRlThumbnail;
    private ImageView mIvThumbnail;

    public ImageGroup(Context context, Field field) {
        super(context, field);
        mContext = context;
    }

    @Override
    protected void inflateLayout(Context context) {
        View v = getInflater(context).inflate(R.layout.field_image_group, this, true);
        // getting view references
        mTvLabel     = (TextView)  v.findViewById(R.id.image_field_label);
        mIvThumbnail = (ImageView) v.findViewById(R.id.image_thumbnail_view);
        mRlThumbnail = v.findViewById(R.id.image_thumbnail);
        mLlValue     = v.findViewById(R.id.image_field_actions);
        v.findViewById(R.id.image_action_camera).setOnClickListener(this);
        v.findViewById(R.id.image_action_gallery).setOnClickListener(this);
        mRlThumbnail.setOnClickListener(this);
    }

    @Override
    protected void setHint(String hint) {
        // not applicable to ImageGroup
    }

    @Override
    protected void setStarting(String[] value) {
        // not applicable to ImageGroup
    }

    @Override
    protected void setOptions(String[] value) {
        // not applicable to ImageGroup
    }

    @Override
    protected void setHeight(Context context, Integer heightInDp) {
        // not applicable to ImageGroup
    }

    @Override
    protected void updateValue() {
        String base64 = getCollection().getValue(getField());
        if (base64 != null) {
            byte[] decodedByte = Base64.decode(base64, 0);
            Bitmap bm = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            if (bm != null) setImage(bm);
        }
    }

    @Override
    protected void setMultiline(boolean multiline) {
        // not applicable to ImageGroup
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean validate() {
        ValidationRule rule = getField().getRule();
        if (rule != null && rule.isRequired()) {
            return mImage != null;
        }
        return true;
    }

    @Override
    public void showError(boolean show) {
        int res = show ? R.drawable.image_bg_error : R.drawable.image_bg_default;
        mLlValue.setBackgroundResource(res);
    }

    @Override
    public void commit() {
        // do nothing, commit happens when photo is taken
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_action_camera:
                if (mContext instanceof ActivityCollect) {
                    ((ActivityCollect)mContext).snapPhoto(this);
                }
                break;
            case R.id.image_action_gallery:
                if (mContext instanceof ActivityCollect) {
                    ((ActivityCollect)mContext).pickPhoto(this);
                }
                break;
            case R.id.image_thumbnail:
                setImage((Bitmap) null);
                break;
        }
    }

    public void setImage(Bitmap image) {
        if (image != null) {
            mRlThumbnail.setVisibility(View.VISIBLE);
            getCollection().addValue(getField(), image);
            mImage = getResizedBitmap(image);
            mIvThumbnail.setImageBitmap(getThumbnail(mImage));
        } else {
            mRlThumbnail.setVisibility(View.GONE);
            mImage.recycle();
            mImage = null;
        }
    }

    public void setImage(String imagePath) {
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        if (bm != null) setImage(bm);
    }

    private Bitmap getResizedBitmap(Bitmap image) {
        return resizePhoto(image, MAX_IMAGE_DIMENSION);
    }

    private Bitmap getThumbnail(Bitmap image) {
        return resizePhoto(image, THUMBNAIL_DIMENSION);
    }

    private Bitmap resizePhoto(Bitmap image, int dimensions) {
        int w = image.getWidth(), h = image.getHeight();

        if (w == h && w > dimensions) return getResizedBitmap(image, dimensions, dimensions);
        if (w >  h && w > dimensions) return getResizedBitmap(image, dimensions, h * dimensions / w);
        if (h >  w && h > dimensions) return getResizedBitmap(image, w * dimensions / h, dimensions);

        return image;
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int w = bm.getWidth(), h = bm.getHeight();

        float scaleWidth  = ((float) newWidth ) / w;
        float scaleHeight = ((float) newHeight) / h;

        // create a matrix for manipulation
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, w, h, matrix, false);
        bm.recycle();

        return resizedBitmap;
    }

}