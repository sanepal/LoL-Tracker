package com.gta0004.loltracker.utils;

public enum Region {
  BR("Brazil", "br"),
  EUNE("EU Nordic & East", "eune"),
  EUW("EU West", "euw"),
  KR("Korea", "kr"),
  LAN("Latin America North", "lan"),
  LAS("Latin America South", "las"),
  NA("North America", "na"),
  OCE("Oceania", "oce"),
  RU("Russia", "ru"),
  TR("Turkey", "tr");
  
  
  private String fullName;
  private String shortName;
  
  private Region (String fullName, String shortName) {
    this.fullName = fullName;
    this.shortName = shortName;
  }
  
  @Override
  public String toString() {
    return fullName;
  }
  
  
  public String getRegionCode() {
    return shortName;
  }
}
