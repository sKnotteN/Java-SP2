package knotten.kristian.computercontroll;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.view.MotionEvent;
import android.widget.ImageView;


public class onTouch extends AppCompatImageView {

    ControlPC controlPC;

    public onTouch(Context context) {
        super(context);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controlPC = new ControlPC();
//        controlPC.moveMouse(event);
        return super.onTouchEvent(event);
    }
}
