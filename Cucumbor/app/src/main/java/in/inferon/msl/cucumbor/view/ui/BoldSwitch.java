package in.inferon.msl.cucumbor.view.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Switch;
import in.inferon.msl.cucumbor.model.Constants;


public class BoldSwitch extends Switch {
    public BoldSwitch(Context context) {
        super(context);
        loadFromAsset();
    }

    public BoldSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadFromAsset();
    }

    public BoldSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadFromAsset();
    }
    private void loadFromAsset() {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), Constants.BOLD_FONT);
        setTypeface(typeface);
    }
}
