package in.inferon.msl.lemonor.view.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import in.inferon.msl.lemonor.model.Constants;


public class LightButton extends androidx.appcompat.widget.AppCompatButton {
    public LightButton(Context context) {
        super(context);
        loadFromAsset();
    }

    public LightButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadFromAsset();
    }

    public LightButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadFromAsset();
    }
    private void loadFromAsset() {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), Constants.LIGHT_FONT);
        setTypeface(typeface);
    }
}
