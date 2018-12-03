/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.returntomapframe.localization.localizationmenu;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.BookmarkStatus;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatVariable;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.softbankrobotics.sample.returntomapframe.R;
import com.softbankrobotics.sample.returntomapframe.localization.LocalizeManager;
import com.softbankrobotics.sample.returntomapframe.localization.Robot;
import com.softbankrobotics.sample.returntomapframe.utils.FutureCancellations;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * The robot for {@link LocalizationMenuScreen}.
 */
class LocalizationMenuRobot implements Robot {

    @NonNull
    private static final String START_BOOKMARK_NAME = "start";
    @NonNull
    private static final String LOCALIZE_BOOKMARK_NAME = "localize";
    @NonNull
    private static final String LOCALIZE_END_BOOKMARK_NAME = "localize_end";
    @NonNull
    private static final String GO_TO_ORIGIN_BOOKMARK_NAME = "go_to_origin";
    @NonNull
    private static final String GO_TO_ORIGIN_END_BOOKMARK_NAME = "go_to_origin_end";
    @NonNull
    private static final String LOCALIZED_BOOKMARK_NAME = "localized";
    @NonNull
    private static final String START_TIMER_BOOKMARK_NAME = "start_timer";
    @NonNull
    private static final String STOP_TIMER_BOOKMARK_NAME = "stop_timer";

    @NonNull
    private final LocalizationMenuScreen screen;
    @NonNull
    private final LocalizeManager localizeManager;

    @Nullable
    private Disposable timerDisposable;
    @NonNull
    private final AtomicBoolean shouldRepeatWithTimer = new AtomicBoolean(true);

    @Nullable
    private Map<String, Bookmark> bookmarks;
    @Nullable
    private QiChatbot qiChatbot;
    @Nullable
    private Chat chat;
    @Nullable
    private Future<Void> discussion;

    LocalizationMenuRobot(@NonNull LocalizationMenuScreen screen, @NonNull LocalizeManager localizeManager) {
        this.screen = screen;
        this.localizeManager = localizeManager;
    }

    @NonNull
    @Override
    public Future<Void> stop() {
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
        return FutureCancellations.cancel(discussion);
    }

    void startDiscussion(@NonNull QiContext qiContext) {
        shouldRepeatWithTimer.set(true);

        Topic topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.localization_menu)
                .build();

        bookmarks = topic.getBookmarks();

        qiChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        if (qiChatbot != null) {
            QiChatVariable proposalVariable = qiChatbot.variable("proposal");

            // Change the speech and enable/disable the "go to origin" option, depending on the localization status.
            if (localizeManager.isLocalized()) {
                proposalVariable.setValue(qiContext.getString(R.string.localization_menu_sentence_localized));
                screen.enableGoToOrigin();
            } else if (bookmarks != null) {
                proposalVariable.setValue(qiContext.getString(R.string.localization_menu_sentence_not_localized));
                BookmarkStatus bookmarkStatus = qiChatbot.bookmarkStatus(bookmarks.get(LOCALIZED_BOOKMARK_NAME));
                bookmarkStatus.setEnabled(false);
            }

            qiChatbot.addOnBookmarkReachedListener(bookmark -> {
                switch (bookmark.getName()) {
                    case LOCALIZE_BOOKMARK_NAME:
                        screen.selectLocalize();
                        screen.disableChoices();
                        break;
                    case LOCALIZED_BOOKMARK_NAME:
                        screen.selectGoToOrigin();
                        screen.disableChoices();
                        break;
                    case LOCALIZE_END_BOOKMARK_NAME:
                        screen.onLocalizeSelected();
                        break;
                    case GO_TO_ORIGIN_END_BOOKMARK_NAME:
                        screen.onGoToOriginSelected();
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

        discussion = chat.async().run();
    }

    void goToLocalizeBookmark() {
        if (!goToBookmark(LOCALIZE_BOOKMARK_NAME, AutonomousReactionImportance.HIGH)) {
            screen.onLocalizeSelected();
        }
    }

    void goToGoToInitialPositionBookmark() {
        if (!goToBookmark(GO_TO_ORIGIN_BOOKMARK_NAME, AutonomousReactionImportance.HIGH)) {
            screen.onGoToOriginSelected();
        }
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
