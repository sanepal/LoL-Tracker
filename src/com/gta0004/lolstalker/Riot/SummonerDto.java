package com.gta0004.lolstalker.Riot;

public class SummonerDto {
	public long id;
	public String name;
	public int profileIconId;
	public long revisionDate;
	public int summonerLevel;
	public LastMatch lastMatch;
	
	public SummonerDto() {
		
	}
	
	public SummonerDto(long id, String name, int profileIconId, long revisionDate, int summonerLevel) {
		this.id = id;
		this.name = name;
		this.profileIconId = profileIconId;
		this.revisionDate = revisionDate;
		this.summonerLevel = summonerLevel;
	}
	
	public String toString() {
		return name;
	}
}
