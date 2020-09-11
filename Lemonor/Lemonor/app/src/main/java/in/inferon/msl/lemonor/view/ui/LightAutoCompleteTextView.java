package in.inferon.msl.lemonor.view.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;
import in.inferon.msl.lemonor.model.Constants;


public class LightAutoCompleteTextView extends AutoCompleteTextView {
    public LightAutoCompleteTextView(Context context) {
        super(context);
        loadFromAsset();
    }

    public LightAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadFromAsset();
    }

    public LightAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadFromAsset();
    }
    private void loadFromAsset() {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), Constants.LIGHT_FONT);
        setTypeface(typeface);
    }
}
