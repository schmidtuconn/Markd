package com.schmidthappens.markd.data_objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Josh on 3/19/2017.
 */

public class TempPaintData {
    private static final TempPaintData myData = new TempPaintData();
    private List<PaintObject> interiorPaintObjectList;
    private List<PaintObject> exteriorPaintObjectList;

    private TempPaintData() {
        interiorPaintObjectList = new ArrayList<PaintObject>();
        interiorPaintObjectList.add(new PaintObject("Living Room", "Sherwin Williams", "Light Blue"));
        interiorPaintObjectList.add(new PaintObject("Master bedroom", "Sherwin Williams", "Yellow"));
        interiorPaintObjectList.add(new PaintObject("Bathroom", "Sherwin Williams", "Loch Blue"));
        interiorPaintObjectList.add(new PaintObject("Dining Room", "Sherwin Williams", "Grape Harvest"));
        interiorPaintObjectList.add(new PaintObject("Kitchen", "Sherwin Williams", "Decor White"));

        exteriorPaintObjectList = new ArrayList<PaintObject>();
        exteriorPaintObjectList.add(new PaintObject("Siding", "Behr", "Cream"));
        exteriorPaintObjectList.add(new PaintObject("Garage", "Behr", "Translucent Silk"));
    }

    public static TempPaintData getInstance() {
        return myData;
    }

    public List<PaintObject> getInteriorPaints() {
        return interiorPaintObjectList;
    }
    public List<PaintObject> getExteriorPaints(){
        return exteriorPaintObjectList;
    }

    public void putExteriorPaint(PaintObject paintObject) {
        exteriorPaintObjectList.add(paintObject);
    }

    public void putInteriorPaint(PaintObject paintObject) {
        interiorPaintObjectList.add(paintObject);
    }

    public void updatePaint(int position, String location, String brand, String color, boolean isExterior) {
        PaintObject paintObject = new PaintObject(location, brand, color);
        if(isExterior)
            exteriorPaintObjectList.set(position, paintObject);
        else
            interiorPaintObjectList.set(position, paintObject);
    }


    public void deletePaintObject(int position, boolean isExterior) {
        if(isExterior) {
            this.exteriorPaintObjectList.remove(position);
        } else {
            this.interiorPaintObjectList.remove(position);
        }
    }
}
