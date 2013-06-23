package com.mattfeury.saucillator.dev.android.tabs;

import java.util.LinkedList;

import com.mattfeury.saucillator.dev.android.templates.Label;
import com.mattfeury.saucillator.dev.android.templates.Table;
import com.mattfeury.saucillator.dev.android.utilities.*;
import com.mattfeury.saucillator.dev.android.visuals.*;

import android.graphics.*;
import android.graphics.Paint.Align;
import android.view.MotionEvent;

public class TabPanel extends Table {
  protected Paint text;
  private String name;
  private int fontSize = 26;

  private Table contents;

  public TabPanel(String name) {
    this(name, 0, 0, 0, 0);
  }
  public TabPanel(String name, int x, int y, int width, int height) {
    super(name, x, y, x + width, y + height);

    this.name = name;

    text = new Paint();
    text.setARGB(255, 255, 255, 255);
    text.setTextSize(fontSize);
    text.setTextAlign(Align.CENTER);

    contents = new Table("tab-panel-contents-" + name);
    contents.setPadding(.05f);
    contents.setRowspan(12);
    contents.setClear(true);
    contents.setBorder(0);

    Label header = new Label(name);
    header.setMinTextSize(30);

    super.addChild(header);
    super.addChild(contents);
  }

  @Override
  public void layoutChanged(int width, int height) {
    //super.layoutChanged(width, height);

    contents.recalculateChildren();
  }

  @Override
  public void addChild(Drawable... newChildren) {
    contents.addChild(newChildren);
  }
}
