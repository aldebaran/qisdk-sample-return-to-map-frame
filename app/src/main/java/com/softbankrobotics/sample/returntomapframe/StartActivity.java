package com.softbankrobotics.sample.returntomapframe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public abstract class StartActivity {
    private Class activity;
    private Bundle bundle;

    public StartActivity(Class activity, Bundle bundle) {
        this.activity = activity;
        this.bundle = bundle;
    }

    protected Intent getIntent(Context context) {
        Intent intent = new Intent(context, this.activity);
        intent.putExtra("bundle", this.bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
