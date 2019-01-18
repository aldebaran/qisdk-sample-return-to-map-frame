/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.menu;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RadioButton;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.BookmarkStatus;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.sample.returntomapframe.GotoAB.GotoPointActivity;
import com.softbankrobotics.sample.returntomapframe.GotoAB.GotoWorldActivity;
import com.softbankrobotics.sample.returntomapframe.R;
import com.softbankrobotics.sample.returntomapframe.StartActivityListener;
import com.softbankrobotics.sample.returntomapframe.core.MapManager;
import com.softbankrobotics.sample.returntomapframe.localization.LocalizationActivity;
import com.softbankrobotics.sample.returntomapframe.mapping.MappingActivity;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * The menu Activity.
 */
public class MenuActivity extends RobotActivity implements RobotLifecycleCallbacks {

    @NonNull
    private static final String TAG = "MenuActivity";

    @NonNull
    private static final String START_BOOKMARK_NAME = "start";
    @NonNull
    private static final String CREATE_BOOKMARK_NAME = "create";
    @NonNull
    private static final String CREATE_END_BOOKMARK_NAME = "create_end";
    @NonNull
    private static final String USE_BOOKMARK_NAME = "use";
    @NonNull
    private static final String USE_END_BOOKMARK_NAME = "use_end";
    @NonNull
    private static final String MAP_BOOKMARK_NAME = "map";
    @NonNull
    private static final String START_TIMER_BOOKMARK_NAME = "start_timer";
    @NonNull
    private static final String STOP_TIMER_BOOKMARK_NAME = "stop_timer";

    @BindView(R.id.createMapButton)
    RadioButton createMapButton;

    @BindView(R.id.useMapButton)
    RadioButton useMapButton;

    @Nullable
    private Map<String, Bookmark> bookmarks;
    @Nullable
    private QiChatbot qiChatbot;
    @Nullable
    private Chat chat;

    @Nullable
    private Disposable timerDisposable;
    @NonNull
    private final AtomicBoolean shouldRepeatWithTimer = new AtomicBoolean(true);

    RadioButton gotbutton;

    RadioButton gotoWorld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
        this.gotbutton = findViewById((R.id.gotoButton));
        this.gotbutton.setOnClickListener(new StartActivityListener(GotoPointActivity.class));

        this.gotoWorld = findViewById(R.id.goToWorld);
        this.gotoWorld.setOnClickListener(new StartActivityListener(GotoWorldActivity.class));

        QiSDK.register(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        createMapButton.setChecked(false);
        useMapButton.setChecked(false);

        createMapButton.setEnabled(true);
        useMapButton.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        shouldRepeatWithTimer.set(true);

        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.menu)
                .build();

        bookmarks = topic.getBookmarks();

        qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        if (qiChatbot != null) {
            QiChatVariable proposalVariable = qiChatbot.variable("proposal");

            // Change the speech and enable/disable the "use map" option, depending on the map existence.
            if (MapManager.getInstance().hasMap(getApplicationContext())) {
                proposalVariable.setValue(getString(R.string.menu_sentence_with_map));
                runOnUiThread(() -> useMapButton.setEnabled(true));
            } else if (bookmarks != null) {
                proposalVariable.setValue(getString(R.string.menu_sentence_no_map));
                BookmarkStatus bookmarkStatus = qiChatbot.bookmarkStatus(bookmarks.get(MAP_BOOKMARK_NAME));
                bookmarkStatus.setEnabled(false);
            }

            qiChatbot.addOnBookmarkReachedListener(bookmark -> {
                switch (bookmark.getName()) {
                    case CREATE_BOOKMARK_NAME:
                        runOnUiThread(() -> createMapButton.setChecked(true));
                        disableButtons();
                        break;
                    case MAP_BOOKMARK_NAME:
                        runOnUiThread(() -> useMapButton.setChecked(true));
                        disableButtons();
                        break;
                    case CREATE_END_BOOKMARK_NAME:
                        startMappingActivity();
                        break;
                    case USE_END_BOOKMARK_NAME:
                        startLocalizationActivity();
                        break;
                    case START_TIMER_BOOKMARK_NAME:
                        if (shouldRepeatWithTimer.getAndSet(false)) {
                            startTimer();
                        }
                        break;
                    case STOP_TIMER_BOOKMARK_NAME:
                        stopTimer();
                        break;
                }
            });
        }

        chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        chat.addOnStartedListener(() -> goToBookmark(START_BOOKMARK_NAME, AutonomousReactionImportance.LOW));

        chat.async().run();
    }

    @Override
    public void onRobotFocusLost() {
        stopTimer();

        bookmarks = null;
        if (qiChatbot != null) {
            qiChatbot.removeAllOnBookmarkReachedListeners();
            qiChatbot = null;
        }
        if (chat != null) {
            chat.removeAllOnStartedListeners();
            chat = null;
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.e(TAG, "onRobotFocusRefused: " + reason);
    }

    @OnClick(R.id.createMapButton)
    public void onClickCreateMap() {
        disableButtons();
        if (!goToBookmark(CREATE_BOOKMARK_NAME, AutonomousReactionImportance.HIGH)) {
            startMappingActivity();
        }
    }

    @OnClick(R.id.useMapButton)
    public void onClickUseMap() {
        disableButtons();
        if (!goToBookmark(USE_BOOKMARK_NAME, AutonomousReactionImportance.HIGH)) {
            startLocalizationActivity();
        }
    }

    @OnClick(R.id.closeButton)
    public void onCloseClicked() {
        finishAffinity();
    }

    private void disableButtons() {
        runOnUiThread(() -> {
            createMapButton.setEnabled(false);
            useMapButton.setEnabled(false);
        });
    }

    private void startLocalizationActivity() {
        startActivity(new Intent(this, LocalizationActivity.class));
    }

    private void startMappingActivity() {
        startActivity(new Intent(this, MappingActivity.class));
    }

    private boolean goToBookmark(@NonNull String name, @NonNull AutonomousReactionImportance importance) {
        Bookmark bookmark;
        if ((bookmarks == null)
                || ((bookmark = bookmarks.get(name)) == null)
                || (qiChatbot == null)) {
            return false;
        }

        qiChatbot.async().goToBookmark(bookmark, importance, AutonomousReactionValidity.IMMEDIATE);
        return true;
    }

    private void startTimer() {
        timerDisposable = Single.timer(5, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(ignored -> {
                    stopTimer();
                    goToBookmark(START_BOOKMARK_NAME, AutonomousReactionImportance.LOW);
                });
    }

    private void stopTimer() {
        if (timerDisposable != null && !timerDisposable.isDisposed()) {
            timerDisposable.dispose();
        }
    }
}
