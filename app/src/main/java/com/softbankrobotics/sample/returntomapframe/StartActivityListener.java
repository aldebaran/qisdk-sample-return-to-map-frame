package com.softbankrobotics.sample.returntomapframe;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class StartActivityListener extends StartActivity implements View.OnClickListener {
    private Context context;

    public StartActivityListener(Class activity) {
        super(activity, new Bundle());
    }

    public StartActivityListener(Class activity, Bundle bundle) {
        super(activity, bundle);
    }

    public StartActivityListener(Class activity, Context context, Bundle bundle) {
        super(activity, bundle);
        this.context = context;
    }

    @Override
    public void onClick(final View view) {
        view.getContext().startActivity(this.getIntent(view.getContext()));
    }
}