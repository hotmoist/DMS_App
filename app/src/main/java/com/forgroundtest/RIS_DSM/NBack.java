package com.forgroundtest.RIS_DSM;

// dataSet for nbackTest.
public class NBack {
    public static String[] nBack = new String[10];
    public static String[] nBackKor = new String[10];
    public NBack() {
        nBack[0] = "1 7 1 9 2 1 7";  // one seven five nine two one seven
        nBack[1] = "1 5 6 5 7 1 5"; // one five six seven seven one five
        nBack[2] = "4 6 1 4 7 4 7"; // four six one four seven nine seven
        nBack[3] = "4 3 4 0 9 2 7"; // four three five zero nine two seven
        nBack[4] = "6 4 0 4 6 9 7"; // six four zero nine six nine seven
        nBack[5] = "1 8 1 3 8 4 8"; // one eight one three eight four one
        nBack[6] = "5 9 5 1 8 1 7"; // five nine five one eight one seven
        nBack[7] = "6 3 4 9 5 9 8"; // six three four six five nine eight
        nBack[8] = "5 0 5 1 5 4 8"; // five zero nine one five four eight
        nBack[9] = "3 9 3 7 3 9 2"; // three nine three seven three nine two

        nBackKor[0] = "일 칠 오 구 이 일 칠";
        nBackKor[1] = "일 오 육 칠 칠 일 오";
        nBackKor[2] = "사 육 일 사 칠 구 칠";
        nBackKor[3] = "사 삼 오 영 구 이 칠";
        nBackKor[4] = "육 사 영 구 육 구 칠";
        nBackKor[5] = "일 팔 일 삼 팔 사 일";
        nBackKor[6] = "오 구 오 일 팔 일 칠";
        nBackKor[7] = "육 삼 사 육 오 구 팔";
        nBackKor[8] = "오 영 구 일 오 사 팔";
        nBackKor[9] = "삼 구 삼 칠 삼 구 이";

    }
}