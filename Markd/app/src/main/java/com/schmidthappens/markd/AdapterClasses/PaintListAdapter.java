package com.schmidthappens.markd.AdapterClasses;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.schmidthappens.markd.R;
import com.schmidthappens.markd.data_objects.PaintSurface;
import com.schmidthappens.markd.data_objects.TempCustomerData;
import com.schmidthappens.markd.customer_menu_activities.PaintingActivity;
import com.schmidthappens.markd.customer_subactivities.PaintEditActivity;

import java.util.List;

/**
 * Created by Josh on 5/29/2017.
 */

//TODO abstract duplicate code in PanelListAdapter
// (May need to make LinearLayout and TableRow the same)
public class PaintListAdapter {

    private float dX, historicX, newX;
    private float originalX = Float.NaN;
    private final float DELTA = (float)-50.0;
    private final float LAG = (float)15.0;
    private final int DURATION = 400;

    private PaintingActivity activityContext = null;
    private static final String TAG = "PaintListAdapter";

    public View createPaintListView(final Context context, final List<PaintSurface> paintSurfaces, boolean isExterior) {
        final boolean isExteriorFinal = isExterior;
        if(context instanceof PaintingActivity) {
            activityContext = (PaintingActivity)context;
        } else {
            Log.e(TAG, "Activity Context not Painting Activity");
        }

        LinearLayout listOfPaints = new LinearLayout(context);
        listOfPaints.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        //From: https://stackoverflow.com/questions/38527508/androidattr-programmatically
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.dividerVertical, typedValue, true);
        listOfPaints.setDividerDrawable(ContextCompat.getDrawable(context,typedValue.resourceId));
        listOfPaints.setOrientation(LinearLayout.VERTICAL);

        LayoutInflater viewInflater = LayoutInflater.from(activityContext);

        if(paintSurfaces.size() == 0) {
            View v = viewInflater.inflate(R.layout.list_row_paint, null);
            TextView paintLocationTextView = (TextView) v.findViewById(R.id.paint_location);
            paintLocationTextView.setText("Add some paint!");
            listOfPaints.addView(v);
        }

        for(final PaintSurface paintSurface : paintSurfaces) {
            final int position = paintSurfaces.indexOf(paintSurface);
            View paintListItemView = viewInflater.inflate(R.layout.list_row_paint, null);

            if(paintSurface != null) {
                insertPaintInfo(paintListItemView, paintSurface);
            }

            paintListItemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    LinearLayout rowToMove = (LinearLayout)v.findViewById(R.id.paint_list_row_information);
                    ImageButton deleteButton = (ImageButton)v.findViewById(R.id.paint_list_row_delete_button);


                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initializeXValues(rowToMove, event);
                            return true;

                        case MotionEvent.ACTION_UP: case MotionEvent.ACTION_CANCEL:
                            //Swiped far enough to left
                            if(newX-historicX < DELTA) {
                                Log.i(TAG, "Swipe Initiated " + position);
                                snapRowToSlideOutPosition(rowToMove, v.getHeight()+30);
                                deleteButton.setClickable(true);
                                return true;
                            } else {
                                Log.d(TAG, "Diff: " + Math.abs(rowToMove.getX()-originalX));
                                //Needs to be moved back to original Position
                                if(Math.abs(rowToMove.getX()-originalX) > LAG) {
                                    Log.i(TAG, "Shift back " + position);
                                    snapRowToOriginalPosition(rowToMove);
                                    deleteButton.setClickable(false);
                                }
                                //Clicked
                                else {
                                    Log.i(TAG, "Click Panel " + position);
                                    viewClickedPaint(position, isExteriorFinal);
                                }
                            }
                            break;

                        case MotionEvent.ACTION_MOVE:
                            moveRowWithDrag(rowToMove, event);
                            return true;

                        default:
                            return false;
                    }
                    return false;
                }
            });

            ImageButton paintSurfaceDeleteButton = (ImageButton)paintListItemView.findViewById(R.id.paint_list_row_delete_button);
            paintSurfaceDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO change to http call to delete PaintSurface
                    return; /*
                    if(activityContext != null) {
                        Log.i(TAG, "Delete Paint Item " + position);
                        activityContext.deletePaintSurface(position, isExteriorFinal);
                    } else {
                        Log.e(TAG, "Activity Context NULL");
                    }*/
                }
            });
            paintSurfaceDeleteButton.setClickable(false);

            listOfPaints.addView(paintListItemView);
        }

        return listOfPaints;
    }


    private void insertPaintInfo(View view, PaintSurface paintSurface) {
        TextView paintLocationView = (TextView) view.findViewById(R.id.paint_location);
        TextView paintBrandView = (TextView) view.findViewById(R.id.paint_brand);
        TextView paintColorView = (TextView) view.findViewById(R.id.paint_color);
        TextView paintDate = (TextView)view.findViewById(R.id.paint_date);

        if (paintLocationView != null) {
            paintLocationView.setText(paintSurface.getLocation());
        }

        if (paintBrandView != null) {
            paintBrandView.setText(paintSurface.getBrand());
        }

        if (paintColorView != null) {
            paintColorView.setText(paintSurface.getColor());
        }

        if(paintDate != null) {
            String paintDateString = paintSurface.getDateString();
            if(paintDateString != null) {
                paintDate.setText(paintDateString);
            } else {
                Log.e(TAG, "Paint Date String was null");
            }
        }
    }

    private void viewClickedPaint(int paintObjectClicked, boolean isExterior) {
        Class destinationClass = PaintEditActivity.class;
        PaintSurface isClicked;

        if(isExterior) {
            isClicked = TempCustomerData.getInstance().getExteriorSurfaces().get(paintObjectClicked);
        } else {
            isClicked = TempCustomerData.getInstance().getInteriorSurfaces().get(paintObjectClicked);
        }

        if(activityContext != null) {
            Intent intentToStartPaintEditActivity = new Intent(activityContext, destinationClass);
            putPaintObjectInIntent(isClicked, intentToStartPaintEditActivity, paintObjectClicked);
            if(isExterior)
                intentToStartPaintEditActivity.putExtra("isExterior", true);
            activityContext.startActivity(intentToStartPaintEditActivity);
        } else {
            Log.e(TAG, "Activity Context is NULL");
        }
    }

    private void putPaintObjectInIntent(PaintSurface paintSurface, Intent intent, int position) {
        intent.putExtra("id", position);
        intent.putExtra("location", paintSurface.getLocation());
        intent.putExtra("paintDate", paintSurface.getDateString());
        intent.putExtra("brand", paintSurface.getBrand());
        intent.putExtra("color", paintSurface.getColor());
    }


    private void initializeXValues(LinearLayout row, MotionEvent event){
        historicX = event.getRawX();
        dX = row.getX() - event.getRawX();
        if(Float.isNaN(originalX)) {
            Log.i(TAG, "Changed OriginalX");
            originalX = historicX + dX;
        }
    }

    private void snapRowToSlideOutPosition(LinearLayout row, float amountToLeft) {
        row.animate().x(originalX-amountToLeft).setDuration(DURATION).start();
    }

    private void snapRowToOriginalPosition(LinearLayout row) {
        row.animate().x(originalX).setDuration(DURATION).start();
    }

    private void moveRowWithDrag(LinearLayout row, MotionEvent event) {
        row.animate().x(event.getRawX() + dX).setDuration(0).start();
        newX = event.getRawX();
    }
}
