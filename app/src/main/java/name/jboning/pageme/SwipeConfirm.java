package name.jboning.pageme;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatSeekBar;

// flagrant abuse of a SeekBar as a slide-to-confirm widget.
public class SwipeConfirm extends AppCompatSeekBar implements SeekBar.OnSeekBarChangeListener {
    public interface OnConfirmedListener {
        public void onConfirmed(SwipeConfirm swipeConfirm);
    }

    private int lastProgress = 0;
    private boolean confirmed = false;
    private OnConfirmedListener onConfirmedListener;

    public SwipeConfirm(Context c) {
        super(c);
        init();
    }

    public SwipeConfirm(Context c, AttributeSet as) {
        super(c, as);
        init();
    }

    public SwipeConfirm(Context c, AttributeSet as, int i) {
        super(c, as, i);
        init();
    }

    private void init() {
        super.setOnSeekBarChangeListener(this);
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        throw new RuntimeException();
    }

    public void setOnConfirmedListener(OnConfirmedListener l) {
        onConfirmedListener = l;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (progress > 25 && lastProgress == 0) {
                setProgress(0);
            }
            lastProgress = getProgress();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        seekBar.setProgress(0);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar.getProgress() == 100 && lastProgress != 0) {
            setConfirmed(true);
            onConfirmedListener.onConfirmed(this);
        } else {
            seekBar.setProgress(0);
        }
    }

    public void setConfirmed(boolean c) {
        confirmed = c;
        if (confirmed) {
            lastProgress = 100;
        } else {
            lastProgress = 0;
        }
        setProgress(lastProgress);
        setEnabled(!confirmed);
    }

}
