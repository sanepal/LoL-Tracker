package com.gta0004.lolstalker.riot;

public class Summoner {
  public long id;
  public String name;
  public int profileIconId;
  public long revisionDate;
  public int summonerLevel;
  public LastMatch lastMatch;

  public Summoner(long id, String name, int profileIconId, long revisionDate, int summonerLevel) {
    this.id = id;
    this.name = name;
    this.profileIconId = profileIconId;
    this.revisionDate = revisionDate;
    this.summonerLevel = summonerLevel;
  }

  public Summoner(long id, String name) {
    this.id = id;
    this.name = name;
  }
  
  public Summoner() {
    
  }

  public String toString() {
    return name;
  }
}
