package com.forgroundtest.RIS_DSM.Model;

public class Case {
    public String currentTime;
    public int wordCnt;
    public boolean isCorrect;
    public int delayToSpeak;
    public int delayDuringSpeak;

    public Case(String currentTime, int wordCnt, boolean isCorrect, int delayToSpeak, int delayDuringSpeak) {
        this.currentTime = currentTime;
        this.wordCnt = wordCnt;
        this.isCorrect = isCorrect;
        this.delayToSpeak = delayToSpeak;
        this.delayDuringSpeak = delayDuringSpeak;
    }

    public int getWordCnt() {
        return wordCnt;
    }

    public void setWordCnt(int wordCnt) {
        this.wordCnt = wordCnt;
    }

    public boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public int getDelayToSpeak() {
        return delayToSpeak;
    }

    public void setDelayToSpeak(int delayToSpeak) {
        this.delayToSpeak = delayToSpeak;
    }

    public int getDelayDuringSpeak() {
        return delayDuringSpeak;
    }

    public void setDelayDuringSpeak(int delayDuringSpeak) {
        this.delayDuringSpeak = delayDuringSpeak;
    }
}
