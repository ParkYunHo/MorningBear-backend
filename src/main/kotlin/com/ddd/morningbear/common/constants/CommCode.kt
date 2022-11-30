package com.ddd.morningbear.common.constants

/**
 * @author yoonho
 * @since 2022.11.29
 */
class CommCode {
    enum class Social(val code: String){
        KAKAO("kakao"),
        NAVER("naver"),
        APPLE("apple")
    }

    enum class Result(val code: String, val message: String) {
        K000("K000", "잘못된 요청입니다."),
        K001("K001", "토큰정보가 올바르지 않습니다."),
        K002("K002", "대상 데이터를 조회할 수 없습니다."),
        K005("K005", "일시적으로 서버를 이용할 수 없습니다. 잠시후에 다시 시도해주세요.")
    }
}