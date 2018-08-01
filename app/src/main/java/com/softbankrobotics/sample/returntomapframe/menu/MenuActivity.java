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
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.sample.returntomapframe.R;
import com.softbankrobotics.sample.returntomapframe.core.MapManager;
import com.softbankrobotics.sample.returntomapframe.localization.LocalizationActivity;
import com.softbankrobotics.sample.returntomapframe.mapping.MappingActivity;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MenuActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MenuActivity";

    private static final String START_BOOKMARK_NAME = "start";
    private static final String CREATE_BOOKMARK_NAME = "create";
    private static final String CREATE_END_BOOKMARK_NAME = "create_end";
    private static final String USE_BOOKMARK_NAME = "use";
    private static final String USE_END_BOOKMARK_NAME = "use_end";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);

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
        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.menu)
                .build();

        bookmarks = topic.getBookmarks();

        qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        if (MapManager.getInstance().hasMap(getApplicationContext())) {
            runOnUiThread(() -> useMapButton.setEnabled(true));
        } else {
            if (qiChatbot != null && bookmarks != null) {
                BookmarkStatus bookmarkStatus = qiChatbot.bookmarkStatus(bookmarks.get(USE_BOOKMARK_NAME));
                bookmarkStatus.setEnabled(false);
            }
        }

        if (qiChatbot != null) {
            qiChatbot.addOnBookmarkReachedListener(bookmark -> {
                switch (bookmark.getName()) {
                    case CREATE_BOOKMARK_NAME:
                        runOnUiThread(() -> createMapButton.setChecked(true));
                        disableButtons();
                        break;
                    case USE_BOOKMARK_NAME:
                        runOnUiThread(() -> useMapButton.setChecked(true));
                        disableButtons();
                        break;
                    case CREATE_END_BOOKMARK_NAME:
                        startMappingActivity();
                        break;
                    case USE_END_BOOKMARK_NAME:
                        startLocalizationActivity();
                        break;
                }
            });
        }

        chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();

        chat.addOnStartedListener(() -> goToBookmark(START_BOOKMARK_NAME));

        chat.async().run();
    }

    @Override
    public void onRobotFocusLost() {
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
        if (!goToBookmark(CREATE_BOOKMARK_NAME)) {
            startMappingActivity();
        }
    }

    @OnClick(R.id.useMapButton)
    public void onClickUseMap() {
        disableButtons();
        if (!goToBookmark(USE_BOOKMARK_NAME)) {
            startLocalizationActivity();
        }
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

    private boolean goToBookmark(@NonNull String name) {
        Bookmark bookmark;
        if ((bookmarks == null)
                || ((bookmark = bookmarks.get(name)) == null)
                || (qiChatbot == null)) {
            return false;
        }

        qiChatbot.async().goToBookmark(bookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
        return true;
    }
}
