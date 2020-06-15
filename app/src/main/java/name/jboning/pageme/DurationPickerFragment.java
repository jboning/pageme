package name.jboning.pageme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;

public class DurationPickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    public interface OnDurationSetListener {
        void onDurationSet(int minutes);
        void onDoneWithDurationSelection();
    }

    private class DurationOption {
        public final int minutes;
        public final String show;
        DurationOption(int m, String s) {
            minutes = m; show = s;
        }
        @Override
        public String toString() {
            return show;
        }
    }

    private final DurationOption[] DURATIONS = {
            new DurationOption(1, "1 minute"),
            new DurationOption(5, "5 minutes"),
            new DurationOption(10, "10 minutes"),
            new DurationOption(15, "15 minutes"),
            new DurationOption(30, "30 minutes"),
            new DurationOption(45, "45 minutes"),
            new DurationOption(60, "1 hour"),
            new DurationOption(60 * 2, "2 hours"),
            new DurationOption(60 * 4, "4 hours"),
            new DurationOption(60 * 6, "6 hours"),
            new DurationOption(60 * 8, "8 hours"),
            new DurationOption(60 * 10, "10 hours"),
            new DurationOption(60 * 12, "12 hours"),
            new DurationOption(60 * 24, "24 hours")
    };

    private OnDurationSetListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.duration_picker, null);
        final Spinner duration = (Spinner) v.findViewById(R.id.duration);
        ArrayAdapter<DurationOption> durationsAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                DURATIONS
        );
        durationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        duration.setAdapter(durationsAdapter);

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setMessage("Set duration");
        b.setView(v);
        b.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("DurationPickerFragment", "Set");
                if (mListener != null) {
                    int minutes = DURATIONS[duration.getSelectedItemPosition()].minutes;
                    mListener.onDurationSet(minutes);
                }
            }
        });
        b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("DurationPickerFragment", "Cancel");
                getDialog().cancel();
            }
        });
        return b.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnDurationSetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnDurationSetListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onDoneWithDurationSelection();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.d("DurationPickerFragment", "Time set");
    }
}
