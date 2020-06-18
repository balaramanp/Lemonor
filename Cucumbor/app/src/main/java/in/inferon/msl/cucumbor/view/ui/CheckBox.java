package in.inferon.msl.cucumbor.view.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import in.inferon.msl.cucumbor.model.Constants;


@SuppressLint("AppCompatCustomView")
public class CheckBox extends android.widget.CheckBox {
    public CheckBox(Context context) {
        super(context);
        loadFromAsset();
    }

    public CheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadFromAsset();
    }

    public CheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadFromAsset();
    }
    private void loadFromAsset() {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), Constants.BOLD_FONT);
        setTypeface(typeface);
    }
}
