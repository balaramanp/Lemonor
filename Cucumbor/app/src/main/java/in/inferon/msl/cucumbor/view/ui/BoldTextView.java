package in.inferon.msl.cucumbor.view.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import in.inferon.msl.cucumbor.model.Constants;


public class BoldTextView extends androidx.appcompat.widget.AppCompatTextView {
    public BoldTextView(Context context) {
        super(context);
        loadFromAsset();
    }

    public BoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadFromAsset();
    }

    public BoldTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadFromAsset();
    }
    private void loadFromAsset() {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), Constants.BOLD_FONT);
        setTypeface(typeface);
    }
}
