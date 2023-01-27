package com.forgroundtest.RIS_DSM.Model;

public class Contents {
    String[] eng = {"", "Are you all set?", "I'm sure you'll do better next time", "I wish you all the best", "Please speak slower", "I'll have to think about it", "He will be home at six", "I got first prize"};
    String[] kor = {"", "준비 다 됐어?", "다음에 더 잘할거라고 확신해.", "모든 일이 잘되시길 빌어요.", "천천히 말해주세요.", "생각해봐야겠네요.", "그는 6시에 집에 갈거야.", "나는 첫 상금을 탔다."};
    String continueTest = "학습을 진행하시겠습니까?";
    String[] positiveAnswer = {"네", "예", "에", "내", "어", "옙", "넵", "엡", "좋아", "좋아요", "조아요", "좋습니다", "조습니다"};
    String[] negativeAnswer = {"아니요", "아니오", "아녀", "노", "아니", "안", "아냐", "싫어요", "시러", "싫어", "하지마", "싫다고", "실타고"};
    String againTest = "다시 말씀해주세요";
    private final String match = "[^\uAC00-\uD7A30-9a-zA-Z]";

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

    public String continueTest() {
        return continueTest;
    }

    public String againTest() {
        return this.againTest;
    }

    public void setContinueTest(String continueTest) {
        this.continueTest = continueTest;
    }

    public int onCheckContinue(String userStr) {
        String userAns = userStr.trim();
        userAns = userAns.replaceAll(match, "");

        for (String s : positiveAnswer) {
            if (userAns.equals(s)) return 1;
        }

        for (String s : negativeAnswer) {
            if (userAns.equals(s)) return -1;
        }

        return 0;
    }
}
