package in.inferon.msl.cucumbor.view.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import in.inferon.msl.cucumbor.model.Constants;


public class LightAutoCompleteTextView extends androidx.appcompat.widget.AppCompatAutoCompleteTextView {
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
