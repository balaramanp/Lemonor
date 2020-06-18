package in.inferon.msl.cucumbor.view.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import in.inferon.msl.cucumbor.model.Constants;


public class BoldEditText extends androidx.appcompat.widget.AppCompatEditText {
    public BoldEditText(Context context) {
        super(context);
        loadFromAsset();
    }

    public BoldEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadFromAsset();
    }

    public BoldEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadFromAsset();
    }
    private void loadFromAsset() {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), Constants.BOLD_FONT);
        setTypeface(typeface);
    }
}
