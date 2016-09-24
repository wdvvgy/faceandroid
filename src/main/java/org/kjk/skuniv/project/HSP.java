package org.kjk.skuniv.project;

/**
 * Created by SIRIUS on 2016-07-14.
 */
public class HSP {

    static final int HSP_HSP_USEHSP = 111;

    static final int HSP_DEVICE_SERVER = 0;
    static final int HSP_DEVICE_MOBILE = 1;
    static final int HSP_DEVICE_DESKTOP = 2;

    static final int HSP_SERVICEID_SERVER = 0;
    static final int HSP_SERVICEID_ADMIN= 1;
    static final int HSP_SERVICEID_USER = 2;


    static final int HSP_ID_UNKNOWN = 100; //로그인 하기 전에 ID
    static final int HSP_ID_SERVER = 99;   //서버의 ID

    static final int HSP_MESSAGE_OK = 1;
    static final int HSP_MESSAGE_FAIL = 2;
    static final int HSP_MESSAGE_DENIED = 3;//거부
    static final int HSP_MESSAGE_PROGRAM_START = 4;//프로그램 시작합니다.
    static final int HSP_MESSAGE_LOGIN_START = 5;//로그인합니다.
    static final int HSP_MESSAGE_FACEDETECTING_START = 6;//안면인식시작
    static final int HSP_MESSAGE_FACEDETECTING_IS_USER = 7;//안면인식 유저
    static final int HSP_MESSAGE_FACEDETECTING_IS_ADMIN = 8;//안면인식 관리자
    static final int HSP_MESSAGE_FACEDETECTING_UNKNOWNPERSON= 9;//안면인식 알수없는 사람
    static final int HSP_MESSAGE_FACEDETECTING_END = 10;//안면인식 종료
    static final int HSP_MESSAGE_REQUIRED_SIGN = 11;//사인필요해요
    static final int HSP_MESSAGE_SIGN_UPLOAD = 12;//사인업로드할게요
    static final int HSP_MESSAGE_IMAGE_UPLOAD = 13;//이미지업로드할게요
    static final int HSP_MESSAGE_PAGE_DONE = 14;//done을 누름
    static final int HSP_MESSAGE_LOGOUT = 15;//로그아웃
    static final int HSP_MESSAGE_REQUEST_FILE = 16;
}
