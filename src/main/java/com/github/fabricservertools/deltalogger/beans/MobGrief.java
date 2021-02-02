package com.github.fabricservertools.deltalogger.beans;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import static com.github.fabricservertools.deltalogger.Chat.*;

public class MobGrief {
  private int id;
  private String time;
  private String entityType;
  private String target;
  private String dimension;
  private int x;
  private int y;
  private int z;

  public MobGrief(int id, String time, String entityType, String target, String dimension, int x, int y, int z) {
    this.id = id;
    this.time = time;
    this.entityType = entityType;
    this.target = target;
    this.dimension = dimension;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getTime() {
    return this.time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getEntityType() {
    return this.entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getTarget() {
    return this.target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getDimension() {
    return this.dimension;
  }

  public void setDimension(String dimension) {
    this.dimension = dimension;
  }

  public int getX() {
    return this.x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return this.y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getZ() {
    return this.z;
  }

  public void setZ(int z) {
    this.z = z;
  }
  public MutableText getTextWithPos() {
    return joinText(format(time, Formatting.GRAY),
        format(new LiteralText(String.valueOf(x) + " " + String.valueOf(y) + " " + String.valueOf(z)), Formatting.AQUA),
        format(getEntityType(), Formatting.ITALIC),
        format(getTarget(), Formatting.YELLOW));
  }
}
