package com.mattfeury.saucillator.dev.android.visuals;

public class LayoutDefinitions {

  // Widths are percentages of the total screen
  public static final float tabSelectorWidthNoTabs = 1f;
  public static final float tabSelectorWidthWithTabs = 0.25f;

  public static float tabSelectorWidth = tabSelectorWidthWithTabs;

  public static final float controllerWidthOpenTab = .6f;
  public static final float controllerWidthNoTab = controllerWidthOpenTab * tabSelectorWidthWithTabs;

  public static float controllerWidth = controllerWidthOpenTab,
                            padHeight = 1f;

  public static void tabOpen() {
    tabSelectorWidth = tabSelectorWidthWithTabs;
    controllerWidth = controllerWidthOpenTab;
  }
  public static void tabClosed() {
    tabSelectorWidth = tabSelectorWidthNoTabs;
    controllerWidth = controllerWidthNoTab;
  }
}
