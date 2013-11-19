package com.ashman.fivehundredpx;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ashman.fivehundredpx.net.json.replies.Photo;
import com.ashman.fivehundredpx.net.json.replies.PhotoReply;
import com.ashman.fivehundredpx.net.json.replies.StatusReply;
import com.ashman.fivehundredpx.net.json.requests.MakeFavoriteRequest;
import com.ashman.fivehundredpx.net.json.requests.PhotoRequest;
import com.ashman.fivehundredpx.util.Util;

import java.util.Timer;
import java.util.TimerTask;


public class PictureViewActivity extends BaseFragmentActivity {
    private static final TimeInterpolator sDecelerator = new DecelerateInterpolator();
    private static final TimeInterpolator sAccelerator = new AccelerateInterpolator();
    private static final int ANIM_DURATION = 500;
    private static final int INVALID_NUM_FAVORITES = -1;

    private ImageView imageView;
    private TextView descriptionText;
    private RelativeLayout topLevelLayout;
    private View buttonsLayout;
    private View favoriteButton;
    private ImageView favoriteIcon;
    private View favoriteLayout;
    private TextView favoriteCount;
    private ProgressBar favoriteLoading;
    private View shareButton;
    private View descriptionLayout;

    private BitmapDrawable bitmapDrawable;
    private ColorDrawable background;
    private int leftDelta;
    private int topDelta;
    private float scaleWidth;
    private float scaleHeight;
    private int originalOrientation;

    private String bitmapFilename;
    private Photo photo;
    private boolean favorited;
    private int numFavorites = INVALID_NUM_FAVORITES;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_view);
        imageView = (ImageView) findViewById(R.id.picView_picture);
        topLevelLayout = (RelativeLayout) findViewById(R.id.picView_layout);
        descriptionText = (TextView) findViewById(R.id.picView_comment);
        descriptionLayout = findViewById(R.id.picView_commentLayout);
        buttonsLayout = findViewById(R.id.picView_buttonsLayout);
        favoriteButton = findViewById(R.id.picView_favoriteLayout);
        shareButton = findViewById(R.id.picView_shareLayout);
        favoriteIcon = (ImageView) findViewById(R.id.picView_star);

        favoriteLayout = findViewById(R.id.picView_favoriteLayout);
        favoriteCount = (TextView) findViewById(R.id.picView_favCount);
        favoriteLoading = (ProgressBar) findViewById(R.id.picView_favoriteLoading);
        Button viewFullSizeButton = (Button) findViewById(R.id.picView_viewFullSize);

        final String packageName = FiveHundredApplication.get().getPackageName();

        // Retrieve the data we need for the picture/description to display and
        // the thumbnail to animate it from
        Bundle bundle = getIntent().getExtras();
        bitmapFilename = bundle.getString(packageName + ".bitmapFilename");
        String bitmapConfig = bundle.getString(packageName + ".bitmapConfig");
        photo = bundle.getParcelable(packageName + ".photoData");
        final int thumbnailTop = bundle.getInt(packageName + ".top");
        final int thumbnailLeft = bundle.getInt(packageName + ".left");
        final int thumbnailWidth = bundle.getInt(packageName + ".width");
        final int thumbnailHeight = bundle.getInt(packageName + ".height");
        final int bitmapWidth = bundle.getInt(packageName + ".bitmapWidth");
        final int bitmapHeight = bundle.getInt(packageName + ".bitmapHeight");
        originalOrientation = bundle.getInt(packageName + ".orientation");

        //Need bitmap before first draw, so no point loading bitmap in worker thread
        Bitmap imageBitmap = Util.readBitmapFromLocalFile(
                this.getBaseContext().getFilesDir().getAbsolutePath(),
                bitmapWidth, bitmapHeight, bitmapConfig, bitmapFilename);
        bitmapDrawable = new BitmapDrawable(this.getResources(), imageBitmap);
        imageView.setImageDrawable(bitmapDrawable);
        String description = photo.getDescription();
        if(description == null || description.equals("")) {
            descriptionText.setText(getString(R.string.picview_noDescription));
            descriptionText.setGravity(Gravity.CENTER_HORIZONTAL);
        } else {
            //Some descriptions have html links, may have basic html formatting
            descriptionText.setText(Html.fromHtml(photo.getDescription()));
            descriptionText.setMovementMethod(LinkMovementMethod.getInstance());
        }

        background = new ColorDrawable(Color.BLACK);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            topLevelLayout.setBackground(background);
        } else {
            topLevelLayout.setBackgroundDrawable(background);
        }

        viewFullSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PictureViewActivity.this,
                        FullPictureViewActivity.class);
                intent.putExtra(packageName + FullPictureViewActivity.BUNDLE_PHOTO, photo);
                startActivity(intent);
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String largestUrl = photo.getLargestSizeUrl();
                if(largestUrl == null) {
                    Toast.makeText(PictureViewActivity.this, R.string.picview_errorSharing,
                            Toast.LENGTH_SHORT).show();
                } else {
                    FragmentTransaction transaction =
                            getSupportFragmentManager().beginTransaction();
                    Fragment oldFragment = getSupportFragmentManager().findFragmentByTag(
                            "shareDialog");
                    if(oldFragment != null)
                        transaction.remove(oldFragment);
                    ShareDialogFragment dialog = ShareDialogFragment.newInstance(largestUrl);
                    dialog.show(transaction, "shareDialog");
                }
            }
        });

        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)
        if (savedInstanceState == null) {
            ViewTreeObserver observer = imageView.getViewTreeObserver();
            if(observer != null) {
                observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        imageView.getViewTreeObserver().removeOnPreDrawListener(this);

                        // Figure out where the thumbnail and full size versions are, relative
                        // to the screen and each other
                        int[] screenLocation = new int[2];
                        imageView.getLocationOnScreen(screenLocation);
                        leftDelta = thumbnailLeft - screenLocation[0];
                        topDelta = thumbnailTop - screenLocation[1];

                        // Scale factors to make the large version the same size as the thumbnail
                        scaleWidth = (float) thumbnailWidth / imageView.getWidth();
                        scaleHeight = (float) thumbnailHeight / imageView.getHeight();

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
                            runEnterAnimation();

                        return true;
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Start request which will get whether the image has been favorited or not
        getFavoriteRequest();
    }

    @SuppressLint("NewApi")
    public void showFavorite() {
        if(numFavorites == INVALID_NUM_FAVORITES)
            favoriteCount.setText(getString(R.string.picview_unknownFavCount));
        else
            favoriteCount.setText(numFavorites + "");
        if(favorited) {
            favoriteIcon.setImageResource(R.drawable.icon_star);
            favoriteLayout.setClickable(false);
        } else {
            favoriteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(FiveHundredApplication.get().isLoggedIn()) {
                        makeFavoriteRequest();
                    } else {
                        FragmentTransaction transaction =
                                getSupportFragmentManager().beginTransaction();
                        Fragment oldFragment = getSupportFragmentManager().findFragmentByTag(
                                "needLogin");
                        if(oldFragment != null)
                            transaction.remove(oldFragment);
                        NeedLoginDialogFragment dialog = NeedLoginDialogFragment.newInstance(
                                new NeedLoginDialogFragment.LoginPressedListener() {
                            @Override
                            public void onLoginPressed() {
                                finish();
                            }
                        }
                        );
                        dialog.show(transaction, "needLogin");
                    }
                }
            });
        }
        favoriteLayout.setVisibility(View.VISIBLE);
        favoriteLoading.setVisibility(View.GONE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            favoriteLayout.setAlpha(0);
            favoriteLayout.animate().alpha(1).setDuration(ANIM_DURATION / 2).
                    setInterpolator(sDecelerator);
        }
    }

    public void showFavoriteLoading() {
        favoriteLayout.setVisibility(View.GONE);
        favoriteLoading.setVisibility(View.VISIBLE);
    }

    private void getFavoriteRequest() {
        PhotoRequest request = new PhotoRequest(photo.getId());

        Response.Listener<PhotoReply> listener = new Response.Listener<PhotoReply>() {
            @Override
            public void onResponse(PhotoReply response) {
                Photo photo = response.getPhoto();
                numFavorites = photo.getFavoritesCount();
                favorited = photo.isFavorited();
                showFavorite();
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showFavorite();
            }
        };

        getPxApi().get(getRequestQueue(), PhotoReply.class, request, listener, errorListener);
    }

    public void makeFavoriteRequest() {
        MakeFavoriteRequest request = new MakeFavoriteRequest(photo.getId());

        Response.Listener<StatusReply> listener = new Response.Listener<StatusReply>() {
            @Override
            public void onResponse(StatusReply response) {
                if(response.getStatus() == 200 && response.getError().equals("None")) {
                    numFavorites++;
                    favorited = true;
                    showFavorite();
                    favoriteLayout.setClickable(false);
                    if(isActivityActive())
                        Toast.makeText(PictureViewActivity.this,
                                R.string.picview_favorited, Toast.LENGTH_SHORT).show();
                } else {
                    if(isActivityActive())
                        Toast.makeText(PictureViewActivity.this,
                                R.string.picview_errorFavoriting, Toast.LENGTH_SHORT).show();
                    showFavorite();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(isActivityActive())
                    Toast.makeText(PictureViewActivity.this,
                            R.string.picview_errorFavoriting, Toast.LENGTH_SHORT).show();
                showFavorite();
            }
        };

        showFavoriteLoading();
        getPxApi().post(getRequestQueue(), StatusReply.class, request, listener, errorListener);
    }

    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location, colorizing it in parallel. In parallel, the background of the
     * activity is fading in. When the pictue is in place, the text description
     * drops down.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void runEnterAnimation() {
        final int duration = ANIM_DURATION;

        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        imageView.setPivotX(0);
        imageView.setPivotY(0);
        imageView.setScaleX(scaleWidth);
        imageView.setScaleY(scaleHeight);
        imageView.setTranslationX(leftDelta);
        imageView.setTranslationY(topDelta);

        // We'll fade the text in later
        descriptionLayout.setAlpha(0);
        buttonsLayout.setAlpha(0);

        // Animate scale and translation to go from thumbnail to full size
        ViewPropertyAnimator imageAnimator = imageView.animate();
        imageAnimator.setDuration(duration).
                scaleX(1).scaleY(1).
                translationX(0).translationY(0).
                setInterpolator(sDecelerator);

        Runnable runnable = new Runnable() {
            public void run() {
                // Animate the description in after the image animation
                // is done. Slide and fade the text in from underneath
                // the picture.
                descriptionLayout.setTranslationY(-descriptionLayout.getHeight());
                descriptionLayout.animate().setDuration(duration/2).
                        translationY(0).alpha(1).
                        setInterpolator(sDecelerator);
                buttonsLayout.animate().setDuration(duration/2).
                        alpha(1).setInterpolator(sDecelerator);
            }
        };

        scheduleEndTask(imageAnimator, runnable, duration);

        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(background, "alpha", 0, 255);
        bgAnim.setDuration(duration);
        bgAnim.start();
    }

    /**
     * The exit animation is basically a reverse of the enter animation, except that if
     * the orientation has changed we simply scale the picture back into the center of
     * the screen.
     *
     * @param endAction This action gets run after the animation completes (this is
     * when we actually switch activities)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void runExitAnimation(final Runnable endAction) {
        final int duration = ANIM_DURATION;

        // No need to set initial values for the reverse animation; the image is at the
        // starting size/location that we want to start from. Just animate to the
        // thumbnail size/location that we retrieved earlier

        // Caveat: configuration change invalidates thumbnail positions; just animate
        // the scale around the center. Also, fade it out since it won't match up with
        // whatever's actually in the center
        final boolean fadeOut;
        if (getResources().getConfiguration().orientation != originalOrientation) {
            imageView.setPivotX(imageView.getWidth() / 2);
            imageView.setPivotY(imageView.getHeight() / 2);
            leftDelta = 0;
            topDelta = 0;
            fadeOut = true;
        } else {
            fadeOut = false;
        }

        // First, slide/fade text out of the way
        ViewPropertyAnimator textAnimator = descriptionLayout.animate();
        textAnimator.translationY(-descriptionLayout.getHeight()).alpha(0).
                setDuration(duration/2).setInterpolator(sAccelerator);
        buttonsLayout.animate().alpha(0).setDuration(duration/2).
                setInterpolator(sAccelerator);

        Runnable runnable = new Runnable() {
            public void run() {
                // Animate image back to thumbnail size/location
                ViewPropertyAnimator imageAnimator = imageView.animate();
                imageAnimator.setDuration(duration).
                        scaleX(scaleWidth).scaleY(scaleHeight).
                        translationX(leftDelta).translationY(topDelta);

                scheduleEndTask(imageAnimator, endAction, duration);
                if (fadeOut) {
                    imageView.animate().alpha(0);
                }
                // Fade out background
                ObjectAnimator bgAnim = ObjectAnimator.ofInt(background, "alpha", 0);
                bgAnim.setDuration(duration);
                bgAnim.start();
            }
        };

        scheduleEndTask(textAnimator, runnable, duration/2);
    }

    private void scheduleEndTask(ViewPropertyAnimator animator,
                                 final Runnable runnable, int duration) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            animator.withEndAction(runnable);
        } else {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runnable.run();
                }
            }, duration);
        }
    }

    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it is complete.
     */
    @Override
    public void onBackPressed() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
        {
            runExitAnimation(new Runnable() {
                public void run() {
                    // *Now* go ahead and exit the activity
                    finish();
                }
            });
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
        {
            // override transitions to skip the standard window animations
            overridePendingTransition(0, 0);
        }

        Util.getLocalFile(this, bitmapFilename).deleteOnExit();
    }
}
