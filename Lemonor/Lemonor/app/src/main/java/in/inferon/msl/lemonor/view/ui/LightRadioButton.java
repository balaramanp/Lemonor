package in.inferon.msl.lemonor.view.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RadioButton;
import in.inferon.msl.lemonor.model.Constants;


public class LightRadioButton extends RadioButton {
    public LightRadioButton(Context context) {
        super(context);
        loadFromAsset();
    }

    public LightRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadFromAsset();
    }

    public LightRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadFromAsset();
    }
    private void loadFromAsset() {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), Constants.LIGHT_FONT);
        setTypeface(typeface);
    }
}
