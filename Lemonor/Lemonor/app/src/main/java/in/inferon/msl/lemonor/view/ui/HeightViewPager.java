package in.inferon.msl.lemonor.view.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class HeightViewPager extends ViewPager {
    public HeightViewPager(@NonNull Context context) {
        super(context);
    }

    public HeightViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)   {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View firstChild = getChildAt(0);
        firstChild.measure(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(firstChild.getMeasuredHeight(), MeasureSpec.EXACTLY));
    }
}
