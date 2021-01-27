package com.github.fabricservertools.deltalogger.beans;

public class KilledEntity {
  private int id;
  private String name;
  private String source;
  private String killer;
  private String dimension;
  private String time;
  private int x;
  private int y;
  private int z;

  public KilledEntity(int id, String name, String source, String killer, String dimension, String time, int x, int y, int z) {
    this.id = id;
    this.name = name;
    this.source = source;
    this.killer = killer;
    this.dimension = dimension;
    this.time = time;
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

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSource() {
    return this.source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getKiller() {
    return this.killer;
  }

  public void setKiller(String killer) {
    this.killer = killer;
  }

  public String getDimension() {
    return this.dimension;
  }

  public void setDimension(String dimension) {
    this.dimension = dimension;
  }

  public String getTime() {
    return this.time;
  }

  public void setTime(String time) {
    this.time = time;
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
}
