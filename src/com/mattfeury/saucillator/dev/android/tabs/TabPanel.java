package com.mattfeury.saucillator.dev.android.tabs;

import java.util.HashMap;
import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.templates.SmartRect;
import com.mattfeury.saucillator.dev.android.utilities.*;
import com.mattfeury.saucillator.dev.android.visuals.*;

import android.graphics.*;
import android.graphics.Paint.Align;
import android.util.SparseIntArray;
import android.view.MotionEvent;

public class TabPanel extends SmartRect {
  protected Paint bg, text;
  protected LinkedList<Drawable> children = new LinkedList<Drawable>();
  private String name;
  private int fontSize = 26;

  private static final float contentPadding = .15f;

  public TabPanel(String name) {
    this(name, 0, 0, 0, 0);
  }
  public TabPanel(String name, int x, int y, int width, int height) {
    super(x, y, x + width, y + height);

    this.name = name;

    bg = new Paint();
    bg.setStyle(Paint.Style.STROKE);
    bg.setStrokeWidth(5);
    bg.setARGB(200, 12, 81, 4);

    text = new Paint();
    text.setARGB(255, 255, 255, 255);
    text.setTextSize(fontSize);
    text.setTextAlign(Align.CENTER);
  }
  
  public void addChild(Drawable... newChildren) {
    for (Drawable child : newChildren) {
      children.add(child);
    }

    recalculateChildren();
  }

  /**
   * Recalculate children positioning
   * 
   * We use a very simple templating system here, based on tables:
   * All Drawables have a shouldClearFloat that determines if it should create a new row.
   * Anything else is assumed to be on the same row. Column widths are distributed evenly
   * based on the number of columns in a row. 
   */
  public void recalculateChildren() {
    int width = (int) (right - left);
    int height = (int) (bottom - top);

    // Determine table structure
    SparseIntArray columnCountPerRow = new SparseIntArray();
    SparseIntArray rowspanCountPerRow = new SparseIntArray();
    int row = 0, rowCount = 0;
    for (Drawable child : children) {
      int columnCount = columnCountPerRow.get(row, 0);
      int colspan = child.getColspan();
      int rowspan = child.getRowspan();

      if (child.shouldClearFloat()) {
        columnCountPerRow.put(++row, colspan);
      } else {
        columnCountPerRow.put(row, columnCount + colspan);
      }

      int maxRowspan = rowspanCountPerRow.get(row);
      if (maxRowspan < rowspan) {
        rowspanCountPerRow.put(row, rowspan);
        rowCount += rowspan - maxRowspan;
      }
    }

    // Do the math and position the table elements appropriately
    int contentPadding = (int) (width * TabPanel.contentPadding);
    int contentWidth = (int) (width - contentPadding * 2);
    int rowHeight = (int) (height - fontSize * 4) / rowCount;

    int column = 0;
    row = 0;
    int spannedRows = 0;
    for (Drawable child : children) {
      if (child.shouldClearFloat()) {
        int maxRowspan = rowspanCountPerRow.get(row, 1);
        spannedRows += maxRowspan;

        row++;
        column = 0;
      }

      int colspan = child.getColspan();
      int rowspan = child.getRowspan();
      int columnCount = columnCountPerRow.get(row, 1);
      int columnWidth = contentWidth / columnCount;

      int newLeft = (int)(left + contentPadding) + column * columnWidth;
      int newTop = (int)(top + contentPadding) + (spannedRows * rowHeight);
      child.set(newLeft, newTop, columnWidth * colspan, rowHeight * rowspan);

      column += colspan;
    }
  }

  public void draw(Canvas canvas) {
    canvas.drawRect(left, top, right, bottom, bg);
    canvas.drawText(name, (right + left) / 2f, top + fontSize*2, text);

    for (Drawable child : children)
      child.draw(canvas);
  }

  @Override
  public void set(int x, int y, int width, int height) {
    super.set(x, y, width, height);

    recalculateChildren();
  }
  public Box<Fingerable> handleTouch(int id, MotionEvent event) {
    /*final int action = event.getAction();
    final int actionCode = action & MotionEvent.ACTION_MASK;
    final int actionIndex = event.getActionIndex();
    final int actionId = event.getPointerId(actionIndex);*/
    final int index = event.findPointerIndex(id);
    final int y = (int) event.getY(index);
    final int x = (int) event.getX(index);

    for (Drawable child : children) {
      if (child.contains(x, y) && child instanceof Fingerable) {
        ((Fingerable)child).handleTouch(id, event);
        return new Full<Fingerable>((Fingerable)child);
      }
    }
    return new Empty<Fingerable>();
  }
}
