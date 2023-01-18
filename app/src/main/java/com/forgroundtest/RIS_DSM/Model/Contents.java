package com.forgroundtest.RIS_DSM.Model;

public class Contents {
    String[] eng = {"", "Are you all set?", "I'm sure you'll do better next time", "I wish you all the best", "Please speak slower", "I'll have to think about it", "He will be home at six", "I got first prize"};
    String[] kor = {"", "준비 다 됐어?", "다음에 더 잘할거라고 확신해.", "모든 일이 잘되시길 빌어요.", "천천히 말해주세요.", "생각해봐야겠네요.", "그는 6시에 집에 갈거야.", "나는 첫 상금을 탔다."};
    String[] continueTest = {"", "apple", "banana", "mango", "melon", "orange", "lemon", "strawberry"};

    public Contents() {

    }

    public String[] english() {
        return eng;
    }

    public void setEnglish(String[] eng) {
        this.eng = eng;
    }

    public String onSetEnglish(int index) {
        return english()[index];
    }

    public String onSetKorean(int index) {
        return korean()[index];
    }

    public String[] korean() {
        return kor;
    }

    public void setKorean(String[] kor) {
        this.kor = kor;
    }

    public String[] continueTest() {
        return continueTest;
    }

    public void setContinueTest(String[] continueTest) {
        this.continueTest = continueTest;
    }

    public String onSetContinueTest(int index) {
        return continueTest()[index];
    }
    public boolean onCheckContinue(String userStr, int index) {
        String continueS = continueTest[index-1].toLowerCase();
        String userAns = userStr.toLowerCase();

        return continueS.equals(userAns);
    }
}
