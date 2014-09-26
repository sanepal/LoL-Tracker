package com.gta0004.lolstalker.utils;

public class Constants {
  private static final String DEV_KEY = "73d4afb6-1123-4f28-9d23-1ec3e5aac65b";

  public static final String KEY_PARAM = "api_key=" + DEV_KEY;
  
  public static final int EVENT_NEUTRAL = 0;
  public static final int EVENT_POSITIVE = 1;
  public static final int EVENT_NEGATIVE = 2;
  
  public static final String FIND_NAME_URL = "https://%1$s.api.pvp.net/api/lol/%2$s/v1.4/summoner/by-name/%3$s?" + KEY_PARAM;
  public static final String LAST_MATCH_URL = "https://%1$s.api.pvp.net/api/lol/%2$s/v2.2/matchhistory/%3$s?beginIndex=0&endIndex=1&" + KEY_PARAM;
  public static final String GET_CHAMP_URL = "https://global.api.pvp.net/api/lol/static-data/%1$s/v1.2/champion/%2$s?locale=en_US&" + KEY_PARAM;
}
