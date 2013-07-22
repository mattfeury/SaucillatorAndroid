package com.mattfeury.saucillator.dev.android.templates;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.services.ViewService;
import com.mattfeury.saucillator.dev.android.templates.SmartRect;
import com.mattfeury.saucillator.dev.android.utilities.*;
import com.mattfeury.saucillator.dev.android.visuals.*;

import android.graphics.*;
import android.util.SparseIntArray;
import android.view.MotionEvent;

public class Table extends RectButton {
  protected LinkedList<Drawable> children = new LinkedList<Drawable>();

  protected float contentPadding = 0;
  private boolean shouldClearFloat = false;

  public Table(String name) {
    this(name, 0, 0, 0, 0);
  }
  public Table(String name, int x, int y, int width, int height) {
    super(name + "-Table", x, y, x + width, y + height);

    bg = new Paint();
    bg.setStyle(Paint.Style.FILL_AND_STROKE);
    bg.setColor(SauceView.TAB_COLOR);
  }

  public void addChild(Drawable... newChildren) {
    for (Drawable child : newChildren) {
      children.add(child);
    }

    recalculateChildren();
  }
  public void removeChildren() {
    for (Drawable child : children) {
      if (child instanceof Button) {
        ViewService.unregisterButton(((Button)child).getName());
      }
    }

    children.clear();
    //recalculateChildren();
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
    int contentPadding = (int) (width * this.contentPadding);
    int contentWidth = (int) (width - contentPadding * 2);
    int contentHeight = (int) (height - contentPadding * 2);
    int rowHeight = rowCount > 0 ? contentHeight / rowCount : 0;

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

      if (child instanceof Button) {
        ((Button)child).calculateTextSize();
      }

      column += colspan;
    }
  }

  public void draw(Canvas canvas) {
    canvas.drawRect(left, top, right, bottom, bg);

    for (Drawable child : children)
      child.draw(canvas);
  }

  @Override
  public void set(int x, int y, int width, int height) {
    super.set(x, y, width, height);

    recalculateChildren();
  }

  public void setClear(boolean shouldClear) {
    this.shouldClearFloat  = shouldClear;
  }
  @Override
  public boolean shouldClearFloat() {
    return this.shouldClearFloat;
  }

  public void setPadding(float f) {
    this.contentPadding = f;
  }
  public void setBorder(int border) {
    bg.setStrokeWidth(border);
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
        Box<Fingerable> handled = ((Fingerable)child).handleTouch(id, event);
        return handled;
      }
    }
    return new Empty<Fingerable>();
  }
}
